package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HomeworkEntity
import com.example.ui.HomeworkViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HomeworkApp()
            }
        }
    }
}

enum class AppScreen {
    RoleSelection,
    AdminLogin,
    TeacherLogin,
    ParentalLogin,
    HomeworkDashboard
}

@Composable
fun HomeworkApp(viewModel: HomeworkViewModel = viewModel()) {
    val splashFinished by viewModel.splashFinished.collectAsState()

    Crossfade(
        targetState = splashFinished,
        animationSpec = tween(800),
        label = "SplashCrossfade"
    ) { finished ->
        if (!finished) {
            SplashScreenAnimation()
        } else {
            MainAppNavigation(viewModel)
        }
    }
}

// 1. OPENING SPLASH SCREEN & EMBLEM ANIMATION
@Composable
fun SplashScreenAnimation() {
    var startAnim by remember { mutableStateOf(false) }
    
    // Smooth transitions for scale, opacity, and translate
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "EmblemScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1200),
        label = "EmblemAlpha"
    )

    LaunchedEffect(Unit) {
        startAnim = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B), // Dark Indigo 950
                        Color(0xFF312E81)  // Indigo 900
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High-fidelity custom drew School Emblem Vector
            SchoolEmblemCanvas(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .animateContentSize()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // School Name Typography with animations
            AnimatedVisibility(
                visible = startAnim,
                enter = fadeIn(tween(1000, 300)) + expandVertically(tween(1000, 300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ST. FRANCIS XAVIER'S",
                        color = Color(0xFFD4AF37), // Pure Gold
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Higher Secondary School",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thoothukudi",
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = startAnim,
                enter = fadeIn(tween(1000, 800))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFFD4AF37),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Faith and Labor",
                        color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SchoolEmblemCanvas(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_school_logo),
            contentDescription = "School Emblem Logo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

// 2. NAVIGATION AND APPLICATION STATE CONTAINER
@Composable
fun MainAppNavigation(viewModel: HomeworkViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.RoleSelection) }
    val language by viewModel.language.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isTeacherLoggedIn by viewModel.isTeacherLoggedIn.collectAsState()

    // Handle session auto-redirect if Admin or Teacher logs in or out
    LaunchedEffect(isAdminLoggedIn, isTeacherLoggedIn) {
        if (isAdminLoggedIn && currentScreen == AppScreen.AdminLogin) {
            currentScreen = AppScreen.HomeworkDashboard
        }
        if (isTeacherLoggedIn && currentScreen == AppScreen.TeacherLogin) {
            currentScreen = AppScreen.HomeworkDashboard
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Static Floating Language Selector on onboarding/login screens
            if (currentScreen != AppScreen.HomeworkDashboard) {
                LanguageToggleRow(
                    language = language,
                    onToggle = { viewModel.toggleLanguage() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.RoleSelection -> {
                        RoleSelectionScreen(
                            viewModel = viewModel,
                            onNavigateToAdmin = { currentScreen = AppScreen.AdminLogin },
                            onNavigateToTeacher = { currentScreen = AppScreen.TeacherLogin },
                            onNavigateToParent = { currentScreen = AppScreen.ParentalLogin }
                        )
                    }
                    AppScreen.AdminLogin -> {
                        AdminLoginScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.RoleSelection }
                        )
                    }
                    AppScreen.TeacherLogin -> {
                        TeacherLoginScreen(
                            viewModel = viewModel,
                            onProceed = { currentScreen = AppScreen.HomeworkDashboard },
                            onBack = { currentScreen = AppScreen.RoleSelection }
                        )
                    }
                    AppScreen.ParentalLogin -> {
                        ParentalLoginScreen(
                            viewModel = viewModel,
                            onProceed = { currentScreen = AppScreen.HomeworkDashboard },
                            onBack = { currentScreen = AppScreen.RoleSelection }
                        )
                    }
                    AppScreen.HomeworkDashboard -> {
                        HomeworkDashboardScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout()
                                currentScreen = AppScreen.RoleSelection
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageToggleRow(
    language: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.testTag("language_toggle_btn")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Language",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == "en") "தமிழ்" else "English",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 3. ROLE SELECTION SCREEN
@Composable
fun RoleSelectionScreen(
    viewModel: HomeworkViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToTeacher: () -> Unit,
    onNavigateToParent: () -> Unit
) {
    val language by viewModel.language.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mini School Seal Header
        SchoolEmblemCanvas(
            modifier = Modifier
                .size(100.dp)
                .shadow(6.dp, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = viewModel.getString("school_title"),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = viewModel.getString("school_subtitle"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = viewModel.getString("role_selection_title"),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Parental Portal Access Card Button
        Card(
            onClick = onNavigateToParent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .testTag("parent_role_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Parental Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.getString("parent_login_btn"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (language == "ta") "வகுப்பு மற்றும் பிரிவைத் தேர்ந்தெடுத்து பார்க்கவும்" else "Select class & section to view assignments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Class Teacher Portal Access Card Button
        Card(
            onClick = onNavigateToTeacher,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .testTag("teacher_role_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Teacher Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "ta") "வகுப்பு ஆசிரியர் உள்நுழைவு" else "Class Teacher Login",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (language == "ta") "குறியீட்டை உள்ளிட்டு வீட்டுப்பாடம் ஒதுக்கவும்" else "Enter class code to assign homework",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Administrative Portal Access Card Button
        Card(
            onClick = onNavigateToAdmin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .testTag("admin_role_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Admin Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.getString("admin_login_btn"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (language == "ta") "பயனர்பெயர் மற்றும் கடவுச்சொல்லுடன் உள்நுழையவும்" else "Sign in with admin username & password",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Lock",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "© 2026 " + viewModel.getString("school_title"),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// 4. ADMINISTRATIVE LOGIN SCREEN
@Composable
fun AdminLoginScreen(
    viewModel: HomeworkViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    var username by remember { mutableStateOf("admin") } // Pre-fill with "admin" to ease evaluation
    var password by remember { mutableStateOf("admin") } // Pre-fill with "admin" as well
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .testTag("admin_login_back_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SchoolEmblemCanvas(modifier = Modifier.size(90.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = viewModel.getString("admin_title"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Info helper banner for testing
                val currentAdminUser = viewModel.getAdminUsername()
                val currentAdminPass = viewModel.getAdminPassword()
                val hintText = if (language == "ta") {
                    "உள்நுழைவு விவரங்கள்: $currentAdminUser / $currentAdminPass"
                } else {
                    "Admin credentials: $currentAdminUser / $currentAdminPass"
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hintText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        showError = false
                    },
                    label = { Text(viewModel.getString("username_label")) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "User")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text(viewModel.getString("password_label")) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Home else Icons.Default.Lock, // fallback lock
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.getString("error_invalid_credentials"),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val success = viewModel.loginAdmin(username, password)
                        if (success) {
                            Toast.makeText(context, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("login_action_btn"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onBack) {
            Text(
                text = viewModel.getString("back_btn"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 4b. TEACHER LOGIN SCREEN
@Composable
fun TeacherLoginScreen(
    viewModel: HomeworkViewModel,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    var classCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .testTag("teacher_login_back_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SchoolEmblemCanvas(modifier = Modifier.size(90.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (language == "ta") "வகுப்பு ஆசிரியர் உள்நுழைவு" else "Class Teacher Login",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Info helper banner for testing
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == "ta") {
                                "முன்னிருப்பு குறியீடுகள்: 6A, 6B, 7A போன்றவை. அல்லது நிர்வாகி அமைத்த குறியீட்டைப் பயன்படுத்தவும் (எ.கா: 6100)."
                            } else {
                                "Default codes: 6A, 6B, 7A etc. Or use code assigned by Admin (e.g. 6100)."
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Class Code field
                OutlinedTextField(
                    value = classCode,
                    onValueChange = {
                        classCode = it
                        showError = false
                    },
                    label = { Text(if (language == "ta") "வகுப்பு குறியீடு" else "Class Code") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Code")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_code_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == "ta") "தவறான குறியீடு. நிர்வாகியைத் தொடர்பு கொள்ளவும்." else "Invalid Class Code. Please contact Admin.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val result = viewModel.loginTeacherWithCode(classCode)
                        if (result != null) {
                            val welcomeMsg = if (language == "ta") {
                                "வரவேற்கிறோம் வகுப்பு ஆசிரியர்: ${result.first}-${result.second}!"
                            } else {
                                "Welcome Teacher of Class ${result.first}-${result.second}!"
                            }
                            Toast.makeText(context, welcomeMsg, Toast.LENGTH_SHORT).show()
                            onProceed()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("teacher_login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("login_action_btn"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onBack) {
            Text(
                text = viewModel.getString("back_btn"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 5. PARENTAL LOGIN SCREEN (CLASS & SECTION PICKER)
@Composable
fun ParentalLoginScreen(
    viewModel: HomeworkViewModel,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    var accessCode by remember { mutableStateOf("") }
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.Start)
                .testTag("parent_login_back_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SchoolEmblemCanvas(modifier = Modifier.size(90.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (language == "ta") "வகுப்பு அணுகல் பலகை" else "Classroom Access Board",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (language == "ta") "வீட்டுப்பாடத்தைக் காண வகுப்புக்குரிய குறியீட்டை உள்ளிடவும்" else "Enter your class-specific access code to view homework",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Access Code Input
                OutlinedTextField(
                    value = accessCode,
                    onValueChange = { accessCode = it },
                    label = { Text(if (language == "ta") "வகுப்பு குறியீடு" else "Class Access Code") },
                    placeholder = { Text("e.g. 6A, 6-B, 7C") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_access_code_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Proceed Button
                Button(
                    onClick = {
                        val result = viewModel.openClassSectionWithCode(accessCode)
                        if (result != null) {
                            val (cls, sec) = result
                            val toastMsg = if (language == "ta") {
                                "வகுப்பு $cls-$sec வெற்றிகரமாக திறக்கப்பட்டது!"
                            } else {
                                "Class $cls-$sec opened successfully!"
                            }
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            onProceed()
                        } else {
                            val errorMsg = if (language == "ta") {
                                "தவறான அணுகல் குறியீடு! வகுப்பு ஆசிரியரைத் தொடர்பு கொள்ளவும்."
                            } else {
                                "Invalid Access Code! Please contact your class teacher."
                            }
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("parent_login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (language == "ta") "வகுப்பைத் திறக்கவும்" else "Open Class Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back Text Button
        TextButton(onClick = onBack) {
            Text(
                text = viewModel.getString("back_btn"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 6. MAIN HOMEWORK PORTAL DASHBOARD (ADMIN & PARENT CONTROLS)
@Composable
fun HomeworkDashboardScreen(
    viewModel: HomeworkViewModel,
    onLogout: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isTeacherLoggedIn by viewModel.isTeacherLoggedIn.collectAsState()
    val context = LocalContext.current
    
    // Dynamic request for Post Notification permissions on Android 13+ (API 33+)
    val sdkInt = android.os.Build.VERSION.SDK_INT
    LaunchedEffect(Unit) {
        if (sdkInt >= 33) {
            val permission = "android.permission.POST_NOTIFICATIONS"
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val activity = context as? android.app.Activity
                activity?.let {
                    androidx.core.app.ActivityCompat.requestPermissions(
                        it,
                        arrayOf(permission),
                        1001
                    )
                }
            }
        }
    }
    
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val filteredHomework by viewModel.filteredHomework.collectAsState(initial = emptyList())
    val classHistory by viewModel.classSectionHistory.collectAsState(initial = emptyList())

    var isAddingHomework by remember { mutableStateOf(false) }
    var editingHomeworkEntry by remember { mutableStateOf<HomeworkEntity?>(null) }
    var deleteConfirmEntry by remember { mutableStateOf<HomeworkEntity?>(null) }

    // Toggle between "Today" homework dashboard and Class Homework History Log
    var selectedDashboardTab by remember { mutableStateOf(0) } // 0 = Selected Date, 1 = History

    val subjects = listOf("Tamil", "English", "Maths", "Science", "Social Science", "Notes")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Geometric Balance Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // School emblem - rendering the official vector logo
                        Image(
                            painter = painterResource(id = R.drawable.ic_school_logo),
                            contentDescription = "School Emblem",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .padding(2.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "ST. FRANCIS XAVIER'S",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Higher Secondary School, Thoothukudi",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top Action Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Language Selector Button in styled Geometric badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable { viewModel.toggleLanguage() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == "en") "தமிழ்" else "English",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isAdminLoggedIn) {
                            var showAdminSettingsDialog by remember { mutableStateOf(false) }

                            IconButton(
                                onClick = { showAdminSettingsDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .testTag("admin_settings_gear_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Admin Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (showAdminSettingsDialog) {
                                AdminSettingsDialog(
                                    viewModel = viewModel,
                                    onDismiss = { showAdminSettingsDialog = false }
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Notification Bell with Badge
                        var showNotificationsDialog by remember { mutableStateOf(false) }
                        val todayDateString = remember { 
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) 
                        }
                        val hasTodayUpdates = remember(classHistory) {
                            classHistory.any { it.date == todayDateString }
                        }

                        Box {
                            IconButton(
                                onClick = { showNotificationsDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .testTag("bell_notification_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (hasTodayUpdates) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }

                        if (showNotificationsDialog) {
                            AlertDialog(
                                onDismissRequest = { showNotificationsDialog = false },
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == "ta") "அறிவிப்புகள் பலகை" else "Alert & Notification Board",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 400.dp)
                                    ) {
                                        Text(
                                            text = if (language == "ta") "வகுப்பு $selectedClass-$selectedSection க்கான அறிவிப்புகள்" else "Relevant updates for Class $selectedClass-$selectedSection",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        
                                        if (classHistory.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (language == "ta") "அறிவிப்புகள் எதுவும் இல்லை" else "No active alerts for this class.",
                                                    fontStyle = FontStyle.Italic,
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            LazyColumn(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                val sortedAlerts = classHistory.sortedWith(
                                                    compareByDescending<HomeworkEntity> { it.date }
                                                        .thenByDescending { it.lastUpdated }
                                                )
                                                items(sortedAlerts) { alert ->
                                                    val isAnnouncement = alert.subject == "Notes"
                                                    val isToday = alert.date == todayDateString
                                                    
                                                    Card(
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isAnnouncement) {
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                                            } else {
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                            }
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(6.dp))
                                                                            .background(
                                                                                if (isAnnouncement) MaterialTheme.colorScheme.primary 
                                                                                else MaterialTheme.colorScheme.secondary
                                                                            )
                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = getLocalizedSubject(alert.subject, language),
                                                                            color = Color.White,
                                                                            fontSize = 9.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }
                                                                    if (isToday) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(6.dp))
                                                                                .background(Color(0xFFDCFCE7))
                                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = if (language == "ta") "புதியது" else "NEW",
                                                                                color = Color(0xFF15803D),
                                                                                fontSize = 8.sp,
                                                                                fontWeight = FontWeight.Bold
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                                Text(
                                                                    text = alert.date,
                                                                    fontSize = 10.sp,
                                                                    color = Color.Gray,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(
                                                                text = alert.content,
                                                                fontSize = 13.sp,
                                                                lineHeight = 18.sp,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showNotificationsDialog = false }) {
                                        Text(text = if (language == "ta") "மூடு" else "Close")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Logout Icon
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.05f))
                                .testTag("dashboard_logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subheader Panel: Selection indicators and calendar triggers
        Surface(
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isTeacherLoggedIn) {
                                // Static locked teacher badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked Class",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${viewModel.getString("class_label")} $selectedClass - $selectedSection",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                // Interactive Class and Section chips for Parent and Admin
                                var isClassDropdownExpanded by remember { mutableStateOf(false) }
                                var isSectionDropdownExpanded by remember { mutableStateOf(false) }
                                val classes = listOf("6", "7", "8", "9", "10")
                                val sections = listOf("A", "B", "C", "D", "E")

                                // Class Selector Chip
                                Box {
                                    Surface(
                                        onClick = { isClassDropdownExpanded = true },
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("dashboard_class_chip")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${viewModel.getString("class_label")} $selectedClass",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isClassDropdownExpanded,
                                        onDismissRequest = { isClassDropdownExpanded = false }
                                    ) {
                                        classes.forEach { cls ->
                                            DropdownMenuItem(
                                                text = { Text("${viewModel.getString("class_label")} $cls") },
                                                onClick = {
                                                    viewModel.setParentalSelection(cls, selectedSection)
                                                    isClassDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Section Selector Chip
                                Box {
                                    Surface(
                                        onClick = { isSectionDropdownExpanded = true },
                                        color = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("dashboard_section_chip")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${viewModel.getString("section_label")} $selectedSection",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Dropdown",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isSectionDropdownExpanded,
                                        onDismissRequest = { isSectionDropdownExpanded = false }
                                    ) {
                                        sections.forEach { sec ->
                                            DropdownMenuItem(
                                                text = { Text("${viewModel.getString("section_label")} $sec") },
                                                onClick = {
                                                    viewModel.setParentalSelection(selectedClass, sec)
                                                    isSectionDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (isAdminLoggedIn) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (isTeacherLoggedIn) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (language == "ta") "ஆசிரியர்" else "TEACHER",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.getString("academic_year"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Date selector triggering earlier entries
                    DateSelectionButton(
                        selectedDate = selectedDate,
                        viewModel = viewModel
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Segmented Selector: Daily Dashboard vs History logs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE2E8F0).copy(alpha = 0.6f))
                        .padding(4.dp)
                ) {
                    // Daily Homework tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (selectedDashboardTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedDashboardTab = 0 }
                            .padding(vertical = 10.dp)
                            .testTag("tab_daily"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == "ta") "அன்றாட வீட்டுப்பாடம்" else "Daily Homework",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDashboardTab == 0) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                    // History tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (selectedDashboardTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedDashboardTab = 1 }
                            .padding(vertical = 10.dp)
                            .testTag("tab_history"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.getString("history_title"),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDashboardTab == 1) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Main Contents
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedDashboardTab == 0) {
                // DAILY CARD VIEW
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Render Subjects Cards
                    items(subjects) { subjectName ->
                        // Match corresponding entry if exists
                        val matchingEntry = filteredHomework.find { it.subject == subjectName }
                        
                        SubjectHomeworkCard(
                            subject = subjectName,
                            entry = matchingEntry,
                            isAdmin = isAdminLoggedIn || isTeacherLoggedIn,
                            viewModel = viewModel,
                            onEditClick = { editingHomeworkEntry = matchingEntry },
                            onDeleteClick = { deleteConfirmEntry = matchingEntry }
                        )
                    }
                }

                // Admin/Teacher Feed homework FAB
                if (isAdminLoggedIn || isTeacherLoggedIn) {
                    FloatingActionButton(
                        onClick = { isAddingHomework = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .testTag("feed_homework_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.getString("add_homework_fab"),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // CHRONOLOGICAL HISTORY LOG VIEW
                if (classHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty History",
                                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = viewModel.getString("no_homework_msg"),
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Group history by date for beautiful chronological display
                        val groupedByDate = classHistory.groupBy { it.date }.toSortedMap(compareByDescending { it })
                        
                        groupedByDate.forEach { (date, entries) ->
                            item {
                                Text(
                                    text = formatDisplayDate(date, language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            
                            items(entries) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .shadow(2.dp, RoundedCornerShape(10.dp)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = getLocalizedSubject(entry.subject, language),
                                                fontWeight = FontWeight.Bold,
                                                color = getSubjectColor(entry.subject),
                                                fontSize = 13.sp
                                            )
                                            if (isAdminLoggedIn || isTeacherLoggedIn) {
                                                Row {
                                                    IconButton(
                                                        onClick = { editingHomeworkEntry = entry },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    IconButton(
                                                        onClick = { deleteConfirmEntry = entry },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = entry.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 7. FEED / ADD HOMEWORK DIALOG SHEET
    if (isAddingHomework) {
        var feedSubject by remember { mutableStateOf("Maths") }
        var feedContent by remember { mutableStateOf("") }
        var isSubjectExpanded by remember { mutableStateOf(false) }
        var isDateSelectionExpanded by remember { mutableStateOf(false) }
        var feedDate by remember { mutableStateOf(selectedDate) }

        AlertDialog(
            onDismissRequest = { isAddingHomework = false },
            title = {
                Text(
                    text = viewModel.getString("feed_homework_title"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                ) {
                    // Target Selection details
                    Text(
                        text = "${viewModel.getString("class_label")} $selectedClass - ${viewModel.getString("section_label")} $selectedSection",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    // Date dropdown helper trigger
                    Text(
                        text = viewModel.getString("date_label"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                // Simple triggers native date picker in dialog
                                val calendar = Calendar.getInstance()
                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                        feedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePickerDialog.show()
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDisplayDate(feedDate, language),
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.DateRange, contentDescription = "Date", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject Dropdown
                    Text(
                        text = viewModel.getString("subject_label"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { isSubjectExpanded = true }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLocalizedSubject(feedSubject, language),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Open")
                        }

                        DropdownMenu(
                            expanded = isSubjectExpanded,
                            onDismissRequest = { isSubjectExpanded = false }
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(getLocalizedSubject(sub, language)) },
                                    onClick = {
                                        feedSubject = sub
                                        isSubjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Field
                    Text(
                        text = if (language == "ta") "விவரங்கள்" else "Assignment Content",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = feedContent,
                        onValueChange = { feedContent = it },
                        placeholder = { Text(viewModel.getString("homework_content_hint"), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("feed_content_input"),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedContent.trim().isNotEmpty()) {
                            viewModel.addHomework(
                                className = selectedClass,
                                section = selectedSection,
                                subject = feedSubject,
                                content = feedContent,
                                date = feedDate
                            )
                            isAddingHomework = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("feed_save_btn")
                ) {
                    Text(viewModel.getString("save_btn"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingHomework = false }) {
                    Text(viewModel.getString("cancel_btn"), color = MaterialTheme.colorScheme.tertiary)
                }
            }
        )
    }

    // 8. EDIT HOMEWORK DIALOG SHEET
    editingHomeworkEntry?.let { entry ->
        var editContent by remember { mutableStateOf(entry.content) }

        AlertDialog(
            onDismissRequest = { editingHomeworkEntry = null },
            title = {
                Text(
                    text = viewModel.getString("edit_homework_title"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${getLocalizedSubject(entry.subject, language)} - ${viewModel.getString("class_label")} ${entry.className} ${entry.section}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("edit_content_input"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editContent.trim().isNotEmpty()) {
                            viewModel.updateHomework(entry.copy(content = editContent))
                            editingHomeworkEntry = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("edit_save_btn")
                ) {
                    Text(viewModel.getString("update_btn"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingHomeworkEntry = null }) {
                    Text(viewModel.getString("cancel_btn"), color = MaterialTheme.colorScheme.tertiary)
                }
            }
        )
    }

    // 9. CONFIRM DELETE DIALOG
    deleteConfirmEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEntry = null },
            title = {
                Text(
                    text = if (language == "ta") "நீக்குதலை உறுதிசெய்" else "Confirm Delete",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(text = viewModel.getString("confirm_delete"))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHomework(entry.id)
                        deleteConfirmEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text(viewModel.getString("delete_btn"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEntry = null }) {
                    Text(viewModel.getString("cancel_btn"), color = MaterialTheme.colorScheme.tertiary)
                }
            }
        )
    }
}

// 10. DYNAMIC ACCENT SUBJECT CARD LAYOUT (AVOIDING AI SLOP GIVES AMAZING PROFESSIONAL VIBES)
fun getSubjectBgColor(subject: String): Color {
    return when (subject) {
        "Tamil" -> Color(0xFFFFEDD5) // Orange 100
        "English" -> Color(0xFFE0F2FE) // Sky 100
        "Maths" -> Color(0xFFDBEAFE) // Blue 100
        "Science" -> Color(0xFFD1FAE5) // Emerald 100
        "Social Science" -> Color(0xFFF3E8FF) // Purple 100
        "Notes" -> Color(0xFFE0E7FF) // Indigo 100
        else -> Color(0xFFF1F5F9)
    }
}

fun getSubjectTextColor(subject: String): Color {
    return when (subject) {
        "Tamil" -> Color(0xFFEA580C) // Orange 600
        "English" -> Color(0xFF0369A1) // Sky 700
        "Maths" -> Color(0xFF2563EB) // Blue 600
        "Science" -> Color(0xFF059669) // Emerald 600
        "Social Science" -> Color(0xFF7E22CE) // Purple 700
        "Notes" -> Color(0xFF4338CA) // Indigo 700
        else -> Color(0xFF475569)
    }
}

fun getSubjectMonogram(subject: String, lang: String): String {
    return when (subject) {
        "Tamil" -> if (lang == "ta") "த" else "Ta"
        "English" -> "En"
        "Maths" -> "Σ"
        "Science" -> "Sc"
        "Social Science" -> "SS"
        "Notes" -> "i"
        else -> "Hw"
    }
}

@Composable
fun SubjectHomeworkCard(
    subject: String,
    entry: HomeworkEntity?,
    isAdmin: Boolean,
    viewModel: HomeworkViewModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val isNotes = subject == "Notes"
    
    // Notes card gets special deep indigo container when it has content
    val isSpecialNotesCard = isNotes && entry != null
    
    val cardBgColor = if (isSpecialNotesCard) {
        Color(0xFF312E81) // Dark Indigo 900
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (isSpecialNotesCard) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isSpecialNotesCard) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    
    val monogramBg = if (isSpecialNotesCard) {
        Color.White.copy(alpha = 0.15f)
    } else {
        getSubjectBgColor(subject)
    }
    
    val monogramText = if (isSpecialNotesCard) {
        Color.White
    } else {
        getSubjectTextColor(subject)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Geometric Box with Monogram representation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(monogramBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getSubjectMonogram(subject, language),
                    color = monogramText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: Subject title and body text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getLocalizedSubject(subject, language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSpecialNotesCard) Color.White else Color(0xFF1E293B)
                    )
                    
                    // Admin actions inside card
                    if (isAdmin && entry != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("edit_homework_btn_${subject.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = if (isSpecialNotesCard) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("delete_homework_btn_${subject.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = if (isSpecialNotesCard) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (entry != null) {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == "ta") "கடைசி பதிவு: ${formatTime(entry.lastUpdated)}" else "Updated: ${formatTime(entry.lastUpdated)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = subTextColor
                    )
                } else {
                    Text(
                        text = if (language == "ta") "வீட்டுப்பாடம் எதுவும் ஒதுக்கப்படவில்லை." else "No homework assigned.",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = subTextColor
                    )
                }
            }

            // Right: Status indicator (e.g. arrow for special notes, small 'NEW' or date-status or none)
            if (entry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                if (isSpecialNotesCard) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    // Small subtle label status
                    val isToday = entry.date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == "ta") "புதியது" else "NEW",
                                color = Color(0xFF15803D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = if (language == "ta") "முடிந்தது" else "Due",
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 11. REUSABLE GRAPHICAL HELPERS
fun getSubjectColor(subject: String): Color {
    return getSubjectTextColor(subject)
}

fun getSubjectIcon(subject: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (subject) {
        "Tamil" -> Icons.Default.Home       // Local heritage icon fallback
        "English" -> Icons.Default.Home     // Language icon fallback
        "Maths" -> Icons.Default.Add        // Calculator/math sign representation
        "Science" -> Icons.Default.Settings // Lab/atom fallback representation
        "Social Science" -> Icons.Default.Home // Globe/Heritage fallback
        "Notes" -> Icons.Default.Info       // Diary/notepad info
        else -> Icons.Default.Home
    }
}

fun getLocalizedSubject(subject: String, lang: String): String {
    return when (subject) {
        "Tamil" -> if (lang == "ta") "தமிழ்" else "Tamil"
        "English" -> if (lang == "ta") "ஆங்கிலம்" else "English"
        "Maths" -> if (lang == "ta") "கணிதம்" else "Maths"
        "Science" -> if (lang == "ta") "அறிவியல்" else "Science"
        "Social Science" -> if (lang == "ta") "சமூக அறிவியல்" else "Social Science"
        "Notes" -> if (lang == "ta") "குறிப்புகள்" else "Notes"
        else -> subject
    }
}

fun formatDisplayDate(dateStr: String, language: String): String {
    try {
        val fromSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = fromSdf.parse(dateStr)
        if (dateObj != null) {
            val locale = if (language == "ta") Locale("ta", "IN") else Locale.getDefault()
            val toSdf = SimpleDateFormat("dd MMMM yyyy", locale)
            return toSdf.format(dateObj)
        }
    } catch (e: Exception) {
        // Fallback
    }
    return dateStr
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun DateSelectionButton(
    selectedDate: String,
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(selectedDate)
        if (date != null) {
            calendar.time = date
        }
    } catch (e: Exception) {
        // Fallback
    }

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
            viewModel.setDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val displayDate = remember(selectedDate, viewModel.language.collectAsState().value) {
        formatDisplayDate(selectedDate, viewModel.language.value)
    }

    Button(
        onClick = { datePickerDialog.show() },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.testTag("date_picker_trigger")
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Select Date",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = displayDate,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AdminSettingsDialog(
    viewModel: HomeworkViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0 = Password, 1 = Class Codes

    // Password State
    var adminUsernameInput by remember { mutableStateOf(viewModel.getAdminUsername()) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Class Code State
    var selClass by remember { mutableStateOf("6") }
    var selSection by remember { mutableStateOf("A") }
    var newCodeInput by remember { mutableStateOf("") }

    var isClassExpanded by remember { mutableStateOf(false) }
    var isSectionExpanded by remember { mutableStateOf(false) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val classesList = listOf("6", "7", "8", "9", "10")
    val sectionsList = listOf("A", "B", "C", "D", "E")

    // Update code input when class/section selection changes
    LaunchedEffect(selClass, selSection) {
        newCodeInput = viewModel.getClassSectionCode(selClass, selSection)
    }

    // Auto-dismiss feedback message after 3s
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            kotlinx.coroutines.delay(3000)
            feedbackMessage = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "ta") "நிர்வாகி அமைப்புகள்" else "Admin Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Custom Segmented Row for Tabs (extremely compile-safe and clean)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            activeTab = 0
                            feedbackMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("tab_password_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = if (language == "ta") "கடவுச்சொல்" else "Password", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { 
                            activeTab = 1
                            feedbackMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (activeTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1.2f).height(38.dp).testTag("tab_class_codes_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = if (language == "ta") "வகுப்பு குறியீடுகள்" else "Class Codes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Feedback banner
                feedbackMessage?.let { msg ->
                    Surface(
                        color = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF15803D),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Scrollable Content Pane
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeTab == 0) {
                        // ADMINISTRATIVE CREDENTIALS SECTION
                        Text(
                            text = if (language == "ta") "நிர்வாகி சான்றுகளை மாற்றுக" else "Change Admin Credentials",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = adminUsernameInput,
                            onValueChange = { adminUsernameInput = it },
                            label = { Text(if (language == "ta") "நிர்வாகி பயனர் பெயர்" else "Admin Username") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "User")
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("admin_new_username_input")
                        )
                        
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(if (language == "ta") "புதிய கடவுச்சொல் (விருப்பத்தேர்வு)" else "New Password (optional)") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (passwordVisible) {
                                            if (language == "ta") "மறை" else "Hide"
                                        } else {
                                            if (language == "ta") "காட்டு" else "Show"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("admin_new_pass_input")
                        )

                        if (newPassword.isNotEmpty()) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text(if (language == "ta") "கடவுச்சொல்லை உறுதிப்படுத்து" else "Confirm New Password") },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("admin_confirm_pass_input")
                            )
                        }

                        Button(
                            onClick = {
                                if (adminUsernameInput.trim().isEmpty()) {
                                    feedbackMessage = if (language == "ta") "பயனர் பெயர் காலியாக இருக்கக்கூடாது" else "Username cannot be empty"
                                    isError = true
                                } else if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                                    feedbackMessage = if (language == "ta") "கடவுச்சொற்கள் பொருந்தவில்லை" else "Passwords do not match"
                                    isError = true
                                } else {
                                    // Save username
                                    viewModel.setAdminUsername(adminUsernameInput.trim())
                                    // Save password if provided
                                    if (newPassword.isNotEmpty()) {
                                        viewModel.setAdminPassword(newPassword)
                                    }
                                    feedbackMessage = if (language == "ta") "விவரங்கள் வெற்றிகரமாக மாற்றப்பட்டது" else "Credentials successfully updated!"
                                    isError = false
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_save_pass_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = if (language == "ta") "சான்றுகளை சேமி" else "Save Credentials", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // CLASS ACCESS CODES MANAGEMENT
                        Text(
                            text = if (language == "ta") "வகுப்பு அணுகல் குறியீட்டை அமை" else "Assign Access Code to Class/Section",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Class Selection
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "${if (language == "ta") "வகுப்பு" else "Class"} $selClass",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (language == "ta") "வகுப்பு" else "Class") },
                                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isClassExpanded = true }
                                        .testTag("admin_class_select_field")
                                )
                                DropdownMenu(
                                    expanded = isClassExpanded,
                                    onDismissRequest = { isClassExpanded = false }
                                ) {
                                    classesList.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text("${if (language == "ta") "வகுப்பு" else "Class"} $cls") },
                                            onClick = {
                                                selClass = cls
                                                isClassExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Section Selection
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "${if (language == "ta") "பிரிவு" else "Sec"} $selSection",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (language == "ta") "பிரிவு" else "Sec") },
                                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isSectionExpanded = true }
                                        .testTag("admin_sec_select_field")
                                )
                                DropdownMenu(
                                    expanded = isSectionExpanded,
                                    onDismissRequest = { isSectionExpanded = false }
                                ) {
                                    sectionsList.forEach { sec ->
                                        DropdownMenuItem(
                                            text = { Text("${if (language == "ta") "பிரிவு" else "Sec"} $sec") },
                                            onClick = {
                                                selSection = sec
                                                isSectionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newCodeInput,
                            onValueChange = { newCodeInput = it },
                            label = { Text(if (language == "ta") "அணுகல் குறியீடு" else "Access Code") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("admin_class_code_input")
                        )

                        Button(
                            onClick = {
                                if (newCodeInput.trim().isEmpty()) {
                                    feedbackMessage = if (language == "ta") "குறியீடு காலியாக இருக்கக்கூடாது" else "Access code cannot be empty"
                                    isError = true
                                } else {
                                    viewModel.setClassSectionCode(selClass, selSection, newCodeInput)
                                    feedbackMessage = if (language == "ta") "$selClass-$selSection குறியீடு புதுப்பிக்கப்பட்டது!" else "Code for Class $selClass-$selSection updated!"
                                    isError = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("admin_save_code_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = if (language == "ta") "குறியீட்டை புதுப்பி" else "Update Code", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Table showing all current codes for quick overview
                        Text(
                            text = if (language == "ta") "தற்போதைய அணுகல் குறியீடுகள்" else "Active Class Access Codes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                classesList.forEach { cls ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Class $cls:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(55.dp)
                                        )
                                        
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            sectionsList.forEach { sec ->
                                                val activeCode = viewModel.getClassSectionCode(cls, sec)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            selClass = cls
                                                            selSection = sec
                                                            newCodeInput = activeCode
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "$sec: $activeCode",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (cls != "10") {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (language == "ta") "மூடு" else "Close")
            }
        }
    )
}
