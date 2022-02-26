package es.infolojo.keepitdroid.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import es.infolojo.keepitdroid.MainActivity
import es.infolojo.keepitdroid.R


fun showMessage(context: Context, text: String, timeDuration: Int) =
    Toast.makeText(context, text, timeDuration).show()

fun showError(context: Context, view: View, text: String, timeDuration: Int) =
    Snackbar.make(view, text, timeDuration)
        .setBackgroundTint(context.getColor(R.color.red))
        .setTextColor(context.getColor(R.color.white)).show()

fun backLogin(activity: Activity) {
    activity.finish()
    activity.startActivity(Intent(activity, MainActivity::class.java))
}

fun hideVirtualKeyBoard(activity: AppCompatActivity, editText: EditText) {
    val inputMethodManager =
        activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
}

fun getAnyColor(context: Context, resource: Int): Int {
    return  ContextCompat.getColor(
        context,
        resource
    )
}