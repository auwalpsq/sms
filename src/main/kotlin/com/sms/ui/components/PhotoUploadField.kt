package com.sms.ui.components

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.server.streams.UploadHandler
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class PhotoUploadField(
    private val uploadDirectory: Path,
    private val placeholderImage: String = "/images/passports/placeholder.png"
) : Composite<VerticalLayout>() {

    private var uploadedBytes: ByteArray? = null
    private var uploadedFileName: String? = null
    private var savedFileName: String? = null // actual saved file

    var imagePreview = Image(placeholderImage, "Photo preview").apply {
        width = "150px"
        height = "150px"
        style["object-fit"] = "cover"
        isVisible = false
    }

    val upload = Upload().apply {
        maxFiles = 1
        isDropAllowed = true
        addFileRejectedListener { e -> println("Rejected: ${e.errorMessage}") }
    }

    val replaceButton = Button("Replace Photo").apply {
        isVisible = false
    }

    init {
        content.setSizeFull()
        content.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        content.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        content.isPadding = false
        content.isSpacing = true

        val inMemoryHandler = UploadHandler.inMemory { metadata, data ->
            uploadedBytes = data
            uploadedFileName = metadata.fileName

            val base64Data = Base64.getEncoder().encodeToString(data)
            imagePreview.src = "data:${metadata.contentType};base64,$base64Data"
            imagePreview.isVisible = true
            replaceButton.isVisible = true
            upload.isVisible = false
        }

        upload.setUploadHandler(inMemoryHandler)

        replaceButton.addClickListener {
            uploadedBytes = null
            uploadedFileName = null
            imagePreview.isVisible = false
            replaceButton.isVisible = false
            upload.isVisible = true
            upload.clearFileList()
        }

        content.add(imagePreview, upload, replaceButton)
    }

    /** Save the current photo to disk, returns the saved file name */
    fun savePhoto(): String? {
        val bytes = uploadedBytes ?: return savedFileName // nothing new to save
        val fileName = UUID.randomUUID().toString() + "_" + (uploadedFileName ?: "photo.png")
        val filePath = uploadDirectory.resolve(fileName)

        Files.createDirectories(uploadDirectory)
        Files.write(filePath, bytes)

        // Optionally delete previous saved file
        savedFileName?.let {
            val oldFile = uploadDirectory.resolve(it)
            if (Files.exists(oldFile)) Files.delete(oldFile)
        }

        savedFileName = fileName
        uploadedBytes = null
        uploadedFileName = null
        return savedFileName
    }

    /** Set photo from previously saved file */
    fun setPhotoUrl(fileName: String?) {
        if (fileName.isNullOrBlank()) {
            imagePreview.src = placeholderImage
            imagePreview.isVisible = false
            upload.isVisible = true
            replaceButton.isVisible = false
            savedFileName = null
            return
        }

        val filePath = uploadDirectory.resolve(fileName).toFile()
        if (filePath.exists()) {
            val bytes = filePath.readBytes()
            val base64 = Base64.getEncoder().encodeToString(bytes)
            val mimeType = Files.probeContentType(filePath.toPath()) ?: "image/png"
            imagePreview.src = "data:$mimeType;base64,$base64"
            imagePreview.isVisible = true
            upload.isVisible = false
            replaceButton.isVisible = true
            savedFileName = fileName
        } else {
            // File missing
            imagePreview.src = placeholderImage
            imagePreview.isVisible = false
            upload.isVisible = true
            replaceButton.isVisible = false
            savedFileName = null
        }
    }

    fun getSavedPhoto(): String? = savedFileName
}