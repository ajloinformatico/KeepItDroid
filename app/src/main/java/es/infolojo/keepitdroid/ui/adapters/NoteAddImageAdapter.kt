package es.infolojo.keepitdroid.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import es.infolojo.keepitdroid.R

class NoteAddImageAdapter(private val listener: (NotesListener) -> Unit) : ListAdapter<Uri,
        NoteAddImageAdapter.NoteAddImageAdapterViewHolder>(ImageComparator()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteAddImageAdapterViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_note_layout, parent, false)
        return NoteAddImageAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteAddImageAdapterViewHolder, position: Int) {
        val uriImage = getItem(position)
        holder.bind(uriImage, listener)
    }

    class NoteAddImageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePreview: ImageView = itemView.findViewById(R.id.image)
        private val deleteImage: ImageView = itemView.findViewById(R.id.delete_icon)


        fun bind(uriImage: Uri, listener: (NotesListener) -> Unit) {
            imagePreview.setImageURI(uriImage)
            deleteImage.setOnClickListener {
                listener(
                    NotesListener.DeleteImageAction(
                        position = layoutPosition
                    )
                )
            }
        }
    }

    class ImageComparator : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean =
            oldItem.toString() == newItem.toString()

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean =
            oldItem.toString() == newItem.toString()
    }
}