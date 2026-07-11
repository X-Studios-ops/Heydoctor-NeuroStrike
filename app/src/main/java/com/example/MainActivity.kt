package com.example

// Force refresh to clear cache
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HeyDoctorApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HeyDoctorApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier.fillMaxSize().background(DarkBackground)
    ) {
        composable("splash") {
            SplashScreen {
                navController.navigate("lobby") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("lobby") {
            LobbyScreen(
                onDeploy = { navController.navigate("story") },
                onAgents = { navController.navigate("agents") },
                onSanityMonitor = { navController.navigate("sanity_monitor") }
            )
        }
        composable("agents") {
            AgentsScreen {
                navController.popBackStack()
            }
        }
        composable("sanity_monitor") {
            SanityMonitorScreen {
                navController.popBackStack()
            }
        }
        composable("story") {
            StoryScreen {
                navController.navigate("in_match") {
                    popUpTo("story") { inclusive = true }
                }
            }
        }
        composable("in_match") {
            InMatchHudScreen()
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(600)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onTimeout() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1542382156909-9ae37b3f56fd?q=80&w=2500&auto=format&fit=crop",
            contentDescription = "Ruined City Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.5f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Text(
                text = "> HEY_DOCTOR OS v3.1.4 LOADING...",
                color = MedicalGreen.copy(alpha = alpha),
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Simple flatline visual
            Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(40.dp).alpha(alpha)) {
                val path = Path().apply {
                    moveTo(0f, size.height / 2)
                    lineTo(size.width * 0.4f, size.height / 2)
                    lineTo(size.width * 0.45f, size.height * 0.1f)
                    lineTo(size.width * 0.55f, size.height * 0.9f)
                    lineTo(size.width * 0.6f, size.height / 2)
                    lineTo(size.width, size.height / 2)
                }
                drawPath(path = path, color = MedicalGreen, style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}

@Composable
fun LobbyScreen(onDeploy: () -> Unit, onAgents: () -> Unit, onSanityMonitor: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=2500&auto=format&fit=crop",
            contentDescription = "Safehouse Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.4f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("42", color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("GhostActual", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Credits: 14,500 | Biocoin: 350", color = MutedGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center Content - Vitals
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, MutedGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("OPERATIVE READINESS", color = MutedGray, letterSpacing = 2.sp, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VitalsMetric("AVG K/D", "2.05", NeonCyan)
                    VitalsMetric("SANITY SCORE", "68.4", SanityPurple)
                    VitalsMetric("MATCHES", "512", MedicalGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Initiate Deployment Button
        Button(
            onClick = onDeploy,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MedicalGreen)
        ) {
            Text(
                "[ INITIATE DEPLOYMENT ]", 
                color = MedicalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Two buttons in a Row for Landscape ergonomics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Agents Button
            Button(
                onClick = onAgents,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
            ) {
                Text(
                    "[ VIEW OPERATIVES ]", 
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Sanity Monitor Button
            Button(
                onClick = onSanityMonitor,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SanityPurple.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SanityPurple)
            ) {
                Text(
                    "[ SANITY AI MONITOR ]", 
                    color = SanityPurple,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
    }
}

@Composable
fun VitalsMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(label, color = MutedGray, fontSize = 10.sp)
    }
}

@Composable
fun InMatchHudScreen() {
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    var isFiring by remember { mutableStateOf(false) }
    val fireButtonScale by animateFloatAsState(targetValue = if (isFiring) 0.85f else 1f, label = "fireScale")
    
    val infiniteTransition = rememberInfiniteTransition(label = "hudAnimations")
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ), label = "radarRotation"
    )
    val ekgOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing)
        ), label = "ekg"
    )
    
    var timeLeft by remember { mutableIntStateOf(642) }
    LaunchedEffect(Unit) {
        while(timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Mock background map texture or dark gradient
        AsyncImage(
            model = "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=2500&auto=format&fit=crop",
            contentDescription = "Quarantine Zone Map",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.3f)
                .offset { IntOffset((-joystickOffset.x * 1.5f).toInt(), (-joystickOffset.y * 1.5f).toInt()) }
                .alpha(0.6f)
        )

        // Top Left: Mini-map
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(120.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = 0.5f))
                .border(2.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
        ) {
            // Radar sweep
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = radarRotation }) {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.5f),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, 0f),
                    strokeWidth = 2f
                )
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = NeonCyan.copy(alpha = 0.2f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 1f
                )
                drawLine(
                    color = NeonCyan.copy(alpha = 0.2f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1f
                )
                // Enemy blip
                drawCircle(color = CriticalCrimson, radius = 6f, center = Offset(size.width * 0.7f, size.height * 0.3f))
            }
        }

        // Top Right: Timer & Score
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End
        ) {
            val mins = timeLeft / 60
            val secs = timeLeft % 60
            val timeString = "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
            Text(timeString, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
            Text("ALPHA 45 - 32 OMEGA", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }

        // Bottom Left: Movement Joystick Placeholder
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(120.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = { joystickOffset = Offset.Zero },
                        onDragCancel = { joystickOffset = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = joystickOffset + dragAmount
                            val maxRadius = (size.width / 2f) - 24.dp.toPx()
                            val distance = newOffset.getDistance()
                            joystickOffset = if (distance > maxRadius) {
                                newOffset * (maxRadius / distance)
                            } else {
                                newOffset
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(joystickOffset.x.toInt(), joystickOffset.y.toInt()) }
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }

        // Bottom Right: Fire Button Placeholder
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .scale(fireButtonScale)
                .clip(CircleShape)
                .background(CriticalCrimson.copy(alpha = if (isFiring) 0.4f else 0.2f))
                .border(2.dp, CriticalCrimson, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isFiring = true
                            tryAwaitRelease()
                            isFiring = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("FIRE", color = CriticalCrimson, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        // Bottom Center: HeyDoctor Medical UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val warningAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "warningAlpha"
            )
            Text(
                "[WARNING] HEART RATE ELEVATED",
                color = WarningAmber.copy(alpha = warningAlpha),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Health Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("HP", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.DarkGray)
                ) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.8f).background(Color.White))
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Sanity Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SP", color = SanityPurple, fontSize = 10.sp, modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.DarkGray)
                ) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.6f).background(SanityPurple))
                }
            }
        }
    }
}

