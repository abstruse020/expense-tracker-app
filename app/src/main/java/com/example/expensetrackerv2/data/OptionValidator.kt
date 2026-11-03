package com.example.expensetrackerv2.data

/** Validation for user-entered category / sub-category / payment-mode names. */
object OptionValidator {
    // The CSV reader splits on commas and strips quotes, so these would corrupt rows.
    private val forbidden = charArrayOf(',', '"', '\n', '\r')

    /**
     * Returns an error message, or null if [raw] is a valid name.
     * [existing] are the names already in the same list; [ignore] is the name being
     * renamed (so keeping or re-casing the same name is allowed).
     */
    fun validate(raw: String, existing: List<String>, ignore: String? = null): String? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> "Name can't be empty"
            name.any { it in forbidden } -> "Name can't contain commas, quotes or line breaks"
            existing.any { it.equals(name, ignoreCase = true) && !it.equals(ignore, ignoreCase = true) } ->
                "\"$name\" already exists"
            else -> null
        }
    }
}
