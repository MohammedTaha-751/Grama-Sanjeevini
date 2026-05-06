package com.ruralhealth.gramasanjeevini

object EmergencyProvider {
    // A robust list of critical life-saving medications for rural emergencies
    val criticalMeds = listOf(
        // --- DIABETES & METABOLIC ---
        "Insulin", "Insulin Glargine", "Insulin Aspart", "Insulin Degludec", "Glucagon",

        // --- TRAUMA, BITES & INFECTIONS ---
        "Snake Venom", "ASV", "Anti-Snake Venom", "Anti-Rabies", "Tetanus Toxoid",
        "Rabies Vaccine", "Immunoglobulin",

        // --- RESPIRATORY & SEVERE ALLERGY ---
        "Oxygen", "Adrenaline", "Epinephrine", "Hydrocortisone", "Dexamethasone",
        "Salbutamol", "Nebulizer Solution", "Aminophylline",

        // --- HEART ATTACK & BLOOD PRESSURE ---
        "Nitroglycerin", "Sorbitrate", "Aspirin 75", "Clopidogrel", "Atorvastatin",
        "Amlodipine", "Digoxin", "Heparin",

        // --- PAIN & SURGICAL ---
        "Morphine", "Atropine", "Fentanyl", "Ketamine", "Lidocaine",

        // --- MATERNAL & NEWBORN CARE ---
        "Oxytocin", "Magnesium Sulfate", "Misoprostol", "Vitamin K Injection",

        // --- CRITICAL FLUIDS & DEFICIENCIES ---
        "Iron Sucrose", "Calcium Gluconate", "Potassium Chloride", "Dextrose 25%"
    )

    // Function to check if a medicine is life-saving
    // Returns true if the medicine name contains any of the critical keywords
    fun isCritical(medicineName: String): Boolean {
        return criticalMeds.any { medicineName.contains(it, ignoreCase = true) }
    }
}