@Composable
fun TypewriterStoryText(
    text: String,
    speedMillis: Long = 25,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember(text) { mutableStateOf("") }
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText = text.substring(0, i + 1)
            // Auto scroll down as typewriter prints more lines
            if (i % 10 == 0) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            delay(speedMillis)
        }
        scrollState.animateScrollTo(scrollState.maxValue)
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = displayedText,
            color = MedicalGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = MedicalGreen.copy(alpha = 0.5f),
                    blurRadius = 3f
                )
            )
        )
    }
}

@Composable
fun StoryScreen(onSkip: () -> Unit) {
    val fullText = "MISSION PARAMETERS: INFILTRATION. Ghost entered Vironova via HALO drop, crashing through the glass dome of Sector 4's botanical gardens. Isolated. Surrounded by the Fractured. His only lifeline is HeyDoctor, the neural AI implanted in his cortex. Vitals synchronized. You are in the Dark Zone now. Do not trust the shadows. Do not listen to the whispers. Trust my HUD."
    
    // Highly-optimized animatables for Ken Burns + Eerie Fade-In
    val imageAlpha = remember { Animatable(0f) }
    val imageScale = remember { Animatable(1.0f) }
    
    // Synchronize image loading/animations exactly with the screen entering
    LaunchedEffect(Unit) {
        launch {
            imageAlpha.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
            )
        }
        launch {
            // Very slow continuous scale-up simulating tension and breathing
            imageScale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Visual (Top 70% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(Color.Black)
                .clip(androidx.compose.ui.graphics.RectangleShape)
        ) {
            // Background Character Image with Ken Burns animation
            // NOTE FOR COOP/PLUGGING ASSETS:
            // You can easily plug in your local drawable here:
            // Image(
            //     painter = painterResource(id = R.drawable.ghost_image),
            //     contentDescription = "Ghost Image",
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = imageScale.value, scaleY = imageScale.value, alpha = imageAlpha.value)
            // )
            AsyncImage(
                model = "https://images.unsplash.com/photo-1599321458897-b846ff9dce58?q=80&w=2500&auto=format&fit=crop",
                contentDescription = "Ghost Mission Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = imageScale.value,
                        scaleY = imageScale.value,
                        alpha = imageAlpha.value
                    )
            )
            
            // Atmospheric gradient shadow to blend into the bottom terminal box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black
                            )
                        )
                    )
            )
            
            // Subtle neon tactical grid overlay for the cyber-medical UI feel
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
                val gridSpacing = 40.dp.toPx()
                for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
            
            // Top HUD Status Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYS_STATUS: DEPLOYED // LINK_01",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                
                // Skip Button
                Text(
                    text = "SKIP BRIEFING >>",
                    color = MutedGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSkip() }
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(0.5.dp, MutedGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        // Terminal Box (Bottom 30% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, MedicalGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(12.dp)
            ) {
                // Secure AI Subtitle terminal header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MedicalGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE_LINK // USER: HEY_DOCTOR AI",
                            color = MedicalGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "ONLINE",
                        color = MedicalGreen,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Synchronized Typewriter Story Text
                TypewriterStoryText(
                    text = fullText,
                    speedMillis = 22,
                    onComplete = {
                        // Briefing auto-navigates or displays complete state
                    }
                )
            }
        }
    }
}

