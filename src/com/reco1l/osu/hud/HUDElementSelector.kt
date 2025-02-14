package com.reco1l.osu.hud

import com.reco1l.andengine.Anchor
import com.reco1l.andengine.Axes
import com.reco1l.andengine.attachTo
import com.reco1l.andengine.container.Container
import com.reco1l.andengine.container.LinearContainer
import com.reco1l.andengine.container.Orientation
import com.reco1l.andengine.container.ScrollableContainer
import com.reco1l.andengine.getPaddedHeight
import com.reco1l.andengine.getPaddedWidth
import com.reco1l.andengine.shape.Box
import com.reco1l.andengine.shape.RoundedBox
import com.reco1l.andengine.text.ExtendedText
import com.reco1l.framework.ColorARGB
import com.reco1l.framework.math.Vec4
import com.reco1l.osu.hud.data.HUDElementLayoutData
import com.reco1l.osu.hud.data.HUDElementSkinData
import com.reco1l.osu.hud.elements.HUDAccuracyCounter
import com.reco1l.osu.hud.elements.HUDComboCounter
import com.reco1l.osu.hud.elements.HUDElement
import com.reco1l.osu.hud.elements.HUDHealthBar
import com.reco1l.osu.hud.elements.HUDPPCounter
import com.reco1l.osu.hud.elements.HUDPieSongProgress
import com.reco1l.osu.hud.elements.HUDScoreCounter
import com.reco1l.toolkt.kotlin.fastForEach
import org.anddev.andengine.input.touch.TouchEvent
import ru.nsu.ccfit.zuev.osu.Config
import ru.nsu.ccfit.zuev.osu.ResourceManager
import ru.nsu.ccfit.zuev.osu.game.GameScene
import ru.nsu.ccfit.zuev.osu.scoring.StatisticV2

class HUDElementSelector(private val hud: GameplayHUD) : Container() {


    private val elements = createAllElements()


    init {

        relativeSizeAxes = Axes.Y
        height = 1f
        x = -SELECTOR_WIDTH

        // The button to show/hide the element selector
        object : Container() {

            init {
                background = RoundedBox().apply {
                    cornerRadius = BUTTON_RADIUS
                    color = ColorARGB(0xFF181825)
                }
                width = BUTTON_WIDTH + BUTTON_RADIUS
                height = 150f
                x = SELECTOR_WIDTH - BUTTON_RADIUS
                anchor = Anchor.CenterLeft
                origin = Anchor.CenterLeft

                ExtendedText().apply {
                    rotation = -90f
                    anchor = Anchor.Center
                    origin = Anchor.Center
                    font = ResourceManager.getInstance().getFont("smallFont")
                    text = "Elements"
                    x = BUTTON_RADIUS / 2
                } attachTo this
            }

            override fun onAreaTouched(event: TouchEvent, localX: Float, localY: Float): Boolean {

                if (event.isActionUp) {

                    this@HUDElementSelector.clearEntityModifiers()

                    if (this@HUDElementSelector.x < 0f) {
                        this@HUDElementSelector.moveToX(0f, 0.2f)

                        hud.moveToX(SELECTOR_WIDTH, 0.2f)
                        hud.sizeToX(Config.getRES_WIDTH() - SELECTOR_WIDTH, 0.2f)
                    } else {
                        this@HUDElementSelector.moveToX(-SELECTOR_WIDTH, 0.2f)

                        hud.moveToX(0f, 0.2f)
                        hud.sizeToX(Config.getRES_WIDTH().toFloat(), 0.2f)
                    }
                }

                return false
            }

        } attachTo this

        ScrollableContainer().apply {

            scrollAxes = Axes.Y
            relativeSizeAxes = Axes.Y
            height = 1f
            width = SELECTOR_WIDTH

            indicatorY!!.width = 4f

            background = Box().apply {
                color = ColorARGB(0xFF1E1E2E)
            }

            val linearContainer = LinearContainer().apply {
                relativeSizeAxes = Axes.X
                width = 1f
                padding = Vec4(16f)
                spacing = 12f
                orientation = Orientation.Vertical
            } attachTo this

            elements.forEach { element ->

                val elementWrapper = Container().apply {
                    relativeSizeAxes = Axes.X
                    width = 1f
                    height = 120f
                    padding = Vec4(12f)
                    background = RoundedBox().apply {
                        color = ColorARGB(0xFF363653)
                        cornerRadius = 12f
                    }
                } attachTo linearContainer

                ExtendedText().apply {
                    font = ResourceManager.getInstance().getFont("smallFont")
                    anchor = Anchor.BottomLeft
                    origin = Anchor.BottomLeft
                    text = element.name
                    color = ColorARGB.White
                } attachTo elementWrapper

                element.elementData = HUDElementSkinData(element::class, HUDElementLayoutData())
                element attachTo elementWrapper
                element.onApplyElementSkinData()

                // Scaling the element inside the box

                if (element.drawHeight > elementWrapper.getPaddedHeight()) {
                    element.setScale(elementWrapper.getPaddedHeight() / element.drawHeight)
                }

                if (element.drawWidth > elementWrapper.getPaddedWidth()) {
                    element.setScale(elementWrapper.getPaddedWidth() / element.drawWidth)
                }
            }

        } attachTo this

    }


    //region Gameplay Events
    fun onNoteHit(statistics: StatisticV2) {
        elements.fastForEach {
            (it as? HUDElement)?.onNoteHit(statistics)
        }
    }

    fun onBreakStateChange(isBreak: Boolean) {
        elements.fastForEach {
            (it as? HUDElement)?.onBreakStateChange(isBreak)
        }
    }

    fun onGameplayUpdate(game: GameScene, statistics: StatisticV2, secondsElapsed: Float) {
        elements.fastForEach {
            (it as? HUDElement)?.onGameplayUpdate(game, statistics, secondsElapsed)
        }
    }
    //endregion


    companion object {


        const val SELECTOR_WIDTH = 300f

        const val BUTTON_WIDTH = 48f

        const val BUTTON_RADIUS = 12f


        fun createAllElements(): List<HUDElement> {
            return listOf(
                HUDAccuracyCounter(),
                HUDComboCounter(),
                HUDHealthBar(),
                HUDPieSongProgress(),
                HUDPPCounter(),
                HUDScoreCounter()
            )
        }

    }

}