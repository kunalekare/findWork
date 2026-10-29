package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

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
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        "Live Ops",
        "KYC Queue (${kycList.count { it.status == "PENDING" }})",
        "Disputes (${disputes.count { it.status == "OPEN" }})",
        "Categories",
        "Heatmap",
        "Broadcast"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SkillBridge Ops Console",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "RBAC Role: ADMIN & DISPATCH SUPERVISOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgePrimaryHover,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(SkillBridgePrimarySoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Nagpur Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = SkillBridgeSurfaceMuted,
            contentColor = SkillBridgePrimary,
            edgePadding = 0.dp
        ) {
            sections.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSection == idx) SkillBridgePrimaryHover else SkillBridgeTextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSection) {
            0 -> {
                // Live Ops Metrics
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Active Jobs", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                                    Text("${jobs.count { it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED) }}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Escrow Vault", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                                    Text("₹${jobs.sumOf { it.budget }}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                                }
                            }
                        }
                    }

                    item {
                        Text("Active Real-Time Dispatch Jobs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(jobs) { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(job.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("₹${job.budget}", style = MaterialTheme.typography.titleSmall, color = SkillBridgeSuccess, fontWeight = FontWeight.Bold)
                                }
                                Text("Poster: ${job.taskPosterName} • Worker: ${job.workerName ?: "Awaiting"}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                                Text("Status: ${job.status.name} • Location: ${job.locationCity}", style = MaterialTheme.typography.labelSmall, color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            1 -> {
                // KYC Queue
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(kycList) { kyc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Text(kyc.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (kyc.status == "APPROVED") SkillBridgeSuccess.copy(alpha = 0.12f) else SkillBridgePrimarySoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(kyc.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (kyc.status == "APPROVED") SkillBridgeSuccess else SkillBridgePrimaryHover)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Doc: ${kyc.documentType} • ${kyc.docNumberMasked}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeText)
                                Text("Submitted: ${kyc.submittedAt}", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = SkillBridgeTextMuted)

                                if (kyc.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onRejectKyc(kyc.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeDanger)
                                        ) {
                                            Text("Reject Doc")
                                        }
                                        Button(
                                            onClick = { onApproveKyc(kyc.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeSuccess)
                                        ) {
                                            Text("Verify & Approve", color = SkillBridgeWhite)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Dispute Console
                if (disputes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active disputes.", color = SkillBridgeTextMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(disputes) { dispute ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Dispute #${dispute.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SkillBridgeDanger)
                                        Text("Escrow: ₹${dispute.escrowAmount}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(dispute.jobTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Raised by: ${dispute.raisedByName} (${dispute.raisedByRole})", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reason: \"${dispute.reason}\"", style = MaterialTheme.typography.bodySmall, color = SkillBridgeText)

                                    if (dispute.status == "OPEN") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onResolveDispute(dispute.id, false) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeText)
                                            ) {
                                                Text("Refund Employer", fontSize = 11.sp, color = SkillBridgeWhite)
                                            }
                                            Button(
                                                onClick = { onResolveDispute(dispute.id, true) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                                            ) {
                                                Text("Release to Worker", fontSize = 11.sp, color = SkillBridgeWhite)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Status: RESOLVED", style = MaterialTheme.typography.labelSmall, color = SkillBridgeSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Categories & Pricing Management
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(categories) { cat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${cat.name} (${cat.nameHi})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Base Rate: ₹${cat.basePrice} ${cat.priceUnit} • Type: ${cat.type.name}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                                }
                                if (cat.isHotspot) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SkillBridgePrimarySoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Demand Surge", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Demand & Supply City Heatmap with live city filtering
                val availableCities = listOf("Nagpur", "Mumbai", "Pune", "Delhi NCR")
                var selectedHeatmapCity by remember { mutableStateOf("Nagpur") }

                val cityJobs = jobs.filter {
                    it.locationCity.equals(selectedHeatmapCity, ignoreCase = true) ||
                    (selectedHeatmapCity == "Delhi NCR" && (it.locationCity.contains("Delhi") || it.locationCity.contains("Noida") || it.locationCity.contains("Gurugram")))
                }
                val activeCityRequests = cityJobs.count { it.status != JobStatus.COMPLETED && it.status != JobStatus.CANCELLED }

                val zones = when (selectedHeatmapCity) {
                    "Mumbai" -> listOf(
                        Triple("Bandra West", "${activeCityRequests + 14} Requests • 12 Active Workers", "1.5x Surge"),
                        Triple("Andheri East Metro", "${activeCityRequests + 24} Requests • 16 Active Workers", "1.7x High Surge"),
                        Triple("Dadar Central", "9 Requests • 14 Active Workers", "Normal 1.0x"),
                        Triple("Powai Tech Park", "18 Requests • 10 Active Workers", "1.4x Surge")
                    )
                    "Pune" -> listOf(
                        Triple("Hinjewadi IT Phase 1", "${activeCityRequests + 19} Requests • 9 Active Workers", "1.6x High Surge"),
                        Triple("Kothrud Paud Road", "11 Requests • 12 Active Workers", "Normal 1.0x"),
                        Triple("Viman Nagar Square", "15 Requests • 8 Active Workers", "1.4x Surge"),
                        Triple("Baner High Street", "8 Requests • 11 Active Workers", "Normal 1.0x")
                    )
                    "Delhi NCR" -> listOf(
                        Triple("Connaught Place Outer", "${activeCityRequests + 21} Requests • 11 Active Workers", "1.6x High Surge"),
                        Triple("Cyber City Gurugram", "28 Requests • 14 Active Workers", "1.8x High Surge"),
                        Triple("Noida Sector 62", "16 Requests • 15 Active Workers", "Normal 1.0x"),
                        Triple("South Extension Market", "12 Requests • 9 Active Workers", "1.3x Surge")
                    )
                    else -> listOf(
                        Triple("Dharampeth West", "${activeCityRequests + 8} Requests • 8 Active Workers", if (activeCityRequests > 0) "1.5x Surge" else "1.2x Surge"),
                        Triple("Sitabuldi Metro Hub", "12 Requests • 14 Active Workers", "Normal 1.0x"),
                        Triple("Ramdaspeth Medical Square", "${activeCityRequests + 12} Requests • 6 Active Workers", "1.6x High Surge"),
                        Triple("Wardha Road Tech Park", "8 Requests • 10 Active Workers", "Normal 1.0x")
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Text("Live Geographic Demand vs Supply Heatmap", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Real-time surge multipliers calculated from live online workers & open requests", fontSize = 11.sp, color = SkillBridgeTextMuted)
                        Spacer(modifier = Modifier.height(8.dp))

                        // City Selection Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableCities.forEach { city ->
                                val isSelected = city == selectedHeatmapCity
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedHeatmapCity = city },
                                    label = { Text(city, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SkillBridgePrimary,
                                        selectedLabelColor = SkillBridgeWhite
                                    )
                                )
                            }
                        }
                    }

                    items(zones) { (zoneName, stats, surge) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(zoneName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(stats, fontSize = 11.sp, color = SkillBridgeTextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (surge.contains("Surge")) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .border(1.dp, if (surge.contains("Surge")) SkillBridgePrimary else SkillBridgeSuccess, RoundedCornerShape(6.dp))
                                ) {
                                    Text(surge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (surge.contains("Surge")) SkillBridgePrimaryHover else SkillBridgeSuccess)
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Ops Push Broadcast Tool
                var broadcastAudience by remember { mutableStateOf("All Online Electricians & Plumbers in Nagpur") }
                var broadcastMsg by remember { mutableStateOf("High demand alert in Dharampeth: Earn ₹150 extra per completed task before 6 PM today!") }
                var sentSuccess by remember { mutableStateOf(false) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Text("Ops Announcement & Push Broadcast", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Dispatch instant push and SMS alerts across worker fleets", fontSize = 11.sp, color = SkillBridgeTextMuted)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Target Audience:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = broadcastAudience,
                                    onValueChange = { broadcastAudience = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Broadcast Message:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = broadcastMsg,
                                    onValueChange = {
                                        broadcastMsg = it
                                        sentSuccess = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { sentSuccess = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = SkillBridgeWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dispatch Fleet Push Broadcast", color = SkillBridgeWhite)
                                }

                                if (sentSuccess) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("✓ Broadcast sent to 48 online workers via FCM & SMS relay.", color = SkillBridgeSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
