package com.example.skillbridge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.*
import com.example.skillbridge.ui.theme.*

private val ProfileCardShape = RoundedCornerShape(22.dp)
private val ProfileTileShape = RoundedCornerShape(17.dp)

/**
 * SkillBridge Profile — redesigned as a polished account and professional dashboard.
 * Uses the existing model and callback contract so navigation/business logic stays outside this UI.
 */
@Composable
fun ProfileScreen(
    user: User,
    learningCourses: List<LearningCourse>,
    portfolioItems: List<PortfolioItem> = emptyList(),
    isLowDataMode: Boolean,
    onToggleLowDataMode: (Boolean) -> Unit,
    onUpgradePro: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onTakeQuizClick: () -> Unit,
    onLaunchVoiceOnboarding: () -> Unit,
    onLaunchFormOnboarding: () -> Unit,
    onLaunchIvrOnboarding: () -> Unit,
    onCompleteCourse: (String) -> Unit,
    onOpenBusinessTeam: () -> Unit,
    onOpenStaffLogin: () -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    onUpdateTrustedContact: (String) -> Unit
) {
    var showShareQrDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf(false) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showDataErasureDialog by remember { mutableStateOf(false) }
    var contactInput by remember(user.trustedContactPhone) {
        mutableStateOf(user.trustedContactPhone)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // Screen heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "My profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText
                )
                Text(
                    "Your identity, reputation and account settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted
                )
            }
            Surface(
                shape = CircleShape,
                color = SkillBridgeWhite,
                border = BorderStroke(1.dp, SkillBridgeBorder),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Profile settings",
                        tint = SkillBridgeText
                    )
                }
            }
        }

        // Profile / skill passport hero
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
            border = BorderStroke(1.dp, SkillBridgeBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SkillBridgePrimary, SkillBridgePrimaryHover)
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.88f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "SKILL PASSPORT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Column(Modifier.padding(horizontal = 17.dp).padding(bottom = 17.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 0.dp)
                                .offset(y = (-27).dp)
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(SkillBridgePrimarySoft)
                                .border(4.dp, SkillBridgeWhite, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.avatarEmoji, fontSize = 39.sp)
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(
                            modifier = Modifier.weight(1f).padding(top = 10.dp, bottom = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    user.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SkillBridgeText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (user.isKycVerified) {
                                    Spacer(Modifier.width(5.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "KYC verified",
                                        tint = SkillBridgeSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SkillBridgeTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    user.locationCity,
                                    fontSize = 12.sp,
                                    color = SkillBridgeTextMuted
                                )
                            }
                        }
                        IconButton(
                            onClick = { showShareQrDialog = true },
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SkillBridgePrimarySoft)
                        ) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = "Share skill passport QR",
                                tint = SkillBridgePrimaryHover
                            )
                        }
                    }

                    Text(
                        user.bio.ifBlank { "Build your professional profile and let customers discover your skills." },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = SkillBridgeTextMuted
                    )

                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileMetric(
                            modifier = Modifier.weight(1f),
                            value = "${user.trustScore}%",
                            label = "Trust score",
                            icon = Icons.Default.Shield,
                            valueColor = SkillBridgeSuccess
                        )
                        ProfileMetric(
                            modifier = Modifier.weight(1f),
                            value = "★ ${user.rating}",
                            label = "Rating",
                            icon = Icons.Default.Star,
                            valueColor = SkillBridgePrimaryHover
                        )
                        ProfileMetric(
                            modifier = Modifier.weight(1f),
                            value = "${user.jobsCompleted}",
                            label = "Jobs done",
                            icon = Icons.Default.TaskAlt,
                            valueColor = SkillBridgeText
                        )
                    }

                    if (user.badges.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "VERIFIED SKILLS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = SkillBridgeTextMuted
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            user.badges.forEach { badge ->
                                Surface(
                                    shape = CircleShape,
                                    color = SkillBridgePrimarySoft,
                                    border = BorderStroke(1.dp, SkillBridgePrimary.copy(alpha = 0.14f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = SkillBridgePrimaryHover,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            badge,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SkillBridgePrimaryHover
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pro membership
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF20242C)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFC66D), modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SkillBridge Pro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (user.isProMember) {
                            Spacer(Modifier.width(7.dp))
                            Surface(shape = CircleShape, color = SkillBridgeSuccess.copy(alpha = 0.20f)) {
                                Text("ACTIVE", modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF86EFAC))
                            }
                        }
                    }
                    Text(
                        if (user.isProMember) "Your Pro benefits are active." else "Lower platform fees and priority dispatch.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.72f),
                        lineHeight = 16.sp
                    )
                }
                if (!user.isProMember) {
                    Button(
                        onClick = onUpgradePro,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 10.dp)
                    ) {
                        Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        SectionHeading("Preferences", "Make SkillBridge work the way you do")

        // Preferences grouped in a single settings surface
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
            border = BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Column {
                SettingRow(
                    icon = Icons.Default.DataSaverOn,
                    title = "Low-data mode",
                    subtitle = "Reduce map animations and data usage",
                    trailing = {
                        Switch(
                            checked = isLowDataMode,
                            onCheckedChange = onToggleLowDataMode,
                            colors = SwitchDefaults.colors(checkedTrackColor = SkillBridgePrimary)
                        )
                    }
                )
                ProfileDivider()
                SettingRow(
                    icon = Icons.Default.ContactPhone,
                    title = "Trusted emergency contact",
                    subtitle = user.trustedContactPhone.ifBlank { "Add a trusted phone number" },
                    onClick = { showEditContactDialog = true }
                )
                ProfileDivider()
                Column(Modifier.fillMaxWidth().padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingIcon(Icons.Default.Language)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text("App language", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                            Text("Choose your preferred language", fontSize = 11.sp, color = SkillBridgeTextMuted)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val selected = user.language == lang
                            Surface(
                                onClick = { onLanguageChange(lang) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) SkillBridgePrimary else SkillBridgeBg,
                                border = BorderStroke(1.dp, if (selected) SkillBridgePrimary else SkillBridgeBorder)
                            ) {
                                Box(Modifier.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        lang.nativeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else SkillBridgeText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Portfolio
        if (portfolioItems.isNotEmpty()) {
            SectionHeading("Work portfolio", "Showcase the quality of your work")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                portfolioItems.forEach { item ->
                    Card(
                        modifier = Modifier.width(172.dp),
                        shape = ProfileCardShape,
                        colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
                        border = BorderStroke(1.dp, SkillBridgeBorder)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(78.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(SkillBridgeSurfaceMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.emoji, fontSize = 34.sp)
                            }
                            Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.category, fontSize = 11.sp, color = SkillBridgeTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Learning hub
        SectionHeading("Learning hub", "Build skills and earn new badges")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
            border = BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Column {
                learningCourses.forEachIndexed { index, course ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(43.dp).clip(RoundedCornerShape(13.dp))
                                .background(if (course.isCompleted) Color(0xFFEAF8EF) else SkillBridgeSurfaceMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (course.isCompleted) Icons.Default.CheckCircle else Icons.Default.School,
                                contentDescription = null,
                                tint = if (course.isCompleted) SkillBridgeSuccess else SkillBridgePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(course.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${course.durationMin} min  •  Badge: ${course.rewardBadge}", fontSize = 10.sp, color = SkillBridgeTextMuted)
                        }
                        Spacer(Modifier.width(8.dp))
                        if (course.isCompleted) {
                            Surface(shape = CircleShape, color = Color(0xFFEAF8EF)) {
                                Text("Done", modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), color = SkillBridgeSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextButton(onClick = { onCompleteCourse(course.id) }) {
                                Text("Start", color = SkillBridgePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (index != learningCourses.lastIndex) ProfileDivider()
                }
            }
        }
        OutlinedButton(
            onClick = onTakeQuizClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SkillBridgePrimary.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgePrimaryHover)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Take a skills quiz", fontWeight = FontWeight.Bold)
        }

        // Onboarding
        SectionHeading("Profile setup", "Choose how you want to complete onboarding")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
            border = BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OnboardingAction(Icons.Default.Mic, "Voice onboarding", "Set up your profile by speaking", onLaunchVoiceOnboarding)
                OnboardingAction(Icons.Default.Description, "Guided form", "Enter your details step by step", onLaunchFormOnboarding)
                OnboardingAction(Icons.Default.Dialpad, "IVR onboarding", "Use the assisted phone flow", onLaunchIvrOnboarding)
            }
        }

        // Account & support
        SectionHeading("Account & support", "Manage your workspace and get help")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
            border = BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Column {
                SettingRow(Icons.Default.Business, "Business & team", "Billing, dispatchers and team roles", onClick = onOpenBusinessTeam)
                ProfileDivider()
                SettingRow(Icons.Default.AdminPanelSettings, "Staff & admin portal", "Restricted to authorised staff", onClick = onOpenStaffLogin)
                ProfileDivider()
                SettingRow(Icons.Default.MonitorHeart, "System diagnostics", "Maps, connectivity and offline sync", onClick = onOpenDiagnostics)
                ProfileDivider()
                SettingRow(Icons.Default.CardGiftcard, "Refer & earn ₹200", "Invite customers and service professionals", onClick = { showReferralDialog = true })
                ProfileDivider()
                SettingRow(Icons.Default.SupportAgent, "Help centre", "Support, disputes and escalation", onClick = { showHelpCenterDialog = true })
                ProfileDivider()
                SettingRow(Icons.Default.DeleteOutline, "Request data erasure", "Privacy and personal data controls", danger = true, onClick = { showDataErasureDialog = true })
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ProfileCardShape,
            color = SkillBridgeSurfaceMuted
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SkillBridgeSuccess, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Your account, your control", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                    Text("Manage trusted contacts, privacy and service preferences here.", fontSize = 10.sp, color = SkillBridgeTextMuted, lineHeight = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (showShareQrDialog) {
        AlertDialog(
            onDismissRequest = { showShareQrDialog = false },
            title = { Text("Skill Passport", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Share your profile with customers and employers.", color = SkillBridgeTextMuted)
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier.size(176.dp).clip(RoundedCornerShape(18.dp))
                            .background(SkillBridgePrimarySoft)
                            .border(2.dp, SkillBridgePrimary, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = SkillBridgePrimaryHover, modifier = Modifier.size(105.dp))
                            Text("skillbridge.in/p/${user.id}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (user.isKycVerified) "KYC verified" else "KYC verification pending",
                        fontSize = 12.sp,
                        color = if (user.isKycVerified) SkillBridgeSuccess else SkillBridgeWarning,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showShareQrDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)) {
                    Text("Done")
                }
            },
            containerColor = SkillBridgeWhite,
            shape = ProfileCardShape
        )
    }

    if (showEditContactDialog) {
        AlertDialog(
            onDismissRequest = { showEditContactDialog = false },
            title = { Text("Trusted emergency contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the phone number that should receive emergency alerts.", fontSize = 13.sp, color = SkillBridgeTextMuted)
                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = { contactInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Phone number") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTrustedContact(contactInput)
                        showEditContactDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) { Text("Save contact") }
            },
            dismissButton = { TextButton(onClick = { showEditContactDialog = false }) { Text("Cancel") } },
            containerColor = SkillBridgeWhite,
            shape = ProfileCardShape
        )
    }

    if (showHelpCenterDialog) {
        HelpCenterDialog(
            onSubmitTicket = { _, _ -> showHelpCenterDialog = false },
            onDismiss = { showHelpCenterDialog = false }
        )
    }
    if (showReferralDialog) {
        ReferralProgramDialog(
            referralCode = "SKILL${user.phone.takeLast(4).ifEmpty { "2026" }}",
            onDismiss = { showReferralDialog = false }
        )
    }
    if (showDataErasureDialog) {
        DataErasureDialog(
            onConfirmErasure = { showDataErasureDialog = false },
            onDismiss = { showDataErasureDialog = false }
        )
    }
}

@Composable
private fun ProfileMetric(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = SkillBridgeBg,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            Modifier.padding(vertical = 11.dp, horizontal = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(17.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, maxLines = 1)
            Text(label, fontSize = 9.sp, color = SkillBridgeTextMuted, maxLines = 1)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText)
        Text(subtitle, fontSize = 11.sp, color = SkillBridgeTextMuted)
    }
}

@Composable
private fun SettingIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(SkillBridgeSurfaceMuted),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                .background(if (danger) SkillBridgeDanger.copy(alpha = 0.09f) else SkillBridgeSurfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (danger) SkillBridgeDanger else SkillBridgePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (danger) SkillBridgeDanger else SkillBridgeText)
            Text(subtitle, fontSize = 10.sp, color = SkillBridgeTextMuted, lineHeight = 14.sp)
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 67.dp),
        color = SkillBridgeBorder.copy(alpha = 0.75f)
    )
}

@Composable
private fun OnboardingAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ProfileTileShape,
        color = SkillBridgeBg,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIcon(icon)
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                Text(subtitle, fontSize = 10.sp, color = SkillBridgeTextMuted)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(18.dp))
        }
    }
}
