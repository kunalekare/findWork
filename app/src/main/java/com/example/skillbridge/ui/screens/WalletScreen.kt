package com.example.skillbridge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.theme.*

private val WalletCardShape = RoundedCornerShape(22.dp)
private val WalletTileShape = RoundedCornerShape(16.dp)

@Composable
fun WalletScreen(
    user: User,
    transactions: List<Transaction>,
    onWithdrawUpi: (String, Int) -> Unit
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var upiIdInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    val parsedAmount = amountInput.toIntOrNull()
    val isUpiValid = upiIdInput.trim().matches(Regex("^[A-Za-z0-9._-]{2,}@[A-Za-z0-9.-]{2,}$"))
    val isAmountValid = parsedAmount != null && parsedAmount > 0 && parsedAmount <= user.walletBalance

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SkillBridgeBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Your Wallet",
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillBridgeText
                )
                Text(
                    "Instant payouts, escrow balance, and complete transaction ledger.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = SkillBridgeTextMuted
                )
            }
        }

        item {
            WalletBalanceCard(
                balance = user.walletBalance,
                isPro = user.isProMember,
                onWithdrawClick = {
                    upiIdInput = ""
                    amountInput = ""
                    showWithdrawDialog = true
                }
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WalletHighlight(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Shield,
                    title = "Escrow Ledger",
                    subtitle = "100% Protected",
                    tint = SkillBridgeSuccess
                )
                WalletHighlight(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ReceiptLong,
                    title = "Transparent Fee",
                    subtitle = if (user.isProMember) "10% Pro Fee" else "15% Platform Fee",
                    tint = SkillBridgePrimary
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transactions (${transactions.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeText
                )
                Text(
                    "Real-Time",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SkillBridgeSuccess
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = WalletTileShape,
                    color = SkillBridgeWhite,
                    border = BorderStroke(1.dp, SkillBridgeBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SkillBridgeSurfaceMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = SkillBridgePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            "No Transactions Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeText
                        )
                        Text(
                            "Complete bookings or receive payouts to see your ledger activity here.",
                            fontSize = 13.sp,
                            color = SkillBridgeTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(transaction = tx)
            }
        }
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SkillBridgePrimarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = SkillBridgePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "Instant UPI Payout",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeText
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SkillBridgeSurfaceMuted,
                        border = BorderStroke(1.dp, SkillBridgeBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Available for Withdrawal", fontSize = 13.sp, color = SkillBridgeTextMuted)
                            Text(
                                "₹${user.walletBalance}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgePrimary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = { Text("UPI ID (e.g. worker@okhdfcbank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkillBridgePrimary,
                            unfocusedBorderColor = SkillBridgeBorder
                        )
                    )

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkillBridgePrimary,
                            unfocusedBorderColor = SkillBridgeBorder
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(500, 1000, user.walletBalance).distinct().forEach { quickAmt ->
                            if (quickAmt > 0 && quickAmt <= user.walletBalance) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SkillBridgeWhite,
                                    border = BorderStroke(1.dp, SkillBridgeBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { amountInput = quickAmt.toString() }
                                ) {
                                    Text(
                                        text = if (quickAmt == user.walletBalance) "Max (₹$quickAmt)" else "₹$quickAmt",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SkillBridgeText,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Transfers are processed via NPCI UPI Rails within 15 seconds.",
                        fontSize = 11.sp,
                        color = SkillBridgeTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toIntOrNull() ?: 0
                        onWithdrawUpi(upiIdInput.trim(), amt)
                        showWithdrawDialog = false
                    },
                    enabled = isUpiValid && isAmountValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkillBridgePrimary,
                        disabledContainerColor = SkillBridgeBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("withdraw_upi_button")
                ) {
                    Text(
                        "Withdraw Now",
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeWhite
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel", color = SkillBridgeTextMuted)
                }
            },
            containerColor = SkillBridgeWhite,
            shape = WalletCardShape
        )
    }
}

@Composable
fun WalletBalanceCard(
    balance: Int,
    isPro: Boolean = false,
    onWithdrawClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WalletCardShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "AVAILABLE BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeTextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        "₹$balance",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SkillBridgeText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SkillBridgePrimarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = SkillBridgePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isPro) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SkillBridgeSurfaceMuted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = SkillBridgePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Worker Pro Member", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkillBridgeText)
                        }
                        Text(
                            "10% Flat Fee",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgePrimary
                        )
                    }
                }
            }

            Button(
                onClick = onWithdrawClick,
                enabled = balance > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("withdraw_upi_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkillBridgePrimary,
                    disabledContainerColor = SkillBridgeBorder
                )
            ) {
                Icon(
                    Icons.Default.ArrowOutward,
                    contentDescription = null,
                    tint = SkillBridgeWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Instant Withdraw to UPI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeWhite
                )
            }
        }
    }
}

@Composable
fun WalletHighlight(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = WalletTileShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeText
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = SkillBridgeTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    val isCredit = transaction.isCredit

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WalletTileShape,
        color = SkillBridgeWhite,
        border = BorderStroke(1.dp, SkillBridgeBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCredit) SkillBridgeSuccess.copy(alpha = 0.12f)
                            else SkillBridgeSurfaceMuted
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isCredit) SkillBridgeSuccess else SkillBridgePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        transaction.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SkillBridgeText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${transaction.timestamp} • UPI: ${transaction.upiReference}",
                        fontSize = 11.sp,
                        color = SkillBridgeTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${if (isCredit) "+" else "-"}₹${transaction.amount}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) SkillBridgeSuccess else SkillBridgeText
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCredit) SkillBridgeSuccess.copy(alpha = 0.12f) else SkillBridgeSurfaceMuted
                ) {
                    Text(
                        text = transaction.type,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) SkillBridgeSuccess else SkillBridgePrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}