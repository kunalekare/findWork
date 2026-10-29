package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.*
import com.example.skillbridge.ui.theme.*

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
    onUpdateTrustedContact: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var showShareQrDialog by remember { mutableStateOf(false) }
    var showEditContactDialog by remember { mutableStateOf(false) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    var showDataErasureDialog by remember { mutableStateOf(false) }
    var contactInput by remember { mutableStateOf(user.trustedContactPhone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. SKILL PASSPORT HERO CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .testTag("skill_passport_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgePrimary))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.avatarEmoji, fontSize = 42.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (user.isKycVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified KYC",
                                        tint = SkillBridgeSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Skill Passport #${user.id.uppercase()} • ${user.locationCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }

                    // Share QR Button
                    IconButton(
                        onClick = { showShareQrDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SkillBridgePrimarySoft)
                            .testTag("share_qr_button")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR Code", tint = SkillBridgePrimaryHover)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkillBridgeText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Trust Score & Metrics Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Trust Score", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                            Text("${user.trustScore}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rating", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                            Text("⭐ ${user.rating}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Completed", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                            Text("${user.jobsCompleted} Jobs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Response", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                            Text("${user.responseTimeMinutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Badges
                Text(
                    text = "Verified Skill Badges",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    user.badges.forEach { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(SkillBridgePrimarySoft)
                                .border(1.dp, SkillBridgeBorder, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏅 $badge",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgePrimaryHover
                            )
                        }
                    }
                }
            }
        }

        // 2. WORKER PRO MEMBERSHIP (WS-6: 10% Platform Fee Tier)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SkillBridge Pro Tier", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (user.isProMember) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SkillBridgeSuccess.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                            }
                        }
                    }
                    Text(
                        text = if (user.isProMember) "You enjoy 10% commission fee (5% discount) on all jobs." else "Upgrade to Pro: 10% commission fee + priority dispatch badge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeTextMuted
                    )
                }
                if (!user.isProMember) {
                    Button(
                        onClick = onUpgradePro,
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Upgrade", fontSize = 11.sp, color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. LOW DATA MODE & RESILIENCE (WS-7)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Low-Data Mode (2G / Low Memory)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Disables animated radar and maps for 2-GB-RAM devices & slow connections.", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Switch(
                    checked = isLowDataMode,
                    onCheckedChange = { onToggleLowDataMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = SkillBridgePrimary)
                )
            }
        }

        // 4. TRUSTED CONTACT LIVE LOCATION SHARING (WS-4)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Family Trusted Contact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Live location is automatically shared during night & emergency dispatches: ${user.trustedContactPhone}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                TextButton(onClick = { showEditContactDialog = true }) {
                    Text("Edit", color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. WORKER PORTFOLIO GALLERY (WS-6)
        if (portfolioItems.isNotEmpty()) {
            Text("Work Portfolio Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(portfolioItems) { item ->
                    Card(
                        modifier = Modifier.width(180.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(item.category, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = SkillBridgeTextMuted)
                        }
                    }
                }
            }
        }

        // 6. BUSINESS ACCOUNTS & TEAM DELEGATION (WS-6)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenBusinessTeam() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Business Account & Team Roles", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Manage company billing, dispatchers, and finance admins.", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgePrimaryHover)
            }
        }

        // 7. THREE EQUAL ONBOARDING PATHS SWITCHER
        Text(
            text = "Onboarding Flow Simulator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLaunchVoiceOnboarding,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft, contentColor = SkillBridgePrimaryHover)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Voice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLaunchFormOnboarding,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft, contentColor = SkillBridgePrimaryHover)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Form", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLaunchIvrOnboarding,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft, contentColor = SkillBridgePrimaryHover)
            ) {
                Icon(imageVector = Icons.Default.Dialpad, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("IVR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 8. LANGUAGE SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "App Language / भाषा / भाषा निवडा",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = user.language == lang
                        Button(
                            onClick = { onLanguageChange(lang) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) SkillBridgePrimary else SkillBridgeBg,
                                contentColor = if (isSelected) SkillBridgeWhite else SkillBridgeText
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Text(lang.nativeLabel, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 9. STAFF & ADMIN CONSOLE PORTAL (WS-1 RBAC Authorization)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenStaffLogin() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = SkillBridgePrimaryHover)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Staff Ops & Admin Portal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Requires authorized staff credentials (RBAC)", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SkillBridgeTextMuted)
            }
        }

        // REFER & EARN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showReferralDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Refer & Earn ₹200", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Invite artisans or clients, earn ₹200 on their first job", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgeTextMuted)
            }
        }

        // HELP CENTER & 24/7 SUPPORT
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showHelpCenterDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Help Center & 24/7 Support", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Create support tickets, dispute resolution & live escalation", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgeTextMuted)
            }
        }

        // DPDP ACT 2023 DATA ERASURE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDataErasureDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = SkillBridgeDanger)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("DPDP Act 2023 Data Erasure", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Request complete erasure of your personal & financial data", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgeTextMuted)
            }
        }

        // 10. FREE LEARNING HUB & MICRO-COURSES
        Text(
            text = "Learning Hub (Free Micro-Courses)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        learningCourses.forEach { course ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(course.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (course.isCompleted) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Completed", tint = SkillBridgeSuccess, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${course.durationMin} mins • Badge: ${course.rewardBadge}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                    }

                    Button(
                        onClick = { onCompleteCourse(course.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (course.isCompleted) SkillBridgeSuccess else SkillBridgePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (course.isCompleted) "Completed" else "Start Course", fontSize = 11.sp, color = SkillBridgeWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Share QR Dialog
    if (showShareQrDialog) {
        AlertDialog(
            onDismissRequest = { showShareQrDialog = false },
            title = {
                Text("Verified Skill Passport QR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Share this verified QR code with prospective clients or employers.", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkillBridgePrimarySoft)
                            .border(2.dp, SkillBridgePrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = SkillBridgePrimaryHover, modifier = Modifier.size(110.dp))
                            Text("skillbridge.in/p/${user.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Govt. KYC Verified • 94% Trust Score", style = MaterialTheme.typography.labelSmall, color = SkillBridgeSuccess, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showShareQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text("Done", color = SkillBridgeWhite)
                }
            },
            containerColor = SkillBridgeBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Contact Dialog
    if (showEditContactDialog) {
        AlertDialog(
            onDismissRequest = { showEditContactDialog = false },
            title = { Text("Update Trusted Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter emergency contact phone number for automated live alerts:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = { contactInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                ) {
                    Text("Save Contact", color = SkillBridgeWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditContactDialog = false }) { Text("Cancel") }
            },
            containerColor = SkillBridgeBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showHelpCenterDialog) {
        HelpCenterDialog(
            onSubmitTicket = { _, _ ->
                showHelpCenterDialog = false
            },
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
            onConfirmErasure = {
                showDataErasureDialog = false
            },
            onDismiss = { showDataErasureDialog = false }
        )
    }
}
