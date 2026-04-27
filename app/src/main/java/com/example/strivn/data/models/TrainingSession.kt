package com.example.strivn.data.models

import java.time.Duration
import java.time.LocalDate

data class TrainingSession(
    val date: LocalDate,
    val duration: Duration,
)

