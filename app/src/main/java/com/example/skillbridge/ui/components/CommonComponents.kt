package com.example.skillbridge.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

// Top bar matching Section 12: location selector (left), search, notification bell
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillBridgeTopBar(
    selectedCity: String,
    onCityClick: () -> Unit,
    activeRole: UserRole,
    onRoleToggle: (UserRole) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    unreadNotificationCount: Int = 2,
    onNotificationClick: () -> Unit,
    onSosClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp),
        color = SkillBridgeBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Row 1: Location & Role Switch & Emergency SOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .clickable { onCityClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("location_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = SkillBridgePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCity,
                        style = MaterialTheme.typography.labelLarge,
                        color = SkillBridgePrimaryHover,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select City",
                        tint = SkillBridgePrimaryHover,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Middle: Mode Switch (Worker / Poster / Admin)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SkillBridgeBorder)
                        .padding(2.dp)
                ) {
                    val roles = listOf(
                        UserRole.TASK_POSTER to "Hire",
                        UserRole.WORKER to "Earn"
                    )
                    roles.forEach { (role, label) ->
                        val isSelected = activeRole == role
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) SkillBridgePrimary else Color.Transparent)
                                .clickable { onRoleToggle(role) }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .testTag("role_tab_${role.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SkillBridgeWhite else SkillBridgeTextMuted
                            )
                        }
                    }
                }

                // Right: SOS & Notifications
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSosClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkillBridgeDanger.copy(alpha = 0.12f))
                            .testTag("sos_button")
                    ) {
                        Text(
                            text = "SOS",
                            color = SkillBridgeDanger,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = SkillBridgeText
                            )
                        }
                        if (unreadNotificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SkillBridgePrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar with in-field Mic button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("search_field"),
                placeholder = {
                    Text(
                        text = if (activeRole == UserRole.WORKER) "Search jobs, skills, locations..." else "Search 'Electrician', 'Plumber', 'Logo'...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SkillBridgePrimary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SkillBridgePrimary,
                    unfocusedBorderColor = SkillBridgeBorder,
                    focusedContainerColor = SkillBridgeBg,
                    unfocusedContainerColor = SkillBridgeBg
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// Bottom Navigation with center raised orange microphone button
@Composable
fun SkillBridgeBottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onVoiceClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),
        color = SkillBridgeBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home
            BottomNavItem(
                icon = if (currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentScreen == "home",
                onClick = { onNavigate("home") },
                tag = "nav_home"
            )

            // 2. Jobs / Orders
            BottomNavItem(
                icon = if (currentScreen == "jobs") Icons.Filled.Work else Icons.Outlined.WorkOutline,
                label = "Orders",
                isSelected = currentScreen == "jobs",
                onClick = { onNavigate("jobs") },
                tag = "nav_jobs"
            )

            // 3. Center Raised Orange Mic Button
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SkillBridgePrimary)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clickable { onVoiceClick() }
                    .testTag("voice_central_mic"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Assistant",
                    tint = SkillBridgeWhite,
                    modifier = Modifier.size(30.dp)
                )
            }

            // 4. Wallet & Escrow
            BottomNavItem(
                icon = if (currentScreen == "wallet") Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                label = "Wallet",
                isSelected = currentScreen == "wallet",
                onClick = { onNavigate("wallet") },
                tag = "nav_wallet"
            )

            // 5. Profile & Skill Passport
            BottomNavItem(
                icon = if (currentScreen == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                label = "Profile",
                isSelected = currentScreen == "profile",
                onClick = { onNavigate("profile") },
                tag = "nav_profile"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) SkillBridgePrimary else SkillBridgeTextMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) SkillBridgePrimaryHover else SkillBridgeTextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Status Stepper for Job Lifecycle
@Composable
fun JobStatusStepper(
    currentStatus: JobStatus,
    isLocalJob: Boolean
) {
    val steps = if (isLocalJob) {
        listOf(
            JobStatus.REQUESTED to "Requested",
            JobStatus.MATCHED to "Matched",
            JobStatus.EN_ROUTE to "En Route",
            JobStatus.ARRIVED to "Arrived",
            JobStatus.IN_PROGRESS to "In Progress",
            JobStatus.COMPLETED to "Done"
        )
    } else {
        listOf(
            JobStatus.POSTED to "Posted",
            JobStatus.PROPOSALS to "Proposals",
            JobStatus.HIRED to "Hired",
            JobStatus.IN_PROGRESS to "In Progress",
            JobStatus.DELIVERED to "Delivered",
            JobStatus.COMPLETED to "Done"
        )
    }

    val currentIndex = steps.indexOfFirst { it.first == currentStatus }.let { if (it == -1) 0 else it }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SkillBridgeSurfaceMuted)
            .border(1.dp, SkillBridgeBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Job Status: ${currentStatus.name.replace("_", " ")}",
            style = MaterialTheme.typography.titleSmall,
            color = SkillBridgePrimaryHover,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (status, label) ->
                val isCompleted = index < currentIndex
                val isCurrent = index == currentIndex

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> SkillBridgeSuccess
                                    isCurrent -> SkillBridgePrimary
                                    else -> SkillBridgeBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SkillBridgeWhite,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) SkillBridgeWhite else SkillBridgeTextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isCurrent) SkillBridgePrimaryHover else SkillBridgeTextMuted,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(2.dp)
                            .background(if (index < currentIndex) SkillBridgeSuccess else SkillBridgeBorder)
                    )
                }
            }
        }
    }
}

