package com.balex.familyteam.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalTasks(
    val externalTasks: List<ExternalTask>
) : Parcelable
