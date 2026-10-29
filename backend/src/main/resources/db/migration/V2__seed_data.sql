-- SkillBridge Production Seed Data Migration (V2)
-- 14 Categories, 50 Workers, 20 Employers, 30 Realistic Jobs with Milestones & Proposals

-- 1. Insert 14 Categories
INSERT INTO categories (id, name_en, name_hi, name_mr, work_type, icon_key, base_price_paise, price_unit, commission_rate, is_hotspot, description) VALUES
('cat_elec', 'Electrician', 'इलेक्ट्रीशियन', 'इलेक्ट्रिशियन', 'LOCAL_ON_DEMAND', 'bolt', 35000, '₹/hr', 0.150, true, 'Wiring, switchboard, inverter, geyser, short-circuit fixes'),
('cat_plumb', 'Plumber', 'प्लम्बर', 'प्लंबर', 'LOCAL_ON_DEMAND', 'plumbing', 35000, '₹/hr', 0.150, true, 'Pipe leaks, tap fitments, water tank cleaning, bathroom fittings'),
('cat_cook', 'Cook / Maid', 'रसोइया / कामवाली', 'स्वयंपाकी / मदतनीस', 'LOCAL_ON_DEMAND', 'restaurant', 50000, '₹/day', 0.150, true, 'Daily home meals, party catering, deep house cleaning'),
('cat_driver', 'Driver / Chauffeur', 'ड्राइवर', 'चालक / ड्रायव्हर', 'LOCAL_ON_DEMAND', 'drive_eta', 60000, '₹/day', 0.150, false, 'Daily city driving, outstation highway trips, commercial transit'),
('cat_carpenter', 'Carpenter', 'बढ़ई', 'सुतार', 'LOCAL_ON_DEMAND', 'carpenter', 40000, '₹/hr', 0.150, false, 'Furniture repair, door latch fix, modular fittings, polishing'),
('cat_mason', 'Mason / Builder', 'राजमिस्त्री', 'गवंडी', 'LOCAL_ON_DEMAND', 'construction', 65000, '₹/day', 0.150, false, 'Brickwork, plastering, tile laying, concrete repairs'),
('cat_appliance', 'Appliance Repair', 'उपकरण मरम्मत', 'उपकरण दुरुस्ती', 'LOCAL_ON_DEMAND', 'build', 45000, '₹/visit', 0.150, true, 'AC servicing, refrigerator repair, washing machine diagnostics'),
('cat_painter', 'Painter', 'पेंटर', 'रंगारी / पेंटर', 'LOCAL_ON_DEMAND', 'format_paint', 55000, '₹/day', 0.150, false, 'Interior/exterior whitewash, putty, waterproof coating'),
('cat_tailor', 'Tailor / Stitching', 'दर्जी / सिलाई', 'शिंपी', 'LOCAL_ON_DEMAND', 'stitching', 30000, '₹/piece', 0.150, false, 'Blouse, kurti, shirt alterations, custom dress stitching'),
('cat_webdev', 'Web & App Developer', 'वेब और ऐप डेवलपर', 'वेब व ॲप डेव्हलपर', 'PROJECT_SKILLED', 'code', 1500000, 'fixed', 0.150, true, 'Fullstack React, Spring Boot, Android Kotlin, WordPress, Shopify'),
('cat_design', 'Graphic & UI Designer', 'ग्राफिक और यूआई डिज़ाइनर', 'ग्राफिक व यूआय डिझायनर', 'PROJECT_SKILLED', 'brush', 800000, 'fixed', 0.150, true, 'Figma UX, branding, logo kits, social media graphics, 3D render'),
('cat_writer', 'Content Writer & Translator', 'कंटेंट राइटर और अनुवादक', 'कंटेंट लेखक व अनुवादक', 'PROJECT_SKILLED', 'edit_note', 400000, 'fixed', 0.150, false, 'SEO articles, Hindi/Marathi translations, technical documentation'),
('cat_tutor', 'Tutor & Skills Trainer', 'ट्यूटर और शिक्षक', 'शिक्षक व मार्गदर्शक', 'PROJECT_SKILLED', 'school', 50000, '₹/hr', 0.150, false, 'Maths, Science, English speaking, coding coaching for K-12'),
('cat_data_entry', 'Data Entry & Accounting', 'डेटा एंट्री और मुनीम', 'डेटा एंट्री व हिशोबनीस', 'PROJECT_SKILLED', 'table_chart', 600000, 'fixed', 0.150, false, 'Excel bookkeeping, Tally Prime, GST filing, invoices scanning');

