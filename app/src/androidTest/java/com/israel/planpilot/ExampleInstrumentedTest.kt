package com.israel.planpilot

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateActivityFragmentTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testSaveActivity() {
        onView(withId(R.id.nameActivity)).perform(typeText("Atividade de Teste"), closeSoftKeyboard())
        onView(withId(R.id.timePicker)).perform(typeText("10:00"), closeSoftKeyboard())
        onView(withId(R.id.alarmSwitch)).perform(click())

        onView(withId(R.id.saveButton)).perform(click())

        onView(allOf(withId(com.google.android.material.R.id.snackbar_text), withText("Atividade criada com sucesso!")))
            .check(matches(isDisplayed()))

        onView(withId(R.id.nav_home)).check(matches(isDisplayed()))
    }
}
