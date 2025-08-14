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
    private val placeholderImage: String = "/images/placeholder.png"
) : Composite<VerticalLayout>() {

    private var imagePreview = Image(placeholderImage, "Photo preview").apply {
        width = "150px"
        height = "150px"
        style["object-fit"] = "cover"
        isVisible = false
    }

    private var uploadedFileUrl: String? = null

    init {
        // Center everything inside the root VerticalLayout
        content.setSizeFull()
        content.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        content.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        content.isPadding = false
        content.isSpacing = true

        val upload = Upload().apply {
            maxFiles = 1
            isDropAllowed = true
            addFileRejectedListener { e -> println("Rejected: ${e.errorMessage}") }
        }
        val replaceButton = Button("Replace Photo").apply {
            isVisible = false
        }
        val inMemoryHandler = UploadHandler.inMemory { metadata, data ->
            val filePath: Path

            if (uploadedFileUrl != null && uploadedFileUrl!!.startsWith("/uploads/")) {
                // Overwrite existing file
                filePath = uploadDirectory.resolve(uploadedFileUrl!!.removePrefix("/uploads/"))
            } else {
                // New file
                val fileName = UUID.randomUUID().toString() + "_" + metadata.fileName
                filePath = uploadDirectory.resolve(fileName)
                uploadedFileUrl = "/uploads/$fileName"
            }

            Files.createDirectories(uploadDirectory)
            Files.write(filePath, data)

            val base64Data = Base64.getEncoder().encodeToString(data)
            imagePreview.src = "data:${metadata.contentType};base64,$base64Data"
            imagePreview.isVisible = true
            replaceButton.isVisible = true
            upload.isVisible = false
        }

        upload.setUploadHandler(inMemoryHandler)

        replaceButton.addClickListener {
            imagePreview.isVisible = false
            replaceButton.isVisible = false
            upload.isVisible = true
            upload.clearFileList()
        }

        content.add(imagePreview, upload, replaceButton)
    }

    fun getPhotoUrl(): String? = uploadedFileUrl

    fun setPhotoUrl(url: String?) {
        uploadedFileUrl = url
        if (url != null && url.startsWith("http")) {
            imagePreview.src = url
            imagePreview.isVisible = true
        } else {
            imagePreview.src = url ?: placeholderImage
            imagePreview.isVisible = false
        }
    }
}