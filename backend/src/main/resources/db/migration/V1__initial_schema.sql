-- SkillBridge Production Schema Migration (V1)
-- Framework: PostgreSQL 16 + PostGIS
-- Financial Invariant: All monetary values stored strictly as BIGINT in integer paise (1 Rupee = 100 paise)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 1. Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120),
    role VARCHAR(30) NOT NULL DEFAULT 'WORKER', -- WORKER, POSTER, ADMIN, FINANCE, SUPPORT, BUSINESS_OWNER, BUSINESS_MANAGER
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en', -- en, hi, mr
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Worker & Poster Profiles
CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    avatar_emoji VARCHAR(10) DEFAULT '👨‍🔧',
    bio TEXT,
    trust_score INT NOT NULL DEFAULT 60 CHECK (trust_score BETWEEN 0 AND 100),
    rating NUMERIC(3, 2) NOT NULL DEFAULT 5.00 CHECK (rating BETWEEN 1.00 AND 5.00),
    review_count INT NOT NULL DEFAULT 0,
    jobs_completed INT NOT NULL DEFAULT 0,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    is_kyc_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_pro_member BOOLEAN NOT NULL DEFAULT FALSE,
    hourly_rate_paise BIGINT NOT NULL DEFAULT 40000, -- Default ₹400/hr
    service_radius_km INT NOT NULL DEFAULT 15,
    location_city VARCHAR(100) NOT NULL DEFAULT 'Nagpur',
    location_area VARCHAR(150) NOT NULL DEFAULT 'Dharampeth',
    location_coords GEOMETRY(Point, 4326),
    response_time_minutes INT NOT NULL DEFAULT 5,
    cancellation_rate_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    wallet_balance_paise BIGINT NOT NULL DEFAULT 0 CHECK (wallet_balance_paise >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_profiles_location ON profiles USING GIST (location_coords);
CREATE INDEX idx_profiles_trust ON profiles(trust_score DESC);

-- 3. Categories (Local On-Demand vs Project Skilled)
CREATE TABLE categories (
    id VARCHAR(50) PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_hi VARCHAR(100) NOT NULL,
    name_mr VARCHAR(100) NOT NULL,
    work_type VARCHAR(30) NOT NULL, -- LOCAL_ON_DEMAND, PROJECT_SKILLED
    icon_key VARCHAR(50) NOT NULL,
    base_price_paise BIGINT NOT NULL,
    price_unit VARCHAR(30) NOT NULL,
    commission_rate NUMERIC(4, 3) NOT NULL DEFAULT 0.150 CHECK (commission_rate BETWEEN 0.100 AND 0.200),
    is_hotspot BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT
);

-- 4. Skills Taxonomy & Worker Skills
CREATE TABLE skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    category_id VARCHAR(50) NOT NULL REFERENCES categories(id),
    skill_name VARCHAR(100) NOT NULL,
    proficiency_level VARCHAR(30) NOT NULL DEFAULT 'EXPERIENCED', -- BEGINNER, EXPERIENCED, EXPERT
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    jobs_done INT NOT NULL DEFAULT 0,
    UNIQUE(profile_id, skill_name)
);

-- 5. Jobs Table
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(200) NOT NULL,
    category_id VARCHAR(50) NOT NULL REFERENCES categories(id),
    category_name VARCHAR(100) NOT NULL,
    work_type VARCHAR(30) NOT NULL, -- LOCAL_ON_DEMAND, PROJECT_SKILLED
    task_poster_id UUID NOT NULL REFERENCES users(id),
    worker_id UUID REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED', 
    -- Local: REQUESTED, MATCHED, EN_ROUTE, ARRIVED, IN_PROGRESS, COMPLETED, DISPUTED, CANCELLED
    -- Project: POSTED, PROPOSALS, HIRED, IN_PROGRESS, DELIVERED, COMPLETED, DISPUTED, CANCELLED
    budget_paise BIGINT NOT NULL CHECK (budget_paise > 0),
    escrow_held BOOLEAN NOT NULL DEFAULT FALSE,
    commission_rate NUMERIC(4, 3) NOT NULL DEFAULT 0.150 CHECK (commission_rate BETWEEN 0.100 AND 0.200),
    urgency VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    location_city VARCHAR(100) NOT NULL,
    location_address TEXT NOT NULL,
    location_coords GEOMETRY(Point, 4326),
    description TEXT,
    match_explanation TEXT,
    eta_minutes INT,
    distance_km NUMERIC(5, 2),
    completion_proof_photo_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_location ON jobs USING GIST (location_coords);
