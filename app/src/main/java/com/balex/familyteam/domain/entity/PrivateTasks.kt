package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrivateTasks(
    val privateTasks: List<Task>
) : Parcelable
