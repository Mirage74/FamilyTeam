package com.balex.familyteam.presentation.customnavigation

import android.os.Parcel
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.subscribe
import com.arkivanov.essenty.parcelable.Parcelable
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.observableInterval
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.mainScheduler
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

class DefaultKittenComponent(
    componentContext: ComponentContext,
    private val imageResourceId: ImageResourceId,
) : KittenComponent, ComponentContext by componentContext {

    private val handler =
        instanceKeeper.getOrCreate(KEY_STATE) {
            Handler(initialState = stateKeeper.consume(key = KEY_STATE, State::class) ?: State())
        }

    override val model: Value<KittenComponent.Model> = handler.state.map { it.toModel() }

    init {
        lifecycle.subscribe(
            onStart = handler::resume,
            onStop = handler::pause,
        )

        stateKeeper.register(key = KEY_STATE) { handler.state.value }
    }

    private fun State.toModel(): KittenComponent.Model =
        KittenComponent.Model(
            imageResourceId = imageResourceId,
            text = count.toString().padStart(length = 3, padChar = '0'),
        )

    private companion object {
        private const val KEY_STATE = "STATE"
    }

    @Parcelize
    private data class State(
        val count: Int = 0,
    ) : Parcelable

    private class Handler(initialState: State) : InstanceKeeper.Instance {
        val state: MutableValue<State> = MutableValue(initialState)

        private var disposable: Disposable? = null

        fun resume() {
            disposable?.dispose()

            disposable =
                observableInterval(periodMillis = 250L, scheduler = mainScheduler).subscribe(isThreadLocal = true) {
                    state.update { it.copy(count = it.count + 1) }
                }
        }

        fun pause() {
            disposable?.dispose()
            disposable = null
        }

        override fun onDestroy() {
            pause()
        }
    }
}