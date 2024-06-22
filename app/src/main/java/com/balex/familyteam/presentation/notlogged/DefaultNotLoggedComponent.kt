package com.balex.familyteam.presentation.notlogged

import com.arkivanov.decompose.ComponentContext

class DefaultNotLoggedComponent(
    componentContext: ComponentContext
) : NotLoggedComponent, ComponentContext by componentContext