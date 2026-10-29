package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

private val HomeCardShape = RoundedCornerShape(22.dp)
private val HomeTileShape = RoundedCornerShape(17.dp)

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
    val activeLiveJob = activeJobs.firstOrNull {
        it.status !in listOf(JobStatus.COMPLETED, JobStatus.CANCELLED)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SkillBridgeBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            HomeGreetingHeader(city = selectedCity, onVoiceAssistClick = onVoiceAssistClick)
        }

        item {
            SearchPrompt(onClick = onRequestNowClick)
        }

        if (activeLiveJob != null) {
            item {
                ActiveJobCard(job = activeLiveJob, onClick = { onTrackJobClick(activeLiveJob) })
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Book a service",
                    description = "Get trusted help nearby",
                    icon = Icons.Default.Bolt,
                    accent = SkillBridgePrimary,
                    container = SkillBridgePrimarySoft,
                    tag = "request_service_banner",
                    onClick = onRequestNowClick
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Post a project",
                    description = "Find the right expertise",
                    icon = Icons.Default.EditNote,
                    accent = SkillBridgePrimaryHover,
                    container = SkillBridgeSurfaceMuted,
                    tag = "post_project_banner",
                    onClick = onPostProjectClick
                )
            }
        }

        item {
            SectionTitle(
                title = "Explore categories",
                subtitle = "Find the right person for the job",
                trailing = "${categories.size} services"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CategoryCard(
                                category = category,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(category) }
                            )
                        }
                        if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = "Your trusted professionals",
                subtitle = "Book again or discover local talent",
                trailing = if (workersPool.isNotEmpty()) "${workersPool.size} nearby" else null
            )
        }

        if (workersPool.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(workersPool) { worker ->
                        WorkerSpotlightCard(worker = worker, onBookClick = { onBookAgainClick(worker) })
                    }
                }
            }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SkillBridgeSurfaceMuted,
                    shape = HomeCardShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeIconTile(icon = Icons.Default.Groups, tint = SkillBridgePrimary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Your next favourite professional", fontWeight = FontWeight.Bold, color = SkillBridgeText)
                            Text("Available professionals will appear here.", fontSize = 12.sp, color = SkillBridgeTextMuted)
                        }
                    }
                }
            }
        }

        item { TrustAndSafetyCard() }
    }
}

@Composable
private fun HomeGreetingHeader(city: String, onVoiceAssistClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(16.dp))
                Text(city.ifBlank { "Choose your location" }, fontSize = 12.sp, color = SkillBridgeTextMuted, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(16.dp))
            }
            Text(
                "What do you need\na hand with?",
                fontSize = 28.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillBridgeText
            )
            Text("Reliable local help, made simple.", fontSize = 13.sp, color = SkillBridgeTextMuted)
        }
        Surface(
            modifier = Modifier.size(54.dp).clickable(onClick = onVoiceAssistClick).testTag("btn_voice_assist"),
            shape = RoundedCornerShape(18.dp),
            color = SkillBridgePrimarySoft,
            border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Mic, contentDescription = "Voice assistant", tint = SkillBridgePrimary, modifier = Modifier.size(25.dp))
            }
        }
    }
}

@Composable
private fun SearchPrompt(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("home_search_prompt"),
        shape = RoundedCornerShape(17.dp),
        color = SkillBridgeWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Search services or professionals", fontSize = 13.sp, color = SkillBridgeText, fontWeight = FontWeight.SemiBold)
                Text("e.g. electrician, tutor, designer", fontSize = 11.sp, color = SkillBridgeTextMuted)
            }
            Icon(Icons.Default.Tune, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun ActiveJobCard(job: Job, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag("pinned_active_job_card"),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgePrimary.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(SkillBridgePrimarySoft.copy(alpha = 0.75f), SkillBridgeWhite)
                )
            ).padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(9.dp), color = SkillBridgePrimary) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                        Text("IN PROGRESS", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Schedule, contentDescription = null, tint = SkillBridgePrimary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("~${job.etaMinutes} min ETA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
            }
            Text(job.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    job.workerName ?: "Matching with a nearby professional",
                    fontSize = 12.sp,
                    color = SkillBridgeTextMuted,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Track your service", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    container: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(156.dp).clickable(onClick = onClick).testTag(tag),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = container),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Top) {
                HomeIconTile(icon = icon, tint = Color.White, background = accent, size = 42)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(description, fontSize = 11.sp, lineHeight = 15.sp, color = SkillBridgeTextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText)
            Text(subtitle, fontSize = 12.sp, color = SkillBridgeTextMuted)
        }
        if (trailing != null) {
            Surface(shape = RoundedCornerShape(9.dp), color = SkillBridgePrimarySoft) {
                Text(
                    trailing,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    fontSize = 10.sp,
                    color = SkillBridgePrimaryHover,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HomeIconTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color = tint.copy(alpha = 0.12f),
    size: Int = 42
) {
    Box(
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(14.dp)).background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.52f).dp))
    }
}

@Composable
fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).testTag("category_card_${category.id}"),
        shape = HomeTileShape,
        color = SkillBridgeWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
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
            HomeIconTile(icon = icon, tint = SkillBridgePrimary, background = SkillBridgePrimarySoft, size = 42)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        category.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (category.isHotspot) {
                        Spacer(Modifier.width(4.dp))
                        Surface(shape = RoundedCornerShape(5.dp), color = SkillBridgePrimarySoft) {
                            Text("HOT", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgePrimaryHover)
                        }
                    }
                }
                Text(
                    "From ₹${category.basePrice}",
                    fontSize = 11.sp,
                    color = SkillBridgePrimaryHover,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(category.nameHi, fontSize = 10.sp, color = SkillBridgeTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun WorkerSpotlightCard(worker: User, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.width(204.dp),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = SkillBridgeWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(16.dp), color = SkillBridgeSurfaceMuted) {
                    Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        Text(worker.avatarEmoji, fontSize = 30.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(9.dp), color = SkillBridgeSuccess.copy(alpha = 0.10f)) {
                    Text(
                        "${worker.trustScore}% trust",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeSuccess
                    )
                }
            }
            Text(worker.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Text("${worker.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                Text("(${worker.reviewCount}) · ${worker.jobsCompleted} jobs", fontSize = 10.sp, color = SkillBridgeTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SkillBridgeTextMuted, modifier = Modifier.size(14.dp))
                Text(
                    "${worker.locationArea}, ${worker.locationCity}",
                    fontSize = 10.sp,
                    color = SkillBridgeTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("Book professional", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun TrustAndSafetyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardShape,
        color = SkillBridgeSurfaceMuted,
        border = androidx.compose.foundation.BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            HomeIconTile(icon = Icons.Default.Shield, tint = SkillBridgeSuccess, background = SkillBridgeSuccess.copy(alpha = 0.12f), size = 46)
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Your safety comes first", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SkillBridgeText)
                Text(
                    "SkillBridge is designed around trusted professionals and safer bookings. Review the service details before confirming.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = SkillBridgeTextMuted
                )
            }
        }
    }
}
