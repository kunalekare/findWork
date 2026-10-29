package com.example.skillbridge.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.BuildConfig
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.JobStatusStepper
import com.example.skillbridge.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LiveTrackingScreen(
    job: Job,
    isWorkerView: Boolean,
    isLowDataMode: Boolean = false,
    onBack: () -> Unit,
    onAdvanceStatus: (JobStatus, String?) -> Unit,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit,
    onSosClick: () -> Unit,
    onDisputeClick: () -> Unit,
    onRateAndReleaseClick: () -> Unit,
    onReleaseMilestone: (String) -> Unit = {},
    onReportBlockClick: () -> Unit = {},
    onViewInvoiceClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Radar pulse animation for matching / transit
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Live Order #${job.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${job.categoryName} • ${job.locationCity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeTextMuted
                    )
                }
            }

            IconButton(
                onClick = onSosClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SkillBridgeDanger.copy(alpha = 0.12f))
            ) {
                Text("SOS", color = SkillBridgeDanger, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Status Stepper
            JobStatusStepper(
                currentStatus = job.status,
                isLocalJob = job.type == WorkType.LOCAL_ON_DEMAND
            )

            // 2. Google Maps / Radar View with dynamic search radius
            val isMapsKeyConfigured = remember {
                try {
                    com.example.BuildConfig.MAPS_API_KEY.isNotBlank() &&
                    !com.example.BuildConfig.MAPS_API_KEY.contains("Placeholder")
                } catch (e: Throwable) {
                    false
                }
            }

            if (!isMapsKeyConfigured) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maps_key_warning_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeWarning.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeWarning))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SkillBridgeWarning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Maps Key Setup Notice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeText
                            )
                            Text(
                                text = "MAPS_API_KEY is not set in .env. Running on local Vector Radar engine with dynamic search radius (${(job.searchRadiusMeters / 1000).toInt()} km).",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .testTag("map_view_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val posterLatLng = remember(job.posterLatitude, job.posterLongitude) {
                        LatLng(job.posterLatitude, job.posterLongitude)
                    }
                    val workerLatLng = remember(job.workerLatitude, job.workerLongitude) {
                        LatLng(job.workerLatitude, job.workerLongitude)
                    }

                    if (isMapsKeyConfigured) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(posterLatLng, 14f)
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(position = posterLatLng),
                                title = "Your Location"
                            )
                            if (job.status == JobStatus.REQUESTED) {
                                Circle(
                                    center = posterLatLng,
                                    radius = job.searchRadiusMeters,
                                    fillColor = SkillBridgePrimary.copy(alpha = 0.15f),
                                    strokeColor = SkillBridgePrimary,
                                    strokeWidth = 3f
                                )
                            }
                            if (job.status != JobStatus.REQUESTED) {
                                Marker(
                                    state = MarkerState(position = workerLatLng),
                                    title = job.workerName ?: "Assigned Worker"
                                )
                                Polyline(
                                    points = listOf(workerLatLng, posterLatLng),
                                    color = SkillBridgePrimary,
                                    width = 10f
                                )
                            }
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            drawLine(Color(0xFFE5E7EB), Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 2f)
                            drawLine(Color(0xFFE5E7EB), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 2f)
                            drawLine(Color(0xFFE5E7EB), Offset(w * 0.4f, 0f), Offset(w * 0.4f, h), strokeWidth = 2f)
                            drawLine(Color(0xFFE5E7EB), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 2f)

                            val centerPoster = Offset(w * 0.6f, h * 0.5f)
                            val radiusScale = (job.searchRadiusMeters.toFloat() / 10000f).coerceIn(0.25f, 1f)
                            val maxRadarRadius = (w * 0.45f) * radiusScale

                            if (job.status == JobStatus.REQUESTED) {
                                drawCircle(
                                    color = SkillBridgePrimary.copy(alpha = pulseAlpha * 0.3f),
                                    radius = maxRadarRadius * (pulseRadius / 90f),
                                    center = centerPoster
                                )
                                drawCircle(
                                    color = SkillBridgePrimary.copy(alpha = 0.5f),
                                    radius = maxRadarRadius,
                                    center = centerPoster,
                                    style = Stroke(width = 3f)
                                )
                                drawCircle(color = SkillBridgePrimary.copy(alpha = 0.7f), radius = 8f, center = Offset(centerPoster.x - 60f, centerPoster.y - 40f))
                                drawCircle(color = SkillBridgePrimary.copy(alpha = 0.7f), radius = 8f, center = Offset(centerPoster.x + 80f, centerPoster.y + 30f))
                                drawCircle(color = SkillBridgePrimary.copy(alpha = 0.7f), radius = 8f, center = Offset(centerPoster.x - 30f, centerPoster.y + 70f))
                            } else {
                                val startWorker = Offset(w * 0.25f, h * 0.65f)
                                val mid = Offset(w * 0.45f, h * 0.55f)
                                drawLine(color = SkillBridgePrimary, start = startWorker, end = mid, strokeWidth = 6f)
                                drawLine(color = SkillBridgePrimary, start = mid, end = centerPoster, strokeWidth = 6f)
                                drawCircle(color = SkillBridgePrimary, radius = 14f, center = startWorker)
                                drawCircle(color = Color.White, radius = 6f, center = startWorker)
                            }

                            drawCircle(color = SkillBridgeText, radius = 14f, center = centerPoster)
                            drawCircle(color = Color.White, radius = 6f, center = centerPoster)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(SkillBridgePrimary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (job.status) {
                                JobStatus.REQUESTED -> "Searching Radar: ${(job.searchRadiusMeters / 1000).toInt()} km Radius • 3 Workers Nearby"
                                JobStatus.MATCHED -> "Worker Assigned! Getting Equipment..."
                                JobStatus.EN_ROUTE -> "Arriving in ~${job.etaMinutes} mins (${job.distanceKm} km)"
                                JobStatus.ARRIVED -> "Worker Has Arrived at Site"
                                JobStatus.IN_PROGRESS -> "Work In Progress"
                                JobStatus.COMPLETED -> "Service Completed & Inspected"
                                else -> "Status: ${job.status.name}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SkillBridgeBg.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (job.status == JobStatus.REQUESTED) "📍 You  🟠 3 Nearby Candidates" else "🟠 Assigned Worker  ⚫ Your Address",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = SkillBridgeText
                        )
                    }
                }
            }

            // 3. Worker / Employer Profile & Quick Contact Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SkillBridgePrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isWorkerView) "👩‍💼" else "⚡", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isWorkerView) job.taskPosterName else (job.workerName ?: "Assigning..."),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isWorkerView) "Task Poster • Verified Account" else "Verified Technician • 4.85★ (64 jobs)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkillBridgeTextMuted
                                )
                            }
                        }

                        // Masked Call & Chat buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onCallClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SkillBridgeSurfaceMuted)
                                    .testTag("masked_call_button")
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = SkillBridgePrimary)
                            }

                            IconButton(
                                onClick = onChatClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SkillBridgePrimarySoft)
                                    .testTag("in_app_chat_button")
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat", tint = SkillBridgePrimaryHover)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = SkillBridgeBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Address details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = job.locationAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onReportBlockClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SkillBridgeDanger, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report / Block User", color = SkillBridgeDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Escrow & Payment Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Escrow Payment Protected",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (job.escrowHeld) "₹${job.budget} is safely deposited in Escrow" else "Payment released upon satisfaction",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeTextMuted
                        )
                    }
                    Text(
                        text = "₹${job.budget}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess
                    )
                }
            }

            // Milestone Escrow Section (WS-3 & WS-6)
            if (job.milestones.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Project Milestones Escrow",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        job.milestones.forEach { ms ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ms.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text(if (ms.isReleased) "✅ Released to Worker" else "🔒 Escrow Locked", fontSize = 10.sp, color = if (ms.isReleased) SkillBridgeSuccess else SkillBridgePrimaryHover)
                                }
                                Text("₹${ms.amount}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (!isWorkerView && !ms.isReleased) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onReleaseMilestone(ms.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeSuccess),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Release", fontSize = 10.sp, color = SkillBridgeWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Contextual State Machine Step Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isWorkerView) {
                    // WORKER ACTIONS
                    when (job.status) {
                        JobStatus.MATCHED -> {
                            Button(
                                onClick = { onAdvanceStatus(JobStatus.EN_ROUTE, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_transit_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("I Am En Route (Start GPS Navigation)", fontWeight = FontWeight.Bold)
                            }
                        }
                        JobStatus.EN_ROUTE -> {
                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${job.posterLatitude},${job.posterLongitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        val fallbackUri = Uri.parse("geo:${job.posterLatitude},${job.posterLongitude}?q=${job.posterLatitude},${job.posterLongitude}(Job+Location)")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("open_google_maps_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgePrimary),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgePrimary))
                            ) {
                                Icon(imageVector = Icons.Default.Directions, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open in Google Maps (Turn-by-Turn)", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onAdvanceStatus(JobStatus.ARRIVED, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("mark_arrived_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("I Have Arrived at Location", fontWeight = FontWeight.Bold)
                            }
                        }
                        JobStatus.ARRIVED -> {
                            Button(
                                onClick = { onAdvanceStatus(JobStatus.IN_PROGRESS, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_work_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Work & Diagnostic", fontWeight = FontWeight.Bold)
                            }
                        }
                        JobStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { onAdvanceStatus(JobStatus.COMPLETED, "proof_mcb_repaired.jpg") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("complete_work_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeSuccess)
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Photo Proof & Finish (Release ₹${(job.budget * 0.85).toInt()})", fontWeight = FontWeight.Bold)
                            }
                        }
                        JobStatus.COMPLETED -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SkillBridgeSuccess.copy(alpha = 0.12f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✅ Job Completed! ₹${(job.budget * 0.85).toInt()} credited to your wallet.",
                                    color = SkillBridgeSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {}
                    }
                } else {
                    // TASK POSTER ACTIONS
                    when (job.status) {
                        JobStatus.IN_PROGRESS -> {
                            OutlinedButton(
                                onClick = onDisputeClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeDanger),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Report an Issue / Hold Escrow")
                            }
                        }
                        JobStatus.COMPLETED -> {
                            Button(
                                onClick = onRateAndReleaseClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("rate_and_release_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rate Technician & Release", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = onViewInvoiceClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View & Download GST Tax Invoice", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = onDisputeClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeTextMuted),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel Request or Need Help?")
                            }
                        }
                    }
                }
            }
        }
    }
}
