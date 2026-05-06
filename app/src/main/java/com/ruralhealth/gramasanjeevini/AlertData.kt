package com.ruralhealth.gramasanjeevini

import androidx.compose.ui.graphics.Color

data class HealthAlert(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val color: Color,
    val timeLabel: String,
    val actionText: String = "VIEW INFO",
    val aiSummary: String,
    val precautions: List<String>,
    val contactType: String = "General Support",
    val phoneNumber: String = "112"
)

object AlertProvider {
    val simulatedAlerts = listOf(
        // --- MIXED & SHUFFLED TIMELINE ---
        HealthAlert(1, "Monsoon Health Warning", "Cases of Dengue are rising in nearby villages.", "🔴 EMERGENCY", Color(0xFFD32F2F), "10 mins ago", "VIEW INFO", "Grama AI Analysis: Stagnant water clusters have increased by 40%. Breeding risk is high.", listOf("Use mosquito nets every night", "Clear stagnant water", "Wear long sleeves"), "Hospital", "102"),

        HealthAlert(11, "Generic Medicine Stock", "Fresh stock of BP tablets at Janaushadhi.", "🟡 STOCK", Color(0xFFFBC02D), "25 mins ago", "FIND SHOP", "Inventory Alert: Metformin and Amlodipine verified back in stock. Prices are 70% lower.", listOf("Bring latest prescription", "Ask for Generic", "Check the seal"), "Pharmacy", "112"),

        HealthAlert(21, "Ayushman Bharat Update", "Registration drive for new Health Cards.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "45 mins ago", "VIEW INFO", "Scheme Benefit: Coverage expanded to 50+ new procedures. Insurance up to ₹5 Lakhs.", listOf("Bring Aadhaar and Ration Card", "Link mobile number", "Free registration"), "Panchayat", "112"),

        HealthAlert(36, "Veterinary Health Alert", "Foot and Mouth disease in local cattle.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "1 hour ago", "VIEW INFO", "AI Vet-Bot: Local cluster detected. Vaccination prevents 95% transmission.", listOf("Isolate sick cattle", "Disinfect the shed", "Call Gov Vet"), "Vet Support", "112"),

        HealthAlert(2, "Snake Bite Advisory", "Active movement of vipers reported in local fields.", "🔴 EMERGENCY", Color(0xFFD32F2F), "1 hour ago", "VIEW INFO", "Field Safety: AI suggests caution during dawn and dusk hours due to high humidity.", listOf("Carry a torch light", "Wear high rubber boots", "Do not cut the wound"), "Ambulance", "108"),

        HealthAlert(12, "Insulin Availability", "Refrigerated stock restocked at Apollo Pharmacy.", "🟡 STOCK", Color(0xFFFBC02D), "2 hours ago", "FIND SHOP", "Stock Note: High-demand Insulin pens and vials are now available.", listOf("Carry a cool-bag", "Verify expiry date", "Keep in fridge"), "Pharmacy", "112"),

        HealthAlert(22, "Child Nutrition Scheme", "Free iron supplement distribution at Anganwadi.", "🟢 WELLNESS", Color(0xFF388E3C), "2 hours ago", "VIEW INFO", "Health Focus: Children aged 1-6 years are eligible for free nutritional kits.", listOf("Visit before 1 PM", "Bring Mamta card", "Consult ASHA"), "Anganwadi", "102"),

        HealthAlert(49, "Anti-Snake Venom", "Fresh supply received at Taluk hospital.", "🟡 STOCK", Color(0xFFFBC02D), "3 hours ago", "VIEW INFO", "Stock Update: AI verified 20 vials of polyvalent venom now available.", listOf("Treatment is 100% free", "Rush within golden hour", "Do not visit tantriks"), "Hospital", "102"),

        HealthAlert(3, "Heatwave Advisory", "Temperature expected to cross 42°C today.", "🔴 EMERGENCY", Color(0xFFD32F2F), "3 hours ago", "VIEW INFO", "Climate Alert: Local temperatures peaking 5 degrees above average. High stroke risk.", listOf("Drink 5L of water", "Keep livestock in shade", "Avoid 12 PM - 4 PM sun"), "Ambulance", "108"),

        HealthAlert(23, "Eye Check-up Camp", "Free cataract screening this weekend.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "4 hours ago", "VIEW INFO", "Vision Drive: Free surgery for selected senior patients over 60.", listOf("Empty stomach not req", "Free transport available", "Bring old reports"), "Clinic", "112"),

        HealthAlert(37, "Poultry Safety", "Preventive measures against Bird Flu.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "5 hours ago", "VIEW INFO", "Vet Alert: Increased bird deaths reported. AI recommends coop bio-security.", listOf("Clean poultry mesh", "Report sudden deaths", "Wash eggs before use"), "Vet Support", "112"),

        HealthAlert(50, "Grama AI Daily Tip", "Simple check-up for a healthy family.", "🟢 WELLNESS", Color(0xFF388E3C), "6 hours ago", "VIEW INFO", "Wellness: Small habits lead to long lives. Follow this 5-minute routine.", listOf("Drink warm water", "Walk for 20 mins", "Eat fruit before tea"), "Support", "112"),

        HealthAlert(4, "Fake Medicine Warning", "Counterfeit Paracetamol syrup detected.", "🔴 EMERGENCY", Color(0xFFD32F2F), "Today", "VIEW INFO", "Drug Safety: Batch #XP-2026 flagged as unsafe. Check home stock.", listOf("Check batch number", "Look for spelling errors", "Do not use if discolored"), "Drug Inspector", "112"),

        HealthAlert(13, "First Aid Kits", "Emergency home kits now at discounted rates.", "🟡 STOCK", Color(0xFFFBC02D), "Today", "FIND SHOP", "Wellness Deal: Panchayat-approved kits available at local centers.", listOf("Ideal for farm use", "Check contents list", "Keep in easy reach"), "Support", "102"),

        HealthAlert(24, "Maternity Benefits", "Apply for PMMVY financial aid now.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "Today", "VIEW INFO", "Mother Care: Get financial aid for your first child. Apply before 6th month.", listOf("Register at Anganwadi", "Bring Bank Passbook", "Aadhaar mandatory"), "Anganwadi", "102"),

        HealthAlert(38, "Rabies Vaccination", "Free drive for street and pet dogs.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "Today", "VIEW INFO", "Safety Drive: AI suggests mandatory shots for all local guard dogs.", listOf("Secure pets during drive", "Vaccination is free", "Follow-up in 1 month"), "Vet Support", "112"),

        HealthAlert(5, "Electrical Fire Hazard", "Report loose hanging wires after storm.", "🔴 EMERGENCY", Color(0xFFD32F2F), "Today", "VIEW INFO", "Safety Alert: High voltage risks detected in Sector 3. Keep kids indoors.", listOf("Do not touch fallen wires", "Stay away from wet poles", "Unplug heavy appliances"), "Support", "112"),

        HealthAlert(14, "Oxygen Cylinder Hub", "New refill station opened in Taluk.", "🟡 STOCK", Color(0xFFFBC02D), "Today", "VIEW INFO", "Resource Alert: AI verified a new 24/7 oxygen refill point.", listOf("Keep doctor note ready", "Book 4 hours in advance", "Follow storage rules"), "Support", "102"),

        HealthAlert(25, "Yoga for Seniors", "Daily morning sessions at local park.", "🟢 WELLNESS", Color(0xFF388E3C), "Today", "VIEW INFO", "Wellness Hub: Guided sessions to control BP and Stress for ages 50+.", listOf("Starts at 6:30 AM", "Wear cotton clothes", "Bring water bottle"), "Support", "112"),

        HealthAlert(39, "Livestock Feed Warning", "Check for fungus in stored hay/fodder.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "Today", "VIEW INFO", "Agri-AI: Moldy feed detected due to humidity. Can lead to milk toxicity.", listOf("Sun-dry fodder", "Store on raised platforms", "Discard moldy parts"), "Vet Support", "112"),

        HealthAlert(6, "Rabies Outbreak Note", "Stray dog bites reported near bus stand.", "🔴 EMERGENCY", Color(0xFFD32F2F), "Yesterday", "VIEW INFO", "AI Safety Note: Three cases reported. Immediate vaccination planned.", listOf("Avoid unfamiliar dogs", "Wash wound with soap", "Seek vaccine in 6 hours"), "Clinic", "108"),

        HealthAlert(15, "Vitamin C Shortage", "Supply delay for chewable supplements.", "🟡 STOCK", Color(0xFFFBC02D), "Yesterday", "VIEW INFO", "Logistics Alert: Truck strike delayed supply. Use natural alternatives.", listOf("Consume Lemons/Guavas", "Check local dispensaries", "Do not panic buy"), "Support", "112"),

        HealthAlert(26, "TB Awareness Month", "Free testing and DOTS meds at Taluk.", "🟢 WELLNESS", Color(0xFF388E3C), "Yesterday", "VIEW INFO", "Health Scheme: Get ₹500/month nutrition support. TB is 100% curable.", listOf("Check for cough > 2 weeks", "Testing is private", "Don't skip doses"), "Hospital", "102"),

        HealthAlert(40, "Goat Pox Warning", "Watch for skin lesions in your flock.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "Yesterday", "VIEW INFO", "AI Flock Alert: Highly contagious viral disease detected in Sector 1.", listOf("Check for skin bumps", "Do not sell sick animals", "Keep pens clean"), "Vet Support", "112"),

        HealthAlert(7, "Flash Flood Warning", "Water levels rising in river basin.", "🔴 EMERGENCY", Color(0xFFD32F2F), "Yesterday", "VIEW INFO", "Environmental Scan: Upstream dam release. Sector 2 is at risk.", listOf("Move to higher ground", "Store clean water", "Avoid crossing bridges"), "Support", "108"),

        HealthAlert(16, "Inhaler Stock Update", "Asthma pumps restocked at Sanjeevini.", "🟡 STOCK", Color(0xFFFBC02D), "Yesterday", "FIND SHOP", "Stock Note: Standard Blue/Purple inhalers available. Max 2 per person.", listOf("Test pump before leaving", "Check dose counter", "Carry medical ID"), "Pharmacy", "112"),

        HealthAlert(27, "Kisan Health Card", "Renew your card for subsidized treatment.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "2 days ago", "VIEW INFO", "Farmer Support: Insurance for farm injuries. Link card to your bank.", listOf("Visit CSC center", "Bring old card", "Update photo"), "Panchayat", "112"),

        HealthAlert(41, "Honey Bee Health", "Safe pesticide use near bee colonies.", "🟢 WELLNESS", Color(0xFF388E3C), "2 days ago", "VIEW INFO", "Eco Alert: Pollination is dropping. Use bee-safe chemicals.", listOf("Spray after sunset", "Avoid blooming flowers", "Provide clean water"), "Agri Support", "112"),

        HealthAlert(8, "Chikungunya Alert", "Rise in joint-pain complaints reported.", "🔴 EMERGENCY", Color(0xFFD32F2F), "2 days ago", "VIEW INFO", "Health Trend: Cluster of symptoms flagged. Vector control intensified.", listOf("Eliminate breeding spots", "Use DEET repellents", "Sleep under nets"), "Hospital", "102"),

        HealthAlert(17, "Blood Bag Inventory", "O-Negative blood urgently needed.", "🟡 STOCK", Color(0xFFFBC02D), "3 days ago", "CALL NOW", "Emergency Hub: Shortage of rare blood groups at District center.", listOf("Must be over 18", "Weight above 50kg", "Bring ID proof"), "Hospital", "102"),

        HealthAlert(28, "Water Safety Alert", "Pipe repairs affecting local quality.", "🟠 WARNING", Color(0xFFFF9800), "3 days ago", "VIEW INFO", "Quality Control: Potential turbidity in Sector 4. Use home filtration.", listOf("Boil for 10 mins", "Strain through cloth", "Wash hands with soap"), "Support", "112"),

        HealthAlert(42, "Cattle Deworming", "Quarterly drive starting this Monday.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "3 days ago", "VIEW INFO", "Growth Note: Deworming increases milk yield. AI Verified schedule.", listOf("Register tag number", "Dosage by weight", "Keep empty stomach"), "Vet Support", "112"),

        HealthAlert(9, "Scrub Typhus Alert", "Mite-borne illness in grazing lands.", "🔴 EMERGENCY", Color(0xFFD32F2F), "4 days ago", "VIEW INFO", "AI Medical Note: High fever cases after field work. Caused by mites.", listOf("Shower after field work", "Wash clothes in hot water", "Check for dark marks"), "Hospital", "102"),

        HealthAlert(18, "Disinfectant Drive", "Bleaching powder available at Panchayat.", "🟡 STOCK", Color(0xFFFBC02D), "4 days ago", "FIND SHOP", "Sanitation Note: Free lime provided for drain cleaning.", listOf("Outdoor drains only", "Wear a mask", "Keep away from pets"), "Panchayat", "112"),

        HealthAlert(29, "Pesticide Safety Drive", "Safe handling training for farmers.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "4 days ago", "VIEW INFO", "Farmer Welfare: Learn 'Green Label' techniques to avoid respiratory issues.", listOf("Wear rubber gloves", "Spray with wind", "Shower after handling"), "Support", "112"),

        HealthAlert(43, "Fish Pond Safety", "Maintain oxygen levels during heatwave.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "5 days ago", "VIEW INFO", "Aquaculture AI: Risk of fish kill due to low oxygen in village ponds.", listOf("Add fresh water evening", "Avoid noon overfeeding", "Check for gasping"), "Vet Support", "112"),

        HealthAlert(10, "Gas Leak Advisory", "Chemical odor near cold storage.", "🔴 EMERGENCY", Color(0xFFD32F2F), "5 days ago", "VIEW INFO", "Industrial Alert: Precautionary evacuation of 100m radius.", listOf("Cover face with wet cloth", "Move against wind", "Close all doors"), "Ambulance", "108"),

        HealthAlert(19, "Surgical Mask Batch", "N95 masks available at subsidized rates.", "🟡 STOCK", Color(0xFFFBC02D), "5 days ago", "FIND SHOP", "Safety Supply: High-quality masks for farm dust and allergies.", listOf("Reusable 5 times", "Hand wash with soap", "Ensure tight fit"), "Support", "112"),

        HealthAlert(30, "Safe Disposal Alert", "Discard expired meds at Panchayat bins.", "🟢 WELLNESS", Color(0xFF388E3C), "6 days ago", "VIEW INFO", "Environment: Don't throw meds in soil; can poison local cattle.", listOf("Remove pills from carton", "Drop in Green Bins", "Check cabinet monthly"), "Panchayat", "112"),

        HealthAlert(44, "Calf Care Winter", "Warm housing for newborn livestock.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "6 days ago", "VIEW INFO", "Seasonal Alert: Cold winds increase pneumonia risk in young calves.", listOf("Use gunny bag covers", "Keep floor dry with husk", "Provide warm water"), "Vet Support", "112"),

        HealthAlert(31, "World Heart Day", "Free ECG and BP check at primary center.", "🟢 WELLNESS", Color(0xFF388E3C), "Last Week", "VIEW INFO", "Heart Health: AI suggests check-up if you are over 40.", listOf("Avoid heavy meals", "Bring ID for registration", "Diet charts provided"), "Clinic", "102"),

        HealthAlert(20, "Dialysis Slot Open", "New evening shift added at Hospital.", "🟡 STOCK", Color(0xFFFBC02D), "Last Week", "VIEW INFO", "Capacity: 4 new slots under the free government scheme verified.", listOf("Register with Health Card", "Shift starts at 6 PM", "Free village transport"), "Hospital", "102"),

        HealthAlert(32, "Solar Light Safety", "New lights on forest-fringe roads.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "Last Week", "VIEW INFO", "Security: Improved visibility to prevent animal encounters.", listOf("Report non-working lights", "Avoid walking alone", "Use bicycle reflectors"), "Panchayat", "112"),

        HealthAlert(45, "Artificial Insemination", "Free doorstep service for 1 week.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "Last Week", "VIEW INFO", "Breed Improvement: High-yield straws available for village farmers.", listOf("Book via help-line", "Keep animal ready", "Provide health history"), "Vet Support", "112"),

        HealthAlert(33, "Mother-Child Nutrition", "Egg and milk distribution at schools.", "🔵 GOVT SCHEME", Color(0xFF1976D2), "Last Week", "VIEW INFO", "School Health: Every child gets daily protein under state budget.", listOf("Registered students only", "Check food allergies", "Distribution at 11 AM"), "Anganwadi", "102"),

        HealthAlert(46, "Milk Hygiene Alert", "Clean hands and vessels for milking.", "🟢 WELLNESS", Color(0xFF388E3C), "Last Week", "VIEW INFO", "Dairy AI: Bacterial counts rising. Use hot water wash for vessels.", listOf("Wash udder properly", "Use stainless steel", "Deliver in 30 mins"), "Support", "112"),

        HealthAlert(34, "Malaria Prevention", "Fogging schedule starts at 6 PM.", "🟠 WARNING", Color(0xFFFF9800), "Last Week", "VIEW INFO", "Sanitation: Municipal fogging to kill larvae in open drains.", listOf("Cover drinking water", "Keep infants away", "Clear bushes"), "Support", "112"),

        HealthAlert(47, "Pet De-ticking", "Prevent tick-fever in farm dogs.", "🟣 LIVESTOCK", Color(0xFF8E24AA), "Last Week", "VIEW INFO", "Vet Note: Ticks spread diseases to cattle. Summer peak starting.", listOf("Use approved powders", "Clear tall grass", "Check paws regularly"), "Vet Support", "112"),

        HealthAlert(35, "Hand-Wash Drive", "School workshop on hygiene for kids.", "🟢 WELLNESS", Color(0xFF388E3C), "Last Month", "VIEW INFO", "Hygiene: Proper washing reduces stomach infections by 60%.", listOf("Use soap for 20s", "Clean nails properly", "Wash before meals"), "Clinic", "112"),

        HealthAlert(48, "Silo Ventilation", "Prevent carbon-monoxide in grain pits.", "🔴 EMERGENCY", Color(0xFFD32F2F), "Last Month", "VIEW INFO", "Safety: Fermenting grain releases toxic gases in closed pits.", listOf("Open pits 1 hr before", "Never enter alone", "Use candle test"), "Support", "112")
    )
}