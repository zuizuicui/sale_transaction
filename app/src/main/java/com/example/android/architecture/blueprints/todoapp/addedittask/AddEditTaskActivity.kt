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

package com.example.android.architecture.blueprints.todoapp.addedittask

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

import javax.inject.Inject

import dagger.android.support.DaggerAppCompatActivity

/**
 * Displays an add or edit task screen.
 */
public class AddEditTaskActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var mAddEditTasksPresenter: AddEditTaskContract.Presenter

    @Inject
    lateinit var mFragment: AddEditTaskFragment

    @Inject
    lateinit var mTaskId: String

    private var mActionBar: ActionBar? = null

    // In a rotation it's important to know if we want to let the framework restore view state or
    // need to load data from the repository. This is saved into the state bundle.
    internal var isDataMissing = true
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addtask_act)

        // Set up the toolbar.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mActionBar = supportActionBar
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar!!.setDisplayShowHomeEnabled(true)
        setToolbarTitle(mTaskId)

        var addEditTaskFragment: AddEditTaskFragment? = supportFragmentManager.findFragmentById(R.id.contentFrame) as AddEditTaskFragment

        if (addEditTaskFragment == null) {
            addEditTaskFragment = mFragment

            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                    addEditTaskFragment!!, R.id.contentFrame)
        }
        restoreState(savedInstanceState)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        // Prevent the presenter from loading data from the repository if this is a config change.
        if (savedInstanceState != null) {
            // Data might not have loaded when the config change happen, so we saved the state.
            isDataMissing = savedInstanceState.getBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY)
        }
    }

    private fun setToolbarTitle(taskId: String?) {
        if (taskId == null) {
            mActionBar!!.setTitle(R.string.add_task)
        } else {
            mActionBar!!.setTitle(R.string.edit_task)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the state so that next time we know if we need to refresh data.
        outState.putBoolean(SHOULD_LOAD_DATA_FROM_REPO_KEY, mAddEditTasksPresenter!!.isDataMissing)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {

        val REQUEST_ADD_TASK = 1

        val SHOULD_LOAD_DATA_FROM_REPO_KEY = "SHOULD_LOAD_DATA_FROM_REPO_KEY"
    }
}