// Incoming Request Card with 20-Second Circular Countdown Ring for Worker
@Composable
fun IncomingRequestCard(
    job: Job,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(20) }

    LaunchedEffect(job.id) {
        secondsRemaining = 20
        while (secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            secondsRemaining -= 1
        }
        if (secondsRemaining <= 0) {
            onReject()
        }
    }

    val progress = secondsRemaining / 20f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .testTag("incoming_request_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgePrimary))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkillBridgePrimarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = SkillBridgePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NEW WORK REQUEST",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkillBridgePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = job.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 20s Circular Countdown Ring
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (secondsRemaining > 5) SkillBridgePrimary else SkillBridgeDanger,
                        strokeWidth = 3.dp,
                        trackColor = SkillBridgeBorder,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (secondsRemaining > 5) SkillBridgePrimaryHover else SkillBridgeDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = job.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📍 ${job.locationAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = SkillBridgeTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI Match Explanation Card
            if (job.matchExplanation.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "💡 ${job.matchExplanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgePrimaryHover
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Payout & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated Net Payout",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeTextMuted
                    )
                    Text(
                        text = "₹${(job.budget * 0.85).toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = SkillBridgeSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeTextMuted
                    )
                    Text(
                        text = "${job.distanceKm} km (~${job.etaMinutes} min)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: Accept / Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reject_request_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeDanger)
                ) {
                    Text("Decline", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("accept_request_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkillBridgePrimary,
                        contentColor = SkillBridgeWhite
                    )
                ) {
                    Text("Accept Now (₹${(job.budget * 0.85).toInt()})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Voice Assistant Waveform Dialog
@Composable
fun VoiceAssistantDialog(
    isListening: Boolean,
    spokenText: String,
    extractedEntities: Map<String, String>,
    onSpeakSample: (String) -> Unit,
    onConfirmEntities: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (extractedEntities.isNotEmpty()) {
                Button(
                    onClick = onConfirmEntities,
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                    modifier = Modifier.testTag("confirm_voice_entities_button")
                ) {
                    Text("Confirm & Search / Post", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SkillBridgeTextMuted)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = SkillBridgePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SkillBridge Voice Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Speak in Hindi, Marathi, or English",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Waveform visualization
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(scale1, scale2, scale3, scale2, scale1, scale3, scale2)
                    heights.forEach { factor ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .width(6.dp)
                                .height((40 * factor).dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(SkillBridgePrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Spoken transcript card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (spokenText.isNotBlank()) "\"$spokenText\"" else "Listening for: 'Mujhe Dharampeth me urgent electrician chahiye'...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SkillBridgeText
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick presets
                Text(
                    text = "Tap to simulate voice prompt:",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onSpeakSample("Dharampeth me urgent electrician chahiye switchboard repair ke liye") },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("⚡ Electrician", fontSize = 11.sp, color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSpeakSample("Ghar ke liye cook chahiye Marathi khane ke liye") },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("🍳 Cook / Chef", fontSize = 11.sp, color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                    }
                }

                // Extracted Entities
                if (extractedEntities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AI Extracted Details:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    extractedEntities.forEach { (k, v) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = k, style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                            Text(text = v, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                        }
                    }
                }
            }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// SOS Emergency Broadcast Dialog
@Composable
fun SosDialog(
    onDismiss: () -> Unit,
    onBroadcastEmergency: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SkillBridgeDanger
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency SOS & Live Alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeDanger
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "This will immediately notify your trusted contacts and local SkillBridge Safety Response Team with your live GPS location.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkillBridgeText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("📍 Current Location: Dharampeth, Nagpur", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("🛰 Lat/Lng: 21.1458° N, 79.0882° E", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                        Text("🛡 Emergency Dial: 112 / +91 712 2560000", style = MaterialTheme.typography.bodySmall, color = SkillBridgeDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBroadcastEmergency,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeDanger),
                modifier = Modifier.testTag("send_sos_confirm_button")
            ) {
                Text("Broadcast Live SOS", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SkillBridgeTextMuted)
            }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Masked Call Dialog
@Composable
fun MaskedCallDialog(
    calleeName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SkillBridge Masked Calling", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Your personal phone number is hidden for privacy. Calling $calleeName via secure SkillBridge IVR relay (+91 712 660 XXXX).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Call status: Connected & Recorded for safety",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeDanger)
            ) {
                Text("End Call", color = SkillBridgeWhite)
            }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Interactive Skill Verification Quiz Dialog
@Composable
fun SkillQuizDialog(
    categoryName: String,
    onQuizPassed: () -> Unit,
    onDismiss: () -> Unit
) {
    val questions = remember {
        listOf(
            QuizQuestion(
                1,
                "What safety equipment MUST be worn before servicing a live distribution board?",
                listOf("Rubber insulated gloves & safety shoes", "Cloth gloves", "Any regular footwear", "No gloves needed"),
                0
            ),
            QuizQuestion(
                2,
                "What is the standard household AC voltage in India?",
                listOf("110V AC", "230V AC ± 10%", "440V AC", "12V DC"),
                1
            ),
            QuizQuestion(
                3,
                "What is the first action to take when an electrical burning smell is detected?",
                listOf("Turn off the main incoming breaker / MCB", "Throw water immediately", "Keep using appliances", "Touch the wire"),
                0
            )
        )
    }

    var currentQIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("$categoryName Skill Verification Quiz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!quizFinished) {
                    val q = questions[currentQIndex]
                    Text("Question ${currentQIndex + 1} of ${questions.size}", style = MaterialTheme.typography.labelSmall, color = SkillBridgePrimaryHover)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    q.options.forEachIndexed { optIndex, text ->
                        val isSel = selectedOption == optIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                                .border(1.dp, if (isSel) SkillBridgePrimary else SkillBridgeBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedOption = optIndex }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { selectedOption = optIndex },
                                colors = RadioButtonDefaults.colors(selectedColor = SkillBridgePrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text, style = MaterialTheme.typography.bodySmall, color = SkillBridgeText)
                        }
                    }
                } else {
                    // Result
                    val passed = score >= 2
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (passed) SkillBridgeSuccess else SkillBridgeDanger,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (passed) "Skill Verification Passed! ($score/3)" else "Verification Failed ($score/3)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (passed) SkillBridgeSuccess else SkillBridgeDanger
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (passed) "Badge 'Verified $categoryName' added to your Skill Passport! Trust Score boosted." else "Please review safety modules and try again tomorrow.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!quizFinished) {
                Button(
                    onClick = {
                        if (selectedOption != null) {
                            if (selectedOption == questions[currentQIndex].correctIndex) {
                                score += 1
                            }
                            if (currentQIndex < questions.size - 1) {
                                currentQIndex += 1
                                selectedOption = null
                            } else {
                                quizFinished = true
                            }
                        }
                    },
                    enabled = selectedOption != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text(if (currentQIndex < questions.size - 1) "Next Question" else "Submit Quiz", color = SkillBridgeWhite)
                }
            } else {
                Button(
                    onClick = {
                        if (score >= 2) onQuizPassed()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text("Close & Update Passport", color = SkillBridgeWhite)
                }
            }
        },
        dismissButton = {
            if (!quizFinished) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// IVR Phone Keypad Simulator Dialog
@Composable
fun IvrSimulatorDialog(
    onDismiss: () -> Unit,
    onIvrJobBooked: (Category, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var ivrAudioPrompt by remember { mutableStateOf("Namaste. Press 1 for Hindi. Marathi sathi 2 daba. For English press 3.") }
    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var selectedCategory by remember { mutableStateOf("Electrician") }
    var enteredPincode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phone Keypad / IVR Onboarding", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🔊 IVR Audio: $ivrAudioPrompt",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgePrimaryHover,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Keypad Display
                Text(
                    text = "Keypad Input: ${if (step == 3) enteredPincode else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeText
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3x4 Keypad Grid
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { digit ->
                            Button(
                                onClick = {
                                    when (step) {
                                        1 -> {
                                            selectedLanguage = when (digit) {
                                                "1" -> "Hindi"
                                                "2" -> "Marathi"
                                                else -> "English"
                                            }
                                            step = 2
                                            ivrAudioPrompt = "Aapko kaam chahiye to 1 dabaye. Worker chahiye to 2 dabaye."
                                        }
                                        2 -> {
                                            step = 3
                                            ivrAudioPrompt = "Apne area ka 6-digit PIN code dial kijiye followed by #"
                                        }
                                        3 -> {
                                            if (digit == "#") {
                                                step = 4
                                                ivrAudioPrompt = "Dhanyawad! PIN $enteredPincode me aapka order register ho gaya. SMS bhej diya gaya hai."
                                            } else if (digit != "*") {
                                                if (enteredPincode.length < 6) {
                                                    enteredPincode += digit
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SkillBridgeSurfaceMuted,
                                    contentColor = SkillBridgeText
                                ),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                            ) {
                                Text(digit, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step >= 4) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text("Finish IVR Call", color = SkillBridgeWhite)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hang Up") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}