CREATE INDEX idx_jobs_status ON jobs (status);

-- 6. Project Proposals
CREATE TABLE proposals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES users(id),
    quoted_price_paise BIGINT NOT NULL CHECK (quoted_price_paise > 0),
    cover_note TEXT NOT NULL,
    days_to_deliver INT NOT NULL CHECK (days_to_deliver > 0),
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, SHORTLISTED, ACCEPTED, REJECTED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Applications (Consent-first tracked worker applications)
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES users(id),
    stage VARCHAR(30) NOT NULL DEFAULT 'APPLIED', -- APPLIED, SHORTLISTED, INTERVIEW, HIRED, REJECTED
    consent_granted BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(job_id, worker_id)
);

-- 8. Bookings (Direct reservation schedule records)
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. Milestones (Project-based work)
CREATE TABLE milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_released BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL DEFAULT 1,
    released_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Escrow Ledger Entries (Double-Entry Invariant Accounting)
CREATE TABLE escrow_ledger_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_ref VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    job_id UUID NOT NULL REFERENCES jobs(id),
    milestone_id UUID REFERENCES milestones(id),
    debit_account VARCHAR(50) NOT NULL,   -- e.g., ESCROW_LIABILITY, WORKER_WALLET
    credit_account VARCHAR(50) NOT NULL,  -- e.g., WORKER_WALLET, PLATFORM_REVENUE, GST_TAX_PAYABLE
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    narration TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ledger_idempotency UNIQUE(idempotency_key, debit_account, credit_account)
);

CREATE INDEX idx_ledger_job ON escrow_ledger_entries(job_id);

-- 11. Payments & Payouts (Razorpay / UPI Integration Records)
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    job_id UUID REFERENCES jobs(id),
    payment_type VARCHAR(30) NOT NULL, -- ESCROW_DEPOSIT, WORKER_PAYOUT, REFUND
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    gateway VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY', -- RAZORPAY, CASHFREE, UPI_MANUAL
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, REFUNDED
    upi_vpa VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 12. Two-Way Ratings & Reviews
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id),
    reviewer_id UUID NOT NULL REFERENCES users(id),
    target_user_id UUID NOT NULL REFERENCES users(id),
    rating NUMERIC(3, 2) NOT NULL CHECK (rating BETWEEN 1.00 AND 5.00),
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(job_id, reviewer_id)
);

-- 13. KYC Records (Zero-Raw Aadhaar Storage)
CREATE TABLE kyc_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- AADHAAR_MASKED, TRADE_LICENSE, DEGREE
    masked_identifier VARCHAR(50) NOT NULL, -- e.g. "XXXX-XXXX-1234"
    document_hash VARCHAR(128) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    rejection_reason TEXT,
    verified_by VARCHAR(100),
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. Disputes
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES jobs(id),
    raised_by_id UUID NOT NULL REFERENCES users(id),
    raised_by_role VARCHAR(30) NOT NULL,
    reason TEXT NOT NULL,
    escrow_amount_paise BIGINT NOT NULL CHECK (escrow_amount_paise >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, DISMISSED
    resolution_notes TEXT,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 16. Reports & Blocks (Safety & Moderation)
CREATE TABLE reports_blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(id),
    reported_user_id UUID NOT NULL REFERENCES users(id),
    reason TEXT NOT NULL,
    action_taken VARCHAR(30) NOT NULL DEFAULT 'BLOCKED', -- BLOCKED, REPORTED, UNDER_REVIEW
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(reporter_id, reported_user_id)
);

-- 17. Trusted Contacts (Live Location & SOS Sharing)
CREATE TABLE trusted_contacts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) NOT NULL DEFAULT 'FAMILY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 18. Audit Log (Immutable Record of Financial & State Events)
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_log(entity_name, entity_id);
