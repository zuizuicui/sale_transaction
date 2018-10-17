/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import com.example.android.architecture.blueprints.todoapp.data.Task

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat

@RunWith(AndroidJUnit4::class)
class TasksDaoTest {

    private var mDatabase: ToDoDatabase? = null

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                ToDoDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        mDatabase!!.close()
    }

    @Test
    fun insertTaskAndGetById() {
        // When inserting a task
        mDatabase!!.taskDao().insertTask(TASK)

        // When getting the task by id from the database
        val loaded = mDatabase!!.taskDao().getTaskById(TASK.id)

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title", "description", true)
    }

    @Test
    fun insertTaskReplacesOnConflict() {
        //Given that a task is inserted
        mDatabase!!.taskDao().insertTask(TASK)

        // When a task with the same id is inserted
        val newTask = Task("title2", "description2", "id", true)
        mDatabase!!.taskDao().insertTask(newTask)
        // When getting the task by id from the database
        val loaded = mDatabase!!.taskDao().getTaskById(TASK.id)

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true)
    }

    @Test
    fun insertTaskAndGetTasks() {
        // When inserting a task
        mDatabase!!.taskDao().insertTask(TASK)

        // When getting the tasks from the database
        val tasks = mDatabase!!.taskDao().tasks

        // There is only 1 task in the database
        assertThat(tasks.size, `is`(1))
        // The loaded data contains the expected values
        assertTask(tasks[0], "id", "title", "description", true)
    }

    @Test
    fun updateTaskAndGetById() {
        // When inserting a task
        mDatabase!!.taskDao().insertTask(TASK)

        // When the task is updated
        val updatedTask = Task("title2", "description2", "id", true)
        mDatabase!!.taskDao().updateTask(updatedTask)

        // When getting the task by id from the database
        val loaded = mDatabase!!.taskDao().getTaskById("id")

        // The loaded data contains the expected values
        assertTask(loaded, "id", "title2", "description2", true)
    }

    @Test
    fun updateCompletedAndGetById() {
        // When inserting a task
        mDatabase!!.taskDao().insertTask(TASK)

        // When the task is updated
        mDatabase!!.taskDao().updateCompleted(TASK.id, false)

        // When getting the task by id from the database
        val loaded = mDatabase!!.taskDao().getTaskById("id")

        // The loaded data contains the expected values
        assertTask(loaded, TASK.id, TASK.title, TASK.description, false)
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() {
        //Given a task inserted
        mDatabase!!.taskDao().insertTask(TASK)

        //When deleting a task by id
        mDatabase!!.taskDao().deleteTaskById(TASK.id)

        //When getting the tasks
        val tasks = mDatabase!!.taskDao().tasks
        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    @Test
    fun deleteTasksAndGettingTasks() {
        //Given a task inserted
        mDatabase!!.taskDao().insertTask(TASK)

        //When deleting all tasks
        mDatabase!!.taskDao().deleteTasks()

        //When getting the tasks
        val tasks = mDatabase!!.taskDao().tasks
        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    @Test
    fun deleteCompletedTasksAndGettingTasks() {
        //Given a completed task inserted
        mDatabase!!.taskDao().insertTask(TASK)

        //When deleting completed tasks
        mDatabase!!.taskDao().deleteCompletedTasks()

        //When getting the tasks
        val tasks = mDatabase!!.taskDao().tasks
        // The list is empty
        assertThat(tasks.size, `is`(0))
    }

    private fun assertTask(task: Task, id: String, title: String?,
                           description: String?, completed: Boolean) {
        assertThat(task, notNullValue())
        assertThat(task.id, `is`(id))
        assertThat<String>(task.title, `is`<String>(title))
        assertThat<String>(task.description, `is`<String>(description))
        assertThat(task.isCompleted, `is`(completed))
    }

    companion object {

        private val TASK = Task("title", "description", "id", true)
    }
}
