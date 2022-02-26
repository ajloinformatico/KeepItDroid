package es.infolojo.keepitdroid.ui.viewmodels

import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {

    private var _email: String = ""
    private var _password: String = ""

    fun init(emailInit: String, passwordInit: String) {
        _email = emailInit
        _password = passwordInit
    }

    fun setEmail(newEmail: String) {
        if (newEmail != _email) {
            _email = newEmail
        }
    }

    fun setPassWord(newPassWord: String) {
        if (newPassWord != _password) {
            _password = newPassWord
        }
    }

    fun checkCorrectEmail(): Boolean = _email.isNotEmpty() && _email.contains("@")

    fun checkCorrectPassword(): Boolean = _password.isNotEmpty() && _password.length >= 8

}