package es.infolojo.keepitdroid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.infolojo.keepitdroid.ui.activities.ForgotPasswordActivity
import es.infolojo.keepitdroid.ui.activities.NotesActivity
import es.infolojo.keepitdroid.ui.activities.SignInActivity
import es.infolojo.keepitdroid.databinding.ActivityMainBinding
import es.infolojo.keepitdroid.ui.viewmodels.LoginViewModel
import es.infolojo.keepitdroid.utils.USER_EMAIL
import es.infolojo.keepitdroid.utils.hideVirtualKeyBoard
import es.infolojo.keepitdroid.utils.showError
import es.infolojo.keepitdroid.utils.showMessage
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LoginViewModel by viewModels()
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initFireBase()
        initViewModel()
        initViews()
    }

    private fun initViews() {
        binding.root.setOnClickListener {
            hideKeyBoard()
        }

        binding.loginButton.setOnClickListener {
            hideKeyBoard()

            val email = this.binding.logingEmail.text.toString()
            val password = this.binding.loggingPassword.text.toString()

            //Note: Set data
            viewModel.setEmail(email)
            viewModel.setPassWord(password)

            //Note: Check email and password
            when {
                viewModel.checkCorrectEmail().not() && viewModel.checkCorrectPassword().not() -> {
                    showError(
                        this,
                        binding.root,
                        "Email or password not valid",
                        Snackbar.LENGTH_SHORT
                    )
                }

                viewModel.checkCorrectEmail().not() -> {
                    showError(this, binding.root, "Email is not valid", Snackbar.LENGTH_SHORT)
                }

                viewModel.checkCorrectPassword().not() -> {
                    showError(this, binding.root, "Password is not valid", Snackbar.LENGTH_SHORT)
                }

                else -> {
                    firebaseAuth?.signInWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                firebaseAuth?.currentUser?.let {
                                    checkVerificationLogging(it)
                                }
                            } else {
                                Timber.d("Login Error")
                                showError(
                                    this,
                                    binding.root,
                                    "Something was wrong",
                                    Snackbar.LENGTH_SHORT
                                )
                                showMessage(
                                    this,
                                    "Please try again or reset your password",
                                    Toast.LENGTH_LONG
                                )
                            }
                        }
                }
            }
        }

        binding.forgetPassword.setOnClickListener {
            hideKeyBoard()

            val email = this.binding.logingEmail.text.toString()

            //Note: Set data
            viewModel.setEmail(email)

            //Note: Check email
            if (viewModel.checkCorrectEmail()) {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                intent.putExtra(USER_EMAIL, email)
                startActivity(intent)
            } else {
                showError(this, binding.root, "Your account does not exist", Snackbar.LENGTH_SHORT)
            }
        }

        binding.newUserButton.setOnClickListener {
            hideKeyBoard()
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun checkVerificationLogging(firebaseUser: FirebaseUser) {
        if (firebaseUser.isEmailVerified) {
            showMessage(this, "${firebaseUser.email} Logged", Toast.LENGTH_LONG)
            finish()
            startActivity(Intent(this, NotesActivity::class.java))

        } else {
            Timber.d("Error: email not verified")
            showError(this, binding.root, "Your account does not exist", Snackbar.LENGTH_LONG)
            firebaseAuth?.signOut()
        }
    }

    private fun hideKeyBoard(){
        hideVirtualKeyBoard(this, this.binding.logingEmail)
        hideVirtualKeyBoard(this, this.binding.loggingPassword)
    }

    private fun initViewModel() {
        viewModel.init(binding.logingEmail.text.toString(), binding.loggingPassword.text.toString())
    }

    private fun initFireBase() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            if (firebaseAuth == null) {
                firebaseAuth = FirebaseAuth.getInstance()
            }
            if (firebaseUser == null) {
                firebaseUser = firebaseAuth?.currentUser
            }
        } else {
            finish()
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }

}