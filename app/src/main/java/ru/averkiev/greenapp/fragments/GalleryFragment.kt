package ru.averkiev.greenapp.fragments

import GalleryAdapter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import ru.averkiev.greenapp.R
import ru.averkiev.greenapp.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding ?: throw Exception("FragmentGalleryBinding is null")

    private lateinit var galleryAdapter: GalleryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        binding.goPhotoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_galleryFragment_to_imageCaptureFragment)
        }

        setupGallery()

        return binding.root
    }

    private fun setupGallery() {
        val galleryAdapter = GalleryAdapter(fetchMediaFiles(), { uri, mediaType ->
            val position = fetchMediaFiles().indexOfFirst { it.first == uri && it.second == mediaType }
            val action = GalleryFragmentDirections.actionGalleryFragmentToViewerFragment(
                uri.toString(),
                mediaType,
                position
            )
            findNavController().navigate(action)
        }, this)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = galleryAdapter
        }
    }

    private fun fetchMediaFiles(): List<Triple<Uri, Int, String>> {
        val mediaList = mutableListOf<Triple<Uri, Int, String>>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED
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
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val type = cursor.getInt(typeColumn)
                val dateAdded = cursor.getLong(dateColumn) * 1000
                val dateFormatted = android.text.format.DateFormat.format("dd.MM.yyyy", dateAdded).toString()
                val uri = MediaStore.Files.getContentUri("external", id)
                mediaList.add(Triple(uri, type, dateFormatted))
            }
        }

        return mediaList
    }

    fun deleteMediaFile(uri: Uri) {
        val contentResolver = requireContext().contentResolver
        contentResolver.delete(uri, null, null)
        setupGallery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}