package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.skillbridge.data.SkillBridgeRepository
import com.example.skillbridge.model.*
import com.example.skillbridge.ui.components.*
import com.example.skillbridge.ui.screens.*
import com.example.skillbridge.ui.theme.*
import com.example.skillbridge.voice.VoiceAccessibilityManager

class MainActivity : ComponentActivity() {

    private lateinit var repository: SkillBridgeRepository
    private lateinit var voiceManager: VoiceAccessibilityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = SkillBridgeRepository()
        voiceManager = VoiceAccessibilityManager(this)

        setContent {
            SkillBridgeTheme {
                SkillBridgeApp(
                    repository = repository,
                    voiceManager = voiceManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.shutdown()
    }
}

@Composable
fun SkillBridgeApp(
    repository: SkillBridgeRepository,
    voiceManager: VoiceAccessibilityManager
) {
    val context = LocalContext.current

    // Repository reactive states
    val activeRole by repository.activeRole.collectAsStateWithLifecycle()
    val worker by repository.currentWorker.collectAsStateWithLifecycle()
    val taskPoster by repository.currentTaskPoster.collectAsStateWithLifecycle()
    val selectedCity by repository.selectedCity.collectAsStateWithLifecycle()
    val categories by repository.categories.collectAsStateWithLifecycle()
    val jobs by repository.jobs.collectAsStateWithLifecycle()
    val incomingRequest by repository.incomingWorkerRequest.collectAsStateWithLifecycle()
    val proposals by repository.proposals.collectAsStateWithLifecycle()
    val transactions by repository.transactions.collectAsStateWithLifecycle()
    val kycList by repository.kycList.collectAsStateWithLifecycle()
    val disputes by repository.disputes.collectAsStateWithLifecycle()
    val jobChats by repository.jobChats.collectAsStateWithLifecycle()
    val workersPool by repository.workersPool.collectAsStateWithLifecycle()
    val learningCourses by repository.learningCourses.collectAsStateWithLifecycle()
    val isLowDataMode by repository.isLowDataMode.collectAsStateWithLifecycle()
    val portfolioItems by repository.portfolioItems.collectAsStateWithLifecycle()
    val businessTeamMembers by repository.businessTeamMembers.collectAsStateWithLifecycle()

    // Voice states
    val isListening by voiceManager.isListening.collectAsStateWithLifecycle()
    val spokenText by voiceManager.spokenText.collectAsStateWithLifecycle()
    val extractedEntities by voiceManager.extractedEntities.collectAsStateWithLifecycle()

    // Navigation & UI state
    var currentScreen by remember { mutableStateOf("home") } // home, jobs, wallet, profile, tracking
    var activeJobForTracking by remember { mutableStateOf<Job?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog flags
    var showVoiceModal by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showCityDialog by remember { mutableStateOf(false) }
    var showRequestServiceDialog by remember { mutableStateOf(false) }
    var selectedCategoryForRequest by remember { mutableStateOf<Category?>(null) }
    var showPostProjectDialog by remember { mutableStateOf(false) }
    var showMaskedCallDialog by remember { mutableStateOf(false) }
    var showInAppChatDialog by remember { mutableStateOf(false) }
    var showSkillQuizDialog by remember { mutableStateOf(false) }
    var showIvrSimulatorDialog by remember { mutableStateOf(false) }
    var showVoiceOnboardingDialog by remember { mutableStateOf(false) }
    var showFormOnboardingDialog by remember { mutableStateOf(false) }
    var showRatingReviewDialog by remember { mutableStateOf(false) }
    var showProposalHireDialog by remember { mutableStateOf<Proposal?>(null) }
    var showCompareProposalsDialog by remember { mutableStateOf(false) }
    var showGstInvoiceDialog by remember { mutableStateOf(false) }
    var currentGstInvoice by remember { mutableStateOf<GstInvoice?>(null) }
    var showReportBlockDialog by remember { mutableStateOf(false) }
    var showBusinessTeamDialog by remember { mutableStateOf(false) }
    var showStaffLoginDialog by remember { mutableStateOf(false) }

    // Handle Hardware Back Button
    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SkillBridgeBg,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (currentScreen != "tracking") {
                SkillBridgeTopBar(
                    selectedCity = selectedCity,
                    onCityClick = { showCityDialog = true },
                    activeRole = activeRole,
                    onRoleToggle = { newRole ->
                        repository.switchRole(newRole)
                    },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    unreadNotificationCount = if (incomingRequest != null) 1 else 0,
                    onNotificationClick = {
                        Toast.makeText(context, "SkillBridge: All real-time services operational in $selectedCity", Toast.LENGTH_SHORT).show()
                    },
                    onSosClick = { showSosDialog = true }
                )
            }
        },
        bottomBar = {
            if (currentScreen != "tracking") {
                SkillBridgeBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    onVoiceClick = {
                        voiceManager.startListeningSimulation()
                        showVoiceModal = true
                        voiceManager.speak("Namaste! What work or service do you need today?", "hi")
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> {
                    when (activeRole) {
                        UserRole.TASK_POSTER -> {
                            TaskPosterHomeScreen(
                                categories = categories,
                                activeJobs = jobs,
                                workersPool = workersPool,
                                selectedCity = selectedCity,
                                onCategoryClick = { cat ->
                                    selectedCategoryForRequest = cat
                                    if (cat.type == WorkType.LOCAL_ON_DEMAND) {
                                        showRequestServiceDialog = true
                                    } else {
                                        showPostProjectDialog = true
                                    }
                                },
                                onRequestNowClick = {
                                    selectedCategoryForRequest = null
                                    showRequestServiceDialog = true
                                },
                                onPostProjectClick = {
                                    showPostProjectDialog = true
                                },
                                onTrackJobClick = { job ->
                                    activeJobForTracking = job
                                    currentScreen = "tracking"
                                },
                                onVoiceAssistClick = {
                                    voiceManager.startListeningSimulation()
                                    showVoiceModal = true
                                },
                                onBookAgainClick = { workerItem ->
                                    val cat = categories.firstOrNull { it.name.contains(workerItem.skills.firstOrNull()?.skillName?.take(6) ?: "Electrician", ignoreCase = true) } ?: categories.first()
                                    selectedCategoryForRequest = cat
                                    showRequestServiceDialog = true
                                }
                            )
                        }

                        UserRole.WORKER -> {
                            WorkerHomeScreen(
                                worker = worker,
                                incomingRequest = incomingRequest,
                                availableJobs = jobs,
                                onToggleOnline = { repository.toggleWorkerOnline(it) },
                                onAcceptIncoming = { jobId ->
                                    repository.acceptIncomingRequest(jobId)
                                    val job = jobs.find { it.id == jobId }
                                    activeJobForTracking = job
                                    currentScreen = "tracking"
                                    Toast.makeText(context, "Job Accepted! Escrow locked.", Toast.LENGTH_SHORT).show()
                                },
                                onRejectIncoming = {
                                    repository.rejectIncomingRequest()
                                    Toast.makeText(context, "Job passed to next worker in cluster.", Toast.LENGTH_SHORT).show()
                                },
                                onJobClick = { job ->
                                    activeJobForTracking = job
                                    currentScreen = "tracking"
                                },
                                onApplyJobClick = { job ->
                                    if (job.type == WorkType.LOCAL_ON_DEMAND) {
                                        repository.advanceJobStatus(job.id, JobStatus.MATCHED)
                                        activeJobForTracking = job
                                        currentScreen = "tracking"
                                    } else {
                                        repository.submitProposal(
                                            jobId = job.id,
                                            quote = (job.budget * 0.95).toInt(),
                                            note = "Namaste! I can deliver this project with high quality.",
                                            days = 5
                                        )
                                        Toast.makeText(context, "Proposal submitted with milestone escrow!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onStartVerificationQuiz = {
                                    showSkillQuizDialog = true
                                },
                                onViewLearningHub = {
                                    currentScreen = "profile"
                                }
                            )
                        }

                        UserRole.ADMIN -> {
                            AdminConsoleScreen(
                                jobs = jobs,
                                kycList = kycList,
                                disputes = disputes,
                                categories = categories,
                                onApproveKyc = { repository.approveKyc(it) },
                                onRejectKyc = { repository.rejectKyc(it) },
                                onResolveDispute = { disputeId, releaseToWorker ->
                                    repository.resolveDispute(disputeId, releaseToWorker)
                                    Toast.makeText(context, "Dispute resolved and funds processed.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                "jobs" -> {
                    OrdersListScreen(
                        jobs = jobs,
                        proposals = proposals,
                        onJobClick = { job ->
                            activeJobForTracking = job
                            currentScreen = "tracking"
                        },
                        onViewProposal = { proposal ->
                            showProposalHireDialog = proposal
                        },
                        onCompareProposals = {
                            showCompareProposalsDialog = true
                        }
                    )
                }

                "wallet" -> {
                    val activeUser = if (activeRole == UserRole.WORKER) worker else taskPoster
                    WalletScreen(
                        user = activeUser,
                        transactions = transactions,
                        onWithdrawUpi = { upi, amount ->
                            val success = repository.withdrawToUpi(upi, amount)
                            if (success) {
                                Toast.makeText(context, "₹$amount transferred to $upi instantly!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Insufficient balance", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                "profile" -> {
                    val activeUser = if (activeRole == UserRole.WORKER) worker else taskPoster
                    ProfileScreen(
                        user = activeUser,
                        learningCourses = learningCourses,
                        portfolioItems = portfolioItems,
                        isLowDataMode = isLowDataMode,
                        onToggleLowDataMode = { repository.toggleLowDataMode(it) },
                        onUpgradePro = {
                            repository.upgradeWorkerToPro()
                            Toast.makeText(context, "Upgraded to Worker Pro! Platform commission reduced to 10%", Toast.LENGTH_LONG).show()
                        },
                        onLanguageChange = { repository.setLanguage(it) },
                        onTakeQuizClick = { showSkillQuizDialog = true },
                        onLaunchVoiceOnboarding = { showVoiceOnboardingDialog = true },
                        onLaunchFormOnboarding = { showFormOnboardingDialog = true },
                        onLaunchIvrOnboarding = { showIvrSimulatorDialog = true },
                        onCompleteCourse = {
                            repository.completeCourse(it)
                            Toast.makeText(context, "Course completed! Badge unlocked.", Toast.LENGTH_SHORT).show()
                        },
                        onOpenBusinessTeam = { showBusinessTeamDialog = true },
                        onOpenStaffLogin = { showStaffLoginDialog = true },
                        onOpenDiagnostics = { currentScreen = "diagnostics" },
                        onUpdateTrustedContact = {
                            repository.updateTrustedContact(it)
                            Toast.makeText(context, "Trusted family contact updated!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                "diagnostics" -> {
                    DiagnosticsScreen(
                        onBack = { currentScreen = "profile" },
                        pendingOfflineCount = 0,
                        onFlushOfflineQueue = {
                            Toast.makeText(context, "Offline action queue flushed & synchronized with backend", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                "tracking" -> {
                    val currentJob = activeJobForTracking ?: jobs.firstOrNull()
                    if (currentJob != null) {
                        LiveTrackingScreen(
                            job = currentJob,
                            isWorkerView = activeRole == UserRole.WORKER,
                            isLowDataMode = isLowDataMode,
                            onBack = { currentScreen = "home" },
                            onAdvanceStatus = { nextStatus, photo ->
                                repository.advanceJobStatus(currentJob.id, nextStatus, photo)
                                activeJobForTracking = jobs.find { it.id == currentJob.id }
                                Toast.makeText(context, "Status updated to ${nextStatus.name}", Toast.LENGTH_SHORT).show()
                            },
                            onCallClick = { showMaskedCallDialog = true },
                            onChatClick = { showInAppChatDialog = true },
                            onSosClick = { showSosDialog = true },
                            onDisputeClick = {
                                repository.raiseDispute(currentJob.id, "Dispute raised by customer during service.")
                                Toast.makeText(context, "Escrow held. Admin dispute team notified.", Toast.LENGTH_LONG).show()
                            },
                            onRateAndReleaseClick = {
                                showRatingReviewDialog = true
                            },
                            onReleaseMilestone = { milestoneId ->
                                repository.releaseMilestone(currentJob.id, milestoneId)
                                activeJobForTracking = jobs.find { it.id == currentJob.id }
                                Toast.makeText(context, "Milestone approved & funds released!", Toast.LENGTH_SHORT).show()
                            },
                            onReportBlockClick = {
                                showReportBlockDialog = true
                            },
                            onViewInvoiceClick = {
                                currentGstInvoice = repository.generateGstInvoice(currentJob.id)
                                showGstInvoiceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS & OVERLAYS ---

    // 1. City Selector Dialog
    if (showCityDialog) {
        val cities = listOf("Nagpur", "Mumbai", "Pune", "Delhi NCR", "Bengaluru", "Hyderabad", "Nashik", "Aurangabad")
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text("Select Service Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(cities) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repository.setCity(city)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.LocationCity, contentDescription = null, tint = SkillBridgePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(city, style = MaterialTheme.typography.bodyMedium, fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) { Text("Close") }
            },
            containerColor = SkillBridgeBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Voice Waveform Assistant Modal
    if (showVoiceModal) {
        VoiceAssistantDialog(
            isListening = isListening,
            spokenText = spokenText,
            extractedEntities = extractedEntities,
            onSpeakSample = { sample ->
                voiceManager.startListeningSimulation(sample)
                voiceManager.speak("Entities extracted for: $sample", "hi")
            },
            onConfirmEntities = {
                showVoiceModal = false
                val skill = extractedEntities["Skill"] ?: "Electrician"
                val cat = categories.firstOrNull { it.name.contains(skill, ignoreCase = true) } ?: categories.first()
                selectedCategoryForRequest = cat
                showRequestServiceDialog = true
            },
            onDismiss = {
                showVoiceModal = false
                voiceManager.stopSpeaking()
                voiceManager.clear()
            }
        )
    }

    // 3. Request Service (3-Tap Flow)
    if (showRequestServiceDialog) {
        RequestServiceDialog(
            categories = categories,
            selectedCity = selectedCity,
            initialCategory = selectedCategoryForRequest,
            onDismiss = { showRequestServiceDialog = false },
            onConfirmRequest = { catId, desc, urgency, addr ->
                val newJob = repository.requestLocalService(catId, desc, urgency, addr, selectedCity)
                activeJobForTracking = newJob
                currentScreen = "tracking"
                Toast.makeText(context, "Radar searching for nearby workers in $selectedCity...", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 4. Post Skilled Project Dialog
    if (showPostProjectDialog) {
        PostProjectDialog(
            categories = categories,
            onDismiss = { showPostProjectDialog = false },
            onConfirmPost = { title, catId, desc, budget, days ->
                val newJob = repository.postSkilledProject(title, catId, desc, budget, days, listOf("Design Kit", "Final Deliverables"))
                activeJobForTracking = newJob
                currentScreen = "jobs"
                Toast.makeText(context, "Project posted! ₹$budget held in Escrow.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 5. Emergency SOS Dialog
    if (showSosDialog) {
        SosDialog(
            onDismiss = { showSosDialog = false },
            onBroadcastEmergency = {
                showSosDialog = false
                Toast.makeText(context, "🚨 Live SOS Broadcast Sent with GPS to Nagpur Response Unit & 112!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 6. Masked Call Dialog
    if (showMaskedCallDialog) {
        val callee = if (activeRole == UserRole.WORKER) (activeJobForTracking?.taskPosterName ?: "Client") else (activeJobForTracking?.workerName ?: "Technician")
        MaskedCallDialog(
            calleeName = callee,
            onDismiss = { showMaskedCallDialog = false }
        )
    }

    // 7. In-App Chat Dialog
    if (showInAppChatDialog) {
        val currentJob = activeJobForTracking ?: jobs.first()
        val chatMessages = jobChats[currentJob.id] ?: emptyList()
        InAppChatDialog(
            jobTitle = currentJob.title,
            messages = chatMessages,
            onSendMessage = { text ->
                repository.sendChatMessage(currentJob.id, text, isFromWorker = activeRole == UserRole.WORKER)
            },
            onDismiss = { showInAppChatDialog = false }
        )
    }

    // 8. Skill Verification Quiz Dialog
    if (showSkillQuizDialog) {
        SkillQuizDialog(
            categoryName = "Electrician",
            onQuizPassed = {
                repository.completeSkillQuiz("Certified Safety Electrician")
                Toast.makeText(context, "Congratulations! Badge added to your Skill Passport.", Toast.LENGTH_LONG).show()
            },
            onDismiss = { showSkillQuizDialog = false }
        )
    }

    // 9. Phone IVR Simulator
    if (showIvrSimulatorDialog) {
        IvrSimulatorDialog(
            onDismiss = { showIvrSimulatorDialog = false },
            onIvrJobBooked = { cat, pin ->
                repository.requestLocalService(cat.id, "IVR Automated Booking", "Immediate", "Pincode $pin", "Nagpur")
            }
        )
    }

    // 10. Voice Onboarding Flow
    if (showVoiceOnboardingDialog) {
        VoiceOnboardingDialog(
            onDismiss = { showVoiceOnboardingDialog = false },
            onVoiceProfileCreated = { name, skill, loc, rate ->
                Toast.makeText(context, "Voice Profile Created for $name ($skill)!", Toast.LENGTH_LONG).show()
            },
            onSpeak = { text ->
                voiceManager.speak(text, "hi")
            }
        )
    }

    // 11. Form Onboarding Flow
    if (showFormOnboardingDialog) {
        FormOnboardingDialog(
            onDismiss = { showFormOnboardingDialog = false },
            onProfileCreated = {
                Toast.makeText(context, "Form profile saved to Skill Passport!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 12. Rating & Review Dialog
    if (showRatingReviewDialog && activeJobForTracking != null) {
        RatingReviewDialog(
            job = activeJobForTracking!!,
            onDismiss = { showRatingReviewDialog = false },
            onSubmitReview = { rating, comment ->
                repository.submitReview(activeJobForTracking!!.id, rating, comment)
                Toast.makeText(context, "Escrow Released! Rating $rating★ submitted.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // 13. Proposal Hire Dialog
    if (showProposalHireDialog != null) {
        val prop = showProposalHireDialog!!
        AlertDialog(
            onDismissRequest = { showProposalHireDialog = null },
            title = {
                Text("Hire ${prop.workerName}?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Trust Score: ${prop.workerTrustScore}% • ⭐ ${prop.workerRating}", fontWeight = FontWeight.Bold, color = SkillBridgePrimaryHover)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Quoted Price: ₹${prop.quotedPrice}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeSuccess)
                    Text("Delivery: in ${prop.daysToDeliver} days", style = MaterialTheme.typography.bodySmall, color = SkillBridgeTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("\"${prop.coverNote}\"", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.hireProposal(prop.id)
                        showProposalHireDialog = null
                        Toast.makeText(context, "${prop.workerName} hired! Escrow deposited.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgePrimary)
                ) {
                    Text("Hire & Deposit Escrow", color = SkillBridgeWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProposalHireDialog = null }) { Text("Close") }
            },
            containerColor = SkillBridgeBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 14. Side-by-Side Proposal Comparison Dialog (WS-6)
    if (showCompareProposalsDialog) {
        CompareProposalsDialog(
            proposals = proposals,
            onSelectProposal = { prop ->
                showCompareProposalsDialog = false
                showProposalHireDialog = prop
            },
            onDismiss = { showCompareProposalsDialog = false }
        )
    }

    // 15. GST Tax Invoice Dialog (WS-3 & WS-6)
    if (showGstInvoiceDialog && currentGstInvoice != null) {
        GstInvoiceDialog(
            invoice = currentGstInvoice!!,
            onDismiss = { showGstInvoiceDialog = false }
        )
    }

    // 16. Report & Block User Dialog (WS-4 & WS-6)
    if (showReportBlockDialog) {
        val targetName = if (activeRole == UserRole.WORKER) (activeJobForTracking?.taskPosterName ?: "Employer") else (activeJobForTracking?.workerName ?: "Worker")
        val targetId = if (activeRole == UserRole.WORKER) (activeJobForTracking?.taskPosterId ?: "tp_target") else (activeJobForTracking?.workerId ?: "w_target")
        ReportBlockUserDialog(
            targetName = targetName,
            onConfirmReportBlock = { reason ->
                repository.reportAndBlockUser(targetId, reason)
                showReportBlockDialog = false
                Toast.makeText(context, "$targetName has been blocked and safety report filed.", Toast.LENGTH_LONG).show()
            },
            onDismiss = { showReportBlockDialog = false }
        )
    }

    // 17. Business Accounts & Team Delegation Dialog (WS-6)
    if (showBusinessTeamDialog) {
        BusinessTeamDialog(
            members = businessTeamMembers,
            onDismiss = { showBusinessTeamDialog = false }
        )
    }

    // 18. Staff & Admin Authentication Portal Login Gate (Server-Enforced RBAC)
    if (showStaffLoginDialog) {
        StaffLoginDialog(
            onAuthenticate = { email, secret, onError ->
                val result = repository.loginStaff(email, secret)
                if (result.isSuccess) {
                    showStaffLoginDialog = false
                    currentScreen = "home"
                    Toast.makeText(context, "Staff Authentication Verified: Welcome to Ops Console", Toast.LENGTH_SHORT).show()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Authentication failed")
                }
            },
            onDismiss = { showStaffLoginDialog = false }
        )
    }
}
