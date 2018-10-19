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

package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.capture
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.google.common.collect.Lists

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Unit tests for the implementation of [StatisticsPresenter]
 */
class StatisticsPresenterTest {

    @Mock
    lateinit var mTasksRepository: TasksRepository

    @Mock
    lateinit var mStatisticsView: StatisticsContract.View

    /**
     * [ArgumentCaptor] is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private val mLoadTasksCallbackCaptor: ArgumentCaptor<TasksDataSource.LoadTasksCallback>? = null


    private var mStatisticsPresenter: StatisticsPresenter? = null

    @Before
    fun setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // Get a reference to the class under test
        mStatisticsPresenter = StatisticsPresenter(mTasksRepository)
        mStatisticsPresenter!!.takeView(mStatisticsView)

        // The presenter won't update the view unless it's active.
        `when`(mStatisticsView.isActive).thenReturn(true)

        // We start the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(Task("Title1", "Description1"),
                Task("Title2", "Description2", true), Task("Title3", "Description3", true))
    }

    @Test
    fun loadEmptyTasksFromRepository_CallViewToDisplay() {
        // Given an initialized StatisticsPresenter with no tasks
        TASKS!!.clear()

        //Then progress indicator is shown
        verify(mStatisticsView).setProgressIndicator(true)

        // Callback is captured and invoked with stubbed tasks
        verify<TasksRepository>(mTasksRepository).getTasks(capture(mLoadTasksCallbackCaptor!!))
        mLoadTasksCallbackCaptor.value.onTasksLoaded(TASKS!!)

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false)
        verify(mStatisticsView).showStatistics(0, 0)
    }

    @Test
    fun loadNonEmptyTasksFromRepository_CallViewToDisplay() {
        // Given an initialized StatisticsPresenter with 1 active and 2 completed tasks

        //Then progress indicator is shown
        verify(mStatisticsView).setProgressIndicator(true)

        // Callback is captured and invoked with stubbed tasks
        verify<TasksRepository>(mTasksRepository).getTasks(capture(mLoadTasksCallbackCaptor!!))
        mLoadTasksCallbackCaptor.value.onTasksLoaded(TASKS!!)

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false)
        verify(mStatisticsView).showStatistics(1, 2)
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // And tasks data isn't available
        verify<TasksRepository>(mTasksRepository).getTasks(capture(mLoadTasksCallbackCaptor!!))
        mLoadTasksCallbackCaptor.value.onDataNotAvailable()

        // Then an error message is shown
        verify(mStatisticsView).showLoadingStatisticsError()
    }

    companion object {

        private var TASKS: MutableList<Task>? = null
    }
}
