/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.util.ActivityUtils

import javax.inject.Inject

import dagger.android.support.DaggerAppCompatActivity

/**
 * Displays task details screen.
 */
class TaskDetailActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var injectedFragment: TaskDetailFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.taskdetail_act)

        // Set up the toolbar.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(true)
        ab.setDisplayShowHomeEnabled(true)

        var taskDetailFragment: TaskDetailFragment? = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as TaskDetailFragment

        if (taskDetailFragment == null) {
            taskDetailFragment = injectedFragment
            ActivityUtils.addFragmentToActivity(supportFragmentManager,
                    taskDetailFragment!!, R.id.contentFrame)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val EXTRA_TASK_ID = "TASK_ID"
    }
}