-- 2. Insert 20 Employers / Task-Posters
DO $$
DECLARE
    i INT;
    new_user_id UUID;
    first_names TEXT[] := ARRAY['Anjali', 'Vikram', 'Rajesh', 'Sunita', 'Pooja', 'Amit', 'Deepak', 'Meera', 'Kavita', 'Sanjay', 'Manoj', 'Neeta', 'Rohit', 'Alok', 'Sneha', 'Tanvi', 'Abhishek', 'Gaurav', 'Shweta', 'Pradeep'];
    last_names TEXT[] := ARRAY['Deshmukh', 'Kulkarni', 'Sharma', 'Patil', 'Joshi', 'Gupta', 'Verma', 'Shinde', 'Bhosale', 'Mehta', 'Chavan', 'Rao', 'Tiwari', 'Kale', 'Nair', 'Bhat', 'Bawankule', 'Pawar', 'Gokhale', 'Mishra'];
BEGIN
    FOR i IN 1..20 LOOP
        new_user_id := uuid_generate_v4();
        INSERT INTO users (id, phone, full_name, email, role, preferred_language)
        VALUES (
            new_user_id,
            '+9198' || lpad((22000000 + i)::text, 8, '0'),
            first_names[i] || ' ' || last_names[i],
            lower(first_names[i]) || '.' || lower(last_names[i]) || '@example.in',
            'POSTER',
            CASE WHEN i % 3 = 0 THEN 'mr' WHEN i % 3 = 1 THEN 'hi' ELSE 'en' END
        );

        INSERT INTO profiles (user_id, avatar_emoji, bio, trust_score, rating, is_online, is_kyc_verified, wallet_balance_paise, location_city, location_area, location_coords)
        VALUES (
            new_user_id,
            '🏢',
            'Verified Homeowner & Business Task-Poster in Central India.',
            85 + (i % 15),
            4.80,
            TRUE,
            TRUE,
            5000000, -- ₹50,000 prepaid escrow
            'Nagpur',
            'Dharampeth',
            ST_SetSRID(ST_MakePoint(79.0882 + (i * 0.002), 21.1458 + (i * 0.002)), 4326)
        );

        INSERT INTO trusted_contacts (user_id, contact_name, phone, relationship)
        VALUES (new_user_id, 'Family Emergency Contact', '+919822099881', 'FAMILY');
    END LOOP;
END $$;

