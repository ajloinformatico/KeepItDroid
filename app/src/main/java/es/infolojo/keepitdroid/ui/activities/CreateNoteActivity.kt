package es.infolojo.keepitdroid.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.FireBaseModel
import es.infolojo.keepitdroid.data.vo.SAVE_NOTE_STATE_VO
import es.infolojo.keepitdroid.databinding.ActivityCreateNoteBinding
import es.infolojo.keepitdroid.ui.adapters.NoteAddImageAdapter
import es.infolojo.keepitdroid.ui.adapters.NotesListener
import es.infolojo.keepitdroid.ui.viewmodels.NotesViewModel
import es.infolojo.keepitdroid.utils.*
import timber.log.Timber

class CreateNoteActivity : AppCompatActivity() {

  private lateinit var binding: ActivityCreateNoteBinding
  private val viewModel: NotesViewModel by viewModels()
  private var firebaseAuth: FirebaseAuth? = null
  private var firebaseUser: FirebaseUser? = null
  private var fireBaseStore: FirebaseFirestore? = null
  private var menuOptions: Menu? = null
  private val notesImagesAdapter: NoteAddImageAdapter by lazy { NoteAddImageAdapter(::notesListener) }
  private val imageAdapterLayoutManager =
    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

  //EditNoteData
  private var noteContent: FireBaseModel? = null
  private var id: String? = null

  //Images
  private var imageUrl: Uri? = null
  private val contentValues by lazy {
    ContentValues()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityCreateNoteBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getIntentContent()
    configureNewToolbar()
    initFireBase()
    initViewModel()
    initNotesImagesAdapter()
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

        binding.root.setBackgroundColor(backGroundColor)
        binding.createNoteToolbar.setBackgroundColor(backGroundColor)
        binding.bodyNote.setBackgroundColor(backGroundColor)

        binding.titleNote.setTextColor(textColor)
        binding.bodyNote.setTextColor(textColor)

        //Back arrow
        binding.backBtn.setColorFilter(textColor)

        //DynamicMenu
        menuOptions?.let {
          updateMenuDynamic(it, textColor)
        }
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuOptions = menu
    menuInflater.inflate(R.menu.create_note_menu, menuOptions)
    updateMenuDynamic(menuOptions!!, noteContent?.color?.toIntColor() ?: R.color.black)
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

  private fun updateMenuDynamic(menu: Menu, color: Int) {
    menu.findItem(R.id.color_note).icon.apply {
      this.mutate()
      this.setTint(color)
    }
    onPrepareOptionsMenu(menu)
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

  /** Configure the adapter to add new images*/
  private fun initNotesImagesAdapter() {
    binding.addRecyclerImages.layoutManager = imageAdapterLayoutManager
    binding.addRecyclerImages.adapter = notesImagesAdapter
    viewModel.firstImage()
    viewModel.getImageList().observe(this) {
      notesImagesAdapter.submitList(it)
      binding.addRecyclerImages.visibility = View.VISIBLE
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


    //note save note
    binding.newButton.setOnClickListener {
      saveNote(needToUpdateNote)
    }

    //Add image
    binding.newImage.setOnClickListener {
      openNewImageOptions()
    }
  }

  /** Show dialog to open gallery or camera**/
  private fun openNewImageOptions() {
    AlertDialog.Builder(this).apply {
      this.setMessage("How do you wan to upload")
        .setTitle("Uploader options")
        .setPositiveButton("Camera") { _, _ ->
          openCamera()
        }
        .setNegativeButton("Gallery") {_, _ ->
          openGallery()
        }
    }.create().show()
  }

  private fun saveNewNote(
    documentReference: DocumentReference,
    dataToSave: Map<String, Any>,
    message: String,
  ) {
    documentReference.set(dataToSave).addOnSuccessListener {
      Timber.d(message)
      showMessage(this, message, Toast.LENGTH_LONG)
      backToNotesActivity(this)

    }.addOnFailureListener { exception ->
      Timber.d("Error: $exception")
      showLastOnCreateNoteError()
    }
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

  private fun deleteNote(
    noteTitle: String,
    dataToSave: Map<String, Any>
  ) {
    if (id.isNullOrEmpty().not()) {
      fireBaseStore?.let { fStore ->
        firebaseUser?.let { fUser ->
          val documentReference: DocumentReference = fStore.collection(
            FIRE_BASE_COLLECTION_NAME
          )
            .document(fUser.uid)
            .collection(FIRE_BASE_USER_COLLECTION)
            .document(id.orEmpty())

          documentReference.delete().addOnSuccessListener {
            Timber.d("$noteTitle has been deleted")
            saveNewNote(documentReference, dataToSave, NOTE_EDIT_SUCCESS)

          }.addOnFailureListener {
            showError(
              this,
              binding.root,
              "Something was wrong, please try again",
              Snackbar.LENGTH_LONG
            )
            Timber.d("Something was wrong, please try again")
          }
        }
      }
    }
  }

  private fun saveNote(needToUpdateNote: Boolean) {
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
            saveNewNote(documentReference, dataToSave, SAVE_NOTE_STATE_VO.NOTE_SAVE.data)
            // Edit actual note
          } else {
            deleteNote(title, dataToSave)
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

  private fun hideKeyBoard() {
    hideVirtualKeyBoard(this, binding.titleNote)
    hideVirtualKeyBoard(this, binding.bodyNote)
  }

  private fun showLastOnCreateNoteError() {
    showError(this, binding.root, "Something is wrong, try again", Snackbar.LENGTH_LONG)
    initFireBase()
  }

  private fun notesListener(listener: NotesListener) {
    if (listener is NotesListener.DeleteImageAction) {
      //TODO
      showMessage(this, "Delete image", Toast.LENGTH_LONG)
    }
  }

  /** Init gallery intent  */
  private fun openGallery() {
    val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
    startActivityForResult(intentGallery, SELECT_ACTIVITY_IMAGE)
  }

  /** init intent for camera intent */
  private fun openCamera() {
    contentValues.put(MediaStore.Images.Media.TITLE, "")
    contentValues.put(MediaStore.Images.Media.DESCRIPTION, "")

    imageUrl = this.contentResolver?.insert(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    )

    if (imageUrl != null) {
      imageUrl?.let {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, it)
        startActivityForResult(intent, TAKE_ACTIVITY_IMAGE)
      }

    } else {
      showError(this, binding.root,"Something was wrong opening camera", Snackbar.LENGTH_LONG)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when {
      requestCode == SELECT_ACTIVITY_IMAGE && resultCode == Activity.RESULT_OK -> {
        data?.let {
          imageUrl = it.data
          imageUrl?.let { imageUri ->
            viewModel.addNewImage(imageUri)
          }
        }
      }

      requestCode == TAKE_ACTIVITY_IMAGE && resultCode == Activity.RESULT_OK -> {
        imageUrl?.let { imageUri ->
          viewModel.addNewImage(imageUri)
        }
      }
    }
  }


}