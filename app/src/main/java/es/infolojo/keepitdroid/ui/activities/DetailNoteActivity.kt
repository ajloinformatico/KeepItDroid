package es.infolojo.keepitdroid.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.FireBaseModel
import es.infolojo.keepitdroid.databinding.ActivityDetailNoteBinding
import es.infolojo.keepitdroid.utils.*

class DetailNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailNoteBinding
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null

    private var noteContent: FireBaseModel? = null
    private var id: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        getIntentContent()
        initFireBase()
        initViews()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToNotesActivity(this)
    }

    private fun initViews() {
        setContent()
        binding.editButton.setOnClickListener {
            if (id.orEmpty().isNotEmpty()) {
                goToEdit()
            } else {
                showError(
                    this,
                    binding.root,
                    "Can not create note, please try again",
                    Snackbar.LENGTH_LONG
                )
            }
        }
    }

    private fun setContent() {
        noteContent?.color?.let { noteColor ->
            if (noteColor != FireBaseColors.UNKNOWN) {
                val textColor =
                    if (noteColor == FireBaseColors.BLACK_NOTE || noteColor == FireBaseColors.RED_NOTE) {
                        R.color.white
                    } else {
                        R.color.black
                    }

                binding.root.setBackgroundColor(noteColor.toIntColor())
                binding.titleNote.setTextColor(this.getColor(textColor))
                binding.bodyNote.setTextColor(this.getColor(textColor))
            }
        }

        binding.titleNote.text = noteContent?.title.orEmpty()
        binding.bodyNote.text = noteContent?.content.orEmpty()

    }

    private fun goToEdit() {
        startActivity(
            Intent(this, CreateNoteActivity::class.java).apply {
                this.putExtra(NOTE_TITLE, noteContent?.title)
                this.putExtra(NOTE_BODY, noteContent?.content)
                this.putExtra(NOTE_COLOR, noteContent?.color)
                this.putExtra(NOTE_KEY, id)
            }
        )
    }

    private fun getIntentContent() {
        if (noteContent == null) {
            noteContent = FireBaseModel(
                title = intent.getStringExtra(NOTE_TITLE).orEmpty(),
                content = intent.getStringExtra(NOTE_BODY).orEmpty(),
                color = intent.getStringExtra(NOTE_COLOR).orEmpty().toFireBaseColor()
            )
        }

        if (id == null) {
            id = intent.getStringExtra(NOTE_KEY).orEmpty()
        }
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