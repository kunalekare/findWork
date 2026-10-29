package com.example.skillbridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

// 3-Tap Urgent Service Request Dialog
@Composable
fun RequestServiceDialog(
    categories: List<Category>,
    selectedCity: String,
    initialCategory: Category? = null,
    onDismiss: () -> Unit,
    onConfirmRequest: (categoryId: String, description: String, urgency: String, address: String) -> Unit
) {
    var step by remember { mutableIntStateOf(if (initialCategory != null) 2 else 1) }
    var chosenCat by remember { mutableStateOf(initialCategory ?: categories.first()) }
    var issueText by remember { mutableStateOf("Main MCB switch is tripping frequently") }
    var urgencyText by remember { mutableStateOf("Immediate (Within 30 mins)") }
    var addressText by remember { mutableStateOf("Dharampeth, West High Court Rd, Nagpur") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (step) {
                    1 -> "Step 1: Choose Service"
                    2 -> "Step 2: Describe Issue & Urgency"
                    else -> "Step 3: Escrow & Confirm"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (step) {
                    1 -> {
                        Text("Select service needed right now:", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories.filter { it.type == WorkType.LOCAL_ON_DEMAND }) { cat ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            chosenCat = cat
                                            step = 2
                                        },
                                    colors = CardDefaults.cardColors(containerColor = if (chosenCat.id == cat.id) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("From ₹${cat.basePrice}", fontSize = 11.sp, color = SkillBridgeTextMuted)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text("Service: ${chosenCat.name}", fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = issueText,
                            onValueChange = { issueText = it },
                            label = { Text("What needs repair/service?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = addressText,
                            onValueChange = { addressText = it },
                            label = { Text("Service Address in $selectedCity") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Urgency:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Immediate (Within 30 mins)", "Schedule Later Today").forEach { urg ->
                                val isSel = urgencyText == urg
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) SkillBridgePrimarySoft else SkillBridgeBorder)
                                        .clickable { urgencyText = urg }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(urg, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) SkillBridgePrimaryHover else SkillBridgeText)
                                }
                            }
                        }
                    }

                    3 -> {
                        val estAmount = chosenCat.basePrice * 2
                        Text("Order Summary & Escrow Guarantee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkillBridgeSurfaceMuted)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Service: ${chosenCat.name}", fontWeight = FontWeight.Bold)
                                Text("Location: $addressText", style = MaterialTheme.typography.bodySmall)
                                Text("Urgency: $urgencyText", style = MaterialTheme.typography.bodySmall, color = SkillBridgePrimaryHover)
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SkillBridgeBorder)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimated Escrow Deposit:", fontWeight = FontWeight.Bold)
                                    Text("₹$estAmount", fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🛡 Your ₹$estAmount will be held safely in SkillBridge Escrow and released only after you are 100% satisfied.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = SkillBridgeTextMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 3) {
                        step += 1
                    } else {
                        onConfirmRequest(chosenCat.id, issueText, urgencyText, addressText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                modifier = Modifier.testTag("confirm_service_dialog_button")
            ) {
                Text(if (step < 3) "Next" else "Hold Escrow & Request Worker", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Post Skilled Project Dialog (with AI Job Description helper)
@Composable
fun PostProjectDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirmPost: (title: String, categoryId: String, description: String, budget: Int, deadlineDays: Int) -> Unit
) {
    val skilledCategories = categories.filter { it.type == WorkType.PROJECT_SKILLED }
    var selectedCat by remember { mutableStateOf(skilledCategories.first()) }
    var title by remember { mutableStateOf("Design brand identity & packaging for spices startup") }
    var desc by remember { mutableStateOf("Need vector logo, typography guidelines, and 3 pouch label designs.") }
    var budgetText by remember { mutableStateOf("8000") }
    var daysText by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Post Skilled / Remote Project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    skilledCategories.take(3).forEach { cat ->
                        val isSel = selectedCat.id == cat.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SkillBridgePrimarySoft else SkillBridgeSurfaceMuted)
                                .clickable { selectedCat = cat }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cat.name, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) SkillBridgePrimaryHover else SkillBridgeText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AI Assist Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Description & Deliverables:", style = MaterialTheme.typography.labelSmall)
                    TextButton(
                        onClick = {
                            desc = "Deliverables: 1. Primary & Secondary Logo Vector, 2. Brand Color Palette & Typography, 3. Printable Box/Pouch labels (CMYK 300 DPI). 3 revision rounds included."
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("✨ AI Enhance Description", fontSize = 11.sp, color = SkillBridgePrimaryHover, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Budget (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Days") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetText.toIntOrNull() ?: 5000
                    val days = daysText.toIntOrNull() ?: 5
                    onConfirmPost(title, selectedCat.id, desc, budget, days)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                modifier = Modifier.testTag("submit_post_project_button")
            ) {
                Text("Post Project & Enable Escrow", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// In-App Chat Modal
@Composable
fun InAppChatDialog(
    jobTitle: String,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("In-App Order Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(jobTitle, style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted, maxLines = 1)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isSystem = msg.senderName == "System"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isSystem) Alignment.CenterHorizontally else if (msg.isFromWorker) Alignment.Start else Alignment.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSystem -> SkillBridgeSurfaceMuted
                                            msg.isFromWorker -> SkillBridgePrimarySoft
                                            else -> SkillBridgeBorder
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    fontSize = 12.sp,
                                    color = if (isSystem) SkillBridgePrimaryHover else SkillBridgeText
                                )
                            }
                            Text(
                                text = "${msg.senderName} • ${msg.time}",
                                fontSize = 9.sp,
                                color = SkillBridgeTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Type safe message...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSendMessage(input)
                                input = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SkillBridgePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = SkillBridgeWhite)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close Chat") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Rating & Release Escrow Dialog
@Composable
fun RatingReviewDialog(
    job: Job,
    onDismiss: () -> Unit,
    onSubmitReview: (rating: Float, comment: String) -> Unit
) {
    var rating by remember { mutableFloatStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("Very polite, arrived on time with complete tools, solved the tripping problem quickly!") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rate Service & Release Escrow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Job: ${job.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Star Rating selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star.toFloat() }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) SkillBridgeWarning else SkillBridgeTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    label = { Text("Your Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSuccess.copy(alpha = 0.12f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "✅ Submitting will release ₹${(job.budget * 0.85).toInt()} from Escrow to ${job.workerName ?: "worker"}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkillBridgeSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReview(rating, reviewComment)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text("Release Payment & Submit Rating", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Side-by-side Proposal Comparison Dialog (Section 6 & WS-6)
@Composable
fun CompareProposalsDialog(
    proposals: List<Proposal>,
    onSelectProposal: (Proposal) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Side-by-Side Proposal Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Compare verified workers, quotes, and delivery times:", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    proposals.take(2).forEach { prop ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, SkillBridgeBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${prop.workerAvatar} ${prop.workerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⭐ ${prop.workerRating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Trust: ${prop.workerTrustScore}%", fontSize = 11.sp, color = SkillBridgeSuccess, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("₹${prop.quotedPrice}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                                Text("${prop.daysToDeliver} Days Delivery", fontSize = 10.sp, color = SkillBridgeTextMuted)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("\"${prop.coverNote.take(45)}...\"", fontSize = 10.sp, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onSelectProposal(prop) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Hire", fontSize = 11.sp, color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// GST-Ready Tax Invoice Dialog (Section 6 & WS-3)
@Composable
fun GstInvoiceDialog(
    invoice: GstInvoice,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GST Tax Invoice (Tax Invoice)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(invoice.date, fontSize = 11.sp, color = SkillBridgeTextMuted)
                        }
                        Text("SkillBridge Marketplace Pvt Ltd", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("GSTIN: ${invoice.gstin} • SAC: ${invoice.sacCode}", fontSize = 10.sp, color = SkillBridgeTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Job: ${invoice.jobTitle}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Billed To: ${invoice.taskPosterName} | Executed By: ${invoice.workerName}", fontSize = 11.sp, color = SkillBridgeTextMuted)

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Base Service Charge:", fontSize = 12.sp)
                    Text("₹${invoice.baseAmountPaise / 100.0}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CGST (9.0%):", fontSize = 12.sp, color = SkillBridgeTextMuted)
                    Text("₹${invoice.cgstPaise / 100.0}", fontSize = 12.sp, color = SkillBridgeTextMuted)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SGST (9.0%):", fontSize = 12.sp, color = SkillBridgeTextMuted)
                    Text("₹${invoice.sgstPaise / 100.0}", fontSize = 12.sp, color = SkillBridgeTextMuted)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = SkillBridgeBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount Paid:", fontWeight = FontWeight.Bold)
                    Text("₹${invoice.totalAmountPaise / 100.0}", fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SkillBridgePrimary.copy(alpha = 0.08f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "⚠️ TEST MODE: GSTIN (${invoice.gstin}) & SAC (${invoice.sacCode}) are configuration placeholders to be replaced with official registered credentials before production launch.",
                        fontSize = 10.sp,
                        color = SkillBridgePrimaryHover,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text("Print / Save PDF", color = SkillBridgeWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Report & Block User Dialog (Section 10 & WS-4)
@Composable
fun ReportBlockUserDialog(
    targetName: String,
    onConfirmReportBlock: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("Unprofessional or rude conduct") }
    val reasons = listOf(
        "Unprofessional or rude conduct",
        "Requested payment outside platform",
        "Did not arrive as scheduled",
        "Safety or security concern"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = SkillBridgeDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report & Block $targetName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Blocking will immediately prevent this user from being matched with you in any future dispatch.", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                Spacer(modifier = Modifier.height(10.dp))
                reasons.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = r }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == r,
                            onClick = { selectedReason = r },
                            colors = RadioButtonDefaults.colors(selectedColor = SkillBridgeDanger)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(r, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmReportBlock(selectedReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeDanger)
            ) {
                Text("Block & Submit Report", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Business Account Team Roles Dialog (Section 6 & WS-6)
@Composable
fun BusinessTeamDialog(
    members: List<BusinessTeamMember>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Business Team & Role Delegation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Consolidated billing, team roles & multi-job management:", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                Spacer(modifier = Modifier.height(10.dp))

                members.forEach { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(m.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(m.email, fontSize = 10.sp, color = SkillBridgeTextMuted)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(SkillBridgePrimarySoft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(m.role, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text("Done", color = SkillBridgeWhite)
            }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Staff Authentication Login Gate for Admin Console (Server-Enforced RBAC)
@Composable
fun StaffLoginDialog(
    onAuthenticate: (email: String, secret: String, onError: (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("staff@skillbridge.in") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Staff & Admin Portal Authentication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Server-enforced RBAC portal. Enter authorized staff credentials with rate-limiting and audit logging protection:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Staff Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Staff Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = SkillBridgeDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    onAuthenticate(email, password) { err ->
                        isSubmitting = false
                        errorMessage = err
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text(if (isSubmitting) "Verifying..." else "Authenticate with Server", color = SkillBridgeWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// 19. Help Center & Support Ticketing Dialog
@Composable
fun HelpCenterDialog(
    onSubmitTicket: (subject: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf("Payment settlement inquiry") }
    var description by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Help Center & 24/7 Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Submit a support ticket to our dispute and assistance team:", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Issue Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe your problem in detail") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )

                if (submitted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✓ Ticket #SB-HELP-8921 opened. Agent will respond within 15 mins.", color = SkillBridgeSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        onSubmitTicket(subject, description)
                        submitted = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text("Submit Ticket", color = SkillBridgeWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// 20. Referral Program Dialog
@Composable
fun ReferralProgramDialog(
    referralCode: String = "RAMESH2026",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, tint = SkillBridgePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refer & Earn ₹200", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Invite friends and coworkers to SkillBridge. When they complete their first job, both of you receive ₹200 directly in your wallet!", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SkillBridgeSurfaceMuted)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(referralCode, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 2.sp, color = SkillBridgePrimaryHover)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Share this referral code via WhatsApp, SMS, or direct link.", fontSize = 11.sp, color = SkillBridgeTextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
            ) {
                Text("Share Code", color = SkillBridgeWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// 21. DPDP Act 2023 Data Erasure Dialog
@Composable
fun DataErasureDialog(
    onConfirmErasure: () -> Unit,
    onDismiss: () -> Unit
) {
    var acknowledged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = SkillBridgeDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DPDP Right to Erasure", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "In accordance with India's Digital Personal Data Protection (DPDP) Act 2023, you have the right to request permanent erasure of your personal data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SkillBridgeTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Profile details, phone number, and masked KYC records will be deleted.\n• Financial transaction logs will be anonymized per Indian statutory financial retention mandates.",
                    fontSize = 11.sp,
                    color = SkillBridgeText
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = acknowledged, onCheckedChange = { acknowledged = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("I confirm I want to erase my account data permanently.", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (acknowledged) {
                        onConfirmErasure()
                        onDismiss()
                    }
                },
                enabled = acknowledged,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeDanger)
            ) {
                Text("Permanently Erase", color = SkillBridgeWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = SkillBridgeBg,
        shape = RoundedCornerShape(16.dp)
    )
}
