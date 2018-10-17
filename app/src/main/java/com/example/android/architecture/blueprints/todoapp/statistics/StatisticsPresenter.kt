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

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource

import javax.inject.Inject

/**
 * Listens to user actions from the UI ([StatisticsFragment]), retrieves the data and updates
 * the UI as required.
 *
 *
 * By marking the constructor with `@Inject`, Dagger injects the dependencies required to
 * create an instance of the StatisticsPresenter (if it fails, it emits a compiler error). It uses
 * [StatisticsModule] to do so.
 *
 *
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
internal class StatisticsPresenter
/**
 * Dagger strictly enforces that arguments not marked with `@Nullable` are not injected
 * with `@Nullable` values.
 */
@Inject
constructor(private val mTasksRepository: TasksRepository) : StatisticsContract.Presenter {

    private var mStatisticsView: StatisticsContract.View? = null


    private fun loadStatistics() {
        if (mStatisticsView != null) {
            mStatisticsView!!.setProgressIndicator(true)
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice

        mTasksRepository.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                var activeTasks = 0
                var completedTasks = 0

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.idlingResource.isIdleNow()) {
                    EspressoIdlingResource.decrement() // Set app as idle.
                }

                // We calculate number of active and completed tasks
                for (task in tasks) {
                    if (task.isCompleted) {
                        completedTasks += 1
                    } else {
                        activeTasks += 1
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (mStatisticsView == null || !mStatisticsView!!.isActive) {
                    return
                }
                mStatisticsView!!.setProgressIndicator(false)

                mStatisticsView!!.showStatistics(activeTasks, completedTasks)
            }

            override fun onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mStatisticsView!!.isActive) {
                    return
                }
                mStatisticsView!!.showLoadingStatisticsError()
            }
        })
    }

    override fun takeView(view: StatisticsContract.View) {
        mStatisticsView = view
        loadStatistics()
    }

    override fun dropView() {
        mStatisticsView = null
    }
}
