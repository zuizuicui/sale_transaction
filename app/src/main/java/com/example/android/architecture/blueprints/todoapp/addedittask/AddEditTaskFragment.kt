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

import android.app.Activity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped

import javax.inject.Inject

import dagger.android.support.DaggerFragment

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
@ActivityScoped
class AddEditTaskFragment @Inject
constructor()// Required empty public constructor
    : DaggerFragment(), AddEditTaskContract.View {

    @Inject
    lateinit var mPresenter: AddEditTaskContract.Presenter

    private var mTitle: TextView? = null

    private var mDescription: TextView? = null

    override val isActive: Boolean
        get() = isAdded

    override fun onResume() {
        super.onResume()
        //Bind view to the presenter which will signal for the presenter to load the task.
        mPresenter.takeView(this)
    }

    override fun onPause() {
        mPresenter.dropView()
        super.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab_edit_task_done)
        fab.setImageResource(R.drawable.ic_done)
        fab.setOnClickListener { mPresenter.saveTask(mTitle!!.text.toString(), mDescription!!.text.toString()) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.addtask_frag, container, false)
        mTitle = root.findViewById(R.id.add_task_title)
        mDescription = root.findViewById(R.id.add_task_description)

        setHasOptionsMenu(true)
        retainInstance = true
        return root
    }

    override fun showEmptyTaskError() {
        Snackbar.make(mTitle!!, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show()
    }

    override fun showTasksList() {
        activity!!.setResult(Activity.RESULT_OK)
        activity!!.finish()
    }

    override fun setTitle(title: String) {
        mTitle!!.text = title
    }

    override fun setDescription(description: String) {
        mDescription!!.text = description
    }

    companion object {

        val ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID"
    }
}
