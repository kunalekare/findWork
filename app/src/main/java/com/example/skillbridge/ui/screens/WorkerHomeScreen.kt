package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.IncomingRequestCard
import com.example.skillbridge.ui.theme.*

private val WorkerCardShape = RoundedCornerShape(20.dp)

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

    val filteredJobs = remember(availableJobs, selectedFilter) {
        availableJobs.filter { job ->
            when (selectedFilter) {
                "Urgent Local" -> job.type == WorkType.LOCAL_ON_DEMAND
                "Skilled Project" -> job.type == WorkType.PROJECT_SKILLED
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Good to see you, ${worker.name.substringBefore(" ")}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText
                )
                Text(
                    text = "Your next opportunity is just around the corner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkillBridgeTextMuted
                )
            }
        }

        item {
            WorkerStatusCard(worker = worker, onToggleOnline = onToggleOnline)
        }

        if (incomingRequest != null && worker.isOnline) {
            item {
                IncomingRequestCard(
                    job = incomingRequest,
                    onAccept = { onAcceptIncoming(incomingRequest.id) },
                    onReject = onRejectIncoming
                )
            }
        }

        item {
            EarningsOverviewCard(worker = worker)
        }

        item {
            VerificationCard(onClick = onStartVerificationQuiz)
        }

        item {
            LearningHubCard(onClick = onViewLearningHub)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Opportunities for you",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SkillBridgeText
                        )
                        Text(
                            text = "Matched to your skills and location",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeTextMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SkillBridgePrimarySoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(15.dp))
                            Text("${filteredJobs.size}", fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover, fontSize = 13.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Urgent Local", "Skilled Project").forEach { filter ->
                        val selected = selectedFilter == filter
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilter = filter },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) SkillBridgePrimary else SkillBridgeSurfaceMuted,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) SkillBridgePrimary else SkillBridgeBorder
                            )
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) SkillBridgeWhite else SkillBridgeTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (filteredJobs.isEmpty()) {
            item {
                EmptyOpportunitiesCard(selectedFilter = selectedFilter)
            }
        } else {
            items(filteredJobs, key = { it.id }) { job ->
                WorkerJobCard(
                    job = job,
                    onViewDetails = { onJobClick(job) },
                    onApply = { onApplyJobClick(job) }
                )
            }
        }
    }
}

@Composable
private fun WorkerStatusCard(worker: User, onToggleOnline: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, WorkerCardShape)
            .testTag("worker_status_header_card"),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(if (worker.isOnline) SkillBridgeSuccess else SkillBridgeBorder)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SkillBridgePrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(worker.avatarEmoji, fontSize = 30.sp)
                }
                Spacer(Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            worker.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (worker.isKycVerified) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = SkillBridgeSuccess, modifier = Modifier.size(17.dp))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(if (worker.isOnline) SkillBridgeSuccess else SkillBridgeTextMuted))
                        Text(
                            if (worker.isOnline) "You're online and discoverable" else "You're currently offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (worker.isOnline) SkillBridgeSuccess else SkillBridgeTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(14.dp))
                        Text(worker.locationCity, style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Switch(
                    checked = worker.isOnline,
                    onCheckedChange = onToggleOnline,
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
}

@Composable
private fun EarningsOverviewCard(worker: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeText),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(SkillBridgeWhite.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SkillBridgeWhite, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your work snapshot", color = SkillBridgeWhite.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                    Text("Wallet & performance", color = SkillBridgeWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(50), color = SkillBridgePrimary) {
                    Text("15% fee", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = SkillBridgeWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "₹${worker.walletBalance}",
                color = SkillBridgeWhite,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("Available wallet balance", color = SkillBridgeWhite.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = SkillBridgeWhite.copy(alpha = 0.18f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricColumn("Jobs completed", worker.jobsCompleted.toString(), Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(42.dp).background(SkillBridgeWhite.copy(alpha = 0.2f)))
                MetricColumn("Trust score", "${worker.trustScore}%", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value, color = SkillBridgeWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(label, color = SkillBridgeWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun VerificationCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("verification_quiz_card"),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgePrimarySoft),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgePrimary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(SkillBridgePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = SkillBridgeWhite, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Stand out from the crowd", fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover, style = MaterialTheme.typography.titleSmall)
                Text("Complete the skill quiz to work toward a verified badge and better job visibility.", color = SkillBridgeText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Take skill quiz", color = SkillBridgePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LearningHubCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(SkillBridgeSurfaceMuted), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Learning hub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                Text("Build skills and unlock more opportunities", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Open learning hub", tint = SkillBridgeTextMuted)
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
            .shadow(2.dp, WorkerCardShape)
            .clickable { onViewDetails() }
            .testTag("worker_job_card_${job.id}"),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (job.type == WorkType.LOCAL_ON_DEMAND) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (job.type == WorkType.LOCAL_ON_DEMAND) Icons.Default.Bolt else Icons.Default.Work,
                        contentDescription = null,
                        tint = SkillBridgePrimary,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = if (job.type == WorkType.LOCAL_ON_DEMAND) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted
                    ) {
                        Text(
                            text = if (job.type == WorkType.LOCAL_ON_DEMAND) "LOCAL • ${job.urgency}" else "SKILLED PROJECT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgePrimaryHover
                        )
                    }
                    Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(job.categoryName, style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("₹${job.budget}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = SkillBridgeSuccess)
                    Text("Budget", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(16.dp))
                Text(job.locationAddress, style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            if (job.matchExplanation.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SkillBridgeSurfaceMuted).padding(11.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(17.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Why this matches", color = SkillBridgePrimaryHover, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(job.matchExplanation, color = SkillBridgeText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(13.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeText)
                ) {
                    Text("Details", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1.35f).height(46.dp).testTag("apply_job_button_${job.id}"),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text(
                        if (job.type == WorkType.LOCAL_ON_DEMAND) "Accept · ₹${(job.budget * 0.85).toInt()}" else "Send proposal",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyOpportunitiesCard(selectedFilter: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = WorkerCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Work, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(32.dp))
            Text("No opportunities found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeText)
            Text(
                if (selectedFilter == "All") "New jobs will appear here when they become available." else "Try another filter to see more available jobs.",
                style = MaterialTheme.typography.bodySmall,
                color = SkillBridgeTextMuted
            )
        }
    }
}
