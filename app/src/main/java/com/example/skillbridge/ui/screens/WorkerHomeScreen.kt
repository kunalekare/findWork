package com.example.skillbridge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.IncomingRequestCard
import com.example.skillbridge.ui.theme.*

@Composable
fun WorkerHomeScreen(
    worker: User,
    incomingRequest: Job?,
    availableJobs: List<Job>,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptIncoming: (String) -> Unit,
    onRejectIncoming: () -> Unit,
    onJobClick: (Job) -> Unit,
    onApplyJobClick: (Job) -> Unit,
    onStartVerificationQuiz: () -> Unit,
    onViewLearningHub: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ONLINE / OFFLINE TOGGLE & WORKER STATUS HEADER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .testTag("worker_status_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = worker.avatarEmoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = worker.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (worker.isKycVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = SkillBridgeSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (worker.isOnline) "🟢 Ready for Immediate Jobs in ${worker.locationCity}" else "⚪ You are Offline (No dispatch calls)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (worker.isOnline) SkillBridgeSuccess else SkillBridgeTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Online / Offline Switch
                    Switch(
                        checked = worker.isOnline,
                        onCheckedChange = { onToggleOnline(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SkillBridgeWhite,
                            checkedTrackColor = SkillBridgeSuccess,
                            uncheckedThumbColor = SkillBridgeTextMuted,
                            uncheckedTrackColor = SkillBridgeBorder
                        ),
                        modifier = Modifier.testTag("worker_online_switch")
                    )
                }
            }
        }

        // 2. INCOMING REQUEST CARD (20s Countdown Ring)
        if (incomingRequest != null && worker.isOnline) {
            item {
                IncomingRequestCard(
                    job = incomingRequest,
                    onAccept = { onAcceptIncoming(incomingRequest.id) },
                    onReject = onRejectIncoming
                )
            }
        }

        // 3. TODAY'S EARNINGS & PERFORMANCE METRICS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Earnings & Balance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(SkillBridgePrimarySoft)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "15% Fee Model",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgePrimaryHover
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Wallet Balance", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                            Text(
                                text = "₹${worker.walletBalance}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeText
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jobs Done", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                            Text(
                                text = "${worker.jobsCompleted}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgePrimaryHover
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trust Score", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                            Text(
                                text = "${worker.trustScore}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeSuccess
                            )
                        }
                    }
                }
            }
        }

        // 4. SKILL VERIFICATION & BADGE UNLOCK CALLOUT
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartVerificationQuiz() }
                    .testTag("verification_quiz_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgePrimarySoft),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkillBridgePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = SkillBridgeWhite, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Boost Your Jobs: Take 2-Min Skill Quiz",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgePrimaryHover
                        )
                        Text(
                            text = "Get verified badge & priority dispatch in your local cluster.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = SkillBridgeText
                        )
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgePrimary)
                }
            }
        }

        // 5. AI OPPORTUNITY FEED (Ranked jobs with explainability)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Opportunity Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ranked for your skills & location",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeTextMuted
                    )
                }

                // Filter pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Urgent Local", "Skilled Project").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (isSelected) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                                .border(1.dp, if (isSelected) SkillBridgePrimary else SkillBridgeBorder, RoundedCornerShape(999.dp))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SkillBridgePrimaryHover else SkillBridgeTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Filtered jobs list
        val filteredJobs = availableJobs.filter { job ->
            when (selectedFilter) {
                "Urgent Local" -> job.type == WorkType.LOCAL_ON_DEMAND
                "Skilled Project" -> job.type == WorkType.PROJECT_SKILLED
                else -> true
            }
        }

        items(filteredJobs) { job ->
            WorkerJobCard(
                job = job,
                onViewDetails = { onJobClick(job) },
                onApply = { onApplyJobClick(job) }
            )
        }
    }
}

@Composable
fun WorkerJobCard(
    job: Job,
    onViewDetails: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clickable { onViewDetails() }
            .testTag("worker_job_card_${job.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (job.type == WorkType.LOCAL_ON_DEMAND) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (job.type == WorkType.LOCAL_ON_DEMAND) "⚡ ${job.categoryName} • ${job.urgency}" else "💼 Skilled Project • ${job.categoryName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgePrimaryHover
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${job.budget}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess
                    )
                    Text(
                        text = "Escrow Held",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = SkillBridgeTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📍 ${job.locationAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = SkillBridgeTextMuted
            )

            // Plain-Language Match Explanation
            if (job.matchExplanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🎯 ${job.matchExplanation}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = SkillBridgePrimaryHover
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgePrimaryHover)
                ) {
                    Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag("apply_job_button_${job.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text(if (job.type == WorkType.LOCAL_ON_DEMAND) "Accept (₹${(job.budget * 0.85).toInt()})" else "Send Proposal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
