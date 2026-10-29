package com.example.skillbridge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Sos
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.skillbridge.model.Job
import com.example.skillbridge.model.JobStatus
import com.example.skillbridge.model.WorkType
import com.example.skillbridge.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Live order tracking screen for SkillBridge.
 *
 * Uses the existing application Job model, theme tokens, status callbacks and Google Maps setup.
 * This screen does not fabricate live GPS updates: map markers use the coordinates currently
 * present on [job]. Connect a location stream/ViewModel to refresh those coordinates in real time.
 */
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
    val context = LocalContext.current

    val mapsKeyConfigured = remember {
        try {
            BuildConfig.MAPS_API_KEY.isNotBlank() &&
                !BuildConfig.MAPS_API_KEY.contains("Placeholder", ignoreCase = true)
        } catch (_: Throwable) {
            false
        }
    }

    val statusLabel = when (job.status) {
        JobStatus.REQUESTED -> "Finding your professional"
        JobStatus.MATCHED -> "Professional confirmed"
        JobStatus.EN_ROUTE -> "On the way to you"
        JobStatus.ARRIVED -> "Professional has arrived"
        JobStatus.IN_PROGRESS -> "Work in progress"
        JobStatus.COMPLETED -> "Service completed"
        else -> job.status.name.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    val statusSupportingText = when (job.status) {
        JobStatus.REQUESTED -> "We’re matching you with nearby professionals."
        JobStatus.MATCHED -> "Your professional is confirmed for this job."
        JobStatus.EN_ROUTE -> "Your professional is travelling to your location."
        JobStatus.ARRIVED -> "Your professional is at the service location."
        JobStatus.IN_PROGRESS -> "Your service is underway. Track progress here."
        JobStatus.COMPLETED -> "Your service is complete. Review the details below."
        else -> "Your order status is up to date."
    }

    val steps = listOf("Requested", "Matched", "En route", "Arrived", "In progress", "Done")
    val currentStep = when (job.status) {
        JobStatus.REQUESTED -> 0
        JobStatus.MATCHED -> 1
        JobStatus.EN_ROUTE -> 2
        JobStatus.ARRIVED -> 3
        JobStatus.IN_PROGRESS -> 4
        JobStatus.COMPLETED -> 5
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .statusBarsPadding()
    ) {
        // Compact, high-contrast app bar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = SkillBridgeText
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live tracking",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Order #${job.id}  •  ${job.categoryName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                onClick = onSosClick,
                shape = RoundedCornerShape(14.dp),
                color = SkillBridgeDanger.copy(alpha = 0.08f),
                contentColor = SkillBridgeDanger,
                modifier = Modifier.testTag("sos_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Outlined.Sos, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status summary.
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SkillBridgeSurfaceMuted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SkillBridgePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (job.status == JobStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.Navigation,
                                contentDescription = null,
                                tint = SkillBridgePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                statusLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                statusSupportingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = SkillBridgeTextMuted
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (job.status == JobStatus.COMPLETED) SkillBridgeSuccess.copy(alpha = 0.12f)
                            else SkillBridgePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (job.status == JobStatus.COMPLETED) "DONE" else "LIVE",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                color = if (job.status == JobStatus.COMPLETED) SkillBridgeSuccess else SkillBridgePrimaryHover,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        steps.forEachIndexed { index, step ->
                            val completed = index < currentStep
                            val active = index == currentStep
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                completed -> SkillBridgeSuccess
                                                active -> SkillBridgePrimary
                                                else -> SkillBridgeBorder
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (completed) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    } else {
                                        Text(
                                            "${index + 1}",
                                            color = if (active) Color.White else SkillBridgeTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    step,
                                    fontSize = 9.sp,
                                    lineHeight = 12.sp,
                                    color = if (active) SkillBridgePrimaryHover else SkillBridgeTextMuted,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            // Map panel: real Google Map when configured; tasteful placeholder otherwise.
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLowDataMode) 190.dp else 270.dp)
                    .testTag("map_view_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F5F1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    val posterLatLng = remember(job.posterLatitude, job.posterLongitude) {
                        LatLng(job.posterLatitude, job.posterLongitude)
                    }
                    val workerLatLng = remember(job.workerLatitude, job.workerLongitude) {
                        LatLng(job.workerLatitude, job.workerLongitude)
                    }

                    if (mapsKeyConfigured && !isLowDataMode) {
                        val cameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(
                                if (job.status == JobStatus.REQUESTED) posterLatLng else workerLatLng,
                                14.5f
                            )
                        }
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraState
                        ) {
                            Marker(
                                state = MarkerState(position = posterLatLng),
                                title = "Service location"
                            )
                            if (job.status != JobStatus.REQUESTED) {
                                Marker(
                                    state = MarkerState(position = workerLatLng),
                                    title = job.workerName ?: "Assigned professional"
                                )
                                Polyline(
                                    points = listOf(workerLatLng, posterLatLng),
                                    color = SkillBridgePrimary,
                                    width = 8f
                                )
                            } else {
                                Circle(
                                    center = posterLatLng,
                                    radius = job.searchRadiusMeters,
                                    fillColor = SkillBridgePrimary.copy(alpha = 0.08f),
                                    strokeColor = SkillBridgePrimary.copy(alpha = 0.7f),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    } else {
                        TrackingMapPlaceholder(
                            isSearching = job.status == JobStatus.REQUESTED,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.96f),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (job.status == JobStatus.COMPLETED) SkillBridgeSuccess else SkillBridgePrimary)
                            )
                            Text(
                                when (job.status) {
                                    JobStatus.REQUESTED -> "Searching nearby"
                                    JobStatus.EN_ROUTE -> "Professional on the way"
                                    JobStatus.ARRIVED -> "Arrived at location"
                                    JobStatus.IN_PROGRESS -> "Service underway"
                                    JobStatus.COMPLETED -> "Trip completed"
                                    else -> "Professional assigned"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SkillBridgeText
                            )
                        }
                    }

                    if (job.status == JobStatus.EN_ROUTE || job.status == JobStatus.MATCHED) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.97f),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        if (job.status == JobStatus.EN_ROUTE) "Estimated arrival" else "Next step",
                                        fontSize = 10.sp,
                                        color = SkillBridgeTextMuted
                                    )
                                    Text(
                                        if (job.status == JobStatus.EN_ROUTE) "${job.etaMinutes} min · ${job.distanceKm} km away" else "Preparing to leave",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkillBridgeText
                                    )
                                }
                            }
                        }
                    }

                    if (!mapsKeyConfigured || isLowDataMode) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.94f)
                        ) {
                            Text(
                                if (isLowDataMode) "Low data mode" else "Map preview",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }
                }
            }

            if (!mapsKeyConfigured && !isLowDataMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SkillBridgeWarning.copy(alpha = 0.10f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SkillBridgeWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add a valid MAPS_API_KEY to enable the interactive Google Map. The preview is illustrative and does not show live GPS movement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeText
                    )
                }
            }

            // Professional / customer details.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SkillBridgePrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isWorkerView) Icons.Default.Person else Icons.Default.Verified,
                                contentDescription = null,
                                tint = SkillBridgePrimaryHover,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isWorkerView) job.taskPosterName else (job.workerName ?: "Finding a professional"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = SkillBridgeSuccess, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (isWorkerView) "Verified customer" else "Verified professional",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SkillBridgeTextMuted
                                )
                            }
                            if (!isWorkerView) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = SkillBridgeWarning, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("4.85", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("  ·  Experienced service partner", fontSize = 11.sp, color = SkillBridgeTextMuted)
                                }
                            }
                        }
                        ContactAction(
                            icon = { Icon(Icons.Default.Call, contentDescription = "Call professional") },
                            label = "Call",
                            onClick = onCallClick
                        )
                        Spacer(Modifier.width(8.dp))
                        ContactAction(
                            icon = { Icon(Icons.Default.Chat, contentDescription = "Chat with professional") },
                            label = "Chat",
                            onClick = onChatClick,
                            filled = true
                        )
                    }

                    HorizontalDivider(color = SkillBridgeBorder)

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Service address", fontSize = 11.sp, color = SkillBridgeTextMuted)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                job.locationAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                job.locationCity,
                                style = MaterialTheme.typography.bodySmall,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }

                    TextButton(
                        onClick = onReportBlockClick,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = null, tint = SkillBridgeDanger, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Report or block", color = SkillBridgeDanger, fontSize = 12.sp)
                    }
                }
            }

            // Escrow / payment card.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SkillBridgeSuccess.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = SkillBridgeSuccess, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment protected", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                if (job.escrowHeld) "Funds held securely in escrow" else "Payment status managed securely",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkillBridgeTextMuted
                            )
                        }
                        Text(
                            "₹${job.budget}",
                            style = MaterialTheme.typography.titleLarge,
                            color = SkillBridgeSuccess,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    HorizontalDivider(color = SkillBridgeBorder)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (job.escrowHeld) "Amount is held until the agreed completion step." else "Payment release follows your job agreement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkillBridgeTextMuted
                        )
                    }
                }
            }

            // Milestone payments.
            if (job.milestones.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Payment milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        job.milestones.forEach { milestone ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (milestone.isReleased) SkillBridgeSuccess.copy(alpha = 0.12f)
                                            else SkillBridgeSurfaceMuted
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (milestone.isReleased) Icons.Default.CheckCircle else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (milestone.isReleased) SkillBridgeSuccess else SkillBridgePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(milestone.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        if (milestone.isReleased) "Released" else "Secured in escrow",
                                        fontSize = 11.sp,
                                        color = if (milestone.isReleased) SkillBridgeSuccess else SkillBridgeTextMuted
                                    )
                                }
                                Text("₹${milestone.amount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (!isWorkerView && !milestone.isReleased) {
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { onReleaseMilestone(milestone.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Release", color = SkillBridgeSuccess, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Existing workflow actions retained, with clearer hierarchy.
            when {
                isWorkerView -> when (job.status) {
                    JobStatus.MATCHED -> PrimaryTrackingButton(
                        text = "Start navigation",
                        icon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                        testTag = "start_transit_button",
                        onClick = { onAdvanceStatus(JobStatus.EN_ROUTE, null) }
                    )
                    JobStatus.EN_ROUTE -> {
                        OutlinedButton(
                            onClick = {
                                val navUri = Uri.parse("google.navigation:q=${job.posterLatitude},${job.posterLongitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    val fallback = Uri.parse("geo:${job.posterLatitude},${job.posterLongitude}?q=${job.posterLatitude},${job.posterLongitude}(Service+Location)")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, fallback))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("open_google_maps_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgePrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgePrimary)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open turn-by-turn directions", fontWeight = FontWeight.SemiBold)
                        }
                        PrimaryTrackingButton(
                            text = "I've arrived",
                            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                            testTag = "mark_arrived_button",
                            onClick = { onAdvanceStatus(JobStatus.ARRIVED, null) }
                        )
                    }
                    JobStatus.ARRIVED -> PrimaryTrackingButton(
                        text = "Start work",
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        testTag = "start_work_button",
                        onClick = { onAdvanceStatus(JobStatus.IN_PROGRESS, null) }
                    )
                    JobStatus.IN_PROGRESS -> PrimaryTrackingButton(
                        text = "Complete work and submit proof",
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        testTag = "complete_work_button",
                        containerColor = SkillBridgeSuccess,
                        onClick = { onAdvanceStatus(JobStatus.COMPLETED, "proof_mcb_repaired.jpg") }
                    )
                    JobStatus.COMPLETED -> Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SkillBridgeSuccess.copy(alpha = 0.10f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Job completed · ₹${(job.budget * 0.85).toInt()} credited to your wallet",
                            modifier = Modifier.padding(16.dp),
                            color = SkillBridgeSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> Unit
                }
                else -> when (job.status) {
                    JobStatus.IN_PROGRESS -> OutlinedButton(
                        onClick = onDisputeClick,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeDanger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeDanger)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Report an issue / hold payment", fontWeight = FontWeight.SemiBold)
                    }
                    JobStatus.COMPLETED -> {
                        PrimaryTrackingButton(
                            text = "Rate professional & release payment",
                            icon = { Icon(Icons.Default.Star, contentDescription = null) },
                            testTag = "rate_and_release_button",
                            onClick = onRateAndReleaseClick
                        )
                        OutlinedButton(
                            onClick = onViewInvoiceClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeText)
                        ) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View GST invoice", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    else -> OutlinedButton(
                        onClick = onDisputeClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeTextMuted)
                    ) {
                        Text("Cancel request or get help")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ContactAction(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (filled) SkillBridgePrimary else SkillBridgeSurfaceMuted)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .widthIn(min = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides if (filled) Color.White else SkillBridgePrimary) {
            icon()
        }
        Text(
            label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else SkillBridgePrimary
        )
    }
}

@Composable
private fun PrimaryTrackingButton(
    text: String,
    icon: @Composable () -> Unit,
    testTag: String,
    onClick: () -> Unit,
    containerColor: Color = SkillBridgePrimary
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        icon()
        Spacer(Modifier.width(9.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun TrackingMapPlaceholder(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "tracking-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )

    Canvas(modifier = modifier.background(Color(0xFFF1F4EF))) {
        val w = size.width
        val h = size.height

        // Soft map-like blocks and roads for a deliberate preview, not real map data.
        drawLine(Color(0xFFDDE4DB), Offset(0f, h * 0.24f), Offset(w, h * 0.24f), 17f)
        drawLine(Color(0xFFDDE4DB), Offset(0f, h * 0.72f), Offset(w, h * 0.72f), 23f)
        drawLine(Color(0xFFDDE4DB), Offset(w * 0.27f, 0f), Offset(w * 0.27f, h), 16f)
        drawLine(Color(0xFFDDE4DB), Offset(w * 0.73f, 0f), Offset(w * 0.73f, h), 18f)
        drawLine(
            Color.White,
            Offset(w * 0.03f, h * 0.54f),
            Offset(w * 0.95f, h * 0.38f),
            10f
        )
        drawLine(
            Color.White,
            Offset(w * 0.42f, h * 0.02f),
            Offset(w * 0.58f, h * 0.98f),
            9f
        )

        val destination = Offset(w * 0.68f, h * 0.48f)
        if (isSearching) {
            drawCircle(
                color = SkillBridgePrimary.copy(alpha = pulse * 0.16f),
                radius = size.minDimension * 0.22f,
                center = destination
            )
            drawCircle(
                color = SkillBridgePrimary.copy(alpha = 0.55f),
                radius = size.minDimension * 0.22f,
                center = destination,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            )
        } else {
            val worker = Offset(w * 0.23f, h * 0.70f)
            val mid = Offset(w * 0.44f, h * 0.60f)
            drawLine(SkillBridgePrimary, worker, mid, 5.dp.toPx())
            drawLine(SkillBridgePrimary, mid, destination, 5.dp.toPx())
            drawCircle(Color.White, 12.dp.toPx(), worker)
            drawCircle(SkillBridgePrimary, 8.dp.toPx(), worker)
        }

        drawCircle(Color.White, 15.dp.toPx(), destination)
        drawCircle(SkillBridgePrimary, 10.dp.toPx(), destination)
        drawCircle(Color.White, 4.dp.toPx(), destination)
    }
}
