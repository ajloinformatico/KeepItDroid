package es.infolojo.keepitdroid.ui.adapters

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import es.infolojo.keepitdroid.utils.showError
import es.infolojo.keepitdroid.utils.showMessage
import timber.log.Timber

//TODO: ADD LISTENER TO CREATE NOTE ACTIVITY
class FireBaseSingleValueEvent(val context: Context, val view: View): ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
        for (appleSnapshot in snapshot.children) {
            appleSnapshot.ref.removeValue()
            Timber.d("Your note has been deleted")
            showMessage(context, "Your note has been deleted", Toast.LENGTH_LONG)
        }
    }

    override fun onCancelled(error: DatabaseError) {
        Timber.d("Something was wrong with delete")
        showError(
            context,
            view,
            "Something was wrong with delete",
            Snackbar.LENGTH_LONG
        )
    }
}