-- 3. Insert 50 Workers
DO $$
DECLARE
    i INT;
    new_worker_id UUID;
    new_profile_id UUID;
    w_first TEXT[] := ARRAY['Ramesh', 'Priya', 'Santosh', 'Ganesh', 'Sunil', 'Kiran', 'Nitin', 'Savita', 'Arun', 'Sachin', 'Umesh', 'Rekha', 'Mahesh', 'Vishal', 'Anand', 'Anita', 'Dinesh', 'Ajay', 'Rahul', 'Jyoti', 'Chetan', 'Swati', 'Prakash', 'Devendra', 'Kunal', 'Manish', 'Siddharth', 'Pallavi', 'Hemant', 'Sarita', 'Vijay', 'Shankar', 'Nilesh', 'Pramod', 'Smita', 'Harish', 'Bhushan', 'Varsha', 'Sandip', 'Archana', 'Ashok', 'Tushar', 'Geeta', 'Lalit', 'Prashant', 'Rupali', 'Yogesh', 'Babita', 'Mangesh', 'Durga'];
    w_last TEXT[] := ARRAY['Kamble', 'Patil', 'Gaikwad', 'Meshram', 'Jadhav', 'Wankhede', 'Pawar', 'More', 'Chavan', 'Borkar', 'Kale', 'Thakur', 'Ghuge', 'Gawande', 'Dumbre', 'Zade', 'Deshmukh', 'Mundhe', 'Rathod', 'Kadam', 'Bonde', 'Tembhekar', 'Bhagat', 'Shinde', 'Suryavanshi', 'Gedam', 'Kapse', 'Mhaske', 'Kharat', 'Wagh', 'Sonwane', 'Khandare', 'Shende', 'Lokhande', 'Ingle', 'Mohite', 'Bisen', 'Dongre', 'Tekam', 'Narwade', 'Mankar', 'Bachute', 'Nawghare', 'Atram', 'Gondane', 'Gabhane', 'Chaudhari', 'Kumbhalkar', 'Dahiwade', 'Pachpore'];
    cat_keys TEXT[] := ARRAY['cat_elec', 'cat_plumb', 'cat_cook', 'cat_driver', 'cat_carpenter', 'cat_appliance', 'cat_painter', 'cat_tailor', 'cat_webdev', 'cat_design', 'cat_writer', 'cat_tutor', 'cat_data_entry', 'cat_mason'];
    chosen_cat TEXT;
BEGIN
    FOR i IN 1..50 LOOP
        new_worker_id := uuid_generate_v4();
        new_profile_id := uuid_generate_v4();
        chosen_cat := cat_keys[1 + (i % 14)];

        INSERT INTO users (id, phone, full_name, email, role, preferred_language)
        VALUES (
            new_worker_id,
            '+9197' || lpad((33000000 + i)::text, 8, '0'),
            w_first[i] || ' ' || w_last[i],
            lower(w_first[i]) || '.' || lower(w_last[i]) || '@skillbridge.in',
            'WORKER',
            CASE WHEN i % 3 = 0 THEN 'hi' WHEN i % 3 = 1 THEN 'mr' ELSE 'en' END
        );

        INSERT INTO profiles (id, user_id, avatar_emoji, bio, trust_score, rating, review_count, jobs_completed, is_online, is_kyc_verified, is_pro_member, hourly_rate_paise, location_city, location_area, location_coords, wallet_balance_paise)
        VALUES (
            new_profile_id,
            new_worker_id,
            CASE WHEN i % 4 = 0 THEN '⚡' WHEN i % 4 = 1 THEN '🔧' WHEN i % 4 = 2 THEN '💻' ELSE '🎨' END,
            'Certified and background-verified skilled professional with over ' || (3 + (i % 7)) || ' years of field experience in Nagpur region.',
            80 + (i % 20),
            4.60 + ((i % 40) * 0.01),
            12 + (i * 3),
            18 + (i * 4),
            (i % 3 != 0), -- 66% online
            TRUE,
            (i % 5 = 0), -- 20% Pro subscribers
            35000 + ((i % 6) * 5000),
            'Nagpur',
            CASE WHEN i % 3 = 0 THEN 'Dharampeth' WHEN i % 3 = 1 THEN 'Sitabuldi' ELSE 'Ramdaspeth' END,
            ST_SetSRID(ST_MakePoint(79.0800 + ((i % 10) * 0.005), 21.1400 + ((i % 10) * 0.005)), 4326),
            125000 + (i * 10000)
        );

        -- Add skill
        INSERT INTO skills (profile_id, category_id, skill_name, proficiency_level, is_verified, jobs_done)
        VALUES (
            new_profile_id,
            chosen_cat,
            'Professional ' || chosen_cat,
            'EXPERT',
            TRUE,
            15 + (i * 2)
        );

        -- Add verified KYC
        INSERT INTO kyc_records (user_id, document_type, masked_identifier, document_hash, status, verified_at, verified_by)
        VALUES (
            new_worker_id,
            'AADHAAR_MASKED',
            'XXXX-XXXX-' || lpad((1000 + i)::text, 4, '0'),
            md5('hash_' || i),
            'APPROVED',
            CURRENT_TIMESTAMP,
            'UIDAI_API_SETU'
        );
    END LOOP;
