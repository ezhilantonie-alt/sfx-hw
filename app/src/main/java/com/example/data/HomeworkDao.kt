package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework WHERE className = :className AND section = :section AND date = :date")
    fun getHomeworkForClassSectionAndDate(className: String, section: String, date: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework WHERE className = :className AND section = :section ORDER BY date DESC, lastUpdated DESC")
    fun getAllHomeworkForClassSection(className: String, section: String): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework ORDER BY date DESC, lastUpdated DESC")
    fun getAllHomework(): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: HomeworkEntity): Long

    @Update
    suspend fun updateHomework(homework: HomeworkEntity)

    @Query("DELETE FROM homework WHERE id = :id")
    suspend fun deleteHomeworkById(id: Int)
}
