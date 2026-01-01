package org.kuppihub.app.ui.theme

import androidx.compose.ui.graphics.Color

// --- PRIMARY BLUES ---
val Blue50  = Color(0xFFEFF6FF)
val Blue100 = Color(0xFFDBEAFE)
val Blue200 = Color(0xFFBFDBFE) // Used in Body Gradient
val Blue500 = Color(0xFF3B82F6) // Primary Brand
val Blue600 = Color(0xFF2563EB) // Interactive / Hover
val Blue700 = Color(0xFF1D4ED8) // Active

// --- SECONDARY INDIGOS ---
val Indigo100 = Color(0xFFE0E7FF)
val Indigo500 = Color(0xFF6366F1) // Accent
val Indigo600 = Color(0xFF4F46E5)

// --- PURPLES (For Gradients) ---
val Purple200 = Color(0xFFE9D5FF)
val Purple300 = Color(0xFFC4B5FD)
val Violet500 = Color(0xFF8B5CF6)
val DeepPurple = Color(0xFF764BA2)

// --- FUNCTIONAL COLORS ---
val SuccessGreen = Color(0xFF16A34A) // Green-600
val DangerRed    = Color(0xFFEF4444) // Red-500
val White        = Color(0xFFFFFFFF)
val BorderBlue   = Color(0xFFDBEAFE) // Blue tinted border

// --- GRADIENT BRUSHES (Helper for UI) ---
// We can't define Brushes here easily, we do it in the Theme file or a helper object.