package es.infolojo.keepitdroid.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import es.infolojo.keepitdroid.databinding.ActivityCreateNoteBinding
import es.infolojo.keepitdroid.ui.viewmodels.NotesViewModel

class DetailNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNoteBinding
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initFireBase()
        initViews()
    }

    private fun initViews() {
        //TODO: INIT VIEWS
    }

    private fun initFireBase() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            if (firebaseAuth == null) {
                firebaseAuth = FirebaseAuth.getInstance()
            }
            if (firebaseUser == null) {
                firebaseUser = firebaseAuth?.currentUser
            }
        }
    }
}