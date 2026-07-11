package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

// --- CLASS STRUCTURE / MODELS FOR THE SANITY AI MONITOR ---

data class PlayerLog(
    val id: String,
    val characterName: String,
    val timestamp: String,
    val message: String,
    val logType: String, // "COMBAT", "DIALOGUE", "OBSERVATION", "SYSTEM"
    val initialSanity: Int
)

data class HallucinatoryTrigger(
    val triggerType: String, // "AUDIO", "VISUAL", "COGNITIVE", "ENVIRONMENTAL"
    val description: String,
    val severity: String, // "LOW", "MEDIUM", "HIGH"
    val triggerSource: String, // e.g. "Echo Frequency", "Guilt Projector"
    val immediateEffect: String // e.g. "Simulate HUD glitch", "Spawn audio whisper"
)

data class SanityReport(
    val evaluatedCharacterName: String,
    val currentSanityScore: Int,
    val finalSanityStatus: String, // "STABLE", "COMPROMISED", "CRITICAL"
    val drThorneAnalysis: String,
    val triggers: List<HallucinatoryTrigger>
)

// --- CHARACTER AND STORYLINE SCENARIOS ---

data class StoryCharacter(
    val name: String,
    val role: String,
    val affiliation: String,
    val profileImage: String,
    val description: String,
    val baseSanity: Int,
    val baseHeartRate: Int,
    val defaultLogs: List<PlayerLog>
)

val storyCharacters = listOf(
    StoryCharacter(
        name = "GHOST",
        role = "Neural Combat Surgeon",
        affiliation = "M.A.S.T. Operative",
        profileImage = "https://images.unsplash.com/photo-1620641788421-7a1c342ea42e?q=80&w=1200&auto=format&fit=crop",
        description = "Surviving ranger tormented by a failed medical evacuation in Sector 4. Implanted with HEY_DOCTOR AI to stabilize his mind.",
        baseSanity = 55,
        baseHeartRate = 125,
        defaultLogs = listOf(
            PlayerLog("g1", "GHOST", "02:14:05", "The shadows in the botanic dome are stretching asymmetric to the solar angles. Retinal calibration reports zero hardware faults.", "OBSERVATION", 65),
            PlayerLog("g2", "GHOST", "02:15:30", "Engaged 3 infected. Shotgun report echoed inside my skull, matching the audio sequence of my sister's laughter. Must adjust stabilizer dosage.", "COMBAT", 58),
            PlayerLog("g3", "GHOST", "02:17:10", "HEY_DOCTOR: Lobe-Z resonance threshold warning. I am starting to hear code strings instead of silence.", "SYSTEM", 52)
        )
    ),
    StoryCharacter(
        name = "VIPER",
        role = "Dark Zone Scout",
        affiliation = "M.A.S.T. Infiltrator",
        profileImage = "https://images.unsplash.com/photo-1616058913165-8b29c9167e41?q=80&w=1200&auto=format&fit=crop",
        description = "Operates in the frequency cores. Hardened tactical scout who communicates primarily in acoustic coordinates.",
        baseSanity = 40,
        baseHeartRate = 142,
        defaultLogs = listOf(
            PlayerLog("v1", "VIPER", "04:02:12", "Ambient audio frequency humming at 42.8 kHz. Acoustic HUD shows quiet, but my occipital bone is vibrating.", "OBSERVATION", 45),
            PlayerLog("v2", "VIPER", "04:03:45", "Nomad, do you copy? The streetlights in the sector are blinking in Aethelgard binary sequence. It reads: 'RETURN FOR RECALIBRATION'.", "DIALOGUE", 41),
            PlayerLog("v3", "VIPER", "04:05:00", "Saw a secondary reflections map overlay on the wet pavement. My reflection was dressed in clinical surgeon scrubs. I've never been in a lab.", "OBSERVATION", 38)
        )
    ),
    StoryCharacter(
        name = "DR. ARIS THORNE",
        role = "Chief Neuro-Geneticist",
        affiliation = "Aethelgard Biotech (Ex-Lead)",
        profileImage = "https://images.unsplash.com/photo-1508682136015-8d5c4b8b603a?q=80&w=1200&auto=format&fit=crop",
        description = "Designed the original Echo Frequency. Subjected himself to self-infection to document the neural degradation process.",
        baseSanity = 25,
        baseHeartRate = 98,
        defaultLogs = listOf(
            PlayerLog("t1", "DR. THORNE", "00:01:05", "The frequency doesn't delete trauma. It unlocks it. It takes our worst cognitive scars and projects them as real acoustic waveforms.", "OBSERVATION", 35),
            PlayerLog("t2", "DR. THORNE", "00:03:22", "My deceased daughter Chloe is sitting on the terminal desk. She looks perfect. She is asking why I left her in Sector 4 during the evacuation.", "DIALOGUE", 28),
            PlayerLog("t3", "DR. THORNE", "00:04:15", "Sanity level critical. Injectors empty. The lab glass is vibrating in her exact vocal pitch. The dome is cracking.", "TELEMETRY", 21)
        )
    ),
    StoryCharacter(
        name = "COMMANDER VANCE",
        role = "M.A.S.T. Tactical Commander",
        affiliation = "Tactical Command (Offsite)",
        profileImage = "https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=1200&auto=format&fit=crop",
        description = "Stationed outside Vironova, attempting to salvage her squad while preserving Aethelgard classified data core.",
        baseSanity = 75,
        baseHeartRate = 85,
        defaultLogs = listOf(
            PlayerLog("vc1", "COMMANDER VANCE", "12:01:45", "Analyzing telemetry from squad members in Sector 4. Their brainwave resonance matches the infected host signatures.", "SYSTEM", 80),
            PlayerLog("vc2", "COMMANDER VANCE", "12:03:10", "Ghost, ignore the static whispers. They are synthetic frequencies designed to target your specific cortisol profile. Keep moving.", "DIALOGUE", 77),
            PlayerLog("vc3", "COMMANDER VANCE", "12:05:30", "Tactical map overlay glitched for 3 seconds. The topographical map rearranged into a neural diagram of a human cortex. Is command center leaking signal?", "OBSERVATION", 71)
        )
    )
)

