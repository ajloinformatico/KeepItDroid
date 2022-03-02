package es.infolojo.keepitdroid.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.vo.SAVE_NOTE_STATE_VO
import es.infolojo.keepitdroid.databinding.ActivityCreateNoteBinding
import es.infolojo.keepitdroid.ui.viewmodels.NotesViewModel
import es.infolojo.keepitdroid.utils.*
import timber.log.Timber

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNoteBinding
    private val viewModel: NotesViewModel by viewModels()
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var fireBaseStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureNewToolbar()
        initFireBase()
        initViewModel()
        initViews()

        //LifeSate to set color
        viewModel.noteColor.observe(this) { color ->
            if (color != FireBaseColors.UNKNOWN) {
                val textColor =
                    if (color == FireBaseColors.BLACK_NOTE || color == FireBaseColors.RED_NOTE) {
                        R.color.white
                    } else {
                        R.color.black
                    }


                binding.titleNote.setBackgroundColor(this.getColor(color.toIntColor()))
                binding.createNoteToolbar.setBackgroundColor(this.getColor(color.toIntColor()))
                binding.bodyNote.setBackgroundColor(this.getColor(color.toIntColor()))

                binding.titleNote.setTextColor(this.getColor(textColor))
                binding.bodyNote.setTextColor(this.getColor(textColor))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_note_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()

        } else {
            viewModel.setNoteColor(
                item.itemId.toFireBaseColors()
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun configureNewToolbar() {
        setSupportActionBar(binding.createNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initFireBase() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
        if (firebaseUser == null) {
            firebaseUser = firebaseAuth?.currentUser
        }
        if (fireBaseStore == null) {
            fireBaseStore = FirebaseFirestore.getInstance()
        }
    }

    private fun initViewModel() {
        firebaseAuth?.let { mAuth ->
            firebaseUser?.let { mUser ->
                fireBaseStore?.let { fStore ->
                    viewModel.init(mAuth, mUser, fStore)
                    viewModel.setNoteColor(FireBaseColors.WHITE_NOTE)
                }
            }
        }
    }

    private fun initViews() {
        binding.root.setOnClickListener {
            hideKeyBoard()
        }

        binding.newButton.setOnClickListener {
            val title = binding.titleNote.text.toString()
            val noteBody = binding.bodyNote.text.toString()
            hideKeyBoard()

            viewModel.saveDataNote(title, noteBody)
            when (viewModel.checkDataBeforeCreate()) {
                SAVE_NOTE_STATE_VO.NOTE_SAVE -> {

                    if (firebaseUser != null && fireBaseStore != null) {
                        val documentReference =
                            fireBaseStore!!.collection(FIRE_BASE_COLLECTION_NAME)
                                .document(firebaseUser!!.uid)
                                .collection(FIRE_BASE_USER_COLLECTION)
                                .document()

                        documentReference.set(
                            mapOf(
                                NOTE_TITLE to title,
                                NOTE_BODY to noteBody,
                                NOTE_COLOR to viewModel.getNoteColor(),
                            )
                        ).addOnSuccessListener {
                            Timber.d(SAVE_NOTE_STATE_VO.NOTE_SAVE.data)
                            showMessage(this, SAVE_NOTE_STATE_VO.NOTE_SAVE.data, Toast.LENGTH_LONG)
                            finish()
                            startActivity(Intent(this, NotesActivity::class.java))

                        }.addOnFailureListener { exception ->
                            Timber.d("Error: $exception")
                            showLastOnCreateNoteError()
                        }

                    } else {
                        Timber.d("Error: FireBase data is null")
                        showLastOnCreateNoteError()
                    }


                }

                SAVE_NOTE_STATE_VO.TITLE_ERROR -> {
                    showError(
                        this,
                        binding.root,
                        SAVE_NOTE_STATE_VO.TITLE_ERROR.data,
                        Snackbar.LENGTH_LONG
                    )
                }

                SAVE_NOTE_STATE_VO.BODY_ERROR -> {
                    showError(
                        this,
                        binding.root,
                        SAVE_NOTE_STATE_VO.BODY_ERROR.data,
                        Snackbar.LENGTH_LONG
                    )
                }

                SAVE_NOTE_STATE_VO.GLOBAL_ERROR -> {
                    showError(
                        this,
                        binding.root,
                        SAVE_NOTE_STATE_VO.GLOBAL_ERROR.data,
                        Snackbar.LENGTH_LONG
                    )
                }
            }
        }
    }

    private fun hideKeyBoard() {
        hideVirtualKeyBoard(this, binding.titleNote)
        hideVirtualKeyBoard(this, binding.bodyNote)
    }

    private fun showLastOnCreateNoteError() {
        showError(this, binding.root, "Something is wrong, try again", Snackbar.LENGTH_LONG)
        initFireBase()
    }

}