data class AgentData(
    val name: String,
    val role: String,
    val imageUrl: String,
    val subtitleText: String
)

val agentsList = listOf(
    AgentData(
        "GHOST",
        "THE NEURO-MEDIC",
        "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?q=80&w=2500&auto=format&fit=crop",
        "HeyDoctor is injecting medical nanites, but my cerebral sync is dropping... 42% signal coherence. I was an elite neuro-surgeon, but this frequency isn't physical. Stay clear of the dark corridors, squad. I can't suture a fractured psyche."
    ),
    AgentData(
        "VIPER",
        "THE PHANTOM",
        "https://images.unsplash.com/photo-1616058913165-8b29c9167e41?q=80&w=2500&auto=format&fit=crop",
        "HeyDoctor says my heart rate is spiking... I don't know if those shadows are real or just the frequency messing with my sanity. Lock and load. If those shadows come any closer, they'll taste cold lead."
    ),
    AgentData(
        "NOMAD",
        "THE WARDEN",
        "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=2500&auto=format&fit=crop",
        "HeyDoctor has flagged high-frequency biometric distortion in Sector 7. The Faraday barricades are holding, but my optic sensors are registering phantoms. Keep your eyes on the vitals monitor. If I go blind, keep shooting."
    ),
    AgentData(
        "SPECTRE",
        "THE BREACHER",
        "https://images.unsplash.com/photo-1508682136015-8d5c4b8b603a?q=80&w=2500&auto=format&fit=crop",
        "Adrenaline override engaged via HeyDoctor bio-injector! I don't care about the quarantine or the voices in the white noise. When the breach opens, you fire until the barrel melts. Sanity is a luxury we don't have."
    )
)

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    speedMillis: Long = 20,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember(text) { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText = text.substring(0, i + 1)
            delay(speedMillis)
        }
        onComplete()
    }
    
    Text(
        text = displayedText,
        color = MutedGray,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

@Composable
fun AgentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsInstance.language = Locale.US
        ttsInstance.setPitch(0.85f)
        ttsInstance.setSpeechRate(0.95f)
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val currentAgent = agentsList[selectedIndex]

    LaunchedEffect(selectedIndex, isTtsReady) {
        if (isTtsReady && tts != null) {
            tts?.stop()
            tts?.speak(currentAgent.subtitleText, TextToSpeech.QUEUE_FLUSH, null, "AgentSpeech")
        }
    }

    // High performance animation state
    val agentScale = remember { Animatable(1.0f) }
    val agentAlpha = remember { Animatable(0.0f) }

    LaunchedEffect(selectedIndex) {
        agentScale.snapTo(1.0f)
        agentAlpha.snapTo(0.0f)
        launch {
            agentScale.animateTo(
                targetValue = 1.12f,
                animationSpec = tween(durationMillis = 7000, easing = LinearEasing)
            )
        }
        launch {
            agentAlpha.animateTo(
                targetValue = 0.65f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // High performance Cinematic Character Reveal (Ken Burns effect)
        AsyncImage(
            model = currentAgent.imageUrl,
            contentDescription = "Agent Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = agentScale.value,
                    scaleY = agentScale.value,
                    alpha = agentAlpha.value
                )
        )

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Text("<", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Agent Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                agentsList.forEachIndexed { index, agent ->
                    Text(
                        text = agent.name,
                        color = if (index == selectedIndex) NeonCyan else MutedGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clickable { selectedIndex = index }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .border(1.dp, if (selectedIndex % 2 == 0) MedicalGreen else SanityPurple, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "${currentAgent.name} // ${currentAgent.role}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // High-performance isolated typewriter text component
                    TypewriterText(
                        text = currentAgent.subtitleText,
                        speedMillis = 20
                    )
                }
            }
        }
    }
}