// --- GEMINI API INTEGRATION FOR EVALUATION ---

object SanityMonitorService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun evaluateLogs(
        character: StoryCharacter,
        logs: List<PlayerLog>
    ): SanityReport = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Return realistic fallback report if no API key is set
            delay(2000) // Simulate network delay
            return@withContext generateFallbackReport(character)
        }

        // Format the logs for the model
        val formattedLogs = logs.joinToString("\n") { log ->
            "[${log.timestamp}] [${log.logType}] ${log.characterName}: \"${log.message}\""
        }

        val prompt = """
            Evaluate the following gameplay and psychological telemetry logs of the character '${character.name}' (Role: ${character.role}) who is trapped in the quarantine zone of Vironova. 
            The zone is flooded with the 'Echo Frequency'—an acoustic weapon that converts suppressed trauma into terrifying, physical hallucinations.
            
            Logs to evaluate:
            $formattedLogs
            
            You must analyze these logs as 'HEY_DOCTOR OS', a cold, highly advanced neural monitor AI implanted in the user's cortex.
            Dr. Aris Thorne (the creator who went mad) left diagnostic files that we use to diagnose neural failure.
            
            Respond STRICTLY with a single, valid JSON object in the exact format shown below. Do not wrap the JSON in Markdown formatting (such as ```json). Return ONLY the pure JSON string.
            
            JSON Structure:
            {
              "evaluatedCharacterName": "${character.name}",
              "currentSanityScore": ${character.baseSanity - 5},
              "finalSanityStatus": "COMPROMISED",
              "drThorneAnalysis": "A clinical yet highly unsettling analysis of the character's neural drift. Write in the voice of Dr. Aris Thorne—haunted, brilliant, scientific, yet clinically compromised by his own exposure.",
              "triggers": [
                {
                  "triggerType": "AUDIO",
                  "description": "Short, vivid description of the specific audio hallucination they will experience in-game.",
                  "severity": "MEDIUM",
                  "triggerSource": "Trauma Projection",
                  "immediateEffect": "Exact in-game technical glitch or phantom simulation (e.g., 'Gunshot sound plays 180 degrees behind player with 0.5s delay')"
                },
                {
                  "triggerType": "VISUAL",
                  "description": "Description of the visual hallucination.",
                  "severity": "HIGH",
                  "triggerSource": "Echo Resonance",
                  "immediateEffect": "In-game visual effect (e.g., 'Peripheral vision darkens and shadows move toward player')"
                }
              ]
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Use high temperature to generate creative hallucinations
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.85)
                put("responseMimeType", "application/json")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url("${BASE_URL}v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext generateFallbackReport(character)
            }
            val responseBodyString = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBodyString)
            val textResult = jsonResponse
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            // Parse clean JSON returned from Gemini
            val cleanJson = JSONObject(textResult.trim())
            val evaluatedName = cleanJson.getString("evaluatedCharacterName")
            val score = cleanJson.getInt("currentSanityScore")
            val status = cleanJson.getString("finalSanityStatus")
            val analysis = cleanJson.getString("drThorneAnalysis")
            
            val jsonTriggers = cleanJson.getJSONArray("triggers")
            val triggersList = mutableListOf<HallucinatoryTrigger>()
            for (i in 0 until jsonTriggers.length()) {
                val item = jsonTriggers.getJSONObject(i)
                triggersList.add(
                    HallucinatoryTrigger(
                        triggerType = item.getString("triggerType"),
                        description = item.getString("description"),
                        severity = item.getString("severity"),
                        triggerSource = item.getString("triggerSource"),
                        immediateEffect = item.getString("immediateEffect")
                    )
                )
            }

            SanityReport(evaluatedName, score, status, analysis, triggersList)
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackReport(character)
        }
    }

    private fun generateFallbackReport(character: StoryCharacter): SanityReport {
        val triggers = when (character.name) {
            "GHOST" -> listOf(
                HallucinatoryTrigger(
                    "AUDIO",
                    "A static whisper playing through Ghost's tactical headset, reciting combat surgeon triage numbers mixed with children's cries.",
                    "MEDIUM",
                    "Echo Frequency",
                    "Ambient volume dips 40% and a phantom beep repeats in left ear."
                ),
                HallucinatoryTrigger(
                    "VISUAL",
                    "Dr. Thorne's ghost silhouette standing in the darkness of Sector 4 botanic biome.",
                    "HIGH",
                    "Trauma Projection",
                    "Red light flicker on UI screen, a silhouette overlays on screen center for 400ms."
                )
            )
            "VIPER" -> listOf(
                HallucinatoryTrigger(
                    "COGNITIVE",
                    "A holographic warning claiming that Viper's oxygen levels have depleted to 0%, inducing simulated hypoxia.",
                    "MEDIUM",
                    "Guilt Projector",
                    "HUD displays flashing RED warning 'OXYGEN EMPTY' and screen tilts progressively."
                ),
                HallucinatoryTrigger(
                    "ENVIRONMENTAL",
                    "The sound of concrete collapsing behind Viper when moving through the server rooms.",
                    "HIGH",
                    "Echo Resonance",
                    "Controller triggers a massive, continuous rumble, accompanied by dust particle effects."
                )
            )
            "DR. THORNE" -> listOf(
                HallucinatoryTrigger(
                    "VISUAL",
                    "Chloe Thorne's childhood toys scattered across the radioactive floor tiles, emitting a warm neon green glow.",
                    "HIGH",
                    "Occipital Damage",
                    "Retro wireframe shapes render over the normal map, simulating neon toys."
                ),
                HallucinatoryTrigger(
                    "AUDIO",
                    "Aethelgard Biotech's evacuation siren repeating at a distorted, decelerated pitch.",
                    "HIGH",
                    "Acoustic Resonance",
                    "High-pitched screech fades into a slow, demonic alarm rhythm."
                )
            )
            else -> listOf(
                HallucinatoryTrigger(
                    "COGNITIVE",
                    "HUD maps rearranging into human neural paths.",
                    "LOW",
                    "Command Leak",
                    "Radar blips align into a circle, representing a neural loop."
                )
            )
        }

        val diagnosis = when (character.name) {
            "GHOST" -> "Subject GhostActual is exhibiting accelerated auditory drift. The acoustic 'Resonance' wave has locked onto his guilt from the Sector 4 botanical garden casualty event. He is attempting to apply tourniquets to empty air. Stabilizers have lost effectiveness; clinical intervention is secondary. He is slipping into the frequency."
            "VIPER" -> "Viper is attempting to map the server hub, but her occipital nodes are vibrating in harmony with Aethelgard's core sequence. She experiences this as a physical binary hum. The phantoms she reports are structured. She is translating the virus's instructions directly. She represents a critical risk of full cognitive synthesis."
            "DR. THORNE" -> "My daughter Chloe is standing by the generator. I can hear her voice in the hum of the cooling fans. The mathematics of my creation are solid; the Echo Frequency is not a disease, but a translator. It projects our absolute failures into reality. I am documented as fully compromised. Do not look for me."
            else -> "Subject Vance is maintaining high distance, but the tactical feed logs indicate minor sympathetic resonance. When monitoring the team, her cortex is experiencing secondary feedback. Her telemetry is stable, but her diagnostic overlays are manifesting visual anomalies."
        }

        return SanityReport(
            evaluatedCharacterName = character.name,
            currentSanityScore = character.baseSanity - 7,
            finalSanityStatus = if (character.baseSanity < 30) "CRITICAL" else "COMPROMISED",
            drThorneAnalysis = diagnosis,
            triggers = triggers
        )
    }
}

@Composable
fun CortexWaveform(sanity: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "cortex")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )

    val isCritical = sanity < 45
    val ampMult = if (isCritical) 16f else 8f
    val speedFactor = if (isCritical) 2.5f else 1.2f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .border(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val points = 80
        val path = androidx.compose.ui.graphics.Path()

        path.moveTo(0f, midY)
        for (i in 0..points) {
            val x = (i / points.toFloat()) * width
            val angle = (i / points.toFloat()) * 4f * Math.PI.toFloat() * speedFactor - phase
            val noise = if (isCritical) {
                (Math.sin((angle * 6.5f).toDouble()) * 3.5).toFloat()
            } else 0f
            val y = midY + (Math.sin(angle.toDouble()).toFloat() * ampMult) + noise
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = if (isCritical) Color(0xFFFF1744) else Color(0xFFD500F9),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

// --- LANDSCAPE DESIGN USER INTERFACE SCREEN ---

@Composable
fun SanityMonitorScreen(onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var activeHallucinationTrigger by remember { mutableStateOf<HallucinatoryTrigger?>(null) }
    var showGlitchOverlay by remember { mutableStateOf(false) }
    var selectedCharacter by remember { mutableStateOf(storyCharacters[0]) }
    var customLogMessage by remember { mutableStateOf("") }
    
    // Store current logs of characters (allows adding manual entries!)
    val characterLogsMap = remember {
        mutableStateMapOf<String, List<PlayerLog>>().apply {
            storyCharacters.forEach { char ->
                put(char.name, char.defaultLogs)
            }
        }
    }
    
    var currentReport by remember { mutableStateOf<SanityReport?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisStep by remember { mutableStateOf("") }
    
    val currentLogs = characterLogsMap[selectedCharacter.name] ?: emptyList()
    
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        ttsInstance.language = Locale.US
        ttsInstance.setPitch(0.7f)
        ttsInstance.setSpeechRate(0.85f)
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    val neonCyan = Color(0xFF00E5FF)
    val medicalGreen = Color(0xFF00E676)
    val sanityPurple = Color(0xFFD500F9)
    val warningAmber = Color(0xFFFFD600)
    val criticalCrimson = Color(0xFFFF1744)
    val darkSurface = Color(0xFF121212)
    val darkBackground = Color(0xFF0A0A0A)
    val mutedGray = Color(0xFF888888)

    // Layout is 100% Landscape Optimized
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(16.dp)
    ) {
        // Subtle futuristic grid drawing behind
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val columns = (size.width / 40.dp.toPx()).toInt()
            val rows = (size.height / 40.dp.toPx()).toInt()
            for (i in 0..columns) {
                drawLine(
                    color = neonCyan,
                    start = androidx.compose.ui.geometry.Offset(i * 40.dp.toPx(), 0f),
                    end = androidx.compose.ui.geometry.Offset(i * 40.dp.toPx(), size.height),
                    strokeWidth = 1f
                )
            }
            for (j in 0..rows) {
                drawLine(
                    color = neonCyan,
                    start = androidx.compose.ui.geometry.Offset(0f, j * 40.dp.toPx()),
                    end = androidx.compose.ui.geometry.Offset(size.width, j * 40.dp.toPx()),
                    strokeWidth = 1f
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // TOP PANEL (Header, Back button, and Character Selector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, neonCyan, CircleShape)
                            .size(36.dp)
                    ) {
                        Text("<", color = neonCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HEY_DOCTOR OS // SANITY_AI_MONITOR v3.5",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "NEURAL RESONANCE DIAGNOSTICS DECK // SYSTEM STATUS: ACTIVE",
                            color = medicalGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Horizontal Character Selector Tab
                Row(
                    modifier = Modifier
                        .background(darkSurface, RoundedCornerShape(4.dp))
                        .border(1.dp, mutedGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(2.dp)
                ) {
                    storyCharacters.forEach { char ->
                        val isSelected = selectedCharacter.name == char.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) neonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSelected) neonCyan else Color.Transparent, RoundedCornerShape(2.dp))
                                .clickable {
                                    selectedCharacter = char
                                    currentReport = null
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = char.name,
                                color = if (isSelected) Color.White else mutedGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // MAIN SPLIT SCREEN: Left Panel (Operative Profile) and Right Panel (Logs & AI Reports)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // LEFT PANEL: OPERATIVE PROFILE & VITALS (40% width)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(4f)
                        .background(darkSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, neonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    // Profile Header & Avatar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = selectedCharacter.profileImage,
                            contentDescription = selectedCharacter.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, neonCyan, RoundedCornerShape(6.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedCharacter.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = selectedCharacter.role,
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedCharacter.affiliation,
                                color = mutedGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedCharacter.description,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Divider(color = mutedGray.copy(alpha = 0.2f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // METRICS SECTION (Sanity Bar, Heart Rate)
                    Text(
                        text = "BIOMETRIC MONITORING",
                        color = mutedGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Sanity Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SANITY DECAY LEVEL", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("${selectedCharacter.baseSanity}%", color = sanityPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Black)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(selectedCharacter.baseSanity / 100f)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(criticalCrimson, sanityPurple, medicalGreen)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Heart Rate Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HEART OSCILLATIONS", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing heart dot
                            val infiniteTransition = rememberInfiniteTransition(label = "pulseDot")
                            val dotScale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.4f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "scale"
                            )
                            Box(
                                modifier = Modifier
                                    .scale(dotScale)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(criticalCrimson)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${selectedCharacter.baseHeartRate} BPM",
                                color = criticalCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "NEURAL RESONANCE WAVEFORM",
                        color = mutedGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CortexWaveform(sanity = selectedCharacter.baseSanity)

                    Spacer(modifier = Modifier.weight(1f))

                    // INPUT LOG FIELD: Insert custom player actions to evaluate!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, mutedGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = customLogMessage,
                            onValueChange = { customLogMessage = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            decorationBox = { innerTextField ->
                                if (customLogMessage.isEmpty()) {
                                    Text(
                                        "MANUAL FEED INPUT...",
                                        color = mutedGray.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Button(
                            onClick = {
                                if (customLogMessage.isNotEmpty()) {
                                    val newLog = PlayerLog(
                                        id = "custom_" + System.currentTimeMillis(),
                                        characterName = selectedCharacter.name,
                                        timestamp = "05:10:00",
                                        message = customLogMessage,
                                        logType = "OBSERVATION",
                                        initialSanity = selectedCharacter.baseSanity
                                    )
                                    characterLogsMap[selectedCharacter.name] = currentLogs + newLog
                                    customLogMessage = ""
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("ADD", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ANALYSIS TRIGGERS BUTTON
                    Button(
                        onClick = {
                            isAnalyzing = true
                            coroutineScope.launch {
                                analysisStep = "ESTABLISHING CORTEX INTERFACE..."
                                delay(800)
                                analysisStep = "DECRYPTING NEURAL FEED VIA GEMINI..."
                                delay(1200)
                                analysisStep = "GENERATING HALLUCINATORY TRIGGER DECK..."
                                val report = SanityMonitorService.evaluateLogs(selectedCharacter, currentLogs)
                                currentReport = report
                                isAnalyzing = false
                                
                                // Play analysis read voice via TTS!
                                if (isTtsReady && tts != null) {
                                    tts?.stop()
                                    tts?.speak(
                                        "Neural evaluation completed. Diagnostics report ${report.finalSanityStatus} status. Triggers armed.",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "SanityAlert"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = sanityPurple.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, sanityPurple),
                        enabled = !isAnalyzing
                    ) {
                        Text(
                            "[ DECRYPT SANITY VIA GEMINI ]",
                            color = sanityPurple,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // RIGHT PANEL: TERMINAL LOG CONSOLE & SYSTEM DIAGNOSIS (60% width)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(6f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // TOP LOG CONSOLE
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(4f)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, mutedGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "RECEIVING TACTICAL FIELD LOGS // FEED STACK",
                            color = mutedGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                reverseLayout = true
                            ) {
                                items(currentLogs.reversed()) { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "[${log.timestamp}]",
                                            color = neonCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "[${log.logType}]",
                                            color = when (log.logType) {
                                                "COMBAT" -> criticalCrimson
                                                "SYSTEM" -> warningAmber
                                                "DIALOGUE" -> medicalGreen
                                                else -> mutedGray
                                            },
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.message,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // BOTTOM DIAGNOSIS / HALLUCINATIONS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(6f)
                            .background(darkSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, if (currentReport != null) warningAmber.copy(alpha = 0.5f) else mutedGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        if (isAnalyzing) {
                            // LOADING OSCILLATOR VIEW
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = sanityPurple, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = analysisStep,
                                    color = sanityPurple,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SCANNING OCCIPITAL OSCILLATION PATTERNS...",
                                    color = mutedGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                        } else if (currentReport != null) {
                            // DISPLAY THE LIVE GEMINI REPORT
                            val report = currentReport!!
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "NEUROLOGIST RECOVERY FILE: DR. THORNE ANALYSIS",
                                            color = warningAmber,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (report.finalSanityStatus == "CRITICAL") criticalCrimson.copy(alpha = 0.2f) else warningAmber.copy(alpha = 0.15f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (report.finalSanityStatus == "CRITICAL") criticalCrimson else warningAmber,
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "STATUS // ${report.finalSanityStatus}",
                                                color = if (report.finalSanityStatus == "CRITICAL") criticalCrimson else warningAmber,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = report.drThorneAnalysis,
                                        color = Color.LightGray,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .border(1.dp, mutedGray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    )
                                }

                                item {
                                    Text(
                                        text = "GENERATED ACOUSTIC & VISUAL HALLUCINATIONS",
                                        color = neonCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                items(report.triggers) { trigger ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black, RoundedCornerShape(6.dp))
                                            .border(1.dp, sanityPurple.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(60.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(sanityPurple.copy(alpha = 0.2f), CircleShape)
                                                    .border(1.dp, sanityPurple, CircleShape)
                                                    .size(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when (trigger.triggerType) {
                                                        "AUDIO" -> "🔊"
                                                        "VISUAL" -> "👁️"
                                                        "COGNITIVE" -> "🧠"
                                                        else -> "🌌"
                                                    },
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = trigger.triggerType,
                                                color = sanityPurple,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = trigger.triggerSource,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(criticalCrimson.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                                        .border(1.dp, criticalCrimson.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "SEV // ${trigger.severity}",
                                                        color = criticalCrimson,
                                                        fontSize = 8.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = trigger.description,
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "> EFFECT: ${trigger.immediateEffect}",
                                                color = medicalGreen,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    activeHallucinationTrigger = trigger
                                                    showGlitchOverlay = true
                                                    if (isTtsReady && tts != null) {
                                                        tts?.stop()
                                                        tts?.speak(
                                                            "Simulating target trigger. " + trigger.description,
                                                            TextToSpeech.QUEUE_FLUSH,
                                                            null,
                                                            "HallucinationWhisper"
                                                        )
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = sanityPurple.copy(alpha = 0.2f)),
                                                border = BorderStroke(1.dp, sanityPurple.copy(alpha = 0.6f)),
                                                shape = RoundedCornerShape(4.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text(
                                                    text = "⚡ EMULATE NEURAL TRANSITION",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // STANDBY / SAFE RECOVERY MODE
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Awaiting telemetric link",
                                    tint = mutedGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "AWAITING TELEMETRY LINK",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Select an operative above, inspect their feed, or feed custom log inputs.\nPress [ DECRYPT SANITY VIA GEMINI ] to evaluate psychological status.",
                                    color = mutedGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FULL-SCREEN CINEMATIC HALLUCINATION SIMULATION OVERLAY
        if (showGlitchOverlay && activeHallucinationTrigger != null) {
            val trigger = activeHallucinationTrigger!!
            // Launch auto-dismiss timer
            LaunchedEffect(trigger) {
                delay(6000)
                showGlitchOverlay = false
                activeHallucinationTrigger = null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { 
                        showGlitchOverlay = false
                        activeHallucinationTrigger = null
                        tts?.stop()
                    }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background visual effects depending on trigger type
                when (trigger.triggerType) {
                    "VISUAL" -> {
                        // Rapid chromatic flicker effect
                        val infiniteTransition = rememberInfiniteTransition(label = "flicker")
                        val flickerAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 0.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(120, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "alpha"
                        )
                        val shiftOffset by infiniteTransition.animateFloat(
                            initialValue = -5f,
                            targetValue = 5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(90, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "shift"
                        )

                        // Red flashing vignette & horizontal scan lines
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color(0xFFFF1744).copy(alpha = 0.4f)),
                                        radius = 800f
                                    )
                                )
                                .offset(x = shiftOffset.dp)
                        ) {
                            // Dynamic glitch line
                            Canvas(modifier = Modifier.fillMaxSize().alpha(flickerAlpha)) {
                                val linesCount = 15
                                for (k in 0..linesCount) {
                                    val yOffset = (size.height / linesCount) * k + (shiftOffset * 5)
                                    drawLine(
                                        color = Color(0xFFFF1744).copy(alpha = 0.3f),
                                        start = androidx.compose.ui.geometry.Offset(0f, yOffset),
                                        end = androidx.compose.ui.geometry.Offset(size.width, yOffset),
                                        strokeWidth = 2f
                                    )
                                }
                            }
                        }
                    }
                    "AUDIO" -> {
                        // Expanding sound wave circles
                        val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
                        val waveRadius1 by infiniteTransition.animateFloat(
                            initialValue = 40f,
                            targetValue = 280f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "wave1"
                        )
                        val waveAlpha1 by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "alpha1"
                        )
                        val waveRadius2 by infiniteTransition.animateFloat(
                            initialValue = 40f,
                            targetValue = 280f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, delayMillis = 900, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "wave2"
                        )
                        val waveAlpha2 by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, delayMillis = 900, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "alpha2"
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                color = Color(0xFFD500F9).copy(alpha = waveAlpha1 * 0.4f),
                                radius = waveRadius1.dp.toPx(),
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = waveAlpha2 * 0.4f),
                                radius = waveRadius2.dp.toPx(),
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                    "COGNITIVE" -> {
                        // Hypnotic zooming or radial blurring glow
                        val infiniteTransition = rememberInfiniteTransition(label = "hypnotic")
                        val zoomScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = FastOutLinearInEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "zoom"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(zoomScale)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.25f)),
                                        radius = 600f
                                    )
                                )
                        )
                    }
                    else -> {
                        // Environmental shaking / collapsing lines
                        val infiniteTransition = rememberInfiniteTransition(label = "earthquake")
                        val shakeX by infiniteTransition.animateFloat(
                            initialValue = -8f,
                            targetValue = 8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(70, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "shakeX"
                        )
                        val shakeY by infiniteTransition.animateFloat(
                            initialValue = 8f,
                            targetValue = -8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(70, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "shakeY"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = shakeX.dp, y = shakeY.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFD600).copy(alpha = 0.1f), Color.Transparent, Color(0xFFFFD600).copy(alpha = 0.15f))
                                    )
                                )
                        )
                    }
                }

                // Overlay UI details demonstrating exact glitch / technical effect
                Card(
                    modifier = Modifier
                        .widthIn(max = 550.dp)
                        .border(1.5.dp, Color(0xFFFF1744), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠ NEURAL HALLUCINATION BREACH ⚠",
                            color = Color(0xFFFF1744),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "SOURCE: ${trigger.triggerSource.uppercase()} // SEVERITY: ${trigger.severity}",
                            color = Color(0xFFFFD600),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = trigger.description,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFF1744).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0xFFFF1744).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SIMULATED TECHNICAL HUD EXECUTABLE:",
                                    color = Color(0xFFFF1744),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trigger.immediateEffect,
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "[ CLICK ANYWHERE TO CALIBRATE STABILIZERS ]",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
