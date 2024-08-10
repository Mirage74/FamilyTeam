package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalTask(
    val task: Task,
    val taskOwner: String
) : Parcelable
