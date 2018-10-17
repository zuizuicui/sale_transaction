/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.IdlingRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.TestUtils
import com.example.android.architecture.blueprints.todoapp.ToDoApplication
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.di.AppComponent
import com.example.android.architecture.blueprints.todoapp.di.DaggerAppComponent
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isChecked
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.core.IsNot.not

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TaskDetailScreenTest {

    /**
     * [ActivityTestRule] is a JUnit [@Rule][Rule] to launch your activity under test.
     *
     *
     *
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     *
     *
     * Sometimes an [Activity] requires a custom start [Intent] to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target Activity.
     */
    @Rule
    var mTaskDetailActivityTestRule = ActivityTestRule(TaskDetailActivity::class.java, true /* Initial touch mode  */,
            false /* Lazily launch activity */)
    private var mAppComponent: AppComponent? = null


    /**
     * Prepare your test fixture for this test. In this case we register an IdlingResources with
     * Espresso. IdlingResource resource is a great way to tell Espresso when your app is in an
     * idle state. This helps Espresso to synchronize your test actions, which makes tests
     * significantly more reliable.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
    }

    @Before
    fun resetRepository() {
        val application = InstrumentationRegistry.getTargetContext().applicationContext as ToDoApplication
        mAppComponent = DaggerAppComponent.builder().application(application).build()
        mAppComponent!!.inject(application)
        mAppComponent!!.tasksRepository.deleteAllTasks()
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource)
    }

    private fun loadActiveTask() {
        startActivityWithWithStubbedTask(ACTIVE_TASK)
    }

    private fun loadCompletedTask() {
        startActivityWithWithStubbedTask(COMPLETED_TASK)
    }

    /**
     * Setup your test fixture with a fake task id. The [TaskDetailActivity] is started with
     * a particular task id, which is then loaded from the service API.
     *
     *
     *
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private fun startActivityWithWithStubbedTask(task: Task) {
        // Add a task stub to the fake service api layer.
        mAppComponent!!.tasksRepository.saveTask(task)

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        val startIntent = Intent()
        startIntent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
        mTaskDetailActivityTestRule.launchActivity(startIntent)
    }

    @Test
    @Throws(Exception::class)
    fun activeTaskDetails_DisplayedInUi() {
        loadActiveTask()

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))
        onView(withId(R.id.task_detail_complete)).check(matches(not<View>(isChecked())))
    }

    @Test
    @Throws(Exception::class)
    fun completedTaskDetails_DisplayedInUi() {
        loadCompletedTask()

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()))
    }

    @Test
    fun orientationChange_menuAndTaskPersist() {
        loadActiveTask()

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()))

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.activity)

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)))
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)))

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()))
    }

    companion object {

        private val TASK_TITLE = "ATSL"

        private val TASK_DESCRIPTION = "Rocks"

        /**
         * [Task] stub that is added to the fake service API layer.
         */
        private val ACTIVE_TASK = Task(TASK_TITLE, TASK_DESCRIPTION, false)

        /**
         * [Task] stub that is added to the fake service API layer.
         */
        private val COMPLETED_TASK = Task(TASK_TITLE, TASK_DESCRIPTION, true)
    }

}
