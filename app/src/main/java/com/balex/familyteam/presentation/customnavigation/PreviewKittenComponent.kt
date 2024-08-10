package com.balex.familyteam.presentation.customnavigation

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

class PreviewKittenComponent : KittenComponent {

    override val model: Value<KittenComponent.Model> = MutableValue(
        KittenComponent.Model(
            imageResourceId = ImageResourceId.CAT_1,
            text = "Kitten"
        )
    )
}