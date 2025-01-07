package ru.averkiev.greenapp.fragments

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.averkiev.greenapp.R
import ru.averkiev.greenapp.databinding.FragmentViewerBinding

class ViewerFragment : Fragment() {
    private var _binding: FragmentViewerBinding? = null
    private val binding get() = _binding ?: throw Exception("FragmentViewerBinding is null")

    private var currentPosition: Int = 0
    private lateinit var mediaFiles: List<Pair<Uri, Int>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewerBinding.inflate(inflater, container, false)

        val args = ViewerFragmentArgs.fromBundle(requireArguments())
        val mediaUri = Uri.parse(args.uri)
        val mediaType = args.mediaType

        currentPosition = args.position
        mediaFiles = fetchMediaFiles()

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_viewerFragment_to_galleryFragment)
        }

        setupMediaView(mediaUri, mediaType)
        setupNavigationButtons()

        return binding.root
    }

    private fun setupMediaView(uri: Uri, mediaType: Int) {
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {

            binding.imageView.visibility = View.VISIBLE
            binding.videoView.visibility = View.GONE

            Glide.with(this)
                .load(uri)
                .into(binding.imageView)
        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {

            binding.imageView.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE

            binding.videoView.setVideoURI(uri)
            binding.videoView.setOnPreparedListener { it.isLooping = true }
            binding.videoView.start()
        }
    }

    private fun setupNavigationButtons() {

        binding.prevButton.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition -= 1
                navigateToMedia(currentPosition)
            }
        }

        binding.nextButton.setOnClickListener {
            if (currentPosition < mediaFiles.size - 1) {
                currentPosition += 1
                navigateToMedia(currentPosition)
            }
        }
    }

    private fun navigateToMedia(position: Int) {
        // Получаем URI и тип медиафайла по индексу
        val mediaUri = mediaFiles[position].first
        val mediaType = mediaFiles[position].second

        // Используем action для навигации, передавая URI, тип и позицию
        val action = ViewerFragmentDirections.actionViewerFragmentSelf(
            mediaUri.toString(),
            mediaType,
            position
        )

        // Переходим по action
        findNavController().navigate(action)
    }

    private fun fetchMediaFiles(): List<Pair<Uri, Int>> {
        val mediaList = mutableListOf<Pair<Uri, Int>>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        requireContext().contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val type = cursor.getInt(typeColumn)
                val uri = MediaStore.Files.getContentUri("external", id)
                mediaList.add(Pair(uri, type))
            }
        }

        return mediaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}