package es.infolojo.keepitdroid.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.infolojo.keepitdroid.databinding.ActivitySignInBinding
import es.infolojo.keepitdroid.ui.viewmodels.LoginViewModel
import es.infolojo.keepitdroid.utils.backLogin
import es.infolojo.keepitdroid.utils.hideVirtualKeyBoard
import es.infolojo.keepitdroid.utils.showError
import es.infolojo.keepitdroid.utils.showMessage
import timber.log.Timber

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val viewModel: LoginViewModel by viewModels()

    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
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

        binding.root.setOnClickListener {
            hideKeyBoard()
        }

        binding.registerButton.setOnClickListener {
            hideKeyBoard()

            val email = this.binding.registerEmail.text.toString()
            val password = this.binding.registerPassword.text.toString()

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
                    try {
                        firebaseAuth?.let { mAuth ->
                            mAuth.createUserWithEmailAndPassword(
                                email,
                                password
                            ).addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Timber.d("createUserWithEmail:success")
                                    showMessage(this, "Registration success", Toast.LENGTH_SHORT)
                                    mAuth.currentUser?.let {
                                        sendEmailVerification(it)
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Timber.d("createUserWithEmail:failure", task.exception)
                                    showError(
                                        this,
                                        binding.root,
                                        "Authentication failed.",
                                        Snackbar.LENGTH_SHORT
                                    )
                                }
                            }
                        }


                    } catch (e: Exception) {
                        Timber.d(e.toString())
                    }
                }

            }
        }


        binding.loginBackButton.setOnClickListener {
            backLogin(this)
        }
    }

    private fun sendEmailVerification(firebaseUser: FirebaseUser) {
        firebaseUser
            .sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Timber.d("createUserWithEmail:success")
                    showMessage(this, "Verification Email is sent", Toast.LENGTH_SHORT)
                    firebaseAuth?.signOut()
                    backLogin(this)

                } else {
                    Timber.d("createUserWithEmail:faild")
                    showError(
                        this,
                        binding.root,
                        "Verification Email failed. Please tray again",
                        Snackbar.LENGTH_SHORT
                    )
                    firebaseAuth?.signOut()
                }
            }
    }

    private fun initViewModel() {
        viewModel.init(
            binding.registerEmail.text.toString(),
            binding.registerPassword.text.toString()
        )
    }

    private fun initFireBase() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
    }

    private fun hideKeyBoard() {
        hideVirtualKeyBoard(this, binding.registerEmail)
        hideVirtualKeyBoard(this, binding.registerPassword)

    }
}