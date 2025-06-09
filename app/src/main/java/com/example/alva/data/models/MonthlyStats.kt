package com.example.alva.data.models

data class MonthlyStats(
    val averageCalories: Int,
    val daysGoalMet: Int,
    val totalDays: Int,
    val totalCalories: Int,
    val bestWeek: Int,
    val bestWeekCalories: Int,
    val worstWeek: Int,
    val worstWeekCalories: Int
)