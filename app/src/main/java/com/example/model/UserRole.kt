package com.example.model

enum class UserRole(val labelFr: String, val badgeColorHex: Long) {
    SUPER_ADMIN("Super Administrateur", 0xFF6366F1), // Indigo
    ENTERPRISE_ADMIN("Admin Entreprise", 0xFF06B6D4) // Cyan
}

data class UserSession(
    val email: String = "admin@tharatracking.com",
    val name: String = "Vincent Franky",
    val role: UserRole = UserRole.SUPER_ADMIN,
    val enterpriseId: String = "ENT-01",
    val enterpriseName: String = "Logistique Dakar Express"
)
