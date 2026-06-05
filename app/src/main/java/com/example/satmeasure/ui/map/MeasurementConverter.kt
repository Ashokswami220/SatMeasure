package com.example.satmeasure.ui.map

enum class IndianState(val displayName: String) {
    RAJASTHAN("Rajasthan"),
    BIHAR("Bihar"),
    JHARKHAND("Jharkhand"),
    UTTAR_PRADESH("Uttar Pradesh"),
    GUJARAT("Gujarat"),
    ASSAM("Assam"),
    WEST_BENGAL("West Bengal"),
    MADHYA_PRADESH("Madhya Pradesh"),
    PUNJAB_HARYANA("Punjab & Haryana"),
    HIMACHAL_PRADESH("Himachal Pradesh"),
    UTTARAKHAND("Uttarakhand")
}

sealed class AreaUnit {
    abstract val displayName: String

    // 1. Global Standard Units
    data object SquareMeter : AreaUnit() { override val displayName = "Square Meter" }
    data object SquareYard : AreaUnit() { override val displayName = "Square Yard (Gaj)" }
    data object Acre : AreaUnit() { override val displayName = "Acre" }
    data object Hectare : AreaUnit() { override val displayName = "Hectare" }

    // 2. State-Specific "Bigha"
    data class Bigha(val state: IndianState) : AreaUnit() { 
        override val displayName = "Bigha"
    }

    // 3. Regional North Indian Units
    data object Kanal : AreaUnit() { override val displayName = "Kanal" }
    data object Marla : AreaUnit() { override val displayName = "Marla" }
    data class Biswa(val state: IndianState) : AreaUnit() { 
        override val displayName = "Biswa"
    }

    // 4. Regional East Indian Units
    data object KathaAssam : AreaUnit() { override val displayName = "Katha (Assam)" }
    data object KathaBihar : AreaUnit() { override val displayName = "Katha (Bihar)" }
    data object KathaWestBengal : AreaUnit() { override val displayName = "Katha (West Bengal)" }
    data object Dhur : AreaUnit() { override val displayName = "Dhur (Bihar/Jharkhand)" }
    data object Lecha : AreaUnit() { override val displayName = "Lecha (Assam)" }
    data object Decimal : AreaUnit() { override val displayName = "Decimal (West Bengal)" }
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
    fun getOtherLocalUnits(): List<Pair<String, AreaUnit>> {
        return listOf(
            Pair("North India", AreaUnit.Kanal),
            Pair("North India", AreaUnit.Marla),
            Pair("Assam", AreaUnit.KathaAssam),
            Pair("Assam", AreaUnit.Lecha),
            Pair("Bihar", AreaUnit.KathaBihar),
            Pair("Bihar/Jharkhand", AreaUnit.Dhur),
            Pair("West Bengal", AreaUnit.KathaWestBengal),
            Pair("West Bengal", AreaUnit.Decimal)
        )
    }
}
