package com.example.expensetrackerv2

import com.example.expensetrackerv2.data.OptionValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OptionValidatorTest {
    private val existing = listOf("Essential", "Casual")

    @Test fun acceptsNewName() = assertNull(OptionValidator.validate("Travel", existing))

    @Test fun trimsWhitespaceBeforeChecking() =
        assertNotNull(OptionValidator.validate("  casual ", existing))

    @Test fun rejectsEmptyAndBlank() {
        assertNotNull(OptionValidator.validate("", existing))
        assertNotNull(OptionValidator.validate("   ", existing))
    }

    @Test fun rejectsCsvBreakingCharacters() {
        assertNotNull(OptionValidator.validate("Food, Drinks", existing))
        assertNotNull(OptionValidator.validate("Say \"hi\"", existing))
        assertNotNull(OptionValidator.validate("a\nb", existing))
    }

    @Test fun rejectsCaseInsensitiveDuplicate() =
        assertNotNull(OptionValidator.validate("ESSENTIAL", existing))

    @Test fun renameMayKeepOrRecaseOwnName() {
        assertNull(OptionValidator.validate("Essential", existing, ignore = "Essential"))
        assertNull(OptionValidator.validate("ESSENTIAL", existing, ignore = "Essential"))
    }

    @Test fun renameStillRejectsOtherExistingName() =
        assertNotNull(OptionValidator.validate("Casual", existing, ignore = "Essential"))
}
