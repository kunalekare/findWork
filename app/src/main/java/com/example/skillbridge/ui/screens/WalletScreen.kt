package com.example.skillbridge.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

@Composable
fun WalletScreen(
    user: User,
    transactions: List<Transaction>,
    onWithdrawUpi: (String, Int) -> Unit
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var upiIdInput by remember { mutableStateOf("ramesh.iti@okaxis") }
    var amountInput by remember { mutableStateOf("1500") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Wallet & Escrow Ledger",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "100% Protected Escrow • Instant UPI Settlements",
            style = MaterialTheme.typography.bodySmall,
            color = SkillBridgeTextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Card (Section 12: Orange card with white large text)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(16.dp))
                .testTag("wallet_balance_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgePrimary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AVAILABLE FOR WITHDRAWAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeWhite.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "₹${user.walletBalance}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = SkillBridgeWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Platform Fee Model",
                            fontSize = 11.sp,
                            color = SkillBridgeWhite.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "15% Transparent Fee",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeWhite
                        )
                    }

                    Button(
                        onClick = { showWithdrawDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkillBridgeWhite,
                            contentColor = SkillBridgePrimaryHover
                        ),
                        modifier = Modifier.testTag("withdraw_upi_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Withdraw to UPI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Commission & Escrow guarantee badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeSurfaceMuted),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = SkillBridgeSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Double-Entry Idempotent Ledger",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText
                    )
                    Text(
                        text = "Escrow funds are isolated until job completion. Automatic UPI payouts.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = SkillBridgeTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SkillBridgeBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (tx.isCredit) SkillBridgeSuccess.copy(alpha = 0.12f) else SkillBridgePrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (tx.isCredit) SkillBridgeSuccess else SkillBridgePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${tx.timestamp} • ${tx.upiReference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = SkillBridgeTextMuted
                                )
                            }
                        }

                        Text(
                            text = "${if (tx.isCredit) "+" else "-"}₹${tx.amount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.isCredit) SkillBridgeSuccess else SkillBridgeText
                        )
                    }
                }
            }
        }
    }

    // Withdraw Dialog
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Text("Instant UPI Payout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Transfer earnings to any UPI ID / VPA. Free instant payout.", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = { Text("UPI ID (e.g. mobile@upi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountInput.toIntOrNull() ?: 1000
                        onWithdrawUpi(upiIdInput, amount)
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text("Confirm Transfer", color = SkillBridgeWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") }
            },
            containerColor = SkillBridgeBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
