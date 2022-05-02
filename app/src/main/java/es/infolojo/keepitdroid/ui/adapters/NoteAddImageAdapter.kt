package es.infolojo.keepitdroid.ui.adapters

import android.media.Image
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
            .inflate(R.layout.image_row, parent, false)
        return NoteAddImageAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteAddImageAdapterViewHolder, position: Int) {
        val uriImage = getItem(position)
        holder.bind(uriImage, listener)
    }

    class NoteAddImageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(uriImage: Uri, listener: (NotesListener) -> Unit) {
            (itemView.findViewById(R.id.image) as ImageView).setImageURI(uriImage)
            (itemView.findViewById(R.id.delete_icon) as ImageView).setOnClickListener {
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