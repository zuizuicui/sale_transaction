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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity

import java.util.ArrayList

import javax.inject.Inject

import dagger.android.support.DaggerFragment

import com.google.common.base.Preconditions.checkNotNull

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
@ActivityScoped
class TasksFragment @Inject
constructor()// Requires empty public constructor
    : DaggerFragment(), TasksContract.View {

    @Inject
    lateinit var mPresenter: TasksContract.Presenter
    /**
     * Listener for clicks on tasks in the ListView.
     */
    internal var mItemListener: TaskItemListener = object : TaskItemListener {
        override fun onTaskClick(clickedTask: Task) {
            mPresenter!!.openTaskDetails(clickedTask)
        }

        override fun onCompleteTaskClick(completedTask: Task) {
            mPresenter!!.completeTask(completedTask)
        }

        override fun onActivateTaskClick(activatedTask: Task) {
            mPresenter!!.activateTask(activatedTask)
        }
    }
    private var mListAdapter: TasksAdapter? = null
    private var mNoTasksView: View? = null
    private var mNoTaskIcon: ImageView? = null
    private var mNoTaskMainView: TextView? = null
    private var mNoTaskAddView: TextView? = null
    private var mTasksView: LinearLayout? = null
    private var mFilteringLabelView: TextView? = null

    override val isActive: Boolean
        get() = isAdded


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = TasksAdapter(ArrayList(0), mItemListener)
    }

    override fun onResume() {
        super.onResume()
        mPresenter!!.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter!!.dropView()  //prevent leaking activity in
        // case presenter is orchestrating a long running task
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPresenter!!.result(requestCode, resultCode)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.tasks_frag, container, false)

        // Set up tasks view
        val listView = root.findViewById<ListView>(R.id.tasks_list)
        listView.adapter = mListAdapter
        mFilteringLabelView = root.findViewById(R.id.filteringLabel)
        mTasksView = root.findViewById(R.id.tasksLL)

        // Set up  no tasks view
        mNoTasksView = root.findViewById(R.id.noTasks)
        mNoTaskIcon = root.findViewById(R.id.noTasksIcon)
        mNoTaskMainView = root.findViewById(R.id.noTasksMain)
        mNoTaskAddView = root.findViewById(R.id.noTasksAdd)
        mNoTaskAddView!!.setOnClickListener { showAddTask() }

        // Set up floating action button
        val fab = activity!!.findViewById<FloatingActionButton>(R.id.fab_add_task)

        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener { mPresenter!!.addNewTask() }

        // Set up progress indicator
        val swipeRefreshLayout = root.findViewById<ScrollChildSwipeRefreshLayout>(R.id.refresh_layout)
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(activity!!, R.color.colorPrimary),
                ContextCompat.getColor(activity!!, R.color.colorAccent),
                ContextCompat.getColor(activity!!, R.color.colorPrimaryDark)
        )
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView)

        swipeRefreshLayout.setOnRefreshListener { mPresenter!!.loadTasks(false) }

        setHasOptionsMenu(true)

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_clear -> mPresenter!!.clearCompletedTasks()
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> mPresenter!!.loadTasks(true)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun showFilteringPopUpMenu() {
        val popup = PopupMenu(context!!, activity!!.findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> mPresenter!!.filtering = TasksFilterType.ACTIVE_TASKS
                R.id.completed -> mPresenter!!.filtering = TasksFilterType.COMPLETED_TASKS
                else -> mPresenter!!.filtering = TasksFilterType.ALL_TASKS
            }
            mPresenter!!.loadTasks(false)
            true
        }

        popup.show()
    }

    override fun setLoadingIndicator(active: Boolean) {

        if (view == null) {
            return
        }
        val srl = view!!.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post { srl.isRefreshing = active }
    }

    override fun showTasks(tasks: List<Task>) {
        mListAdapter!!.replaceData(tasks)

        mTasksView!!.visibility = View.VISIBLE
        mNoTasksView!!.visibility = View.GONE
    }

    override fun showNoActiveTasks() {
        showNoTasksViews(
                resources.getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp,
                false
        )
    }

    override fun showNoTasks() {
        showNoTasksViews(
                resources.getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        )
    }

    override fun showNoCompletedTasks() {
        showNoTasksViews(
                resources.getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        )
    }

    override fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message))
    }

    private fun showNoTasksViews(mainText: String, iconRes: Int, showAddView: Boolean) {
        mTasksView!!.visibility = View.GONE
        mNoTasksView!!.visibility = View.VISIBLE

        mNoTaskMainView!!.text = mainText

        mNoTaskIcon!!.setImageDrawable(resources.getDrawable(iconRes))
        mNoTaskAddView!!.visibility = if (showAddView) View.VISIBLE else View.GONE
    }

    override fun showActiveFilterLabel() {
        mFilteringLabelView!!.text = resources.getString(R.string.label_active)
    }

    override fun showCompletedFilterLabel() {
        mFilteringLabelView!!.text = resources.getString(R.string.label_completed)
    }

    override fun showAllFilterLabel() {
        mFilteringLabelView!!.text = resources.getString(R.string.label_all)
    }

    override fun showAddTask() {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK)
    }

    override fun showTaskDetailsUi(taskId: String) {
        //Shown in it's own Activity, since it makes more sense that way
        // and it gives us the flexibility to show some Intent stubbing.
        val intent = Intent(context, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }

    override fun showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete))
    }

    override fun showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active))
    }

    override fun showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared))
    }

    override fun showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error))
    }

    private fun showMessage(message: String) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    interface TaskItemListener {

        fun onTaskClick(clickedTask: Task)

        fun onCompleteTaskClick(completedTask: Task)

        fun onActivateTaskClick(activatedTask: Task)
    }

    private class TasksAdapter(tasks: List<Task>, private val mItemListener: TaskItemListener) : BaseAdapter() {

        private var mTasks: List<Task>? = null

        init {
            setList(tasks)
        }

        fun replaceData(tasks: List<Task>) {
            setList(tasks)
            notifyDataSetChanged()
        }

        private fun setList(tasks: List<Task>) {
            mTasks = checkNotNull(tasks)
        }

        override fun getCount(): Int {
            return mTasks!!.size
        }

        override fun getItem(i: Int): Task {
            return mTasks!![i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
            var rowView: View? = view
            if (rowView == null) {
                val inflater = LayoutInflater.from(viewGroup.context)
                rowView = inflater.inflate(R.layout.task_item, viewGroup, false)
            }

            val task = getItem(i)

            val titleTV = rowView!!.findViewById<TextView>(R.id.title)
            titleTV.text = task.titleForList

            val completeCB = rowView.findViewById<CheckBox>(R.id.complete)

            // Active/completed task UI
            completeCB.isChecked = task.isCompleted
            if (task.isCompleted) {

                rowView.setBackgroundDrawable(viewGroup.context
                        .resources.getDrawable(R.drawable.list_completed_touch_feedback))
            } else {

                rowView.setBackgroundDrawable(viewGroup.context
                        .resources.getDrawable(R.drawable.touch_feedback))
            }

            completeCB.setOnClickListener {
                if (!task.isCompleted) {
                    mItemListener.onCompleteTaskClick(task)
                } else {
                    mItemListener.onActivateTaskClick(task)
                }
            }

            rowView.setOnClickListener { mItemListener.onTaskClick(task) }

            return rowView
        }
    }

}
