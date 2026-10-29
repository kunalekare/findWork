package com.example.skillbridge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

private val AdminCardShape = RoundedCornerShape(20.dp)
private val AdminBadgeShape = RoundedCornerShape(999.dp)

enum class AdminTab(val label: String) {
    LIVE_OPS("Live Ops"),
    KYC_QUEUE("KYC Verification"),
    DISPUTES("Disputes"),
    CATEGORIES("Categories")
}

@Composable
fun AdminConsoleScreen(
    jobs: List<Job>,
    kycList: List<KycVerificationItem>,
    disputes: List<DisputeRecord>,
    categories: List<Category>,
    onApproveKyc: (String) -> Unit,
    onRejectKyc: (String) -> Unit,
    onResolveDispute: (String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.LIVE_OPS) }

    val pendingKycCount = kycList.count { it.status == "PENDING" }
    val openDisputesCount = disputes.count { it.status == "OPEN" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AdminHeader(
                pendingKycCount = pendingKycCount,
                openDisputesCount = openDisputesCount
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SkillBridgeSurfaceMuted)
                    .padding(5.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdminTab.values().forEach { tab ->
                    val selected = selectedTab == tab
                    val count = when (tab) {
                        AdminTab.KYC_QUEUE -> pendingKycCount
                        AdminTab.DISPUTES -> openDisputesCount
                        else -> 0
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedTab = tab },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) SkillBridgeWhite else Color.Transparent,
                        border = if (selected) BorderStroke(1.dp, SkillBridgeBorder) else null,
                        shadowElevation = if (selected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                maxLines = 1,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) SkillBridgePrimary else SkillBridgeTextMuted
                            )
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 19.dp, minHeight = 19.dp)
                                        .clip(CircleShape)
                                        .background(SkillBridgeDanger)
                                        .padding(horizontal = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (selectedTab) {
            AdminTab.LIVE_OPS -> {
                val activeJobsCount = jobs.count {
                    it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
                }
                val totalGmv = jobs.sumOf { it.budget }
                val totalCommission = (totalGmv * 0.15).toInt()

                item {
                    SectionHeading(
                        title = "Operations overview",
                        subtitle = "A quick snapshot of marketplace activity"
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Active dispatches",
                                value = activeJobsCount.toString(),
                                icon = Icons.Default.Bolt,
                                tint = SkillBridgePrimary
                            )
                            MetricStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Total jobs GMV",
                                value = "₹$totalGmv",
                                icon = Icons.Default.CurrencyRupee,
                                tint = SkillBridgeSuccess
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Platform fee (15%)",
                                value = "₹$totalCommission",
                                icon = Icons.Default.AccountBalance,
                                tint = SkillBridgeWarning
                            )
                            MetricStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Open disputes",
                                value = openDisputesCount.toString(),
                                icon = Icons.Default.WarningAmber,
                                tint = SkillBridgeDanger
                            )
                        }
                    }
                }

                item {
                    SectionHeading(
                        title = "Live marketplace activity",
                        subtitle = "${jobs.size} total job${if (jobs.size == 1) "" else "s"}"
                    )
                }

                if (jobs.isEmpty()) {
                    item {
                        EmptyAdminState(
                            icon = Icons.Default.Bolt,
                            title = "No marketplace activity yet",
                            message = "New jobs will appear here when they are available."
                        )
                    }
                } else {
                    items(jobs.take(10), key = { it.id }) { job ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AdminCardShape,
                            color = SkillBridgeWhite,
                            border = BorderStroke(1.dp, SkillBridgeBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SkillBridgePrimarySoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = SkillBridgePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = job.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = job.locationAddress.ifEmpty { job.locationCity },
                                        fontSize = 12.sp,
                                        color = SkillBridgeTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "₹${job.budget}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText
                                    )
                                }
                                StatusBadge(status = job.status)
                            }
                        }
                    }
                }
            }

            AdminTab.KYC_QUEUE -> {
                val pendingKyc = kycList.filter { it.status == "PENDING" }
                item {
                    SectionHeading(
                        title = "KYC verification",
                        subtitle = if (pendingKyc.isEmpty()) {
                            "All submitted requests have been reviewed"
                        } else {
                            "$pendingKycCount request${if (pendingKycCount == 1) "" else "s"} need review"
                        }
                    )
                }
                if (pendingKyc.isEmpty()) {
                    item {
                        EmptyAdminState(
                            icon = Icons.Default.VerifiedUser,
                            title = "You're all caught up",
                            message = "There are no pending identity verification requests.",
                            accent = SkillBridgeSuccess
                        )
                    }
                } else {
                    items(pendingKyc, key = { it.id }) { kyc ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AdminCardShape,
                            color = SkillBridgeWhite,
                            border = BorderStroke(1.dp, SkillBridgeBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(SkillBridgePrimarySoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.VerifiedUser,
                                            contentDescription = null,
                                            tint = SkillBridgePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = kyc.userName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SkillBridgeText
                                        )
                                        Text(
                                            text = kyc.documentType,
                                            fontSize = 12.sp,
                                            color = SkillBridgeTextMuted
                                        )
                                        Text(
                                            text = "Document: ${kyc.docNumberMasked}",
                                            fontSize = 12.sp,
                                            color = SkillBridgeTextMuted
                                        )
                                    }
                                    StatusPill(
                                        label = "Pending",
                                        color = SkillBridgeWarning
                                    )
                                }
                                HorizontalDivider(color = SkillBridgeBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onRejectKyc(kyc.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(13.dp),
                                        border = BorderStroke(1.dp, SkillBridgeDanger),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = SkillBridgeDanger
                                        )
                                    ) {
                                        Text("Reject", fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = { onApproveKyc(kyc.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(13.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SkillBridgeSuccess
                                        )
                                    ) {
                                        Text(
                                            "Approve",
                                            fontWeight = FontWeight.SemiBold,
                                            color = SkillBridgeWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.DISPUTES -> {
                val openDisputes = disputes.filter { it.status == "OPEN" }
                item {
                    SectionHeading(
                        title = "Dispute resolution",
                        subtitle = if (openDisputes.isEmpty()) {
                            "No open disputes require action"
                        } else {
                            "${openDisputes.size} case${if (openDisputes.size == 1) "" else "s"} awaiting resolution"
                        }
                    )
                }
                if (openDisputes.isEmpty()) {
                    item {
                        EmptyAdminState(
                            icon = Icons.Default.Shield,
                            title = "No open disputes",
                            message = "There are no pending disputes to resolve.",
                            accent = SkillBridgeSuccess
                        )
                    }
                } else {
                    items(openDisputes, key = { it.id }) { dispute ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AdminCardShape,
                            color = SkillBridgeWhite,
                            border = BorderStroke(1.dp, SkillBridgeBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(13.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(SkillBridgeDanger.copy(alpha = 0.10f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Gavel,
                                            contentDescription = null,
                                            tint = SkillBridgeDanger,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Dispute #${dispute.id.take(8)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SkillBridgeText
                                        )
                                        Text(
                                            text = dispute.jobTitle,
                                            fontSize = 12.sp,
                                            color = SkillBridgeTextMuted,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Escrow amount: ₹${dispute.escrowAmount}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SkillBridgeText
                                        )
                                    }
                                    StatusPill(label = "Open", color = SkillBridgeDanger)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SkillBridgeSurfaceMuted
                                ) {
                                    Text(
                                        text = dispute.reason,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = SkillBridgeText
                                    )
                                }
                                HorizontalDivider(color = SkillBridgeBorder)
                                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                    Button(
                                        onClick = { onResolveDispute(dispute.id, false) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(13.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SkillBridgePrimary
                                        )
                                    ) {
                                        Text(
                                            "Refund to task poster",
                                            fontWeight = FontWeight.SemiBold,
                                            color = SkillBridgeWhite
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onResolveDispute(dispute.id, true) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(13.dp),
                                        border = BorderStroke(1.dp, SkillBridgeSuccess),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = SkillBridgeSuccess
                                        )
                                    ) {
                                        Text(
                                            "Release funds to worker",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.CATEGORIES -> {
                item {
                    SectionHeading(
                        title = "Service categories",
                        subtitle = "${categories.size} configured categor${if (categories.size == 1) "y" else "ies"}"
                    )
                }
                if (categories.isEmpty()) {
                    item {
                        EmptyAdminState(
                            icon = Icons.Default.Category,
                            title = "No categories available",
                            message = "Configured service categories will appear here."
                        )
                    }
                } else {
                    items(categories, key = { it.id }) { category ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AdminCardShape,
                            color = SkillBridgeWhite,
                            border = BorderStroke(1.dp, SkillBridgeBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(13.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(SkillBridgePrimarySoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = SkillBridgePrimary,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = category.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (category.type == WorkType.LOCAL_ON_DEMAND) {
                                            "Local on-demand"
                                        } else {
                                            "Skilled project"
                                        },
                                        fontSize = 12.sp,
                                        color = SkillBridgeTextMuted
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "BASE PRICE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeTextMuted
                                    )
                                    Text(
                                        text = "₹${category.basePrice}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SkillBridgeTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(
    pendingKycCount: Int,
    openDisputesCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Admin console",
                    fontSize = 27.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText
                )
                Text(
                    text = "Monitor operations, review KYC, and resolve disputes.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = SkillBridgeTextMuted
                )
            }
            Surface(
                shape = AdminBadgeShape,
                color = SkillBridgeDanger.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SkillBridgeDanger,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "STAFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SkillBridgeDanger
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = SkillBridgeWhite,
            border = BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SkillBridgePrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = SkillBridgePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Review queue",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText
                    )
                    Text(
                        text = "$pendingKycCount KYC • $openDisputesCount disputes need attention",
                        fontSize = 11.sp,
                        color = SkillBridgeTextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SkillBridgeTextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Bold,
            color = SkillBridgeText
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = SkillBridgeTextMuted
        )
    }
}

@Composable
private fun MetricStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = AdminCardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = SkillBridgeTextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    color: Color
) {
    Surface(
        shape = AdminBadgeShape,
        color = color.copy(alpha = 0.11f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyAdminState(
    icon: ImageVector,
    title: String,
    message: String,
    accent: Color = SkillBridgePrimary
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AdminCardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SkillBridgeText
            )
            Text(
                text = message,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = SkillBridgeTextMuted
            )
        }
    }
}
