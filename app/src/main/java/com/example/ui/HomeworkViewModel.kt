package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HomeworkDatabase
import com.example.data.HomeworkEntity
import com.example.data.HomeworkRepository
import com.example.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeworkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeworkRepository

    // Splash screen state
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    // Language state: "en" for English, "ta" for Tamil
    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    // Login State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Teacher Login State
    private val _isTeacherLoggedIn = MutableStateFlow(false)
    val isTeacherLoggedIn: StateFlow<Boolean> = _isTeacherLoggedIn.asStateFlow()

    private val _teacherClass = MutableStateFlow("")
    val teacherClass: StateFlow<String> = _teacherClass.asStateFlow()

    private val _teacherSection = MutableStateFlow("")
    val teacherSection: StateFlow<String> = _teacherSection.asStateFlow()

    // Parental Selection State
    private val _selectedClass = MutableStateFlow("6")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _selectedSection = MutableStateFlow("A")
    val selectedSection: StateFlow<String> = _selectedSection.asStateFlow()

    // Date State: yyyy-MM-DD
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Raw Homework Flows
    val allHomework: StateFlow<List<HomeworkEntity>>

    init {
        val database = HomeworkDatabase.getDatabase(application)
        repository = HomeworkRepository(database.homeworkDao())
        
        allHomework = repository.getAllHomework()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initialize Android Push Notification Channel
        NotificationHelper.initNotificationChannel(application)

        // Simulate opening splash screen animation for 2.5 seconds
        viewModelScope.launch {
            delay(2500)
            _splashFinished.value = true
        }

        // Populate sample/seed data if database is empty to make it look ready and useful on first launch!
        viewModelScope.launch {
            allHomework.take(2).collect { list ->
                if (list.isEmpty()) {
                    seedSampleHomework()
                }
            }
        }
    }

    // Bilingual localization mapping
    private val translations = mapOf(
        "en" to mapOf(
            "school_title" to "St. Francis Xavier's Higher Secondary School",
            "school_subtitle" to "Thoothukudi, Tamil Nadu",
            "motto" to "Faith and Labor",
            "language_btn" to "தமிழ்",
            "language_label" to "Language",
            "app_subtitle" to "Official Homework Portal",
            "role_selection_title" to "Welcome! Select Login Type",
            "admin_login_btn" to "Admin Login",
            "parent_login_btn" to "Parental Login",
            "admin_title" to "Admin Portal Login",
            "parent_title" to "Parental Portal",
            "username_label" to "Username",
            "password_label" to "Password",
            "login_action_btn" to "Sign In",
            "back_btn" to "Back",
            "class_label" to "Class",
            "section_label" to "Section",
            "date_label" to "Date",
            "subject_label" to "Subject",
            "select_date_btn" to "Select Earlier Date",
            "enter_parental_btn" to "View Homework",
            "logout_btn" to "Logout",
            "logged_in_as" to "Logged in as Admin",
            "feed_homework_title" to "Feed Homework",
            "edit_homework_title" to "Edit Homework Entry",
            "homework_content_hint" to "Enter homework details or daily notes here...",
            "save_btn" to "Save Homework",
            "cancel_btn" to "Cancel",
            "update_btn" to "Update Entry",
            "delete_btn" to "Delete",
            "no_homework_msg" to "No homework found for the selected Class, Section, and Date.",
            "error_empty_content" to "Homework content cannot be empty!",
            "error_invalid_credentials" to "Invalid username or password. (Hint: use admin / admin)",
            "admin_credentials_hint" to "Demo credentials: admin / admin",
            "subject_tamil" to "Tamil",
            "subject_english" to "English",
            "subject_maths" to "Maths",
            "subject_science" to "Science",
            "subject_social" to "Social Science",
            "subject_notes" to "Notes",
            "add_homework_fab" to "Add Homework",
            "academic_year" to "Academic Homework Tracker",
            "history_title" to "All Homework History",
            "success_save" to "Homework saved successfully!",
            "success_delete" to "Homework entry deleted!",
            "confirm_delete" to "Are you sure you want to delete this homework entry?"
        ),
        "ta" to mapOf(
            "school_title" to "புனித பிரான்சிஸ் சவேரியார் மேல்நிலைப் பள்ளி",
            "school_subtitle" to "தூத்துக்குடி, தமிழ்நாடு",
            "motto" to "விசுவாசமும் உழைப்பும்",
            "language_btn" to "English",
            "language_label" to "மொழி",
            "app_subtitle" to "அதிகாரப்பூர்வ வீட்டுப்பாடப் பதிவேடு",
            "role_selection_title" to "வரவேற்கிறோம்! உள்நுழைவு வகையைத் தேர்ந்தெடுக்கவும்",
            "admin_login_btn" to "நிர்வாகி உள்நுழைவு",
            "parent_login_btn" to "பெற்றோர் உள்நுழைவு",
            "admin_title" to "நிர்வாகி உள்நுழைவு",
            "parent_title" to "பெற்றோர் பக்கம்",
            "username_label" to "பயனர் பெயர்",
            "password_label" to "கடவுச்சொல்",
            "login_action_btn" to "உள்நுழை",
            "back_btn" to "பின்செல்லவும்",
            "class_label" to "வகுப்பு",
            "section_label" to "பிரிவு",
            "date_label" to "தேதி",
            "subject_label" to "பாடம்",
            "select_date_btn" to "முந்தைய தேதியைத் தேர்ந்தெடு",
            "enter_parental_btn" to "வீட்டுப்பாடத்தைப் பார்",
            "logout_btn" to "வெளியேறு",
            "logged_in_as" to "நிர்வாகியாக உள்நுழைந்துள்ளார்",
            "feed_homework_title" to "வீட்டுப்பாடம் பதிவு செய்",
            "edit_homework_title" to "வீட்டுப்பாடத்தைத் தொகு",
            "homework_content_hint" to "வீட்டுப்பாட விவரங்கள் அல்லது குறிப்புகளை இங்கே எழுதவும்...",
            "save_btn" to "வீட்டுப்பாடத்தைச் சேமி",
            "cancel_btn" to "ரத்து செய்",
            "update_btn" to "இற்றைப்படுத்து",
            "delete_btn" to "அழி",
            "no_homework_msg" to "தேர்ந்தெடுக்கப்பட்ட வகுப்பு, பிரிவு மற்றும் தேதிக்கான வீட்டுப்பாடங்கள் எதுவும் இல்லை.",
            "error_empty_content" to "வீட்டுப்பாட விவரங்கள் காலியாக இருக்கக்கூடாது!",
            "error_invalid_credentials" to "தவறான பயனர் பெயர் அல்லது கடவுச்சொல். (குறிப்பு: admin / admin)",
            "admin_credentials_hint" to "பயனர் பெயர்: admin / கடவுச்சொல்: admin",
            "subject_tamil" to "தமிழ்",
            "subject_english" to "ஆங்கிலம்",
            "subject_maths" to "கணிதம்",
            "subject_science" to "அறிவியல்",
            "subject_social" to "சமூக அறிவியல்",
            "subject_notes" to "குறிப்புகள்",
            "add_homework_fab" to "வீட்டுப்பாடம் சேர்",
            "academic_year" to "கல்வி வீட்டுப்பாடக் கண்காணிப்பாளர்",
            "history_title" to "அனைத்து வீட்டுப்பாட வரலாறு",
            "success_save" to "வீட்டுப்பாடம் வெற்றிகரமாக சேமிக்கப்பட்டது!",
            "success_delete" to "வீட்டுப்பாடம் நீக்கப்பட்டது!",
            "confirm_delete" to "இந்த வீட்டுப்பாடப் பதிவை நிச்சயமாக நீக்க விரும்புகிறீர்களா?"
        )
    )

    fun getString(key: String): String {
        return translations[_language.value]?.get(key) ?: translations["en"]?.get(key) ?: key
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "en") "ta" else "en"
    }

    fun setLanguage(lang: String) {
        if (lang == "en" || lang == "ta") {
            _language.value = lang
        }
    }

    private val sharedPrefs = getApplication<Application>().getSharedPreferences("sfx_homework_settings", android.content.Context.MODE_PRIVATE)

    fun getAdminUsername(): String {
        return sharedPrefs.getString("admin_username", "admin") ?: "admin"
    }

    fun setAdminUsername(newUsername: String) {
        sharedPrefs.edit().putString("admin_username", newUsername.trim()).apply()
    }

    fun getAdminPassword(): String {
        return sharedPrefs.getString("admin_password", "admin") ?: "admin"
    }

    fun setAdminPassword(newPassword: String) {
        sharedPrefs.edit().putString("admin_password", newPassword).apply()
    }

    fun getClassSectionCode(className: String, section: String): String {
        val defaultCode = "${className}${section}" // e.g. "6A", "6B", "7C"
        return sharedPrefs.getString("code_${className}_${section}", defaultCode) ?: defaultCode
    }

    fun setClassSectionCode(className: String, section: String, newCode: String) {
        sharedPrefs.edit().putString("code_${className}_${section}", newCode.trim()).apply()
    }

    fun openClassSectionWithCode(code: String): Pair<String, String>? {
        val cleanCode = code.trim()
        if (cleanCode.isEmpty()) return null

        val classes = listOf("6", "7", "8", "9", "10")
        val sections = listOf("A", "B", "C", "D", "E")

        // 1. Search through configured/saved codes first
        for (cls in classes) {
            for (sec in sections) {
                val assignedCode = getClassSectionCode(cls, sec)
                if (assignedCode.equals(cleanCode, ignoreCase = true)) {
                    setParentalSelection(cls, sec)
                    return Pair(cls, sec)
                }
            }
        }

        // 2. Fallback to direct naming convention (e.g., "6A", "6-A", "7C", "7-C")
        for (cls in classes) {
            for (sec in sections) {
                val direct1 = "${cls}${sec}"
                val direct2 = "${cls}-${sec}"
                if (cleanCode.equals(direct1, ignoreCase = true) || cleanCode.equals(direct2, ignoreCase = true)) {
                    setParentalSelection(cls, sec)
                    return Pair(cls, sec)
                }
            }
        }

        return null
    }

    fun loginAdmin(username: String, password: String): Boolean {
        val storedUsername = getAdminUsername()
        val storedPassword = getAdminPassword()
        return if (username.trim().lowercase() == storedUsername.lowercase() && password == storedPassword) {
            _isAdminLoggedIn.value = true
            _isTeacherLoggedIn.value = false // Clear teacher login if admin logs in
            true
        } else {
            false
        }
    }

    fun loginTeacherWithCode(code: String): Pair<String, String>? {
        val cleanCode = code.trim()
        if (cleanCode.isEmpty()) return null

        val classes = listOf("6", "7", "8", "9", "10")
        val sections = listOf("A", "B", "C", "D", "E")

        // 1. Search through configured/saved codes first
        for (cls in classes) {
            for (sec in sections) {
                val assignedCode = getClassSectionCode(cls, sec)
                if (assignedCode.equals(cleanCode, ignoreCase = true)) {
                    _isTeacherLoggedIn.value = true
                    _isAdminLoggedIn.value = false // Clear admin login if teacher logs in
                    _teacherClass.value = cls
                    _teacherSection.value = sec
                    setParentalSelection(cls, sec)
                    return Pair(cls, sec)
                }
            }
        }

        // 2. Fallback to direct naming convention (e.g., "6A", "6-A", "7C", "7-C")
        for (cls in classes) {
            for (sec in sections) {
                val direct1 = "${cls}${sec}"
                val direct2 = "${cls}-${sec}"
                if (cleanCode.equals(direct1, ignoreCase = true) || cleanCode.equals(direct2, ignoreCase = true)) {
                    _isTeacherLoggedIn.value = true
                    _isAdminLoggedIn.value = false
                    _teacherClass.value = cls
                    _teacherSection.value = sec
                    setParentalSelection(cls, sec)
                    return Pair(cls, sec)
                }
            }
        }

        return null
    }

    fun logout() {
        _isAdminLoggedIn.value = false
        _isTeacherLoggedIn.value = false
        _teacherClass.value = ""
        _teacherSection.value = ""
    }

    fun logoutAdmin() {
        logout()
    }

    fun setParentalSelection(className: String, section: String) {
        _selectedClass.value = className
        _selectedSection.value = section
    }

    fun setDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun resetDateToToday() {
        _selectedDate.value = getCurrentDateString()
    }

    // Reactive flow of filtered homework for Parent or Admin view based on Selection (Class, Section, Date)
    val filteredHomework: Flow<List<HomeworkEntity>> = combine(
        allHomework, _selectedClass, _selectedSection, _selectedDate
    ) { all, className, section, date ->
        all.filter {
            it.className == className && it.section == section && it.date == date
        }
    }

    // Reactive flow of all history homework for a specific Class + Section
    val classSectionHistory: Flow<List<HomeworkEntity>> = combine(
        allHomework, _selectedClass, _selectedSection
    ) { all, className, section ->
        all.filter {
            it.className == className && it.section == section
        }
    }

    fun addHomework(
        className: String,
        section: String,
        subject: String,
        content: String,
        date: String
    ) {
        if (content.trim().isEmpty()) return
        viewModelScope.launch {
            val entity = HomeworkEntity(
                date = date,
                className = className,
                section = section,
                subject = subject,
                content = content,
                lastUpdated = System.currentTimeMillis()
            )
            repository.insert(entity)

            // Trigger Push Notification
            val isNotes = subject == "Notes"
            val title = if (isNotes) {
                if (_language.value == "ta") {
                    "SFX: வகுப்பு $className-$section முக்கிய அறிவிப்பு"
                } else {
                    "SFX: Special Announcement - Class $className-$section"
                }
            } else {
                val localizedSubject = when (subject) {
                    "Tamil" -> if (_language.value == "ta") "தமிழ்" else "Tamil"
                    "English" -> if (_language.value == "ta") "ஆங்கிலம்" else "English"
                    "Maths" -> if (_language.value == "ta") "கணிதம்" else "Mathematics"
                    "Science" -> if (_language.value == "ta") "அறிவியல்" else "Science"
                    "Social Science" -> if (_language.value == "ta") "சமூக அறிவியல்" else "Social Science"
                    else -> subject
                }
                if (_language.value == "ta") {
                    "SFX: வகுப்பு $className-$section புதிய $localizedSubject வீட்டுப்பாடம்"
                } else {
                    "SFX: New $localizedSubject Homework - Class $className-$section"
                }
            }
            NotificationHelper.showNotification(getApplication(), title, content, className, section)
        }
    }

    fun updateHomework(entity: HomeworkEntity) {
        if (entity.content.trim().isEmpty()) return
        viewModelScope.launch {
            repository.update(entity.copy(lastUpdated = System.currentTimeMillis()))

            // Trigger Push Notification for Update
            val isNotes = entity.subject == "Notes"
            val title = if (isNotes) {
                if (_language.value == "ta") {
                    "SFX: வகுப்பு ${entity.className}-${entity.section} அறிவிப்பு புதுப்பிக்கப்பட்டது"
                } else {
                    "SFX: Announcement Updated - Class ${entity.className}-${entity.section}"
                }
            } else {
                val localizedSubject = when (entity.subject) {
                    "Tamil" -> if (_language.value == "ta") "தமிழ்" else "Tamil"
                    "English" -> if (_language.value == "ta") "ஆங்கிலம்" else "English"
                    "Maths" -> if (_language.value == "ta") "கணிதம்" else "Mathematics"
                    "Science" -> if (_language.value == "ta") "அறிவியல்" else "Science"
                    "Social Science" -> if (_language.value == "ta") "சமூக அறிவியல்" else "Social Science"
                    else -> entity.subject
                }
                if (_language.value == "ta") {
                    "SFX: வகுப்பு ${entity.className}-${entity.section} $localizedSubject வீட்டுப்பாடம் திருத்தப்பட்டது"
                } else {
                    "SFX: $localizedSubject Homework Updated - Class ${entity.className}-${entity.section}"
                }
            }
            NotificationHelper.showNotification(getApplication(), title, entity.content, entity.className, entity.section)
        }
    }

    fun deleteHomework(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private suspend fun seedSampleHomework() {
        val today = getCurrentDateString()
        
        // Let's create a calendar instance for yesterday
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val samples = listOf(
            HomeworkEntity(
                date = today,
                className = "10",
                section = "A",
                subject = "Maths",
                content = "Exercise 3.4 - Problems 1 to 10. Complete in classwork notebook. To be submitted tomorrow without fail."
            ),
            HomeworkEntity(
                date = today,
                className = "10",
                section = "A",
                subject = "English",
                content = "Read Chapter 4: 'A Letter to God'. Write down the central theme and character sketch of Lencho (150 words)."
            ),
            HomeworkEntity(
                date = today,
                className = "8",
                section = "B",
                subject = "Science",
                content = "Draw a neat, labeled diagram of the human plant cell. Write the functions of mitochondria and chloroplasts."
            ),
            HomeworkEntity(
                date = today,
                className = "8",
                section = "B",
                subject = "Tamil",
                content = "திருக்குறள் - அதிகாரம் 2 (வான்சிறப்பு) முதல் 5 குறள்களை மனப்பாடம் செய்து எழுதி வரவும்."
            ),
            HomeworkEntity(
                date = yesterday,
                className = "10",
                section = "A",
                subject = "Science",
                content = "Read page 74-78. Complete the questions at the back of the chapter. Prepare for a short test."
            ),
            HomeworkEntity(
                date = today,
                className = "6",
                section = "C",
                subject = "Social Science",
                content = "Mark the major Harappan Civilization sites on the outline map of India. (Refer to textbook page 45)."
            ),
            HomeworkEntity(
                date = today,
                className = "6",
                section = "C",
                subject = "Notes",
                content = "Parent-Teacher Association (PTA) Meeting is scheduled for this Saturday at 10:00 AM. Please bring the sign diary."
            )
        )

        for (sample in samples) {
            repository.insert(sample)
        }
    }
}
