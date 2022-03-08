package es.infolojo.keepitdroid.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseModel
import es.infolojo.keepitdroid.databinding.ActivityNotesBinding
import es.infolojo.keepitdroid.ui.adapters.FireStoreRecyclerNotesAdapter
import es.infolojo.keepitdroid.ui.adapters.NotesListener
import es.infolojo.keepitdroid.ui.viewmodels.NotesViewModel
import es.infolojo.keepitdroid.utils.*
import timber.log.Timber


class NotesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotesBinding
    private var noteAdapter: FireStoreRecyclerNotesAdapter? = null
    private val staggeredGridLayoutManager =
        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

    private val viewModel: NotesViewModel by viewModels()
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseUser: FirebaseUser? = null
    private var fireStore: FirebaseFirestore? = null
    private var fireBaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureToolbar()
        initFireBase()
        initViews()
    }

    override fun onStart() {
        super.onStart()
        noteAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        noteAdapter?.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.notes_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {
                firebaseAuth?.signOut()
                backLogin(this)
                true
            }
            else -> false
        }
    }

    private fun configureToolbar() {
        val toolbarTextColor = getAnyColor(this, R.color.white_note)
        setSupportActionBar(binding.activityNoteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.activityNoteToolbar.setTitle(R.string.app_name_normalized)
        binding.activityNoteToolbar.setSubtitle(R.string.toolbar_subtitle)
        binding.activityNoteToolbar.setTitleTextColor(toolbarTextColor)
        binding.activityNoteToolbar.setSubtitleTextColor(toolbarTextColor)
    }


    private fun initFireBase() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
        if (firebaseUser == null) {
            firebaseUser = firebaseAuth?.currentUser
        }

        if (fireStore == null) {
            fireStore = FirebaseFirestore.getInstance()
        }

        if (fireBaseReference == null) {
            fireBaseReference = FirebaseDatabase.getInstance().reference
        }

        firebaseAuth?.let { mAuth ->
            firebaseUser?.let { mUser ->
                fireStore?.let { fStore ->
                    viewModel.init(mAuth, mUser, fStore)
                }
            }
        }
    }

    private fun initViews() {

        if (fireStore != null && firebaseUser != null && firebaseAuth != null) {
            fireStore?.let { fStore ->
                firebaseUser?.let { fUser ->
                    firebaseAuth?.let { _ ->
                        //GET USER NOTES ORDERED BY TITLE ASCENDING
                        val query: Query = fStore.collection(FIRE_BASE_COLLECTION_NAME)
                            .document(fUser.uid)
                            .collection(FIRE_BASE_USER_COLLECTION)
                       //     .orderBy(NOTE_TITLE, Query.Direction.ASCENDING)

                        val allNotes: FirestoreRecyclerOptions<FireBaseModel> =
                            FirestoreRecyclerOptions.Builder<FireBaseModel>()
                                .setQuery(query, FireBaseModel::class.java).build()

                        noteAdapter = FireStoreRecyclerNotesAdapter(allNotes, ::listener)

                        binding.recyclerNotes.setHasFixedSize(true)
                        binding.recyclerNotes.layoutManager = staggeredGridLayoutManager
                        binding.recyclerNotes.adapter = noteAdapter
                    }
                }
            }
        } else {
            //ShowError
        }

        binding.newButton.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }


    }

    private fun listener(actions: NotesListener) {
        when (actions) {
            is NotesListener.DeleteAction -> {
                deleteNote(actions.item, actions.position)
            }

            is NotesListener.DetailAction -> {
                val id = noteAdapter?.snapshots?.getSnapshot(actions.position)?.id

                if (id != null) {
                    this.finish()
                    startActivity(
                        Intent(this, DetailNoteActivity::class.java).apply {
                            this.putExtra(NOTE_TITLE, actions.item.title)
                            this.putExtra(NOTE_BODY, actions.item.content)
                            this.putExtra(NOTE_COLOR, actions.item.color.value)
                            this.putExtra(NOTE_KEY, id)
                        }
                    )

                } else {
                    showError(this, binding.root, "Something was wrong. Please try again", Snackbar.LENGTH_LONG)
                }
            }
        }
    }

    private fun deleteNote(item: FireBaseModel, position: Int) {
        val noteId: String = noteAdapter?.snapshots?.getSnapshot(position)?.id.orEmpty()

        if (noteId.isNotEmpty()) {
           fireStore?.let { fStore ->
               firebaseUser?.let { fUser ->
                   val documentReference: DocumentReference = fStore.collection(
                       FIRE_BASE_COLLECTION_NAME)
                       .document(fUser.uid)
                       .collection(FIRE_BASE_USER_COLLECTION)
                       .document(noteId)

                   documentReference.delete().addOnSuccessListener {
                       showMessage(this, "${item.title} has been deleted", Toast.LENGTH_LONG)

                   }.addOnFailureListener {
                       showError(this, binding.root, "Something was wrong, please try again", Snackbar.LENGTH_LONG)
                       Timber.d("Something was wrong, please try again")
                   }
               }
           }
        }
    }
}