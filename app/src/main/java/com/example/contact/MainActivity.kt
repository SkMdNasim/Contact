package com.example.contact

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contact.adapter.ContactAdapter
import com.example.contact.databinding.ActivityMainBinding
import com.example.contact.model.MyContact
import com.example.contact.viewModel.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    lateinit var contactViewModel: ContactViewModel
    companion object {
        val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    }

    var arrayList: ArrayList<MyContact> =  arrayListOf()
    var alsearch: ArrayList<MyContact> =  arrayListOf()
    lateinit var binding : ActivityMainBinding
    lateinit var adapter : ContactAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        contactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        binding.rvContacts.setLayoutManager(LinearLayoutManager(this))
        adapter = ContactAdapter(this, alsearch)
        adapter.setClickListener(object : ContactAdapter.ItemClickListener {
            override fun onItemClick(myContact: MyContact) {
                val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
                intent.putExtra(ContactsContract.Contacts._ID, myContact.contactId)
                startActivity(intent)

            }

            override fun onItemLongClick(myContact: MyContact) {

                contactViewModel.viewModelScope.launch(Dispatchers.IO) {
                    val status = deleteContactById(myContact.contactId)
                    if(status){
                        withContext(Dispatchers.Main) {
                            alsearch.remove(myContact)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(
                                this@MainActivity,
                                "Deleted Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        })
        binding.rvContacts.setAdapter(adapter)

        binding.aetSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                alsearch.clear()
                if(binding.aetSearch.text!!.equals("")){
                    alsearch.addAll(arrayList)
                }else{
                    for (i in 0..(arrayList.size-1)) {
                        if(arrayList[i].contactName.startsWith(binding.aetSearch.text!!)){
                          alsearch.add(arrayList[i])
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
        })

        loadContacts()
    }

     private fun loadContacts() {
         Log.e("Main", "loadContacts")
        contactViewModel.viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
                //callback onRequestPermissionsResult
            } else {
                getContacts()
            }
        }

    }

      override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                              grantResults: IntArray) {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults)
          if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
              if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  loadContacts()
              } else {
                  //  toast("Permission must be granted in order to display contacts information")
              }
          }
      }

      @SuppressLint("Range")
      suspend fun getContacts() {
          val builder = StringBuilder()
          val resolver: ContentResolver = contentResolver;
          val cursor = resolver.query(
              ContactsContract.Contacts.CONTENT_URI, null, null, null,
              null)

          if (cursor != null) {
              if (cursor.count > 0) {
                  while (cursor.moveToNext()) {
                      val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                      val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                      val phoneNumber = (cursor.getString(
                          cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                      if (phoneNumber > 0) {
                          val cursorPhone = contentResolver.query(
                              ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                              null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                          if (cursorPhone != null) {
                              if(cursorPhone.count > 0) {
                                  while (cursorPhone.moveToNext()) {
                                      val phoneNumValue = cursorPhone.getString(
                                          cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                      builder.append("Contact: ").append(name).append(", Phone Number: ").append(
                                          phoneNumValue).append("\n\n")
                                      //builder.append(phoneNumValue).append("\n\n")
                                      arrayList.add(MyContact(id, name, phoneNumValue))
                                      alsearch.add(MyContact(id, name, phoneNumValue))
                                      Log.e("Name ===>",phoneNumValue);
                                  }
                              }
                              cursorPhone.close()
                          }

                      }
                  }
              } else {
                  //   toast("No contacts available!")
              }
          }
          cursor?.close()

          withContext(Dispatchers.Main){
              adapter.notifyDataSetChanged()
          }
      }



    @SuppressLint("Range")
    fun deleteContactById(id: String): Boolean {
        val cr = contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null)
        cur?.let {
            try {
                if (it.moveToFirst()) {
                    do {
                        if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)) == id) {
                            val lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
                            cr.delete(uri, null, null)
                            return true
                            break
                        }

                    } while (it.moveToNext())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                it.close()
            }
        }
        return false
    }

}