package com.example.data.network

data class LuncMarketData(
    val price: Double = 0.0000908,
    val change24h: Double = 11.64,
    val marketCap: Double = 503240000.0,
    val volume24h: Double = 82510000.0,
    val isFetching: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    fun formatPrice(): String {
        return String.format("$%.7f", price)
    }

    fun formatChange(): String {
        val sign = if (change24h >= 0) "+" else ""
        return String.format("%s%.2f%%", sign, change24h)
    }

    fun formatMarketCap(): String {
        return formatCompact(marketCap)
    }

    fun formatVolume(): String {
        return formatCompact(volume24h)
    }

    private fun formatCompact(valDouble: Double): String {
        return when {
            valDouble >= 1_000_000_000 -> String.format("$%.2fB", valDouble / 1_000_000_000)
            valDouble >= 1_000_000 -> String.format("$%.2fM", valDouble / 1_000_000)
            else -> String.format("$%.2f", valDouble)
        }
    }
}
