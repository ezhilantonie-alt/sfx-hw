package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homework")
data class HomeworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format: YYYY-MM-DD
    val className: String, // "6", "7", "8", "9", "10"
    val section: String, // "A", "B", "C", "D", "E"
    val subject: String, // "Tamil", "English", "Maths", "Science", "Social Science", "Notes"
    val content: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
