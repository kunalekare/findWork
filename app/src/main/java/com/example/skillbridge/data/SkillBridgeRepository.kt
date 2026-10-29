package com.example.skillbridge.data

import com.example.skillbridge.model.*
import com.example.skillbridge.provider.*
import com.example.skillbridge.data.offline.OfflineActionQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SkillBridgeRepository {

    // Current active profile
    private val _currentWorker = MutableStateFlow(
        User(
            id = "w_101",
            name = "Ramesh Sharma",
            phone = "+91 98220 12345",
            email = "ramesh.sharma@skillbridge.in",
            role = UserRole.WORKER,
            avatarEmoji = "⚡",
            isOnline = true,
            isKycVerified = true,
            trustScore = 94,
            rating = 4.85f,
            reviewCount = 58,
            jobsCompleted = 64,
            hourlyRate = 450,
            serviceRadiusKm = 10,
            locationCity = "Nagpur",
            locationArea = "Dharampeth",
            language = AppLanguage.HINDI,
            bio = "Govt. ITI certified senior electrician. 7+ yrs solving industrial and household tripping, rewiring, and appliance setups.",
            badges = listOf("Verified Electrician", "Top Rated 2026", "100% On-Time", "Zero Safety Incident"),
            skills = listOf(
                WorkerSkill("Electrical Wiring & MCB", "Expert", true, 42),
                WorkerSkill("Inverter & UPS Setup", "Experienced", true, 18),
                WorkerSkill("Appliance Repair", "Experienced", false, 12)
            ),
            endorsements = 34,
            responseTimeMinutes = 2,
            cancellationRatePercent = 1.2,
            walletBalance = 4250
        )
    )
    val currentWorker: StateFlow<User> = _currentWorker.asStateFlow()

    private val _currentTaskPoster = MutableStateFlow(
        User(
            id = "tp_201",
            name = "Anjali Deshmukh",
            phone = "+91 94221 67890",
            email = "anjali.deshmukh@gmail.com",
            role = UserRole.TASK_POSTER,
            avatarEmoji = "👩‍💼",
            isOnline = true,
            isKycVerified = true,
            trustScore = 98,
            rating = 4.9f,
            reviewCount = 19,
            jobsCompleted = 22,
            hourlyRate = 0,
            serviceRadiusKm = 25,
            locationCity = "Nagpur",
            locationArea = "Ramdaspeth",
            language = AppLanguage.ENGLISH,
            bio = "Small business owner & resident. Regularly hires skilled technicians and creative freelancers.",
            walletBalance = 12500
        )
    )
    val currentTaskPoster: StateFlow<User> = _currentTaskPoster.asStateFlow()

    private val _activeRole = MutableStateFlow(UserRole.TASK_POSTER)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()

    private val _selectedCity = MutableStateFlow("Nagpur")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.HINDI)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // 14 Categories (Blue collar & White collar)
    private val _categories = MutableStateFlow(
        listOf(
            Category("cat_elec", "Electrician", "इलेक्ट्रीशियन", "इलेक्ट्रिशियन", WorkType.LOCAL_ON_DEMAND, "bolt", 400, "₹/hr", isHotspot = true, "Wiring, short circuit, switchboard, fuse fix"),
            Category("cat_plumb", "Plumber", "प्लम्बर", "प्लंबर", WorkType.LOCAL_ON_DEMAND, "plumbing", 350, "₹/hr", isHotspot = false, "Leak repair, tap change, pipeline, motor"),
            Category("cat_cook", "Cook & Chef", "रसोइया / कुक", "स्वयंपाकी", WorkType.LOCAL_ON_DEMAND, "restaurant", 500, "₹/day", isHotspot = true, "Daily home meals, parties, North/South Indian dishes"),
            Category("cat_maid", "Maid / Clean", "सफाई / हेल्पर", "घरकाम / स्वच्छता", WorkType.LOCAL_ON_DEMAND, "cleaning_services", 300, "₹/hr", isHotspot = false, "Deep cleaning, dusting, dishwashing, home help"),
            Category("cat_driver", "Driver", "ड्राइवर", "चालक", WorkType.LOCAL_ON_DEMAND, "drive_eta", 600, "₹/day", isHotspot = true, "Personal car driver, outstation, commercial transport"),
            Category("cat_painter", "Painter", "पेंटर", "रंगारी", WorkType.LOCAL_ON_DEMAND, "format_paint", 800, "₹/day", isHotspot = false, "Interior, exterior wall paint, waterproof coating"),
            Category("cat_tailor", "Tailor & Sewing", "दर्जी / सिलाई", "शिंपी", WorkType.LOCAL_ON_DEMAND, "content_cut", 250, "fixed", isHotspot = false, "Suit stitching, dress fitting, alterations"),
            Category("cat_appliance", "Appliance Repair", "उपकरण मरम्मत", "उपकरण दुरुस्ती", WorkType.LOCAL_ON_DEMAND, "build", 450, "fixed", isHotspot = true, "AC service, Refrigerator, Washing machine repair"),
            Category("cat_mason", "Mason & Civil", "राजमिस्त्री", "गवंडी", WorkType.LOCAL_ON_DEMAND, "foundation", 900, "₹/day", isHotspot = false, "Brickwork, plaster, tile repair, floor laying"),
            Category("cat_delivery", "Express Delivery", "डिलीवरी", "डिलिव्हरी", WorkType.LOCAL_ON_DEMAND, "local_shipping", 150, "fixed", isHotspot = true, "Parcel delivery, medicine pickup, documents"),
            // White Collar / Remote Skilled Work
            Category("cat_web", "Web & App Dev", "वेब और ऐप", "वेब आणि अ‍ॅप", WorkType.PROJECT_SKILLED, "code", 15000, "fixed", isHotspot = true, "Next.js, Android apps, full-stack, bug fixes"),
            Category("cat_design", "Graphic Design", "ग्राफिक डिज़ाइन", "ग्राफिक डिझाइन", WorkType.PROJECT_SKILLED, "brush", 3500, "fixed", isHotspot = true, "Logos, banners, social media creatives, brochures"),
            Category("cat_content", "Content & Hindi", "कंटेंट लेखन", "कंटेंट लेखन", WorkType.PROJECT_SKILLED, "edit_note", 2000, "fixed", isHotspot = false, "Articles, translation, SEO copywriting, scripting"),
            Category("cat_tutor", "Tutor / Teacher", "ट्यूटर / शिक्षक", "खाजगी शिक्षक", WorkType.PROJECT_SKILLED, "school", 500, "₹/hr", isHotspot = true, "Maths, Science, Spoken English, Coding classes")
        )
    )
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Jobs List
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    // Incoming request for worker (with 20s timer trigger)
    private val _incomingWorkerRequest = MutableStateFlow<Job?>(null)
    val incomingWorkerRequest: StateFlow<Job?> = _incomingWorkerRequest.asStateFlow()

    // Proposals for projects
    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals.asStateFlow()

    // Transactions / Ledger
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // KYC Records
    private val _kycList = MutableStateFlow<List<KycVerificationItem>>(emptyList())
    val kycList: StateFlow<List<KycVerificationItem>> = _kycList.asStateFlow()

    // Disputes
    private val _disputes = MutableStateFlow<List<DisputeRecord>>(emptyList())
    val disputes: StateFlow<List<DisputeRecord>> = _disputes.asStateFlow()

    // Chat messages keyed by jobId
    private val _jobChats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val jobChats: StateFlow<Map<String, List<ChatMessage>>> = _jobChats.asStateFlow()

    // Available Workers for matching/browsing
    private val _workersPool = MutableStateFlow<List<User>>(emptyList())
    val workersPool: StateFlow<List<User>> = _workersPool.asStateFlow()

    // Learning courses
    private val _learningCourses = MutableStateFlow<List<LearningCourse>>(emptyList())
    val learningCourses: StateFlow<List<LearningCourse>> = _learningCourses.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        // Seed Workers
        val workers = listOf(
            _currentWorker.value,
            User(
                id = "w_102",
                name = "Priya Patil",
                phone = "+91 97654 32109",
                email = "priya.patil@skillbridge.in",
                role = UserRole.WORKER,
                avatarEmoji = "🎨",
                isOnline = true,
                isKycVerified = true,
                trustScore = 96,
                rating = 4.95f,
                reviewCount = 38,
                jobsCompleted = 45,
                hourlyRate = 600,
                serviceRadiusKm = 50,
                locationCity = "Nagpur",
                locationArea = "Pratap Nagar",
                language = AppLanguage.MARATHI,
                bio = "UI/UX & Branding Designer with 5 years experience creating startup logos, product decks, and social media branding.",
                badges = listOf("Top Rated Designer", "100% Client Recommended", "Skill Verified"),
                skills = listOf(WorkerSkill("Brand Identity", "Expert", true, 30), WorkerSkill("Figma & UI", "Expert", true, 22)),
                endorsements = 29
            ),
            User(
                id = "w_103",
                name = "Amit Khobragade",
                phone = "+91 93255 44332",
                email = "amit.k@skillbridge.in",
                role = UserRole.WORKER,
                avatarEmoji = "🔧",
                isOnline = true,
                isKycVerified = true,
                trustScore = 91,
                rating = 4.7f,
                reviewCount = 62,
                jobsCompleted = 78,
                hourlyRate = 350,
                serviceRadiusKm = 15,
                locationCity = "Nagpur",
                locationArea = "Sadar",
                language = AppLanguage.HINDI,
                bio = "Master plumber specializing in bathroom fixture replacements, leakage detection and overhead tank cleaning.",
                badges = listOf("Leakage Specialist", "Quick Responder"),
                skills = listOf(WorkerSkill("Plumbing Leak Repair", "Expert", true, 60)),
                endorsements = 25
            ),
            User(
                id = "w_104",
                name = "Sunita Bai Verma",
                phone = "+91 91580 99887",
                email = "sunita.verma@skillbridge.in",
                role = UserRole.WORKER,
                avatarEmoji = "🍳",
                isOnline = true,
                isKycVerified = true,
                trustScore = 95,
                rating = 4.9f,
                reviewCount = 84,
                jobsCompleted = 110,
                hourlyRate = 500,
                serviceRadiusKm = 8,
                locationCity = "Nagpur",
                locationArea = "Civil Lines",
                language = AppLanguage.HINDI,
                bio = "Expert home cook for healthy, hygienic North Indian & Maharashtrian vegetarian cooking. 10+ yrs home experience.",
                badges = listOf("Master Home Chef", "5-Star Taste", "Hygiene Certified"),
                skills = listOf(WorkerSkill("Maharashtrian Cuisine", "Expert", true, 80), WorkerSkill("North Indian Meals", "Expert", true, 70)),
                endorsements = 48
            ),
            User(
                id = "w_105",
                name = "Vikram Singh",
                phone = "+91 98901 11223",
                email = "vikram.singh@skillbridge.in",
                role = UserRole.WORKER,
                avatarEmoji = "🚗",
                isOnline = true,
                isKycVerified = true,
                trustScore = 89,
                rating = 4.75f,
                reviewCount = 51,
                jobsCompleted = 85,
                hourlyRate = 650,
                serviceRadiusKm = 40,
                locationCity = "Nagpur",
                locationArea = "Manish Nagar",
                language = AppLanguage.HINDI,
                bio = "Professional chauffeur & driver holding commercial badge. Fluent in city routes and outstation highways.",
                badges = listOf("Defensive Driving", "Punctual Driver", "Aadhaar Verified"),
                skills = listOf(WorkerSkill("Manual & Automatic Driving", "Expert", true, 85)),
                endorsements = 20
            ),
            User(
                id = "w_106",
                name = "Arjun Mehta",
                phone = "+91 98230 77889",
                email = "arjun.m@skillbridge.in",
                role = UserRole.WORKER,
                avatarEmoji = "💻",
                isOnline = true,
                isKycVerified = true,
                trustScore = 97,
                rating = 4.92f,
                reviewCount = 31,
                jobsCompleted = 35,
                hourlyRate = 1200,
                serviceRadiusKm = 100,
                locationCity = "Nagpur",
                locationArea = "IT Park",
                language = AppLanguage.ENGLISH,
                bio = "Full Stack Engineer (Kotlin, Compose, React, Spring Boot). Builds clean production software with tested code.",
                badges = listOf("Code Maestro", "Speed Delivery", "Verified Engineer"),
                skills = listOf(WorkerSkill("Jetpack Compose", "Expert", true, 20), WorkerSkill("Spring Boot & Cloud", "Expert", true, 18)),
                endorsements = 38
            )
        )
        _workersPool.value = workers

        // Seed initial Jobs
        val initialJobs = listOf(
            Job(
                id = "job_301",
                title = "Main MCB Tripping & Kitchen Switchboard Sparking",
                categoryId = "cat_elec",
                categoryName = "Electrician",
                type = WorkType.LOCAL_ON_DEMAND,
                status = JobStatus.IN_PROGRESS,
                taskPosterId = "tp_201",
                taskPosterName = "Anjali Deshmukh",
                taskPosterPhone = "+91 94221 67890",
                workerId = "w_101",
                workerName = "Ramesh Sharma",
                workerPhone = "+91 98220 12345",
                budget = 850,
                escrowHeld = true,
                locationCity = "Nagpur",
                locationAddress = "Flat 302, Green Acre Apts, West High Court Rd, Dharampeth",
                urgency = "Immediate (Urgent)",
                description = "The main 32A MCB is repeatedly tripping when geyser is switched on. Burning smell near kitchen board.",
                createdAt = "15 mins ago",
                matchExplanation = "Top match for Ramesh Sharma: 1.8km distance, 4.85 rating, certified for MCB tripping & household rewiring.",
                etaMinutes = 7,
                distanceKm = 1.8
            ),
            Job(
                id = "job_302",
                title = "E-Commerce Mobile App Wireframes & Brand Kit",
                categoryId = "cat_design",
                categoryName = "Graphic Design",
                type = WorkType.PROJECT_SKILLED,
                status = JobStatus.POSTED,
                taskPosterId = "tp_201",
                taskPosterName = "Anjali Deshmukh",
                taskPosterPhone = "+91 94221 67890",
                budget = 14000,
                escrowHeld = true,
                locationCity = "Nagpur",
                locationAddress = "Remote / Online",
                urgency = "Deadline: 7 Days",
                description = "Need clean, modern Figma wireframes (12 screens) for an organic grocery delivery app + logo vector.",
                createdAt = "2 hours ago",
                matchExplanation = "Matches Graphic Design skill, budget ₹14,000, verified portfolio required.",
                milestones = listOf(
                    JobMilestone("m_1", "Brand Logo & Color System", 4000, isCompleted = false, isReleased = false),
                    JobMilestone("m_2", "Primary Wireframes (6 screens)", 5000, isCompleted = false, isReleased = false),
                    JobMilestone("m_3", "Final Interactive Prototype", 5000, isCompleted = false, isReleased = false)
                )
            ),
            Job(
                id = "job_303",
                title = "Bathroom Concealed Pipe Leakage Repair",
                categoryId = "cat_plumb",
                categoryName = "Plumber",
                type = WorkType.LOCAL_ON_DEMAND,
                status = JobStatus.COMPLETED,
                taskPosterId = "tp_201",
                taskPosterName = "Anjali Deshmukh",
                taskPosterPhone = "+91 94221 67890",
                workerId = "w_103",
                workerName = "Amit Khobragade",
                workerPhone = "+91 93255 44332",
                budget = 650,
                escrowHeld = false,
                locationCity = "Nagpur",
                locationAddress = "Plot 12, Tilak Nagar, Nagpur",
                urgency = "Completed Yesterday",
                description = "Wall dampness due to concealed shower joint leak. Joint replaced and sealed.",
                createdAt = "Yesterday",
                completionPhotoProof = "proof_pipe_sealed.jpg",
                ratingGiven = 5.0f,
                reviewGiven = "Amit fixed the internal pipe leak without breaking excess tiles. Very clean work!"
            ),
            Job(
                id = "job_304",
                title = "Full Day Chauffeur for Outstation Visit to Wardha",
                categoryId = "cat_driver",
                categoryName = "Driver",
                type = WorkType.LOCAL_ON_DEMAND,
                status = JobStatus.MATCHED,
                taskPosterId = "tp_202",
                taskPosterName = "Suresh Kulkarni",
                taskPosterPhone = "+91 98900 44556",
                workerId = "w_105",
                workerName = "Vikram Singh",
                workerPhone = "+91 98901 11223",
                budget = 1200,
                escrowHeld = true,
                locationCity = "Nagpur",
                locationAddress = "Civil Lines, Near High Court, Nagpur",
                urgency = "Tomorrow 8:00 AM",
                description = "Automatic Sedan car. Need careful driver for return day-trip to Sevagram.",
                createdAt = "3 hours ago",
                matchExplanation = "Matched with Vikram Singh: Commercial DL holder with 4.75 rating and highway badge.",
                etaMinutes = 20,
                distanceKm = 4.2
            )
        )
        _jobs.value = initialJobs

        // Seed Proposals for Job 302
        _proposals.value = listOf(
            Proposal(
                id = "prop_1",
                jobId = "job_302",
                workerId = "w_102",
                workerName = "Priya Patil",
                workerAvatar = "🎨",
                workerRating = 4.95f,
                workerTrustScore = 96,
                quotedPrice = 12500,
                coverNote = "Namaste Anjali! I have designed 4 grocery & ecommerce apps on Figma with component libraries. I can deliver the complete kit in 5 days.",
                daysToDeliver = 5,
                status = "SUBMITTED"
            ),
            Proposal(
                id = "prop_2",
                jobId = "job_302",
                workerId = "w_106",
                workerName = "Arjun Mehta",
                workerAvatar = "💻",
                workerRating = 4.92f,
                workerTrustScore = 97,
                quotedPrice = 14000,
                coverNote = "I can do both the design and provide export-ready Compose theme assets so developers can integrate seamlessly.",
                daysToDeliver = 6,
                status = "SUBMITTED"
            )
        )

        // Seed Transactions
        _transactions.value = listOf(
            Transaction("tx_901", "job_301", "Escrow Hold: MCB Tripping Repair", 850, "ESCROW_HOLD", false, "Today, 10:30 AM", "UPI/SB/88392019"),
            Transaction("tx_902", "job_303", "Escrow Release to Amit (Plumbing)", 552, "ESCROW_RELEASE", false, "Yesterday, 4:15 PM", "UPI/SB/77102948"),
            Transaction("tx_903", "job_303", "Platform Commission (15%)", 98, "COMMISSION", true, "Yesterday, 4:15 PM", "COMM/SB/98201"),
            Transaction("tx_904", "wallet_fund", "Added to Wallet via UPI", 5000, "WALLET_ADD", true, "26 Oct 2026", "UPI/AXIS/1928374")
        )

        // Seed KYC records for Admin console
        _kycList.value = listOf(
            KycVerificationItem("kyc_1", "w_101", "Ramesh Sharma", "Aadhaar + Govt ITI Electrician Certificate", "XXXX-XXXX-4912", "Today, 09:15 AM", "APPROVED"),
            KycVerificationItem("kyc_2", "w_104", "Sunita Bai Verma", "Aadhaar Card + Food Handler FSSAI", "XXXX-XXXX-8821", "Yesterday, 02:30 PM", "APPROVED"),
            KycVerificationItem("kyc_3", "w_107", "Dinesh Rao", "Driver Commercial Badge", "MH-31-2024-XXXX", "27 Oct 2026", "PENDING"),
            KycVerificationItem("kyc_4", "w_108", "Kavita Gokhale", "B.Sc Degree + Tutoring Cert", "MAH-UNIV-9921", "27 Oct 2026", "PENDING")
        )

        // Seed Disputes
        _disputes.value = listOf(
            DisputeRecord("disp_1", "job_299", "Ceiling Fan Regulator Not Fixed Properly", "Rahul Bajaj", "TASK_POSTER", "Technician left without testing high speed, fan still hums.", "OPEN", "27 Oct 2026", 450)
        )

        // Seed Chats for Job 301
        _jobChats.value = mapOf(
            "job_301" to listOf(
                ChatMessage("c_1", "System", false, "SkillBridge Escrow of ₹850 held safely. Ramesh Sharma has been assigned to your job.", "10:30 AM"),
                ChatMessage("c_2", "Ramesh Sharma", true, "Namaste Anjali ji, I am on my way with multimeter and replacement 32A MCB. Reaching in 7 minutes.", "10:32 AM"),
                ChatMessage("c_3", "Anjali Deshmukh", false, "Thank you Ramesh ji. Please ring doorbell 302 directly, security guard has been informed.", "10:33 AM")
            )
        )

        // Seed Learning Courses
        _learningCourses.value = listOf(
            LearningCourse("lc_1", "High-Voltage Electrical Safety & Earthing", "Electrical", 15, "Safety Champion Badge", true, "Crucial earthing practices, insulated tool usage and shock prevention protocols."),
            LearningCourse("lc_2", "Customer Courtesy & Professional Ethics", "Soft Skills", 10, "5-Star Etiquette Badge", true, "Polite greetings in Hindi/Marathi, quoting transparency, and clean site handover."),
            LearningCourse("lc_3", "GST & Digital UPI Invoicing for Freelancers", "Finance", 20, "Certified Pro Invoicer", false, "Understanding 18% GST guidelines, e-invoices, and tax compliance.")
        )
    }

    // Role switching (Worker <-> Task-Poster <-> Admin)
    fun switchRole(newRole: UserRole) {
        _activeRole.value = newRole
    }

    fun setCity(city: String) {
        _selectedCity.value = city
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun toggleWorkerOnline(isOnline: Boolean) {
        _currentWorker.value = _currentWorker.value.copy(isOnline = isOnline)
    }

    // On-Demand Service Request Flow (Task Poster)
    fun requestLocalService(
        categoryId: String,
        description: String,
        urgency: String,
        address: String,
        city: String
    ): Job {
        val category = _categories.value.find { it.id == categoryId } ?: _categories.value.first()
        val estimatedPrice = category.basePrice * 2 // standard 2 hours or base rate
        val newJob = Job(
            id = "job_" + System.currentTimeMillis().toString().takeLast(5),
            title = "${category.name}: " + (if (description.isNotBlank()) description.take(35) else "Urgent Service"),
            categoryId = category.id,
            categoryName = category.name,
            type = WorkType.LOCAL_ON_DEMAND,
            status = JobStatus.REQUESTED,
            taskPosterId = _currentTaskPoster.value.id,
            taskPosterName = _currentTaskPoster.value.name,
            taskPosterPhone = _currentTaskPoster.value.phone,
            budget = estimatedPrice,
            escrowHeld = true,
            locationCity = city,
            locationAddress = address,
            urgency = urgency,
            description = description,
            createdAt = "Just now",
            matchExplanation = "Matching with verified nearby ${category.name} in $city...",
            etaMinutes = 10,
            distanceKm = 2.1
        )

        // Deduct from task poster wallet or credit escrow
        _currentTaskPoster.value = _currentTaskPoster.value.copy(
            walletBalance = _currentTaskPoster.value.walletBalance - estimatedPrice
        )

        // Add transaction
        val tx = Transaction(
            id = "tx_" + System.currentTimeMillis().toString().takeLast(6),
            jobId = newJob.id,
            title = "Escrow Hold: ${category.name}",
            amount = estimatedPrice,
            type = "ESCROW_HOLD",
            isCredit = false,
            timestamp = "Just now",
            upiReference = "UPI/SB/" + (100000..999999).random()
        )
        _transactions.value = listOf(tx) + _transactions.value

        _jobs.value = listOf(newJob) + _jobs.value

        // Dispatch simulation: match with a suitable worker from workersPool or current worker
        val matchedWorker = _workersPool.value.firstOrNull { it.id == "w_101" } ?: _workersPool.value.first()
        _incomingWorkerRequest.value = newJob.copy(
            workerId = matchedWorker.id,
            workerName = matchedWorker.name,
            workerPhone = matchedWorker.phone,
            matchExplanation = "Matches your ${category.name} skill, $city location, and ₹${estimatedPrice} payout."
        )

        return newJob
    }

    // Post Skilled Project Flow
    fun postSkilledProject(
        title: String,
        categoryId: String,
        description: String,
        budget: Int,
        deadlineDays: Int,
        milestones: List<String>
    ): Job {
        val category = _categories.value.find { it.id == categoryId } ?: _categories.value.first()
        val milestoneItems = if (milestones.isNotEmpty()) {
            val amountPerMilestone = budget / milestones.size
            milestones.mapIndexed { index, mTitle ->
                JobMilestone("m_${System.currentTimeMillis()}_$index", mTitle, amountPerMilestone)
            }
        } else {
            listOf(
                JobMilestone("m_1", "Initial Draft & Architecture", budget / 2),
                JobMilestone("m_2", "Final Review & Deliverables", budget / 2)
            )
        }

        val newJob = Job(
            id = "job_" + System.currentTimeMillis().toString().takeLast(5),
            title = title,
            categoryId = category.id,
            categoryName = category.name,
            type = WorkType.PROJECT_SKILLED,
            status = JobStatus.POSTED,
            taskPosterId = _currentTaskPoster.value.id,
            taskPosterName = _currentTaskPoster.value.name,
            taskPosterPhone = _currentTaskPoster.value.phone,
            budget = budget,
            escrowHeld = true,
            locationCity = "Remote / Pan-India",
            locationAddress = "Remote Online Project",
            urgency = "Deadline: $deadlineDays Days",
            description = description,
            createdAt = "Just now",
            matchExplanation = "Open to skilled freelancers. Milestone escrow enabled.",
            milestones = milestoneItems
        )

        _jobs.value = listOf(newJob) + _jobs.value
        return newJob
    }

    // Worker accepts incoming request (Local On-Demand)
    fun acceptIncomingRequest(jobId: String) {
        val incoming = _incomingWorkerRequest.value ?: return
        val updated = incoming.copy(
            status = JobStatus.MATCHED,
            workerId = _currentWorker.value.id,
            workerName = _currentWorker.value.name,
            workerPhone = _currentWorker.value.phone,
            etaMinutes = 12
        )
        _jobs.value = _jobs.value.map { if (it.id == jobId) updated else it }
        _incomingWorkerRequest.value = null

        // Add welcome system chat
        val chatList = listOf(
            ChatMessage("c_${System.currentTimeMillis()}", "System", false, "Job accepted! Escrow payment of ₹${updated.budget} is safely secured.", "Just now"),
            ChatMessage("c_${System.currentTimeMillis() + 1}", _currentWorker.value.name, true, "Namaste! I have accepted your request and am getting ready with the equipment.", "Just now")
        )
        val currentChats = _jobChats.value.toMutableMap()
        currentChats[jobId] = chatList
        _jobChats.value = currentChats
    }

    fun rejectIncomingRequest() {
        _incomingWorkerRequest.value = null
    }

    // State machine updates
    fun advanceJobStatus(jobId: String, newStatus: JobStatus, photoProof: String? = null) {
        val current = _jobs.value.find { it.id == jobId } ?: return

        // State Machine validation
        val isValid = when (current.type) {
            WorkType.LOCAL_ON_DEMAND -> {
                when (newStatus) {
                    JobStatus.MATCHED -> current.status == JobStatus.REQUESTED
                    JobStatus.EN_ROUTE -> current.status == JobStatus.MATCHED
                    JobStatus.ARRIVED -> current.status == JobStatus.EN_ROUTE
                    JobStatus.IN_PROGRESS -> current.status == JobStatus.ARRIVED
                    JobStatus.COMPLETED -> current.status == JobStatus.IN_PROGRESS
                    JobStatus.DISPUTED -> current.status != JobStatus.COMPLETED
                    JobStatus.CANCELLED -> current.status in listOf(JobStatus.REQUESTED, JobStatus.MATCHED)
                    else -> false
                }
            }
            WorkType.PROJECT_SKILLED -> {
                when (newStatus) {
                    JobStatus.PROPOSALS -> current.status == JobStatus.POSTED
                    JobStatus.HIRED -> current.status in listOf(JobStatus.POSTED, JobStatus.PROPOSALS)
                    JobStatus.IN_PROGRESS -> current.status == JobStatus.HIRED
                    JobStatus.DELIVERED -> current.status == JobStatus.IN_PROGRESS
                    JobStatus.COMPLETED -> current.status == JobStatus.DELIVERED
                    JobStatus.DISPUTED -> current.status != JobStatus.COMPLETED
                    JobStatus.CANCELLED -> current.status != JobStatus.COMPLETED
                    else -> false
                }
            }
        }

        if (!isValid) return

        var updated = current.copy(status = newStatus)
        if (photoProof != null) {
            updated = updated.copy(completionPhotoProof = photoProof)
        }

        // On completion: release escrow minus 15% platform commission to worker wallet
        if (newStatus == JobStatus.COMPLETED) {
            val commission = (current.budget * current.commissionRate).toInt()
            val workerPayout = current.budget - commission
            updated = updated.copy(escrowHeld = false)

            _currentWorker.value = _currentWorker.value.copy(
                walletBalance = _currentWorker.value.walletBalance + workerPayout,
                jobsCompleted = _currentWorker.value.jobsCompleted + 1
            )

            // Ledger records
            val txPayout = Transaction(
                id = "tx_" + System.currentTimeMillis().toString().takeLast(6),
                jobId = current.id,
                title = "Job Payout: ${current.title.take(24)}",
                amount = workerPayout,
                type = "WORKER_PAYOUT",
                isCredit = true,
                timestamp = "Just now",
                upiReference = "UPI/SB/" + (100000..999999).random()
            )
            val txComm = Transaction(
                id = "tx_c_" + System.currentTimeMillis().toString().takeLast(6),
                jobId = current.id,
                title = "Platform Fee (15%)",
                amount = commission,
                type = "COMMISSION",
                isCredit = false,
                timestamp = "Just now",
                upiReference = "COMM/SB/" + (10000..99999).random()
            )
            _transactions.value = listOf(txPayout, txComm) + _transactions.value
        }

        _jobs.value = _jobs.value.map { if (it.id == jobId) updated else it }
    }

    // Submit Proposal for Project
    fun submitProposal(jobId: String, quote: Int, note: String, days: Int) {
        val worker = _currentWorker.value
        val proposal = Proposal(
            id = "prop_" + System.currentTimeMillis().toString().takeLast(5),
            jobId = jobId,
            workerId = worker.id,
            workerName = worker.name,
            workerAvatar = worker.avatarEmoji,
            workerRating = worker.rating,
            workerTrustScore = worker.trustScore,
            quotedPrice = quote,
            coverNote = note,
            daysToDeliver = days,
            submittedAt = "Just now",
            status = "SUBMITTED"
        )
        _proposals.value = listOf(proposal) + _proposals.value

        // Update job to PROPOSALS status if it was POSTED
        val job = _jobs.value.find { it.id == jobId }
        if (job != null && job.status == JobStatus.POSTED) {
            _jobs.value = _jobs.value.map { if (it.id == jobId) it.copy(status = JobStatus.PROPOSALS) else it }
        }
    }

    // Employer Hires from Proposal
    fun hireProposal(proposalId: String) {
        val prop = _proposals.value.find { it.id == proposalId } ?: return
        val job = _jobs.value.find { it.id == prop.jobId } ?: return

        _proposals.value = _proposals.value.map {
            if (it.id == proposalId) it.copy(status = "HIRED")
            else if (it.jobId == prop.jobId) it.copy(status = "REJECTED")
            else it
        }

        val updatedJob = job.copy(
            status = JobStatus.HIRED,
            workerId = prop.workerId,
            workerName = prop.workerName,
            workerPhone = "+91 98220 12345",
            budget = prop.quotedPrice
        )
        _jobs.value = _jobs.value.map { if (it.id == job.id) updatedJob else it }

        // Start chat
        val chatList = listOf(
            ChatMessage("c_${System.currentTimeMillis()}", "System", false, "Proposal accepted! ₹${prop.quotedPrice} held in Escrow. You can now coordinate deliverables.", "Just now")
        )
        val currentChats = _jobChats.value.toMutableMap()
        currentChats[job.id] = chatList
        _jobChats.value = currentChats
    }

    // Chat
    fun sendChatMessage(jobId: String, text: String, isFromWorker: Boolean) {
        val senderName = if (isFromWorker) _currentWorker.value.name else _currentTaskPoster.value.name
        val msg = ChatMessage(
            id = "c_" + System.currentTimeMillis().toString().takeLast(5),
            senderName = senderName,
            isFromWorker = isFromWorker,
            message = text,
            time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )
        val currentChats = _jobChats.value.toMutableMap()
        val list = currentChats[jobId] ?: emptyList()
        currentChats[jobId] = list + msg
        _jobChats.value = currentChats
    }

    // Submit Review
    fun submitReview(jobId: String, rating: Float, comment: String) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(ratingGiven = rating, reviewGiven = comment) else it
        }
    }

    // Raise Dispute
    fun raiseDispute(jobId: String, reason: String) {
        val job = _jobs.value.find { it.id == jobId } ?: return
        advanceJobStatus(jobId, JobStatus.DISPUTED)
        val record = DisputeRecord(
            id = "disp_" + System.currentTimeMillis().toString().takeLast(4),
            jobId = jobId,
            jobTitle = job.title,
            raisedByName = _currentTaskPoster.value.name,
            raisedByRole = "TASK_POSTER",
            reason = reason,
            status = "OPEN",
            createdAt = "Just now",
            escrowAmount = job.budget
        )
        _disputes.value = listOf(record) + _disputes.value
    }

    // Admin KYC Actions
    fun approveKyc(kycId: String) {
        _kycList.value = _kycList.value.map {
            if (it.id == kycId) it.copy(status = "APPROVED") else it
        }
    }

    fun rejectKyc(kycId: String) {
        _kycList.value = _kycList.value.map {
            if (it.id == kycId) it.copy(status = "REJECTED") else it
        }
    }

    // Admin Resolve Dispute
    fun resolveDispute(disputeId: String, releaseToWorker: Boolean) {
        val dispute = _disputes.value.find { it.id == disputeId } ?: return
        _disputes.value = _disputes.value.map {
            if (it.id == disputeId) it.copy(status = "RESOLVED") else it
        }
        val job = _jobs.value.find { it.id == dispute.jobId }
        if (job != null) {
            if (releaseToWorker) {
                advanceJobStatus(job.id, JobStatus.COMPLETED)
            } else {
                // Refund task poster
                _currentTaskPoster.value = _currentTaskPoster.value.copy(
                    walletBalance = _currentTaskPoster.value.walletBalance + job.budget
                )
                _jobs.value = _jobs.value.map { if (it.id == job.id) it.copy(status = JobStatus.CANCELLED) else it }
            }
        }
    }

    // Wallet Payout (Withdrawal to UPI)
    fun withdrawToUpi(upiId: String, amount: Int): Boolean {
        if (_currentWorker.value.walletBalance < amount) return false
        _currentWorker.value = _currentWorker.value.copy(
            walletBalance = _currentWorker.value.walletBalance - amount
        )
        val tx = Transaction(
            id = "tx_w_" + System.currentTimeMillis().toString().takeLast(6),
            jobId = "WALLET_WITHDRAWAL",
            title = "Instant UPI Withdrawal to $upiId",
            amount = amount,
            type = "WORKER_PAYOUT",
            isCredit = false,
            timestamp = "Just now",
            upiReference = "PAYOUT/UPI/" + (100000..999999).random()
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    // Complete Skill Quiz to earn badge
    fun completeSkillQuiz(badgeName: String) {
        val current = _currentWorker.value
        if (!current.badges.contains(badgeName)) {
            val updatedBadges = current.badges + badgeName
            val updatedTrust = (current.trustScore + 3).coerceAtMost(100)
            _currentWorker.value = current.copy(
                badges = updatedBadges,
                trustScore = updatedTrust
            )
        }
    }

    // Complete Learning Course
    fun completeCourse(courseId: String) {
        _learningCourses.value = _learningCourses.value.map {
            if (it.id == courseId) it.copy(isCompleted = true) else it
        }
    }

    // Matching Engine with Explainability
    fun calculateMatchScore(worker: User, categoryId: String, city: String, budget: Int): Pair<Int, String> {
        var score = 50
        val reasons = mutableListOf<String>()

        // 1. Skill Match
        val hasSkill = worker.skills.any { it.skillName.contains(categoryId, ignoreCase = true) } ||
                worker.badges.any { it.contains("Electrician", ignoreCase = true) }
        if (hasSkill) {
            score += 25
            reasons.add("Skill verified in category")
        }

        // 2. City / Radius
        if (worker.locationCity.equals(city, ignoreCase = true)) {
            score += 15
            reasons.add("Local to $city (${worker.locationArea})")
        }

        // 3. Trust Score & KYC
        if (worker.isKycVerified) {
            score += 10
            reasons.add("Govt KYC verified (Trust score: ${worker.trustScore}%)")
        }

        // 4. Rating & Jobs
        if (worker.rating >= 4.7f) {
            score += 10
            reasons.add("${worker.rating}★ rating across ${worker.jobsCompleted} completed jobs")
        }

        // 5. Fairness & fast response
        if (worker.responseTimeMinutes <= 5) {
            score += 5
            reasons.add("Under 5-min response time")
        }

        val explanation = "Top Match: " + reasons.joinToString(", ")
        return Pair(score.coerceAtMost(99), explanation)
    }

    // --- PHASE 2 ENTERPRISE WORKSTREAMS ---

    private val _isLowDataMode = MutableStateFlow(false)
    val isLowDataMode: StateFlow<Boolean> = _isLowDataMode.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _businessTeamMembers = MutableStateFlow(
        listOf(
            BusinessTeamMember("bm_1", "Anjali Deshmukh", "ADMIN", "anjali@deshmukhhomes.in"),
            BusinessTeamMember("bm_2", "Sameer Verma", "DISPATCHER", "sameer.v@deshmukhhomes.in"),
            BusinessTeamMember("bm_3", "Rohan Gupta", "FINANCE", "rohan.g@deshmukhhomes.in")
        )
    )
    val businessTeamMembers: StateFlow<List<BusinessTeamMember>> = _businessTeamMembers.asStateFlow()

    private val _portfolioItems = MutableStateFlow(
        listOf(
            PortfolioItem("port_1", "Commercial 3-Phase Main Panel Wiring", "Electrical", "⚡"),
            PortfolioItem("port_2", "Solar UPS & Battery Inverter Grid", "Solar & Power", "🔋"),
            PortfolioItem("port_3", "Luxury Kitchen Geyser & Concealed MCB", "Home Wiring", "🛠️")
        )
    )
    val portfolioItems: StateFlow<List<PortfolioItem>> = _portfolioItems.asStateFlow()

    fun toggleLowDataMode(enabled: Boolean) {
        _isLowDataMode.value = enabled
    }

    // Worker Pro Subscription: lowers commission from 15% to 10%
    fun upgradeWorkerToPro() {
        val current = _currentWorker.value
        val updatedBadges = if (!current.badges.contains("Pro Tier Worker")) current.badges + "Pro Tier Worker" else current.badges
        _currentWorker.value = current.copy(
            isProMember = true,
            badges = updatedBadges
        )
        // Update active jobs commission rate to 10%
        _jobs.value = _jobs.value.map {
            if (it.workerId == current.id) it.copy(commissionRate = 0.10) else it
        }
    }

    // Milestone Escrow Release
    fun releaseMilestone(jobId: String, milestoneId: String) {
        val job = _jobs.value.find { it.id == jobId } ?: return
        val milestone = job.milestones.find { it.id == milestoneId } ?: return
        if (milestone.isReleased) return

        val commissionRate = if (_currentWorker.value.isProMember) 0.10 else 0.15
        val commissionPaise = (milestone.amount * 100 * commissionRate).toLong()
        val payoutPaise = (milestone.amount * 100) - commissionPaise
        val payoutRupees = (payoutPaise / 100).toInt()

        val updatedMilestones = job.milestones.map {
            if (it.id == milestoneId) it.copy(isCompleted = true, isReleased = true) else it
        }

        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(milestones = updatedMilestones) else it
        }

        // Credit worker wallet
        _currentWorker.value = _currentWorker.value.copy(
            walletBalance = _currentWorker.value.walletBalance + payoutRupees
        )

        // Ledger entry
        val tx = Transaction(
            id = "tx_ms_" + System.currentTimeMillis().toString().takeLast(6),
            jobId = jobId,
            title = "Milestone Release: ${milestone.title}",
            amount = payoutRupees,
            type = "ESCROW_RELEASE",
            isCredit = true,
            timestamp = "Just now",
            upiReference = "MILESTONE/SB/" + (100000..999999).random()
        )
        _transactions.value = listOf(tx) + _transactions.value
    }

    // Report and Block User
    fun reportAndBlockUser(targetUserId: String, reason: String) {
        _blockedUserIds.value = _blockedUserIds.value + targetUserId
        val dispute = DisputeRecord(
            id = "rep_" + System.currentTimeMillis().toString().takeLast(4),
            jobId = "REPORT_USER_$targetUserId",
            jobTitle = "Safety Report Against User $targetUserId",
            raisedByName = _currentTaskPoster.value.name,
            raisedByRole = _activeRole.value.name,
            reason = reason,
            status = "OPEN",
            createdAt = "Just now",
            escrowAmount = 0
        )
        _disputes.value = listOf(dispute) + _disputes.value
    }

    // Generate GST-Ready Invoice
    fun generateGstInvoice(jobId: String): GstInvoice {
        val job = _jobs.value.find { it.id == jobId } ?: _jobs.value.first()
        val totalPaise = job.budget.toLong() * 100L
        // 18% GST included: Base = Total / 1.18, CGST = 9%, SGST = 9%
        val basePaise = (totalPaise / 1.18).toLong()
        val gstTotalPaise = totalPaise - basePaise
        val cgstPaise = gstTotalPaise / 2
        val sgstPaise = gstTotalPaise - cgstPaise

        return GstInvoice(
            invoiceNumber = "SB-INV-2026-" + job.id.takeLast(4),
            date = "29 Oct 2026",
            jobTitle = job.title,
            baseAmountPaise = basePaise,
            cgstPaise = cgstPaise,
            sgstPaise = sgstPaise,
            totalAmountPaise = totalPaise,
            workerName = job.workerName ?: "SkillBridge Professional",
            taskPosterName = job.taskPosterName
        )
    }

    fun updateTrustedContact(phone: String) {
        _currentWorker.value = _currentWorker.value.copy(trustedContactPhone = phone)
        _currentTaskPoster.value = _currentTaskPoster.value.copy(trustedContactPhone = phone)
    }

    var authProvider: AuthProvider = MockAuthProvider()

    val offlineActionQueue = OfflineActionQueue()

    suspend fun syncOfflineQueue(): Int {
        return offlineActionQueue.syncAll()
    }

    fun loginStaff(email: String, secret: String): Result<AuthSession> {
        val result = authProvider.loginStaff(email, secret)
        if (result.isSuccess) {
            _activeRole.value = UserRole.ADMIN
        }
        return result
    }

    fun updateWorkerLiveCoordinates(latitude: Double, longitude: Double) {
        _jobs.value = _jobs.value.map { job ->
            if (job.status == JobStatus.MATCHED || job.status == JobStatus.EN_ROUTE || job.status == JobStatus.IN_PROGRESS) {
                job.copy(workerLatitude = latitude, workerLongitude = longitude)
            } else job
        }
    }

    fun expandSearchRadius(newRadiusMeters: Double) {
        _jobs.value = _jobs.value.map { job ->
            if (job.status == JobStatus.REQUESTED) {
                job.copy(searchRadiusMeters = newRadiusMeters)
            } else job
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SkillBridgeRepository? = null

        fun getInstance(context: android.content.Context? = null): SkillBridgeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SkillBridgeRepository().also { INSTANCE = it }
            }
        }
    }
}
