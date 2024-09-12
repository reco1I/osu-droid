package com.rian.osu.difficulty.skills

import com.rian.osu.difficulty.DroidDifficultyHitObject
import com.rian.osu.difficulty.evaluators.DroidRhythmEvaluator
import com.rian.osu.difficulty.evaluators.DroidVisualEvaluator
import com.rian.osu.mods.Mod
import com.rian.osu.mods.ModHidden
import kotlin.math.pow

/**
 * Represents the skill required to read every object in the beatmap.
 */
class DroidVisual(
    /**
     * The [Mod]s that this skill processes.
     */
    mods: List<Mod>,

    /**
     * The clock rate of the beatmap.
     */
    private val clockRate: Double,

    /**
     * Whether to consider sliders in the calculation.
     */
    private val withSliders: Boolean
) : DroidStrainSkill(mods) {
    override val starsPerDouble = 1.025

    override val objectStrain: Double
        get() = currentStrain * currentRhythm

    private var currentStrain = 0.0
    private var currentRhythm = 0.0
    private val skillMultiplier = 10
    private val strainDecayBase = 0.1
    private val isHidden = mods.any { it is ModHidden }

    override fun strainValueAt(current: DroidDifficultyHitObject): Double {
        currentStrain *= strainDecay(current.deltaTime)
        currentStrain += DroidVisualEvaluator.evaluateDifficultyOf(current, isHidden, withSliders) * skillMultiplier

        currentRhythm = DroidRhythmEvaluator.evaluateDifficultyOf(current, clockRate)

        return currentStrain * currentRhythm
    }

    override fun calculateInitialStrain(time: Double, current: DroidDifficultyHitObject) =
        currentStrain * currentRhythm * strainDecay(time - current.previous(0)!!.startTime)

    private fun strainDecay(ms: Double) = strainDecayBase.pow(ms / 1000)
}