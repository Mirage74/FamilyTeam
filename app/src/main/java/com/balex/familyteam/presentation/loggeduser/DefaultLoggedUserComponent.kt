package com.balex.familyteam.presentation.loggeduser

import com.arkivanov.decompose.ComponentContext

class DefaultLoggedUserComponent(
    componentContext: ComponentContext
) : LoggedUserComponent, ComponentContext by componentContext