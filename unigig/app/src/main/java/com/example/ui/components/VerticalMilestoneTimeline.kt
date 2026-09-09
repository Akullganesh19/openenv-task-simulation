package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MilestoneEntity
import com.example.data.model.UserRole
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandMint
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun VerticalMilestoneTimelineTracker(
    milestones: List<MilestoneEntity>,
    totalBudgetCoins: Int,
    currentRole: UserRole,
    onSubmitDraftClick: (MilestoneEntity) -> Unit,
    onApproveClick: (MilestoneEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = milestones.size
    val approvedCount = milestones.count { it.status == "Approved" }
    val submittedCount = milestones.count { it.status == "Submitted" }
    val releasedCoins = milestones.filter { it.status == "Approved" }.sumOf { it.coins }
    val progressFraction = if (totalCount > 0) approvedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercent = (progressFraction * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vertical_milestone_timeline_tracker"),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Timeline Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Timeline Icon",
                            tint = BrandCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Milestone Stage Progress",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$approvedCount of $totalCount stages verified",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                // Escrow release summary pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(BrandMint.copy(alpha = 0.12f))
                        .border(1.dp, BrandMint.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$releasedCoins / $totalBudgetCoins Coins Settled",
                        fontSize = 11.sp,
                        color = BrandMint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Completion Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completion: $progressPercent%",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when {
                            approvedCount == totalCount && totalCount > 0 -> "All Milestones Settled"
                            submittedCount > 0 -> "$submittedCount Awaiting Approval"
                            else -> "In Active Development"
                        },
                        fontSize = 11.sp,
                        color = when {
                            approvedCount == totalCount && totalCount > 0 -> BrandMint
                            submittedCount > 0 -> BrandAmber
                            else -> BrandCyan
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrandMint,
                    trackColor = NavyCardElevated,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Vertical Timeline Items
            milestones.forEachIndexed { index, milestone ->
                val isLast = index == milestones.lastIndex
                val isApproved = milestone.status == "Approved"
                val isSubmitted = milestone.status == "Submitted"
                val isCurrentActive = !isApproved && !isSubmitted && (index == 0 || milestones[index - 1].status == "Approved")

                TimelineMilestoneRow(
                    index = index,
                    milestone = milestone,
                    isLast = isLast,
                    isApproved = isApproved,
                    isSubmitted = isSubmitted,
                    isCurrentActive = isCurrentActive,
                    currentRole = currentRole,
                    onSubmitDraftClick = { onSubmitDraftClick(milestone) },
                    onApproveClick = { onApproveClick(milestone) }
                )
            }
        }
    }
}

@Composable
private fun TimelineMilestoneRow(
    index: Int,
    milestone: MilestoneEntity,
    isLast: Boolean,
    isApproved: Boolean,
    isSubmitted: Boolean,
    isCurrentActive: Boolean,
    currentRole: UserRole,
    onSubmitDraftClick: () -> Unit,
    onApproveClick: () -> Unit
) {
    val nodeBgColor by animateColorAsState(
        targetValue = when {
            isApproved -> BrandMint
            isSubmitted -> BrandAmber
            isCurrentActive -> BrandIndigo
            else -> NavyCardElevated
        },
        animationSpec = tween(durationMillis = 300),
        label = "nodeBgColor"
    )
    val nodeBorderColor by animateColorAsState(
        targetValue = when {
            isApproved -> BrandMint.copy(alpha = 0.5f)
            isSubmitted -> BrandAmber.copy(alpha = 0.5f)
            isCurrentActive -> BrandCyan
            else -> NavyBorder
        },
        animationSpec = tween(durationMillis = 300),
        label = "nodeBorderColor"
    )
    val connectorColor by animateColorAsState(
        targetValue = when {
            isApproved -> BrandMint
            isSubmitted -> BrandAmber.copy(alpha = 0.6f)
            else -> NavyBorder
        },
        animationSpec = tween(durationMillis = 300),
        label = "connectorColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .testTag("milestone_timeline_row_$index")
    ) {
        // Vertical Timeline Column (Node Icon + Connecting Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Milestone Node
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(nodeBgColor)
                    .border(2.dp, nodeBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isApproved -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed Stage",
                            tint = NavyCanvas,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    isSubmitted -> {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Submitted for Review",
                            tint = NavyCanvas,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    isCurrentActive -> {
                        Text(
                            text = "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Pending Stage",
                            tint = TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Connecting Vertical Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(connectorColor, shape = RoundedCornerShape(2.dp))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Milestone Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 16.dp)
                .testTag("milestone_card_${milestone.id}"),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentActive) NavyCardElevated else NavySurface
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isCurrentActive) BrandCyan.copy(alpha = 0.5f) else NavyBorder
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Stage header & Coin Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "STAGE ${index + 1}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isApproved -> BrandMint
                                isSubmitted -> BrandAmber
                                isCurrentActive -> BrandCyan
                                else -> TextTertiary
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isApproved -> BrandMint.copy(alpha = 0.15f)
                                        isSubmitted -> BrandAmber.copy(alpha = 0.15f)
                                        isCurrentActive -> BrandIndigo.copy(alpha = 0.2f)
                                        else -> NavyBorder.copy(alpha = 0.4f)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    isApproved -> "VERIFIED & SETTLED"
                                    isSubmitted -> "IN REVIEW"
                                    isCurrentActive -> "ACTIVE STAGE"
                                    else -> "ESCROW LOCKED"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isApproved -> BrandMint
                                    isSubmitted -> BrandAmber
                                    isCurrentActive -> BrandCyan
                                    else -> TextSecondary
                                }
                            )
                        }
                    }

                    // Payout Badge
                    CoinBadge(amount = milestone.coins, label = "Coins")
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Milestone Title
                Text(
                    text = milestone.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Submissions metadata: Gitea Commit + Dolos plagiarism
                if (milestone.submissionHash != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gitea Commit Hash Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyCanvas)
                                .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Commit Hash",
                                tint = BrandCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = milestone.submissionHash,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        // Dolos Plagiarism Score Chip (Issue #58)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyCanvas)
                                .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = "Dolos Originality Check",
                                tint = if (milestone.plagiarismScore < 5f) BrandMint else BrandAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dolos ${String.format("%.1f", milestone.plagiarismScore)}%",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (milestone.plagiarismScore < 5f) BrandMint else BrandAmber
                            )
                        }
                    }
                }

                // Interactive Action Button
                val showStudentSubmit = currentRole == UserRole.STUDENT && !isApproved
                val showClientApprove = (currentRole == UserRole.CLIENT || currentRole == UserRole.ADMIN) && isSubmitted

                if (showStudentSubmit || showClientApprove) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (showStudentSubmit) {
                            Button(
                                onClick = onSubmitDraftClick,
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("submit_milestone_${milestone.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSubmitted) "Update Submission" else "Submit Draft Deliverable",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (showClientApprove) {
                            Button(
                                onClick = onApproveClick,
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("approve_milestone_${milestone.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandMint),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NavyCanvas,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Approve & Settle 90% (${(milestone.coins * 0.9).toInt()} Coins)",
                                    fontSize = 11.sp,
                                    color = NavyCanvas,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
