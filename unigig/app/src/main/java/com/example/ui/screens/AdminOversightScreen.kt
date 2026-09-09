package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DisputeEntity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.CoinBadge
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandMint
import com.example.ui.theme.BrandRose
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminOversightScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val disputes by viewModel.allDisputes.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val highThinkingResult by viewModel.highThinkingResult.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedDisputeForResolve by remember { mutableStateOf<DisputeEntity?>(null) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    // Issue #3: Strict Role Gate. If user is NOT Admin, block screen immediately!
    if (currentRole != UserRole.ADMIN) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandRose)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = BrandRose,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "403 Forbidden: Admin Role Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This terminal contains multi-portal dispute arbitration, platform audit logs, and Blnk root ledger controls. Authenticate with an authorized administrator credential to proceed.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.selectTab(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                    ) {
                        Text("Return to Marketplace")
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Governance Oversight",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandCyan.copy(alpha = 0.15f))
                        .border(1.dp, BrandCyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("SuperAdmin Root", fontSize = 11.sp, color = BrandCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Platform Escrow Stats (Issue #64: real values from Room transactions)
        item {
            val totalVolume = transactions.sumOf { it.amount }
            val totalSettled = transactions.filter { it.type in listOf("ESCROW_RELEASE", "ESCROW_RELEASE_PAYOUT") }.sumOf { it.amount }
            val feeRevenue = transactions.filter { it.type in listOf("PLATFORM_FEE", "PLATFORM_FEE_10") }.sumOf { it.amount }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Ledger Audit Summary (Blnk Core)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(label = "Total Volume", value = "$totalVolume Coins", sub = "$${String.format("%.2f", totalVolume * 0.10f)} USD", modifier = Modifier.weight(1f))
                        StatCard(label = "Settled Payouts", value = "$totalSettled Coins", sub = "$${String.format("%.2f", totalSettled * 0.10f)} USD", modifier = Modifier.weight(1f))
                        StatCard(label = "Protocol Revenue", value = "$feeRevenue Coins", sub = "10% Platform Fee", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Manual Student KYC Certification Queue (Round 3 Audit #3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BrandMint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Institutional KYC Certification Queue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandMint.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Manual Gating", fontSize = 10.sp, color = BrandMint, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Students must be manually certified by governance before milestone payouts and bids are unlocked.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val studentUsers = allUsers.filter { it.role == "STUDENT" }
                    if (studentUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySurface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "No registered student accounts found in directory.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    } else {
                        studentUsers.forEach { student ->
                            val isCertified = student.kycStatus == "VERIFIED_LEVEL_3"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySurface)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${student.fullName} (${student.email})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "ID: ${student.id} • Status: ${student.kycStatus} • ${student.campusAffiliation}",
                                        fontSize = 10.sp,
                                        color = if (isCertified) BrandMint else BrandCyan
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!isCertified) {
                                        Button(
                                            onClick = {
                                                viewModel.reviewAndCertifyKyc(student.id, true) { success, err ->
                                                    actionFeedback = if (success) "${student.fullName} successfully certified to VERIFIED_LEVEL_3." else "Error: $err"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandMint),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Certify L3", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.reviewAndCertifyKyc(student.id, false) { success, err ->
                                                    actionFeedback = if (success) "${student.fullName} KYC rejected." else "Error: $err"
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Reject", fontSize = 11.sp, color = BrandRose)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BrandMint.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Certified L3", fontSize = 11.sp, color = BrandMint, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Manual Strike Issuance Quick Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Three-Strike Rule Governance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "3 strikes automatically bans account from proposal bidding",
                                fontSize = 10.sp,
                                color = BrandAmber
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.issueDisciplinaryStrike("usr_student_alex", "Manual governance review: Academic policy violation") { strikes, banned ->
                                    actionFeedback = if (banned) "Strike logged. Total: $strikes. Account has reached 3 strikes and is now BANNED."
                                    else "Disciplinary strike issued to usr_student_alex. Total active strikes: $strikes/3."
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Issue Strike", fontSize = 11.sp, color = BrandAmber)
                        }
                    }
                }
            }
        }

        // Active Disputes List (Issue #61, #62, #63, #100)
        item {
            Text(
                text = "Active Escrow Disputes (${disputes.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(disputes) { disp ->
            DisputeCard(
                dispute = disp,
                onResolveClick = { selectedDisputeForResolve = disp },
                onEscalateCeoClick = {
                    viewModel.escalateDisputeToCeo(disp) { success, err ->
                        if (success) actionFeedback = "Dispute escalated to Human CEO Review Tribunal."
                        else actionFeedback = err
                    }
                },
                onRunAiArbitrator = {
                    viewModel.runHighThinkingMediation(
                        "Analyze code test coverage, Dolos originality check, and rubric for dispute ${disp.id}: ${disp.reason}"
                    )
                }
            )
        }

        // SmartResolution High Thinking Tribunal Box (Issue #50)
        if (highThinkingResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandCyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SmartResolution AI Arbitrator Verdict",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = highThinkingResult!!,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Modal: Admin Dispute Resolution Dialog (Issue #61, #63, Round 3 Audit #7)
    selectedDisputeForResolve?.let { disp ->
        var verdictText by remember { mutableStateOf("Release 70% to student for passing tests, refund 30% to client.") }
        var penalizeRespondent by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { selectedDisputeForResolve = null },
            title = { Text("Binding Dispute Settlement", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Dispute: ${disp.id} (${disp.gigTitle})\nAmount: ${disp.amount} Coins ($${String.format("%.2f", disp.amount * 0.10f)} USD)\nClaimant: ${disp.claimant} vs ${disp.respondent}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = verdictText,
                        onValueChange = { verdictText = it },
                        label = { Text("Official Ruling & Ledger Distribution") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = penalizeRespondent,
                            onCheckedChange = { penalizeRespondent = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandRose)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Penalize respondent with an Academic Integrity Strike",
                            fontSize = 12.sp,
                            color = if (penalizeRespondent) BrandRose else TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveDispute(disp, verdictText, penalizeRespondent) { success, err ->
                            if (success) {
                                actionFeedback = if (penalizeRespondent) {
                                    "Dispute resolved, funds reallocated, and 1 Disciplinary Strike issued to respondent."
                                } else {
                                    "Dispute resolved and ledger reallocated."
                                }
                                selectedDisputeForResolve = null
                            } else {
                                actionFeedback = err
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Text("Execute Binding Settlement", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDisputeForResolve = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavyCard
        )
    }

    // Feedback Dialog
    actionFeedback?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionFeedback = null },
            title = { Text("Governance Notice", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(msg, color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { actionFeedback = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Text("OK", color = Color.Black)
                }
            },
            containerColor = NavyCard
        )
    }
}

@Composable
fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = sub, fontSize = 9.sp, color = BrandMint)
        }
    }
}

@Composable
fun DisputeCard(
    dispute: DisputeEntity,
    onResolveClick: () -> Unit,
    onEscalateCeoClick: () -> Unit,
    onRunAiArbitrator: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dispute_card_${dispute.id}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (dispute.status == "HUMAN_CEO_ESCALATED") BrandAmber else BrandRose
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dispute.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BrandRose
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (dispute.status == "RESOLVED") BrandMint.copy(alpha = 0.15f)
                                else if (dispute.status == "HUMAN_CEO_ESCALATED") BrandAmber.copy(alpha = 0.15f)
                                else BrandRose.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = dispute.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (dispute.status) {
                                "RESOLVED" -> BrandMint
                                "HUMAN_CEO_ESCALATED" -> BrandAmber
                                else -> BrandRose
                            }
                        )
                    }
                }
                CoinBadge(amount = dispute.amount, label = "Disputed")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dispute.gigTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Reason: ${dispute.reason}",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tribunal Status: ${dispute.verdict}",
                fontSize = 11.sp,
                color = BrandCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Resolve, Escalate to CEO, AI Arbitrator) - Issues #61, #62, #100
            if (dispute.status != "RESOLVED") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onResolveClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_resolve_btn_${dispute.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Resolve", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRunAiArbitrator,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("AI Analysis", fontSize = 11.sp)
                    }

                    if (dispute.status != "HUMAN_CEO_ESCALATED") {
                        OutlinedButton(
                            onClick = onEscalateCeoClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CEO Review", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
