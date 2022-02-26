package es.infolojo.keepitdroid.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors
import es.infolojo.keepitdroid.data.FireBaseModel
import es.infolojo.keepitdroid.databinding.RowNoteLayoutBinding
import es.infolojo.keepitdroid.utils.getAnyColor
import es.infolojo.keepitdroid.utils.showMessage
import es.infolojo.keepitdroid.utils.toIntColor


class FireStoreRecyclerNotesAdapter(
    notes: FirestoreRecyclerOptions<FireBaseModel>,
    private val listener: (NotesListener) -> Unit,
) : FirestoreRecyclerAdapter<FireBaseModel, FireStoreRecyclerNotesAdapter.NotesViewHolder>(notes) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder =
        NotesViewHolder(
            RowNoteLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int, model: FireBaseModel) {
        holder.bind(model, listener, position)
    }


    inner class NotesViewHolder(val binding: RowNoteLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: FireBaseModel,
            listener: (NotesListener) -> Unit,
            notePosition: Int
        ) {

            //Note: initPopUpMenu
            val popupMenu = PopupMenu(
                itemView.context,
                binding.menuPopButton
            )

            //Note: link menu
            popupMenu.menuInflater.inflate(R.menu.row_note_menu, popupMenu.menu)

            val textColor = getAnyColor(
                itemView.context,
                if (item.color == FireBaseColors.BLACK_NOTE || item.color == FireBaseColors.RED_NOTE) {
                    R.color.white
                } else {
                    R.color.black
                }
            )

            if (item.color == FireBaseColors.BLACK_NOTE || item.color == FireBaseColors.RED_NOTE) {
                binding.menuPopButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_baseline_more_vert_blank_white
                    )
                )
            } else {
                binding.menuPopButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_baseline_more_vert_24
                    )
                )
            }

            binding.noteTitle.text = item.title
            binding.noteContent.text = item.content

            binding.noteTitle.setTextColor(textColor)
            binding.noteContent.setTextColor(textColor)

            binding.card.setBackgroundColor(
                getAnyColor(
                    itemView.context,
                    item.color.toIntColor()
                )
            )

            binding.menuPopButton.setOnClickListener {
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit -> {
                            showMessage(itemView.context, "Editas aquí la nota", Toast.LENGTH_SHORT)
                            NotesListener.DetailAction(
                                item,
                                notePosition
                            )
                        }

                        R.id.delete -> {
                            listener(
                                NotesListener.DeleteAction(
                                    item,
                                    notePosition
                                )
                            )
                        }
                    }
                    true
                }
                popupMenu.show()
            }
        }
    }

}