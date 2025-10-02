package com.example.mnotepad.entities.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.mnotepad.entities.enums.NoteType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val categoryIds: List<Int>? = null,
    val color: String? = null,
    val type: NoteType = NoteType.TEXT,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    @Ignore
    constructor(
        title: String,
        content: String,
        categoryIds: List<Int>? = null,
        color: String? = null,
        type: NoteType = NoteType.TEXT
    ) : this(
        id = 0,
        title = title,
        content = content,
        categoryIds = categoryIds,
        color = color,
        type = type,
        isDeleted = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
