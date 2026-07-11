package com.example

// Force refresh to clear cache
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                onAgents = { navController.navigate("agents") }
            )
        }
        composable("agents") {
            AgentsScreen {
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
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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
fun LobbyScreen(onDeploy: () -> Unit, onAgents: () -> Unit) {
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

        Spacer(modifier = Modifier.height(32.dp))

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
                Spacer(modifier = Modifier.height(16.dp))
                
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

        Spacer(modifier = Modifier.height(32.dp))

        // Initiate Deployment Button
        Button(
            onClick = onDeploy,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MedicalGreen)
        ) {
            Text(
                "[ INITIATE DEPLOYMENT ]", 
                color = MedicalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Agents Button
        Button(
            onClick = onAgents,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
        ) {
            Text(
                "[ VIEW OPERATIVES ]", 
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
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
fun StoryScreen(onSkip: () -> Unit) {
    var storyText by remember { mutableStateOf("") }
    var imageIndex by remember { mutableIntStateOf(0) }
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    val images = listOf(
        "https://images.unsplash.com/photo-1581093458791-9f3c3900df4b?q=80&w=2500&auto=format&fit=crop", // Lab / Making the virus
        "https://images.unsplash.com/photo-1599321458897-b846ff9dce58?q=80&w=2500&auto=format&fit=crop", // Soldier backside / Backstory
        "https://images.unsplash.com/photo-1616058913165-8b29c9167e41?q=80&w=2500&auto=format&fit=crop", // Tactical drop
        "https://images.unsplash.com/photo-1542382257-80da9fb9f5c4?q=80&w=2500&auto=format&fit=crop", // Ruined City
        "https://images.unsplash.com/photo-1530740614524-74dce54e8cb2?q=80&w=2500&auto=format&fit=crop"  // Dark atmosphere
    )

    val fullText = """
        > INCOMING TRANSMISSION...
        > DECRYPTION COMPLETE.
        
        > AUDIO LOG RECOVERED: AETHELGARD BIOTECH, SUB-LEVEL 7.
        > SUBJECT: THE ECHO FREQUENCY.
        We thought we were curing PTSD. We engineered a sub-auditory wave to erase fear from the human limbic system. We called it 'Resonance'. But we didn't erase fear... we trapped it. The frequency began rewriting the neural pathways of everyone in Vironova. The 'virus' wasn't biological. It was acoustic. It broke their minds, turning them into violent husks driven by shared hallucinations.
        
        > PERSONNEL FILE: GHOST_ACTUAL.
        Before the quarantine, Ghost was a combat surgeon in the 75th Ranger Regiment. Honorable discharge after a botched extraction in '28 left him with severe survivor's guilt. Aethelgard recruited him for the M.A.S.T. unit, promising a chance to save lives. They didn't tell him he'd be fighting the very people he came to save. 
        
        > MISSION PARAMETERS: INFILTRATION.
        Ghost entered Vironova via HALO drop, crashing through the glass dome of Sector 4's botanical gardens. Isolated. Surrounded by the Fractured.
        His only lifeline: HeyDoctor, the neural AI implanted in his cortex.
        
        M.A.S.T. Operative. Vitals synchronized.
        You are in the Dark Zone now. 
        Do not trust the shadows. Do not listen to the whispers.
        Trust my HUD.
    """.trimIndent()

    LaunchedEffect(Unit) {
        val totalLength = fullText.length
        for (i in fullText.indices) {
            storyText = fullText.substring(0, i + 1)
            
            val progress = i.toFloat() / totalLength
            imageIndex = when {
                progress < 0.25f -> 0
                progress < 0.50f -> 1
                progress < 0.70f -> 2
                progress < 0.90f -> 3
                else -> 4
            }
            
            if (i % 15 == 0) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            
            delay(20) // Typing speed
        }
        scrollState.animateScrollTo(scrollState.maxValue)
        delay(3000)
        onSkip()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        androidx.compose.animation.Crossfade(
            targetState = imageIndex,
            animationSpec = tween(1500),
            label = "storyGraphics"
        ) { index ->
            AsyncImage(
                model = images[index],
                contentDescription = "Story Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.65f)
            )
        }
        
        // Skip Button
        Text(
            text = "SKIP >>",
            color = MutedGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSkip() }
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(12.dp)
        )
        
        // Typing text
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.5f)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = storyText,
                color = MedicalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = NeonCyan,
                        blurRadius = 4f
                    )
                )
            )
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
        "I was a combat surgeon... saved more lives than I took. Until Vironova. The Resonance got into their heads. Now, my medicine is the only thing keeping us sane. I pump stabilizers into their veins so they don't see the shadows moving. But who's stabilizing me?"
    ),
    AgentData(
        "VIPER",
        "THE PHANTOM",
        "https://images.unsplash.com/photo-1616058913165-8b29c9167e41?q=80&w=2500&auto=format&fit=crop",
        "They call me a ghost. I move in the Dark Zones where the frequency is thickest. It screams in my ears, but I've learned to tune it out. I drop acoustic decoys to distract the Fractured. By the time they realize I'm there... it's already over."
    ),
    AgentData(
        "NOMAD",
        "THE WARDEN",
        "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=2500&auto=format&fit=crop",
        "Hold the line. That's what they told me. I deploy the Faraday Barricades. They block the bullets... and they block the frequency. Behind my shield, it's quiet. Out there, it's madness. If I fall, this whole squad loses their minds."
    ),
    AgentData(
        "SPECTRE",
        "THE BREACHER",
        "https://images.unsplash.com/photo-1508682136015-8d5c4b8b603a?q=80&w=2500&auto=format&fit=crop",
        "Subtlety is dead. When the Sanity drops and the Phantoms spawn, you need someone who doesn't care. I push through the hallucinations with an adrenaline spike. The louder my shotgun roars, the less I hear the whispers."
    )
)

@Composable
fun AgentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    var displayedSubtitle by remember { mutableStateOf("") }
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
        displayedSubtitle = ""
        if (isTtsReady && tts != null) {
            tts?.stop()
            tts?.speak(currentAgent.subtitleText, TextToSpeech.QUEUE_FLUSH, null, "AgentSpeech")
        }
        val fullText = currentAgent.subtitleText
        for (i in fullText.indices) {
            displayedSubtitle = fullText.substring(0, i + 1)
            delay(30)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        androidx.compose.animation.Crossfade(
            targetState = currentAgent.imageUrl,
            animationSpec = tween(800),
            label = "agentImage"
        ) { url ->
            AsyncImage(
                model = url,
                contentDescription = "Agent Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.6f)
            )
        }

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), CircleShape)
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
                    Text(
                        text = displayedSubtitle,
                        color = MutedGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}