END $$;

-- 4. Insert 30 Realistic Jobs
DO $$
DECLARE
    j INT;
    chosen_poster_id UUID;
    chosen_worker_id UUID;
    new_job_id UUID;
    cat_keys TEXT[] := ARRAY['cat_elec', 'cat_plumb', 'cat_cook', 'cat_driver', 'cat_webdev', 'cat_design'];
    job_titles TEXT[] := ARRAY[
        'Main MCB Switch Tripping on Heavy Load',
        'Bathroom Concealed Pipe Leak Repair',
        'Daily Vegetarian Lunch & Dinner Cook',
        'Full Day Outstation Chauffeur for Wardha Trip',
        'E-Commerce Mobile App UI Kit in Figma',
        'Spring Boot Microservice Bug Fix & Optimization',
        'Kitchen Sink Clogged & Drain Replacement',
        'Solar Inverter Grid Connection & Testing',
        'House Deep Cleaning Before Diwali',
        'Hindi & Marathi Audio Transcription'
    ];
BEGIN
    FOR j IN 1..30 LOOP
        SELECT id INTO chosen_poster_id FROM users WHERE role = 'POSTER' ORDER BY id LIMIT 1 OFFSET (j % 20);
        SELECT id INTO chosen_worker_id FROM users WHERE role = 'WORKER' ORDER BY id LIMIT 1 OFFSET (j % 50);
        new_job_id := uuid_generate_v4();

        INSERT INTO jobs (
            id, title, category_id, category_name, work_type, task_poster_id, worker_id, status, budget_paise, escrow_held, commission_rate, urgency, location_city, location_address, location_coords, description, match_explanation
        ) VALUES (
            new_job_id,
            job_titles[1 + (j % 10)],
            cat_keys[1 + (j % 6)],
            'Category ' || cat_keys[1 + (j % 6)],
            CASE WHEN j % 2 = 0 THEN 'LOCAL_ON_DEMAND' ELSE 'PROJECT_SKILLED' END,
            chosen_poster_id,
            chosen_worker_id,
            CASE 
                WHEN j % 5 = 0 THEN 'COMPLETED'
                WHEN j % 5 = 1 THEN 'IN_PROGRESS'
                WHEN j % 5 = 2 THEN 'ARRIVED'
                WHEN j % 5 = 3 THEN 'MATCHED'
                ELSE 'REQUESTED'
            END,
            85000 + ((j % 10) * 15000), -- ₹850 to ₹2,350
            TRUE,
            0.150,
            CASE WHEN j % 2 = 0 THEN 'IMMEDIATE' ELSE 'SCHEDULED' END,
            'Nagpur',
            'Plot ' || (10 + j) || ', West High Court Rd, Dharampeth, Nagpur',
            ST_SetSRID(ST_MakePoint(79.0882, 21.1458), 4326),
            'Urgent repair requirement. Please bring safety equipment and multimeters.',
            'Matched with certified technician based on 96% Trust Score and 1.8km radius proximity.'
        );

        -- Add milestone for project jobs
        IF j % 2 != 0 THEN
            INSERT INTO milestones (job_id, title, amount_paise, is_completed, is_released, order_index)
            VALUES 
            (new_job_id, 'Milestone 1: Architectural Draft & Wireframes', 40000, true, true, 1),
            (new_job_id, 'Milestone 2: Final High-Res Deliverables & Code', 60000, false, false, 2);
        END IF;

        -- Record initial Escrow Hold in ledger
        INSERT INTO escrow_ledger_entries (
            transaction_ref, idempotency_key, job_id, debit_account, credit_account, amount_paise, narration
        ) VALUES (
            'TX_HOLD_' || j,
            'IDEMP_HOLD_' || j,
            new_job_id,
            'TASK_POSTER_PREPAID',
            'ESCROW_LIABILITY',
            85000 + ((j % 10) * 15000),
            'Escrow deposit held at job creation'
        );
    END LOOP;
END $$;
