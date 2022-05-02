package es.infolojo.keepitdroid.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.vo.SAVE_NOTE_STATE_VO

class NotesViewModel: ViewModel() {

    private var _mAuth: FirebaseAuth? = null
    private var _mUser: FirebaseUser? = null
    private var _fStore: FirebaseFirestore? = null

    private var _titleNote: String = ""
    private var _saveNote: String = ""
    private val _noteColor = MutableLiveData<FireBaseColors>()

    val noteColor: LiveData<FireBaseColors>
        get() = _noteColor


    private val _isDuplicated = MutableLiveData<Boolean>()

    val isDuplicated: LiveData<Boolean>
        get() = _isDuplicated

    private val imagesList = MutableLiveData<List<Uri>>()
    fun getImageList(): LiveData<List<Uri>> = imagesList

    init {
        _noteColor.postValue(FireBaseColors.WHITE_NOTE)
    }

    fun init(mAut: FirebaseAuth, mUser: FirebaseUser, fireBaseStore: FirebaseFirestore) {
        _mAuth = mAut
        _mUser = mUser
        _fStore = fireBaseStore
    }

    fun setNoteColor(noteColor: FireBaseColors) {
        _noteColor.postValue(noteColor)
    }

    fun setIsDuplicatedTitle(value: Boolean){
        _isDuplicated.postValue(value)
    }

    fun getMauth(): FirebaseAuth? = _mAuth
    fun getMuser(): FirebaseUser? = _mUser
    fun getNoteColor(): FireBaseColors =
        _noteColor.value?:FireBaseColors.WHITE_NOTE

    fun saveDataNote(titleNote: String, saveNote: String) {
        if (_titleNote != titleNote) {
            _titleNote = titleNote
        }

        if (_saveNote != saveNote) {
            _saveNote = saveNote
        }
    }

    fun checkDataBeforeCreate(): SAVE_NOTE_STATE_VO {
        return when {
            _titleNote.isBlank() && _saveNote.isBlank() -> {
                SAVE_NOTE_STATE_VO.GLOBAL_ERROR
            }
            _titleNote.isBlank() && _saveNote.isBlank().not() -> {
                SAVE_NOTE_STATE_VO.TITLE_ERROR
            }
            _saveNote.isBlank() && _titleNote.isBlank().not() -> {
                SAVE_NOTE_STATE_VO.BODY_ERROR
            }
            else -> {
                SAVE_NOTE_STATE_VO.NOTE_SAVE
            }
        }
    }

    //Images
    /** Add images */
    fun addNewImage(uri: Uri){
        val newImagesList: MutableList<Uri> = mutableListOf()
        newImagesList.addAll(imagesList.value.orEmpty()).also {
            newImagesList.add(uri)
        }
        this.imagesList.value = newImagesList
    }

    /** Delete an image */
    fun deleteImage(position: Int){
        val newImageList: MutableList<Uri> = mutableListOf()
        newImageList.addAll(imagesList.value.orEmpty())
        newImageList.removeAt(position)
        this.imagesList.value = newImageList
    }

    /** First insert */
    fun firstImage() = this.addNewImage(Uri.EMPTY)

}