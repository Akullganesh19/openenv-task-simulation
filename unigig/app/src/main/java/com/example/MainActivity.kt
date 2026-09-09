package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.OfflineSyncBanner
import com.example.ui.screens.AdminOversightScreen
import com.example.ui.screens.AiHubScreen
import com.example.ui.screens.GigDetailScreen
import com.example.ui.screens.GigsMarketplaceScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SkillsPortfolioScreen
import com.example.ui.screens.WalletLedgerScreen
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandMint
import com.example.ui.theme.BrandRose
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCanvas
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val userHandle by viewModel.currentUserName.collectAsState()
    val ledgerBalance by viewModel.ledgerBalance.collectAsState()

    // Issue #1, #65: Unauthenticated by default; Login required
    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyCanvas,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(NavySurface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo & App Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.selectTab(0) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandCyan)
                                .border(1.dp, BrandCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NavyCanvas,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UniGig",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = BrandCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = userHandle,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Role Badge & Coin Balance & Logout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Role Badge (fixed, not a dropdown/selector that allows spoofing - Issue #5)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (currentRole) {
                                        UserRole.ADMIN -> BrandRose.copy(alpha = 0.15f)
                                        UserRole.CLIENT -> BrandIndigo.copy(alpha = 0.15f)
                                        else -> BrandMint.copy(alpha = 0.15f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (currentRole) {
                                        UserRole.ADMIN -> BrandRose
                                        UserRole.CLIENT -> BrandIndigo
                                        else -> BrandMint
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = currentRole.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentRole) {
                                    UserRole.ADMIN -> BrandRose
                                    UserRole.CLIENT -> BrandIndigo
                                    else -> BrandMint
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Coin balance pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BrandCyan.copy(alpha = 0.12f))
                                .border(1.dp, BrandCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.selectTab(2) }
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${ledgerBalance.availableCoins}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Logout button
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.size(34.dp).testTag("logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Log Out",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                // Offline Local-Sync Indicator
                OfflineSyncBanner()
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavyCard,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) {
                // Tab 0: Gigs Marketplace
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace") },
                    label = { Text("Market", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = NavyCanvas
                    )
                )

                // Tab 1: Gig Details / Work
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Gig Detail") },
                    label = { Text("Detail", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = NavyCanvas
                    )
                )

                // Tab 2: Wallet & Ledger
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Wallet") },
                    label = { Text("Ledger", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = NavyCanvas
                    )
                )

                // Tab 3: Verified Skills
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Skills") },
                    label = { Text("Skills", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = NavyCanvas
                    )
                )

                // Tab 4: AI Hub
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Hub") },
                    label = { Text("AI Hub", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandCyan,
                        selectedTextColor = BrandCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = NavyCanvas
                    )
                )

                // Tab 5: Admin Oversight (Issue #4: ONLY shown if current user is ADMIN)
                if (currentRole == UserRole.ADMIN) {
                    NavigationBarItem(
                        selected = selectedTab == 5,
                        onClick = { viewModel.selectTab(5) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Governance") },
                        label = { Text("Admin", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandRose,
                            selectedTextColor = BrandRose,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NavyCanvas
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> GigsMarketplaceScreen(viewModel = viewModel)
                1 -> GigDetailScreen(viewModel = viewModel)
                2 -> WalletLedgerScreen(viewModel = viewModel)
                3 -> SkillsPortfolioScreen(viewModel = viewModel)
                4 -> AiHubScreen(viewModel = viewModel)
                5 -> AdminOversightScreen(viewModel = viewModel)
                else -> GigsMarketplaceScreen(viewModel = viewModel)
            }
        }
    }
}
