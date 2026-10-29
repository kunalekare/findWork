package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

@Composable
fun OrdersListScreen(
    jobs: List<Job>,
    proposals: List<Proposal>,
    onJobClick: (Job) -> Unit,
    onViewProposal: (Proposal) -> Unit,
    onCompareProposals: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Orders", "Active", "Completed", "Proposals")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Jobs & Orders Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Real-time status, proposals & history",
            style = MaterialTheme.typography.bodySmall,
            color = SkillBridgeTextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SkillBridgeSurfaceMuted,
            contentColor = SkillBridgePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SkillBridgePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) SkillBridgePrimaryHover else SkillBridgeTextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredJobs = jobs.filter { job ->
            when (selectedTab) {
                1 -> job.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
                2 -> job.status == JobStatus.COMPLETED
                else -> true
            }
        }

        if (selectedTab == 3) {
            // Proposals list
            if (proposals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No submitted proposals yet.", color = SkillBridgeTextMuted)
                }
            } else {
                Column {
                    if (proposals.size >= 2) {
                        Button(
                            onClick = onCompareProposals,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(bottom = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimarySoft),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Compare Proposals Side-by-Side", color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(proposals) { proposal ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(1.dp, RoundedCornerShape(12.dp))
                                    .clickable { onViewProposal(proposal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${proposal.workerAvatar} ${proposal.workerName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "₹${proposal.quotedPrice}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SkillBridgeSuccess
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = proposal.coverNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Application Tracker Stages (Applied -> Shortlisted -> Interview -> Hired)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SkillBridgeSurfaceMuted)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        listOf("Applied", "Shortlist", "Interview", "Hired").forEachIndexed { idx, stage ->
                                            val isReached = when (proposal.status) {
                                                "HIRED" -> true
                                                "SHORTLISTED" -> idx <= 1
                                                else -> idx == 0
                                            }
                                            Text(
                                                text = if (isReached) "✓ $stage" else "○ $stage",
                                                fontSize = 9.sp,
                                                fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isReached) SkillBridgePrimaryHover else SkillBridgeTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Delivery in ${proposal.daysToDeliver} days",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SkillBridgeTextMuted
                                        )
                                        Text(
                                            text = "Status: ${proposal.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (proposal.status == "HIRED") SkillBridgeSuccess else SkillBridgePrimaryHover
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Jobs list
            if (filteredJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders in this section.", color = SkillBridgeTextMuted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredJobs) { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(12.dp))
                                .clickable { onJobClick(job) }
                                .testTag("order_item_${job.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (job.status) {
                                                        JobStatus.COMPLETED -> SkillBridgeSuccess.copy(alpha = 0.12f)
                                                        JobStatus.IN_PROGRESS -> SkillBridgePrimarySoft
                                                        else -> SkillBridgeSurfaceMuted
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = job.status.name.replace("_", " "),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (job.status) {
                                                    JobStatus.COMPLETED -> SkillBridgeSuccess
                                                    JobStatus.IN_PROGRESS -> SkillBridgePrimaryHover
                                                    else -> SkillBridgeText
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = job.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "₹${job.budget}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📍 ${job.locationCity} • ${job.createdAt}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SkillBridgeTextMuted
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = SkillBridgePrimary
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
