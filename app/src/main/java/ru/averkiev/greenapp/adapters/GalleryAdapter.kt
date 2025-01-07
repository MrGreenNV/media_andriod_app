import android.app.AlertDialog
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.averkiev.greenapp.R
import ru.averkiev.greenapp.fragments.GalleryFragment

class GalleryAdapter(
    private val mediaFiles: List<Triple<Uri, Int, String>>,
    private val onMediaClick: (Uri, Int) -> Unit,
    private val galleryFragment: GalleryFragment
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val (fileUri, mediaType, dateAdded) = mediaFiles[position]

        Glide.with(holder.itemView.context)
            .load(fileUri)
            .centerCrop()
            .into(holder.imageView)

        holder.videoIcon.visibility =
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onMediaClick(fileUri, mediaType)
        }

        holder.itemView.setOnLongClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, fileUri)
            true
        }

        // Устанавливаем дату в TextView
        holder.dateTextView.text = dateAdded
    }

    private fun showDeleteConfirmationDialog(context: android.content.Context, fileUri: Uri) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Удалить файл?")
            .setMessage("Вы уверены, что хотите удалить этот файл?")
            .setPositiveButton("Удалить") { _, _ ->
                galleryFragment.deleteMediaFile(fileUri)
                Toast.makeText(context, "Файл удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .create()

        alertDialog.show()
    }

    override fun getItemCount() = mediaFiles.size

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val videoIcon: ImageView = view.findViewById(R.id.videoIcon)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView) // Добавляем TextView
    }
}