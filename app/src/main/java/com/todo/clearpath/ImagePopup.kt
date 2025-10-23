package com.todo.clearpath

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri

fun Context.showImagePopup(imageUri: String) {
    val dialog = Dialog(this)
    dialog.setContentView(R.layout.dialog_image_preview)
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    dialog.window?.setBackgroundDrawableResource(android.R.color.black)

    val fullImageView = dialog.findViewById<ImageView>(R.id.fullImageView)
    fullImageView.setImageURI(imageUri.toUri())
    fullImageView.setOnClickListener { dialog.dismiss() }

    dialog.show()
}
