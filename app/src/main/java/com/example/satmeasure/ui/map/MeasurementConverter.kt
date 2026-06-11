package com.example.satmeasure.ui.map

import androidx.annotation.StringRes
import com.example.satmeasure.R

enum class IndianState(@param:StringRes val displayNameResId: Int) {
    RAJASTHAN(R.string.state_rajasthan),
    BIHAR(R.string.state_bihar),
    JHARKHAND(R.string.state_jharkhand),
    UTTAR_PRADESH(R.string.state_uttar_pradesh),
    GUJARAT(R.string.state_gujarat),
    ASSAM(R.string.state_assam),
    WEST_BENGAL(R.string.state_west_bengal),
    MADHYA_PRADESH(R.string.state_madhya_pradesh),
    PUNJAB_HARYANA(R.string.state_punjab_haryana),
    HIMACHAL_PRADESH(R.string.state_himachal_pradesh),
    UTTARAKHAND(R.string.state_uttarakhand)
}

sealed class AreaUnit {
    @get:StringRes
    abstract val displayNameResId: Int

    // 1. Global Standard Units
    data object SquareMeter : AreaUnit() { override val displayNameResId = R.string.unit_square_meter }
    data object SquareYard : AreaUnit() { override val displayNameResId = R.string.unit_square_yard }
    data object Acre : AreaUnit() { override val displayNameResId = R.string.unit_acre }
    data object Hectare : AreaUnit() { override val displayNameResId = R.string.unit_hectare }

    // 2. State-Specific "Bigha"
    data class Bigha(val state: IndianState) : AreaUnit() { 
        override val displayNameResId = R.string.unit_bigha
    }

    // 3. Regional North Indian Units
    data object Kanal : AreaUnit() { override val displayNameResId = R.string.unit_kanal }
    data object Marla : AreaUnit() { override val displayNameResId = R.string.unit_marla }
    data class Biswa(val state: IndianState) : AreaUnit() { 
        override val displayNameResId = R.string.unit_biswa
    }

    // 4. Regional East Indian Units
    data object KathaAssam : AreaUnit() { override val displayNameResId = R.string.unit_katha_assam }
    data object KathaBihar : AreaUnit() { override val displayNameResId = R.string.unit_katha_bihar }
    data object KathaWestBengal : AreaUnit() { override val displayNameResId = R.string.unit_katha_wb }
    data object Dhur : AreaUnit() { override val displayNameResId = R.string.unit_dhur }
    data object Lecha : AreaUnit() { override val displayNameResId = R.string.unit_lecha }
    data object Decimal : AreaUnit() { override val displayNameResId = R.string.unit_decimal }
}

object MeasurementConverter {

    /**
     * Converts a base area in Square Feet into the target AreaUnit.
     * Uses precise double-precision floating point math for accuracy.
     */
    fun convertArea(baseAreaSqFt: Double, targetUnit: AreaUnit): Double {
        if (baseAreaSqFt == 0.0) return 0.0

        val conversionFactorSqFt = when (targetUnit) {
            // Global Units
            is AreaUnit.SquareMeter -> 10.7639
            is AreaUnit.SquareYard -> 9.0
            is AreaUnit.Acre -> 43560.0
            is AreaUnit.Hectare -> 107639.0

            // Bigha
            is AreaUnit.Bigha -> getBighaSqFt(targetUnit.state)

            // North Indian
            is AreaUnit.Kanal -> 5445.0
            is AreaUnit.Marla -> 272.25
            is AreaUnit.Biswa -> getBighaSqFt(targetUnit.state) / 20.0

            // East Indian
            is AreaUnit.KathaAssam -> 2880.0
            is AreaUnit.KathaBihar -> 1361.25
            is AreaUnit.KathaWestBengal -> 720.0
            is AreaUnit.Dhur -> 68.06
            is AreaUnit.Lecha -> 144.0
            is AreaUnit.Decimal -> 435.6
        }

        return baseAreaSqFt / conversionFactorSqFt
    }

    /**
     * Retrieves the precise Square Feet value for 1 Bigha based on the Indian State.
     */
    fun getBighaSqFt(state: IndianState): Double {
        return when (state) {
            IndianState.RAJASTHAN -> 27225.0
            IndianState.BIHAR -> 27220.0
            IndianState.JHARKHAND -> 27211.0
            IndianState.UTTAR_PRADESH -> 27000.0
            IndianState.GUJARAT -> 17424.0
            IndianState.ASSAM -> 14400.0
            IndianState.WEST_BENGAL -> 14400.0
            IndianState.MADHYA_PRADESH -> 12000.0
            IndianState.PUNJAB_HARYANA -> 9070.0
            IndianState.HIMACHAL_PRADESH -> 8712.0
            IndianState.UTTARAKHAND -> 6804.0
        }
    }

    /**
     * Helper to get a comprehensive list of all Bigha variations
     */
    fun getAllBighaUnits(): List<AreaUnit.Bigha> {
        return IndianState.entries.map { AreaUnit.Bigha(it) }
    }

    /**
     * Helper to get all non-Bigha regional units
     */
    fun getOtherLocalUnits(): List<Pair<Int, AreaUnit>> {
        return listOf(
            Pair(R.string.tab_local_units, AreaUnit.Kanal),
            Pair(R.string.tab_local_units, AreaUnit.Marla),
            Pair(R.string.state_assam, AreaUnit.KathaAssam),
            Pair(R.string.state_assam, AreaUnit.Lecha),
            Pair(R.string.state_bihar, AreaUnit.KathaBihar),
            Pair(R.string.state_jharkhand, AreaUnit.Dhur),
            Pair(R.string.state_west_bengal, AreaUnit.KathaWestBengal),
            Pair(R.string.state_west_bengal, AreaUnit.Decimal)
        )
    }
}
