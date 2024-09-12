package com.rian.osu.difficulty.skills

import com.rian.osu.difficulty.DroidDifficultyHitObject
import com.rian.osu.difficulty.evaluators.DroidRhythmEvaluator
import com.rian.osu.mods.Mod
import kotlin.math.pow

/**
 * Represents the skill required to properly follow a beatmap's rhythm.
 */
class DroidRhythm(
    /**
     * The [Mod]s that this skill processes.
     */
    mods: List<Mod>,

    /**
     * The clock rate of the beatmap.
     */
    private val clockRate: Double
) : DroidStrainSkill(mods) {
    override val reducedSectionCount = 5
    override val starsPerDouble = 1.75

    override val objectStrain: Double
        get() = currentStrain

    private var currentStrain = 0.0
    private val strainDecayBase = 0.3

    override fun strainValueAt(current: DroidDifficultyHitObject): Double {
        currentStrain *= strainDecay(current.deltaTime)
        currentStrain += DroidRhythmEvaluator.evaluateDifficultyOf(current, clockRate) - 1

        return currentStrain
    }

    override fun calculateInitialStrain(time: Double, current: DroidDifficultyHitObject) =
        currentStrain * strainDecay(time - current.previous(0)!!.startTime)

    private fun strainDecay(ms: Double) = strainDecayBase.pow(ms / 1000)
}