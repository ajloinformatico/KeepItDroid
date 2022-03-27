package es.infolojo.keepitdroid.ui.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.wifi.hotspot2.pps.Credential
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.text.toSpannable
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.FireBaseModel
import es.infolojo.keepitdroid.data.vo.SAVE_NOTE_STATE_VO
import es.infolojo.keepitdroid.databinding.ActivityCreateNoteBinding
import es.infolojo.keepitdroid.ui.adapters.FireBaseSingleValueEvent
import es.infolojo.keepitdroid.ui.viewmodels.NotesViewModel
import es.infolojo.keepitdroid.utils.*
import timber.log.Timber

class CreateNoteActivity : AppCompatActivity() {

  private lateinit var binding: ActivityCreateNoteBinding
  private val viewModel: NotesViewModel by viewModels()
  private var firebaseAuth: FirebaseAuth? = null
  private var firebaseUser: FirebaseUser? = null
  private var fireBaseStore: FirebaseFirestore? = null

  //EditNoteData
  private var noteContent: FireBaseModel? = null
  private var id: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityCreateNoteBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getIntentContent()
    configureNewToolbar()
    initFireBase()
    initViewModel()
    initViews()

    //LifeSate to set color
    viewModel.noteColor.observe(this) { color ->
      if (color != FireBaseColors.UNKNOWN) {
        val textColor =
          getAnyColor(
            this,
            if (color == FireBaseColors.BLACK_NOTE || color == FireBaseColors.RED_NOTE) {
              R.color.white
            } else {
              R.color.black
            }
          )

        val backGroundColor = getAnyColor(
          this,
          color.toIntColor()
        )

        binding.titleNote.setBackgroundColor(backGroundColor)
        binding.createNoteToolbar.setBackgroundColor(backGroundColor)
        binding.bodyNote.setBackgroundColor(backGroundColor)

        binding.titleNote.setTextColor(textColor)
        binding.bodyNote.setTextColor(textColor)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.create_note_menu, menu)
    return true
  }


  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    viewModel.setNoteColor(
      item.itemId.toFireBaseColors()
    )
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    super.onBackPressed()
    backToNotesActivity(this)
  }

  private fun configureNewToolbar() {
    setSupportActionBar(binding.createNoteToolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
    binding.backBtn.setOnClickListener {
      onBackPressed()
    }
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
          setNoteColor()
          viewModel.setNoteColor(FireBaseColors.WHITE_NOTE)
        }
      }
    }
  }

  /**Check if come from detail to set the note color or default*/
  private fun setNoteColor() {
    if (noteContent != null && noteContent?.color != null) {
      noteContent?.let {
        viewModel.setNoteColor(it.color)
      }
    } else {
      viewModel.setNoteColor(FireBaseColors.WHITE_NOTE)
    }
  }

  private fun initViews() {
    binding.root.setOnClickListener {
      hideKeyBoard()
    }

    val needToUpdateNote = setEditNoteData()


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

            val dataToSave = mapOf(
              NOTE_TITLE to title,
              NOTE_BODY to noteBody,
              NOTE_COLOR to viewModel.getNoteColor(),
            )

            //Create new note
            if (needToUpdateNote.not()) {
              documentReference.set(dataToSave).addOnSuccessListener {
                Timber.d(SAVE_NOTE_STATE_VO.NOTE_SAVE.data)
                showMessage(this, SAVE_NOTE_STATE_VO.NOTE_SAVE.data, Toast.LENGTH_LONG)
                backToNotesActivity(this)

              }.addOnFailureListener { exception ->
                Timber.d("Error: $exception")
                showLastOnCreateNoteError()
              }

            // Edit actual note
            } else {
              documentReference.set(dataToSave).addOnSuccessListener {
                Timber.d(SAVE_NOTE_STATE_VO.NOTE_SAVE.data)
                showMessage(this, NOTE_EDIT_SUCCESS, Toast.LENGTH_LONG)
                backToNotesActivity(this)

              }.addOnFailureListener { exception ->
                Timber.d("Error: $exception")
                showLastOnCreateNoteError()
              }

//              correct child reference.
//              myRef.setValue("Hello, World")
//              myRef.child("someChild").child("name").setValue(name)

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

  private fun createNewNote() {

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

  private fun setEditNoteData(): Boolean = if (id.orEmpty().isNotEmpty() && noteContent != null) {
      binding.titleNote.text.clear()
      binding.bodyNote.text.clear()
      binding.titleNote.text.append(noteContent?.title)
      binding.bodyNote.text.append(noteContent?.content)
      true
    } else {
      false
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