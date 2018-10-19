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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped
import com.google.common.base.Preconditions

import javax.inject.Inject

import dagger.android.support.DaggerFragment

/**
 * Main UI for the task detail screen.
 */
@ActivityScoped
class TaskDetailFragment @Inject
constructor() : DaggerFragment(), TaskDetailContract.View {
    @Inject
    lateinit var taskId: String
    @Inject
    lateinit var mPresenter: TaskDetailContract.Presenter
    private var mDetailTitle: TextView? = null
    private var mDetailDescription: TextView? = null
    private var mDetailCompleteStatus: CheckBox? = null

    override val isActive: Boolean
        get() = isAdded


    override fun onResume() {
        super.onResume()
        mPresenter!!.takeView(this)
    }

    override fun onDestroy() {
        mPresenter!!.dropView()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.taskdetail_frag, container, false)
        setHasOptionsMenu(true)
        mDetailTitle = root.findViewById(R.id.task_detail_title)
        mDetailDescription = root.findViewById(R.id.task_detail_description)
        mDetailCompleteStatus = root.findViewById(R.id.task_detail_complete)

        // Set up floating action button
        val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab_edit_task)

        fab.setOnClickListener { mPresenter!!.editTask() }

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_delete -> {
                mPresenter.deleteTask()
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.taskdetail_fragment_menu, menu)
    }

    override fun setLoadingIndicator(active: Boolean) {
        if (active) {
            mDetailTitle!!.text = ""
            mDetailDescription!!.text = getString(R.string.loading)
        }
    }

    override fun hideDescription() {
        mDetailDescription!!.visibility = View.GONE
    }

    override fun hideTitle() {
        mDetailTitle!!.visibility = View.GONE
    }

    override fun showDescription(description: String) {
        mDetailDescription!!.visibility = View.VISIBLE
        mDetailDescription!!.text = description
    }

    override fun showCompletionStatus(complete: Boolean) {
        Preconditions.checkNotNull<CheckBox>(mDetailCompleteStatus)

        mDetailCompleteStatus!!.isChecked = complete
        mDetailCompleteStatus!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mPresenter!!.completeTask()
            } else {
                mPresenter!!.activateTask()
            }
        }
    }

    override fun showEditTask(taskId: String) {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
        startActivityForResult(intent, REQUEST_EDIT_TASK)
    }

    override fun showTaskDeleted() {
        activity!!.finish()
    }

    override fun showTaskMarkedComplete() {
        Snackbar.make(view!!, getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG)
                .show()
    }

    override fun showTaskMarkedActive() {
        Snackbar.make(view!!, getString(R.string.task_marked_active), Snackbar.LENGTH_LONG)
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                activity!!.finish()
            }
        }
    }

    override fun showTitle(title: String) {
        mDetailTitle!!.visibility = View.VISIBLE
        mDetailTitle!!.text = title
    }

    override fun showMissingTask() {
        mDetailTitle!!.text = ""
        mDetailDescription!!.text = getString(R.string.no_data)
    }

    companion object {

        private val ARGUMENT_TASK_ID = "TASK_ID"

        private val REQUEST_EDIT_TASK = 1
    }

}
