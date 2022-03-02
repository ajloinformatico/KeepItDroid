package es.infolojo.keepitdroid.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import es.infolojo.keepitdroid.databinding.ActivityForgotPasswordBinding
import es.infolojo.keepitdroid.ui.viewmodels.ForgotPasswordViewModel
import es.infolojo.keepitdroid.utils.*
import timber.log.Timber

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var firebaseAuth: FirebaseAuth? = null

    private val email: String by lazy {
        intent.extras?.getString(USER_EMAIL).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initFireBase()
        initViewModel()
        initViews()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backLogin(this)
    }
    
    private fun initViews() {

        binding.backLoginButton.setOnClickListener {
            backLogin(this)
        }

        binding.recoverPasswordButton.setOnClickListener {
            firebaseAuth?.let { mAuth ->
                mAuth.sendPasswordResetEmail(viewModel.getEmail()).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Timber.d("Recover password success")
                        showMessage(this, "Recover password success", Toast.LENGTH_SHORT)
                        showMessage(this, "Check your email to restart your account", Toast.LENGTH_LONG)
                        backLogin(this)
                    } else {
                        Timber.d("Recover password fail")
                        showError(this, binding.root, "Recover password fail", Snackbar.LENGTH_SHORT)
                    }
                }

            }

        }
    }
    private fun initViewModel() {
        if (email.isNotEmpty()) {
            viewModel.init(email)
        } else {
            backLogin(this)
        }
    }

    private fun initFireBase() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
    }
}