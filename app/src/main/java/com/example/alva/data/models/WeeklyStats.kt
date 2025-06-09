package com.example.alva.data.models

data class WeeklyStats(
    val averageCalories: Int,
    val daysGoalMet: Int,
    val totalCalories: Int,
    val bestDay: String,
    val bestDayCalories: Int,
    val worstDay: String,
    val worstDayCalories: Int
)