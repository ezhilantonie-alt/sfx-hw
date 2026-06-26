package com.example.data

import kotlinx.coroutines.flow.Flow

class HomeworkRepository(private val homeworkDao: HomeworkDao) {
    fun getHomework(className: String, section: String, date: String): Flow<List<HomeworkEntity>> {
        return homeworkDao.getHomeworkForClassSectionAndDate(className, section, date)
    }

    fun getAllHomeworkForClassSection(className: String, section: String): Flow<List<HomeworkEntity>> {
        return homeworkDao.getAllHomeworkForClassSection(className, section)
    }

    fun getAllHomework(): Flow<List<HomeworkEntity>> = homeworkDao.getAllHomework()

    suspend fun insert(homework: HomeworkEntity): Long = homeworkDao.insertHomework(homework)

    suspend fun update(homework: HomeworkEntity) = homeworkDao.updateHomework(homework)

    suspend fun deleteById(id: Int) = homeworkDao.deleteHomeworkById(id)
}
