package com.balex.familyteam.presentation.regadmin

import kotlinx.coroutines.flow.StateFlow

interface RegAdminComponent {
    val model: StateFlow<RegAdminStore.State>
}