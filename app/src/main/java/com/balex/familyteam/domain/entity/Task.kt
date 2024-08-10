package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

@Parcelize
data class Task(
    val description: String,
    val cutoffTime: Calendar
) : Parcelable
