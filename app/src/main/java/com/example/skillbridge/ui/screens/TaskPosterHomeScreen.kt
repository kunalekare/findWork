package com.example.skillbridge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

@Composable
fun TaskPosterHomeScreen(
    categories: List<Category>,
    activeJobs: List<Job>,
    workersPool: List<User>,
    selectedCity: String,
    onCategoryClick: (Category) -> Unit,
    onRequestNowClick: () -> Unit,
    onPostProjectClick: () -> Unit,
    onTrackJobClick: (Job) -> Unit,
    onVoiceAssistClick: () -> Unit,
    onBookAgainClick: (User) -> Unit
) {
    val activeLiveJob = activeJobs.firstOrNull { it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. PINNED ACTIVE JOB CARD (if any live job is active)
        if (activeLiveJob != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                        .clickable { onTrackJobClick(activeLiveJob) }
                        .testTag("pinned_active_job_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgePrimary))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SkillBridgePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE ORDER IN PROGRESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkillBridgePrimaryHover,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ETA ~${activeLiveJob.etaMinutes} mins",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgePrimaryHover
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = activeLiveJob.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Assigned: ${activeLiveJob.workerName ?: "Matching nearby worker..."} • Status: ${activeLiveJob.status.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeTextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onTrackJobClick(activeLiveJob) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Track Live & View Map", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. HERO CALL TO ACTION BANNERS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Urgent on-demand service (3 Taps)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clickable { onRequestNowClick() }
                        .testTag("request_service_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgePrimarySoft),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SkillBridgePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = SkillBridgeWhite, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Need Service Now?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                            Text("Electrician, Cook, Driver in 20m", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = SkillBridgeTextMuted)
                        }
                    }
                }

                // Post skilled project
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clickable { onPostProjectClick() }
                        .testTag("post_project_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SkillBridgePrimaryHover),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, tint = SkillBridgeWhite, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Post a Project", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                            Text("Dev, Design, Tutors & Jobs", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = SkillBridgeTextMuted)
                        }
                    }
                }
            }
        }

        // 3. EXPLORE CATEGORIES (Section 12: Category Grid with rounded icon tiles on surface-muted)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explore Work Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${categories.size} Available",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgePrimaryHover,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Grid of Categories (2 columns or 4x4)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CategoryCard(
                                category = category,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(category) }
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. "BOOK AGAIN" / TRUSTED VERIFIED WORKERS
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Book Again & Top Rated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verified KYC",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(workersPool) { worker ->
                    WorkerSpotlightCard(
                        worker = worker,
                        onBookClick = { onBookAgainClick(worker) }
                    )
                }
            }
        }

        // 5. PLATFORM PROMISE CARD (Safety, Escrow, Masked Calling)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = SkillBridgeSuccess, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SkillBridge Escrow Protection",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Payment is safely held in escrow and released ONLY after you confirm complete satisfaction with photo proof.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("category_card_${category.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SkillBridgePrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (category.iconKey) {
                    "bolt" -> Icons.Default.Bolt
                    "plumbing" -> Icons.Default.Plumbing
                    "restaurant" -> Icons.Default.Restaurant
                    "cleaning_services" -> Icons.Default.CleaningServices
                    "drive_eta" -> Icons.Default.DirectionsCar
                    "format_paint" -> Icons.Default.FormatPaint
                    "content_cut" -> Icons.Default.ContentCut
                    "build" -> Icons.Default.Build
                    "foundation" -> Icons.Default.Foundation
                    "local_shipping" -> Icons.Default.LocalShipping
                    "code" -> Icons.Default.Code
                    "brush" -> Icons.Default.Brush
                    "school" -> Icons.Default.School
                    else -> Icons.Default.Work
                }
                Icon(
                    imageVector = icon,
                    contentDescription = category.name,
                    tint = SkillBridgePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (category.isHotspot) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SkillBridgePrimarySoft)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Hot", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                        }
                    }
                }
                Text(
                    text = "${category.nameHi} • From ₹${category.basePrice}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = SkillBridgeTextMuted
                )
            }
        }
    }
}

@Composable
fun WorkerSpotlightCard(
    worker: User,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(190.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worker.avatarEmoji,
                    fontSize = 28.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(SkillBridgeSuccess.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${worker.trustScore}% Trust",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = worker.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = "⭐ ${worker.rating} (${worker.reviewCount}) • ${worker.jobsCompleted} jobs",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = SkillBridgeTextMuted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "📍 ${worker.locationArea}, ${worker.locationCity}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = SkillBridgeTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onBookClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Hire Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
