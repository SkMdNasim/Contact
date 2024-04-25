package com.example.contact.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.contact.model.MyContact

class ContactViewModel(app: Application): AndroidViewModel(app) {
    private var map: Application


    init {
            map = app
    }

}