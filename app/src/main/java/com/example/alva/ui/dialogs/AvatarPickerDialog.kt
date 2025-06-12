package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.alva.R
import com.example.alva.data.models.Avatar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AvatarPickerDialog(
    private val currentAvatarId: Int? = null,
    private val onAvatarSelected: (Int?) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_avatar_picker, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAvatars)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)

        val avatars = Avatar.getAvailableAvatars()
        val adapter = AvatarAdapter(avatars, currentAvatarId) { selectedAvatarId ->
            onAvatarSelected(selectedAvatarId)
            dismiss()
        }
        recyclerView.adapter = adapter

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Avatar")
            .setView(view)
            .setNegativeButton("Remove Avatar") { _, _ ->
                onAvatarSelected(null)
            }
            .setNeutralButton("Cancel", null)
            .create()
    }

    private class AvatarAdapter(
        private val avatars: List<Avatar>,
        private val currentAvatarId: Int?,
        private val onAvatarClick: (Int) -> Unit
    ) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_avatar, parent, false)
            return AvatarViewHolder(view)
        }

        override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
            val avatar = avatars[position]
            holder.bind(avatar, avatar.id == currentAvatarId, onAvatarClick)
        }

        override fun getItemCount(): Int = avatars.size

        class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.imageViewAvatar)
            private val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)

            fun bind(avatar: Avatar, isSelected: Boolean, onAvatarClick: (Int) -> Unit) {

                Glide.with(itemView.context)
                    .load("file:///android_asset/${avatar.assetPath}")
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView)


                selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    onAvatarClick(avatar.id)
                }
            }
        }
    }
}