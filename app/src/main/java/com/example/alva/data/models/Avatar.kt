package com.example.alva.data.models

data class Avatar(
    val id: Int,
    val fileName: String,
    val assetPath: String
) {
    companion object {
        fun getAvailableAvatars(): List<Avatar> {
            return (1..20).map { id ->
                Avatar(
                    id = id,
                    fileName = "avatar_$id.png",
                    assetPath = "avatars/avatar_$id.png"
                )
            }
        }

        fun getAvatarById(avatarId: Int): Avatar? {
            return getAvailableAvatars().find { it.id == avatarId }
        }
    }
}