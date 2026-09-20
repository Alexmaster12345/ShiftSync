package com.example.shiftsync

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A single end-to-end pass through the app's main flows, driven against whatever state already
 * exists on the device (no SharedPreferences reset — relaunching MainActivity mid-suite trips up
 * androidx.core's SplashScreen, which doesn't replay its exit reliably across activity instances
 * in the same test process). Kept as one test rather than several so there's exactly one launch:
 * clock in/out, edit + delete the entry it creates, then confirm the system back button
 * navigates up through a sub-screen instead of exiting the app.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun clockInOut_editAndDeleteTheEntry_thenBackNavigatesUpFromASubScreen() {
        // Right after launch there's a brief window (system splash-screen exit, this app's own
        // ~1.8s splash) where no Compose content is attached yet — querying nodes then throws
        // instead of just "not found", so the wait predicate has to tolerate that.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("Clock In").fetchSemanticsNodes().isNotEmpty()
            } catch (e: IllegalStateException) {
                false
            }
        }

        // --- Clock in / out creates an entry ---
        composeRule.onNodeWithText("Clock In").performClick()
        composeRule.onNodeWithText("Clock Out").assertExists()
        composeRule.onNodeWithText("Clock Out").performClick()
        composeRule.onNodeWithText("Clock In").assertExists()
        val countAfterCreate = entryRowsShowing0m().fetchSemanticsNodes().size
        assert(countAfterCreate > 0) { "expected a 0m entry to appear after clocking in and out immediately" }

        // --- Edit Shift -> Delete removes it ---
        entryRowsShowing0m().onFirst().performClick()
        composeRule.onNodeWithText("Edit Shift").assertExists()
        composeRule.onNodeWithContentDescription("Delete Shift").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Edit Shift").assertDoesNotExist()
        val countAfterDelete = entryRowsShowing0m().fetchSemanticsNodes().size
        assert(countAfterDelete == countAfterCreate - 1) {
            "expected one fewer 0m entry after delete: was $countAfterCreate, now $countAfterDelete"
        }

        // --- System back from a sub-screen navigates up, not out of the app ---
        composeRule.onNodeWithContentDescription("Profile").performClick()
        composeRule.onNodeWithText("Sign Out").assertExists()
        composeRule.onNodeWithText("Notifications").performClick()
        composeRule.onNodeWithText("Arrival & Departure Alerts").assertExists()
        pressBack()
        composeRule.onNodeWithText("Sign Out").assertExists()
    }

    /** Clickable Recent Activity rows showing a 0-minute duration — merged semantics make plain
     * text lookups match each row's parent card too, so this narrows to just the clickable node. */
    private fun entryRowsShowing0m() = composeRule.onAllNodes(hasText("0m", substring = true) and hasClickAction())
}
