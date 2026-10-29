package com.example.skillbridge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

private val CardShape = RoundedCornerShape(18.dp)
private val BadgeShape = RoundedCornerShape(8.dp)

enum class OrderTab(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    PROPOSALS("Proposals")
}

@Composable
fun OrdersListScreen(
    jobs: List<Job>,
    proposals: List<Proposal>,
    onJobClick: (Job) -> Unit,
    onViewProposal: (Proposal) -> Unit,
    onCompareProposals: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(OrderTab.ALL) }

    val filteredJobs = remember(jobs, selectedTab) {
        when (selectedTab) {
            OrderTab.ALL -> jobs
            OrderTab.ACTIVE -> jobs.filter {
                it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
            }
            OrderTab.COMPLETED -> jobs.filter {
                it.status == JobStatus.COMPLETED
            }
            OrderTab.PROPOSALS -> emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Orders & Gigs",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText
                )
                Text(
                    "Track real-time dispatches, milestones, and proposal bids.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = SkillBridgeTextMuted
                )
            }
        }

        // Segmented Tabs
        // Segmented Tabs — aligned labels and count badges
item {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SkillBridgeSurfaceMuted,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderTab.values().forEach { tab ->
                val isSelected = selectedTab == tab

                val count = when (tab) {
                    OrderTab.ALL -> jobs.size
                    OrderTab.ACTIVE -> jobs.count {
                        it.status !in listOf(
                            JobStatus.COMPLETED,
                            JobStatus.CANCELLED
                        )
                    }
                    OrderTab.COMPLETED -> jobs.count {
                        it.status == JobStatus.COMPLETED
                    }
                    OrderTab.PROPOSALS -> proposals.size
                }

                Surface(
                    onClick = { selectedTab = tab },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 42.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        SkillBridgeWhite
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (isSelected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            color = if (isSelected) {
                                SkillBridgePrimary
                            } else {
                                SkillBridgeTextMuted
                            },
                            maxLines = 1,
                            softWrap = false
                        )

                        if (count > 0) {
                            Spacer(modifier = Modifier.width(4.dp))

                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            SkillBridgePrimary
                                        } else {
                                            SkillBridgeTextMuted.copy(
                                                alpha = 0.18f
                                            )
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        SkillBridgeWhite
                                    } else {
                                        SkillBridgeText
                                    },
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

        if (selectedTab == OrderTab.PROPOSALS) {
            if (proposals.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        color = SkillBridgePrimarySoft,
                        border = BorderStroke(1.dp, SkillBridgePrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Compare Proposals",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillBridgeText
                                )
                                Text(
                                    "AI-powered quote, trust score, and portfolio comparison",
                                    fontSize = 12.sp,
                                    color = SkillBridgeTextMuted
                                )
                            }
                            Button(
                                onClick = onCompareProposals,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Compare", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(proposals, key = { it.id }) { proposal ->
                    ProposalItemCard(
                        proposal = proposal,
                        onViewClick = { onViewProposal(proposal) }
                    )
                }
            } else {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Description,
                        title = "No Proposals Yet",
                        message = "Post a project to receive bids and structured milestone proposals from verified professionals."
                    )
                }
            }
        } else {
            if (filteredJobs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.WorkOutline,
                        title = "No Orders in ${selectedTab.label}",
                        message = "When you request on-demand services or post project tasks, they will appear here in real-time."
                    )
                }
            } else {
                items(filteredJobs, key = { it.id }) { job ->
                    OrderItemCard(
                        job = job,
                        onClick = { onJobClick(job) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    job: Job,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_item_${job.id}"),
        shape = CardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (job.type == WorkType.LOCAL_ON_DEMAND) SkillBridgePrimarySoft
                                else SkillBridgeSurfaceMuted
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (job.type == WorkType.LOCAL_ON_DEMAND) Icons.Default.ElectricBolt
                            else Icons.Default.Handyman,
                            contentDescription = null,
                            tint = SkillBridgePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            job.categoryName.ifEmpty { "Service" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            if (job.type == WorkType.LOCAL_ON_DEMAND) "On-Demand Express" else "Milestone Project",
                            fontSize = 10.sp,
                            color = SkillBridgePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                StatusBadge(status = job.status)
            }

            Text(
                job.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SkillBridgeText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (job.description.isNotBlank()) {
                Text(
                    job.description,
                    fontSize = 13.sp,
                    color = SkillBridgeTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = SkillBridgeBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SkillBridgeTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        job.locationAddress.ifEmpty { job.locationCity },
                        fontSize = 12.sp,
                        color = SkillBridgeTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${job.budget}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SkillBridgeText
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Open Order",
                        tint = SkillBridgePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProposalItemCard(
    proposal: Proposal,
    onViewClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick),
        shape = CardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SkillBridgePrimarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            proposal.workerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgePrimary,
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            proposal.workerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeText
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SkillBridgeWarning, modifier = Modifier.size(14.dp))
                            Text("${proposal.workerRating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SkillBridgeText)
                            Text("• ${proposal.daysToDeliver} days", fontSize = 12.sp, color = SkillBridgeTextMuted)
                        }
                    }
                }

                Surface(
                    shape = BadgeShape,
                    color = SkillBridgeSuccess.copy(alpha = 0.12f)
                ) {
                    Text(
                        "${proposal.workerTrustScore} Trust",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                proposal.coverNote,
                fontSize = 13.sp,
                color = SkillBridgeTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(color = SkillBridgeBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BID QUOTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SkillBridgeTextMuted)
                    Text("₹${proposal.quotedPrice}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText)
                }

                Button(
                    onClick = onViewClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Review Proposal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: JobStatus) {
    val (bgColor, textColor, label) = when (status) {
        JobStatus.REQUESTED -> Triple(SkillBridgeSurfaceMuted, SkillBridgePrimary, "Requested")
        JobStatus.MATCHED -> Triple(SkillBridgePrimarySoft, SkillBridgePrimary, "Matched")
        JobStatus.EN_ROUTE -> Triple(SkillBridgePrimarySoft, SkillBridgePrimary, "En Route")
        JobStatus.ARRIVED -> Triple(SkillBridgePrimarySoft, SkillBridgePrimary, "Arrived")
        JobStatus.IN_PROGRESS -> Triple(SkillBridgePrimarySoft, SkillBridgePrimary, "In Progress")
        JobStatus.COMPLETED -> Triple(SkillBridgeSuccess.copy(alpha = 0.12f), SkillBridgeSuccess, "Completed")
        JobStatus.CANCELLED -> Triple(SkillBridgeBorder, SkillBridgeTextMuted, "Cancelled")
        JobStatus.DISPUTED -> Triple(SkillBridgeDanger.copy(alpha = 0.12f), SkillBridgeDanger, "Disputed")
        JobStatus.POSTED -> Triple(SkillBridgeSurfaceMuted, SkillBridgeText, "Open")
        JobStatus.PROPOSALS -> Triple(SkillBridgeSurfaceMuted, SkillBridgePrimary, "Proposals")
        JobStatus.HIRED -> Triple(SkillBridgePrimarySoft, SkillBridgePrimary, "Hired")
        JobStatus.DELIVERED -> Triple(SkillBridgeSuccess.copy(alpha = 0.12f), SkillBridgeSuccess, "Delivered")
    }

    Surface(
        shape = BadgeShape,
        color = bgColor
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = CardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SkillBridgeSurfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = SkillBridgePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SkillBridgeText
            )
            Text(
                message,
                fontSize = 13.sp,
                color = SkillBridgeTextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}