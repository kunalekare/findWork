package com.example.skillbridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

// Path 1: Voice-First Conversational Onboarding Dialog
@Composable
fun VoiceOnboardingDialog(
    onDismiss: () -> Unit,
    onVoiceProfileCreated: (name: String, skill: String, location: String, rate: Int) -> Unit,
    onSpeak: (String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var extractedName by remember { mutableStateOf("Rajesh Kolhe") }
    var extractedSkill by remember { mutableStateOf("Plumbing & Pipe Fitting") }
    var extractedLocation by remember { mutableStateOf("Sadar, Nagpur") }
    var extractedRate by remember { mutableStateOf("₹400 / hour") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voice Onboarding (बोलकर प्रोफाइल बनाएं)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (step) {
                        1 -> "Audio Prompt: 'Apna naam aur kaam batayein...'"
                        2 -> "Processing voice recording..."
                        else -> "Entity Confirmation Card"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgePrimaryHover,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (step == 1) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SkillBridgePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            onSpeak("Namaste! Aapka naam aur aapka hunar ya skill batayein.")
                            step = 2
                        }) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Tap to Speak", tint = SkillBridgeWhite, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tap mic and speak in Hindi or Marathi", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SkillBridgeSurfaceMuted)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("AI Entity Extraction Card", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• Name: $extractedName", fontWeight = FontWeight.Bold)
                            Text("• Skill Category: $extractedSkill")
                            Text("• Service Area: $extractedLocation")
                            Text("• Expected Rate: $extractedRate")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSpeak("Aapka naam $extractedName hai, aur aap $extractedSkill ka kaam Sadar Nagpur me karte hain.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = SkillBridgePrimaryHover)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🔊 Read Back Aloud (सुनें)", color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        onVoiceProfileCreated(extractedName, extractedSkill, extractedLocation, 400)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text(if (step == 1) "Simulate Voice Answer" else "Confirm & Save Profile", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Path 2: Structured Multi-Step Form Onboarding Dialog
@Composable
fun FormOnboardingDialog(
    onDismiss: () -> Unit,
    onProfileCreated: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var fullName by remember { mutableStateOf("Manoj Sharma") }
    var skillTag by remember { mutableStateOf("Electrician & Solar Inverter") }
    var experienceYears by remember { mutableStateOf("5 Years") }
    var education by remember { mutableStateOf("ITI Diploma in Electrical") }
    var expectedRate by remember { mutableStateOf("₹500 / hr") }
    var resumeParsed by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Structured Form Onboarding", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Resume parser simulation banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgePrimarySoft)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = SkillBridgePrimaryHover)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Upload PDF Resume / Bio-Data", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                            Text("Auto-extracts skills & prefills form", fontSize = 10.sp, color = SkillBridgeTextMuted)
                        }
                        Button(
                            onClick = {
                                resumeParsed = true
                                fullName = "Manoj V. Sharma"
                                skillTag = "Senior Industrial Electrician"
                                education = "ITI Nagpur Govt Polytechnic"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimaryHover),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (resumeParsed) "Parsed ✓" else "Parse PDF", fontSize = 10.sp, color = SkillBridgeWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillTag,
                    onValueChange = { skillTag = it },
                    label = { Text("Primary Skill Tags") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education / Certification") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = experienceYears,
                        onValueChange = { experienceYears = it },
                        label = { Text("Experience") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expectedRate,
                        onValueChange = { expectedRate = it },
                        label = { Text("Expected Rate") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onProfileCreated()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text("Save Profile to Skill Passport", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}
