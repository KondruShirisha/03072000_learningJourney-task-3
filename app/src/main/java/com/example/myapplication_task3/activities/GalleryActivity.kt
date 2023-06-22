package com.example.myapplication_task3.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_task3.Adapter.ImageAdapter
import com.example.myapplication_task3.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class GalleryActivity : BaseActivity() {

    private lateinit var toolbarGallery: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var uploadButton: Button
    private lateinit var chooseImageButton: Button
    private lateinit var chosenImageView: ImageView
    private lateinit var uploadContainer: LinearLayout
    private lateinit var addImageButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var imageList: MutableList<String>
    private lateinit var adapter: ImageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_gallery)

        // this code is for toolbar back icon
        toolbarGallery = findViewById(id.toolbar_gallery_activity)
        setupActionBar()
        "".also { title = it }


        // Initializing views and Firebase instances
        recyclerView = findViewById(id.images_recycler_view)
        mAuth = FirebaseAuth.getInstance()
        uploadButton = findViewById(id.uploadButton)
        chooseImageButton = findViewById(id.chooseButton)
        chosenImageView = findViewById(id.image)
        uploadContainer = findViewById(id.uploadContainer)
        addImageButton = findViewById(id.addImageButton)


        // Set up RecyclerView
        imageList = mutableListOf()
        adapter = ImageAdapter(imageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load images from Firebase
        loadImages()

        // Set clickl istener for Upload Image button
        uploadButton.setOnClickListener {
            recyclerView.visibility = View.GONE
            uploadButton.visibility = View.GONE
            uploadContainer.visibility = View.VISIBLE
        }

        // Set click listener for Choose Image button
        chooseImageButton.setOnClickListener {
            pickImageFromGalleryCamera()
        }

        // Set click listener for Add Image button
        addImageButton.setOnClickListener {
            uploadImage()
        }

    }

    private fun loadImages() {
        db.collection("users").document(user!!.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val imageUrls = data["imageUrls"] as? ArrayList<String>
                        if (imageUrls != null) {
                            imageList.addAll(imageUrls)
                            adapter.notifyDataSetChanged()
                            recyclerView.visibility=View.VISIBLE
                            uploadButton.visibility=View.VISIBLE
                            uploadContainer.visibility=View.GONE
                            addImageButton.visibility=View.GONE

                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
            }
    }




    private fun pickImageFromGalleryCamera() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    if (checkCameraPermission()) {
                        launchCamera()
                    } else {
                        requestCameraPermission()
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    if (checkGalleryPermission()) {
                        launchGallery()
                    } else {
                        requestGalleryPermission()
                    }
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun checkCameraPermission(): Boolean {
        val cameraPermission = android.Manifest.permission.CAMERA
        val result = ContextCompat.checkSelfPermission(this, cameraPermission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkGalleryPermission(): Boolean {
        val galleryPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        val result = ContextCompat.checkSelfPermission(this, galleryPermission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            GALLERY_PERMISSION_REQUEST_CODE
        )
    }

    private fun launchCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE)
    }

    private fun launchGallery() {
        val pickPhoto =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, launch camera
                    launchCamera()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, launch gallery
                    launchGallery()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun uploadImage() {
        val imageUri = selectedImageUri
        if (imageUri != null) {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storage.reference.child("images/$imageName")
            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageUrlToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        db.collection("users").document(user!!.uid)
            .update("imageUrls", FieldValue.arrayUnion(imageUrl))
            .addOnSuccessListener {
                val dialogView = LayoutInflater.from(this).inflate(layout.dialog_success, null)

                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create()

                dialogView.findViewById<TextView>(id.dialog_title)
                dialogView.findViewById<TextView>(id.dialog_message)

                dialog.show()

                Handler().postDelayed({
                    dialog.dismiss()

                    recyclerView.visibility = View.GONE
                    uploadButton.visibility = View.GONE
                    uploadContainer.visibility = View.GONE
                    addImageButton.visibility = View.VISIBLE

                    imageList.add(imageUrl)
                    adapter.notifyDataSetChanged()
                }, 5000)
            }
            .addOnFailureListener {
                val dialogView = LayoutInflater.from(this).inflate(layout.dialog_failure, null)


                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create()

                dialogView.findViewById<TextView>(id.dialog_title)
                dialogView.findViewById<TextView>(id.dialog_message)

                dialog.show()

                Handler().postDelayed({
                    dialog.dismiss()

                    recyclerView.visibility = View.GONE
                    uploadButton.visibility = View.GONE
                    uploadContainer.visibility = View.GONE
                    addImageButton.visibility = View.VISIBLE
                    imageList.add(imageUrl)
                    adapter.notifyDataSetChanged()
                }, 5000)
            }
    }

private var selectedImageUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        chosenImageView.setImageBitmap(imageBitmap)
                        uploadContainer.visibility = View.GONE
                        chosenImageView.visibility = View.VISIBLE
                        addImageButton.visibility = View.VISIBLE
                        // Update the selectedImageUri with the captured image URI
                        selectedImageUri = saveImageToGallery(imageBitmap)
                    }
                }
                PICK_IMAGE_REQUEST -> {
                    val imageUri = data?.data
                    imageUri?.let {
                        chosenImageView.setImageURI(it)
                        uploadContainer.visibility = View.GONE
                        chosenImageView.visibility = View.VISIBLE
                        addImageButton.visibility = View.VISIBLE
                        // Update the selectedImageUri with the picked image URI
                        selectedImageUri = imageUri
                    }
                }

            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentResolver = applicationContext.contentResolver
        val imageUri = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Image",
            "Image taken from camera"
        )
        return Uri.parse(imageUri)
    }


    private fun setupActionBar() {
        setSupportActionBar(toolbarGallery)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(drawable.ic_backbutton_black)

        }
        toolbarGallery.setNavigationOnClickListener { onBackPressed() }
    }



    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val PICK_IMAGE_REQUEST = 2
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
        private const val GALLERY_PERMISSION_REQUEST_CODE = 102
    }




}
