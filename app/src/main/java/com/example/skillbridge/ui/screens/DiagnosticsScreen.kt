package com.example.skillbridge.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.skillbridge.config.AppConfig
import com.example.skillbridge.config.EnvironmentMode
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    pendingOfflineCount: Int = 0,
    onFlushOfflineQueue: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val coroutineScope = rememberCoroutineScope()
    var selectedEnv by remember { mutableStateOf(AppConfig.currentMode) }
    var pingStatus by remember { mutableStateOf("Ready to run a connectivity check") }
    var pingLatencyMs by remember { mutableStateOf<Long?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    // These are currently the same demo values as the original screen.
    // Replace them with values from your SSE connection manager when available.
    val sseHeartbeats = 14
    val lastEventId = "1042"
    val sseConnected = true

    val mapsKeyConfigured = remember {
        val key = BuildConfig.MAPS_API_KEY
        key.isNotBlank() &&
            !key.contains("placeholder", ignoreCase = true) &&
            !key.startsWith("YOUR_", ignoreCase = true)
    }

    val environmentLabel = selectedEnv.name.replace('_', ' ')
    val environmentColor = when (selectedEnv) {
        EnvironmentMode.MOCK -> SkillBridgeWarning
        else -> SkillBridgeSuccess
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "System health",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeText
                        )
                        Text(
                            text = "Diagnostics & operations · Nagpur",
                            fontSize = 12.sp,
                            color = SkillBridgeTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_diagnostics_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SkillBridgeText
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = environmentColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(environmentColor)
                            )
                            Text(
                                environmentLabel,
                                color = environmentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkillBridgeBg)
            )
        },
        containerColor = SkillBridgeBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Overview hero
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SkillBridgePrimary
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Icon(
                                    Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(10.dp).size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Operations overview",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    "A quick view of your app services",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                Icons.Default.ArrowOutward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OverviewMetric(
                                modifier = Modifier.weight(1f),
                                label = "Environment",
                                value = environmentLabel,
                                icon = Icons.Default.Tune
                            )
                            OverviewMetric(
                                modifier = Modifier.weight(1f),
                                label = "Maps SDK",
                                value = if (mapsKeyConfigured) "Configured" else "Needs setup",
                                icon = Icons.Default.LocationOn
                            )
                            OverviewMetric(
                                modifier = Modifier.weight(1f),
                                label = "Offline queue",
                                value = pendingOfflineCount.toString(),
                                icon = Icons.Default.CloudQueue
                            )
                        }
                    }
                }
            }

            item {
                SectionHeading(
                    title = "Environment",
                    subtitle = "Choose which backend configuration the app uses",
                    icon = Icons.Default.SettingsEthernet
                )
                Spacer(Modifier.height(10.dp))
                ElevatedPanel(modifier = Modifier.testTag("card_env_switch")) {
                    Text(
                        "Target environment",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnvironmentMode.entries.forEach { mode ->
                            val selected = selectedEnv == mode
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedEnv = mode
                                    AppConfig.setEnvironment(mode)
                                    pingLatencyMs = null
                                    pingStatus = "Environment changed · run a new check"
                                },
                                label = {
                                    Text(
                                        mode.name.replace('_', ' '),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkillBridgePrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = SkillBridgeBorder,
                                    selectedBorderColor = SkillBridgePrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("ACTIVE BASE URL", style = MaterialTheme.typography.labelSmall, color = SkillBridgeTextMuted)
                    Spacer(Modifier.height(5.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SkillBridgeSurfaceMuted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                AppConfig.apiBaseUrl,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SkillBridgeText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                SectionHeading(
                    title = "Backend connectivity",
                    subtitle = "Check the selected environment",
                    icon = Icons.Default.CloudDone,
                    trailing = {
                        StatusPill(
                            text = if (pingLatencyMs != null) "CHECKED" else "NOT CHECKED",
                            color = if (pingLatencyMs != null) SkillBridgeSuccess else SkillBridgeTextMuted
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
                ElevatedPanel(modifier = Modifier.testTag("card_backend_ping")) {
                    Row(verticalAlignment = Alignment.Top) {
                        FeatureIcon(Icons.Default.Speed, tint = SkillBridgePrimary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("API health check", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "Measure a response time for the selected environment.",
                                fontSize = 12.sp,
                                color = SkillBridgeTextMuted,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (pingLatencyMs != null) SkillBridgeSuccess.copy(alpha = 0.08f) else SkillBridgeSurfaceMuted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(if (pingLatencyMs != null) SkillBridgeSuccess else SkillBridgeWarning)
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(
                                pingStatus,
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                color = SkillBridgeText,
                                fontWeight = FontWeight.Medium
                            )
                            if (pingLatencyMs != null) {
                                Text(
                                    "${pingLatencyMs} ms",
                                    color = SkillBridgeSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isPinging = true
                            pingLatencyMs = null
                            pingStatus = "Running check…"
                            coroutineScope.launch {
                                val start = System.currentTimeMillis()
                                // This delay is retained from the original UI demonstration.
                                // Replace with a real HTTP health endpoint call for production diagnostics.
                                delay(120)
                                pingLatencyMs = System.currentTimeMillis() - start
                                pingStatus = if (selectedEnv == EnvironmentMode.MOCK) {
                                    "Demo response · mock environment selected"
                                } else {
                                    "Demo response · real API request not yet connected"
                                }
                                isPinging = false
                            }
                        },
                        enabled = !isPinging,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_ping_backend"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(17.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Checking…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(19.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Run connectivity check", fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "Note: this button currently runs the original simulated check. Connect it to your health endpoint to verify actual backend availability.",
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = SkillBridgeTextMuted
                    )
                }
            }

            item {
                SectionHeading(
                    title = "Real-time events",
                    subtitle = "Server-Sent Events gateway",
                    icon = Icons.Default.Sync,
                    trailing = {
                        StatusPill(
                            text = if (sseConnected) "CONNECTED" else "DISCONNECTED",
                            color = if (sseConnected) SkillBridgeSuccess else SkillBridgeDanger
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
                ElevatedPanel(modifier = Modifier.testTag("card_sse_status")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureIcon(Icons.Default.Stream, tint = SkillBridgeSuccess)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SSE gateway", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Live event transport and keep-alive telemetry",
                                fontSize = 11.sp,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }
                    Spacer(Modifier.height(15.dp))
                    HorizontalDivider(color = SkillBridgeBorder)
                    Spacer(Modifier.height(4.dp))
                    DiagnosticValueRow(
                        label = "Connection state",
                        value = if (sseConnected) "Connected (active)" else "Disconnected",
                        valueColor = if (sseConnected) SkillBridgeSuccess else SkillBridgeDanger
                    )
                    DiagnosticValueRow(
                        label = "Last event ID",
                        value = lastEventId,
                        mono = true
                    )
                    DiagnosticValueRow(
                        label = "Heartbeats received",
                        value = "$sseHeartbeats · 15s interval"
                    )
                }
            }

            item {
                SectionHeading(
                    title = "Maps platform",
                    subtitle = "Configuration and location telemetry",
                    icon = Icons.Default.Map,
                    trailing = {
                        StatusPill(
                            text = if (mapsKeyConfigured) "CONFIGURED" else "ACTION NEEDED",
                            color = if (mapsKeyConfigured) SkillBridgeSuccess else SkillBridgeWarning
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
                ElevatedPanel(modifier = Modifier.testTag("card_maps_status")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureIcon(Icons.Default.LocationOn, tint = SkillBridgePrimary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Maps Platform", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (mapsKeyConfigured) "API key appears to be set" else "A valid Maps SDK key is required",
                                fontSize = 11.sp,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    DiagnosticValueRow(
                        label = "SDK API key",
                        value = if (mapsKeyConfigured) "Configured" else "Placeholder / missing",
                        valueColor = if (mapsKeyConfigured) SkillBridgeSuccess else SkillBridgeWarning
                    )
                    DiagnosticValueRow(label = "Location update cadence", value = "5s en route · 15s idle")
                    DiagnosticValueRow(label = "Search radius sequence", value = "2 km → 5 km → 10 km")
                    if (!mapsKeyConfigured) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = SkillBridgeWarning.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(11.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SkillBridgeWarning, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Set MAPS_API_KEY in your local build configuration and restrict it to the required Android app and Maps SDK.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = SkillBridgeText
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionHeading(
                    title = "Offline resilience",
                    subtitle = "Pending actions and retry protection",
                    icon = Icons.Default.CloudQueue,
                    trailing = {
                        StatusPill(
                            text = if (pendingOfflineCount > 0) "PENDING" else "CLEAR",
                            color = if (pendingOfflineCount > 0) SkillBridgeWarning else SkillBridgeSuccess
                        )
                    }
                )
                Spacer(Modifier.height(10.dp))
                ElevatedPanel(modifier = Modifier.testTag("card_offline_queue")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureIcon(Icons.Default.Save, tint = SkillBridgePrimary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Offline action queue", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Queued changes will sync when connectivity is available.",
                                fontSize = 11.sp,
                                color = SkillBridgeTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SkillBridgeSurfaceMuted)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Queued mutations", fontSize = 11.sp, color = SkillBridgeTextMuted)
                            Text(
                                "$pendingOfflineCount ${if (pendingOfflineCount == 1) "item" else "items"}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SkillBridgeText
                            )
                        }
                        OutlinedButton(
                            onClick = onFlushOfflineQueue,
                            enabled = pendingOfflineCount > 0,
                            modifier = Modifier.testTag("btn_flush_queue"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgePrimary)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Sync queue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    DiagnosticValueRow(
                        label = "Idempotency protection",
                        value = "UUID v4 header",
                        valueColor = SkillBridgeSuccess
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Diagnostics are for internal app operations",
                        fontSize = 10.sp,
                        color = SkillBridgeTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ElevatedPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(SkillBridgePrimarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SkillBridgePrimaryHover, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
            Text(subtitle, fontSize = 11.sp, color = SkillBridgeTextMuted)
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun FeatureIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.11f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun OverviewMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.11f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.25.sp
        )
    }
}

@Composable
private fun DiagnosticValueRow(
    label: String,
    value: String,
    valueColor: Color = SkillBridgeText,
    mono: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = SkillBridgeTextMuted
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            modifier = Modifier.weight(1.1f),
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            lineHeight = 16.sp
        )
    }
    HorizontalDivider(color = SkillBridgeBorder.copy(alpha = 0.75f), thickness = 0.7.dp)
}
