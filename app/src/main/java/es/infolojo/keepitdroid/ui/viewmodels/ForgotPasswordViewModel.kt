package es.infolojo.keepitdroid.ui.viewmodels

import androidx.lifecycle.ViewModel

class ForgotPasswordViewModel: ViewModel() {

    private var _email = ""
    private var _newPassword = ""

    fun init(email: String) {
        _email = email
    }

    fun getEmail(): String = _email
    fun getPassword(): String = _newPassword

    fun setPassWord(newPassWord: String) {
        if (newPassWord != _newPassword) {
            _newPassword = newPassWord
        }
    }

    fun checkCorrectPassword(): Boolean = _newPassword.isNotEmpty() && _newPassword.length >= 8
}