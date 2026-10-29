package com.example.skillbridge.model

enum class UserRole {
    WORKER,
    TASK_POSTER,
    ADMIN
}

enum class WorkType {
    LOCAL_ON_DEMAND, // Plumber, Electrician, Cook, Driver, etc.
    PROJECT_SKILLED   // Web dev, Graphic design, Writing, Tutoring
}

enum class JobStatus {
    // Local flow
    REQUESTED,
    MATCHED,
    EN_ROUTE,
    ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    DISPUTED,
    CANCELLED,
    
    // Project flow specific states
    POSTED,
    PROPOSALS,
    HIRED,
    DELIVERED
}

enum class AppLanguage(val code: String, val label: String, val nativeLabel: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी")
}

data class Category(
    val id: String,
    val name: String,
    val nameHi: String,
    val nameMr: String,
    val type: WorkType,
    val iconKey: String, // e.g. "bolt", "plumbing", "restaurant", "drive_eta", "code", "brush"
    val basePrice: Int,
    val priceUnit: String, // e.g. "₹/hour", "fixed", "₹/day"
    val isHotspot: Boolean = false,
    val description: String = ""
)

data class WorkerSkill(
    val skillName: String,
    val proficiencyLevel: String, // Beginner, Experienced, Expert
    val isVerified: Boolean = false,
    val jobsDone: Int = 0
)

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: UserRole,
    val avatarEmoji: String = "👨‍🔧",
    val isOnline: Boolean = true,
    val isKycVerified: Boolean = true,
    val trustScore: Int = 92, // 0 - 100
    val rating: Float = 4.8f,
    val reviewCount: Int = 46,
    val jobsCompleted: Int = 52,
    val hourlyRate: Int = 450,
    val serviceRadiusKm: Int = 12,
    val locationCity: String = "Nagpur",
    val locationArea: String = "Dharampeth",
    val language: AppLanguage = AppLanguage.HINDI,
    val bio: String = "Certified technician with 6+ years experience in domestic & commercial setups.",
    val badges: List<String> = listOf("Verified Electrician", "Top Rated", "50+ Jobs Done", "Quick Responder"),
    val skills: List<WorkerSkill> = emptyList(),
    val endorsements: Int = 28,
    val responseTimeMinutes: Int = 3,
    val cancellationRatePercent: Double = 1.8,
    val walletBalance: Int = 3850,
    val isProMember: Boolean = false,
    val isLowDataMode: Boolean = false,
    val trustedContactPhone: String = "+91 98220 99881"
)

data class JobMilestone(
    val id: String,
    val title: String,
    val amount: Int,
    val isCompleted: Boolean = false,
    val isReleased: Boolean = false
)

data class Job(
    val id: String,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val type: WorkType,
    val status: JobStatus,
    val taskPosterId: String,
    val taskPosterName: String,
    val taskPosterPhone: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val workerPhone: String? = null,
    val budget: Int,
    val escrowHeld: Boolean = true,
    val commissionRate: Double = 0.15, // 15% platform commission
    val locationCity: String = "Nagpur",
    val locationAddress: String = "Plot 42, West High Court Rd, Dharampeth",
    val urgency: String = "Immediate (Within 30 mins)",
    val description: String = "",
    val createdAt: String = "Just now",
    val completionPhotoProof: String? = null,
    val matchExplanation: String = "",
    val etaMinutes: Int = 12,
    val distanceKm: Double = 2.4,
    val milestones: List<JobMilestone> = emptyList(),
    val ratingGiven: Float? = null,
    val reviewGiven: String? = null,
    val workerLatitude: Double = 21.1458,
    val workerLongitude: Double = 79.0882,
    val posterLatitude: Double = 21.1466,
    val posterLongitude: Double = 79.0830,
    val searchRadiusMeters: Double = 2000.0
)

data class Proposal(
    val id: String,
    val jobId: String,
    val workerId: String,
    val workerName: String,
    val workerAvatar: String,
    val workerRating: Float,
    val workerTrustScore: Int,
    val quotedPrice: Int,
    val coverNote: String,
    val daysToDeliver: Int,
    val submittedAt: String = "1 hour ago",
    val status: String = "SUBMITTED" // SUBMITTED, SHORTLISTED, HIRED, REJECTED
)

data class Transaction(
    val id: String,
    val jobId: String,
    val title: String,
    val amount: Int,
    val type: String, // ESCROW_HOLD, ESCROW_RELEASE, COMMISSION, PAYOUT, REFUND
    val isCredit: Boolean,
    val timestamp: String,
    val upiReference: String
)

data class DisputeRecord(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val raisedByName: String,
    val raisedByRole: String,
    val reason: String,
    val status: String, // OPEN, INVESTIGATING, RESOLVED
    val createdAt: String,
    val escrowAmount: Int
)

data class KycVerificationItem(
    val id: String,
    val userId: String,
    val userName: String,
    val documentType: String, // Aadhaar, Trade License, Degree Certificate
    val docNumberMasked: String,
    val submittedAt: String,
    var status: String = "PENDING" // PENDING, APPROVED, REJECTED
)

data class LearningCourse(
    val id: String,
    val title: String,
    val category: String,
    val durationMin: Int,
    val rewardBadge: String,
    val isCompleted: Boolean = false,
    val description: String
)

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val isFromWorker: Boolean,
    val message: String,
    val time: String
)

enum class ApplicationStage {
    APPLIED,
    SHORTLISTED,
    INTERVIEW,
    HIRED,
    REJECTED
}

data class BusinessTeamMember(
    val id: String,
    val name: String,
    val role: String, // "ADMIN", "DISPATCHER", "FINANCE"
    val email: String
)

data class GstInvoice(
    val invoiceNumber: String,
    val date: String,
    val jobTitle: String,
    val gstin: String = "27AABCS1423Q1ZP",
    val sacCode: String = "998713",
    val baseAmountPaise: Long,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val totalAmountPaise: Long,
    val workerName: String,
    val taskPosterName: String,
    val isPlaceholderMode: Boolean = true,
    val complianceDisclaimer: String = "TEST-MODE PLACEHOLDER: To be replaced with registered business's real GSTIN & SAC code before official launch"
)

data class PortfolioItem(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String = "📸"
)
