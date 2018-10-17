package com.example.android.architecture.blueprints.todoapp

import android.app.Application
import android.support.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

/**
 * We create a custom [Application] class that extends  [DaggerApplication].
 * We then override applicationInjector() which tells Dagger how to make our @Singleton Component
 * We never have to call `component.inject(this)` as [DaggerApplication] will do that for us.
 */
class ToDoApplication : DaggerApplication() {
    /**
     * Our Espresso tests need to be able to get an instance of the [TasksRepository]
     * so that we can delete all tasks before running each test
     */
    @Inject
    @get:VisibleForTesting
    var tasksRepository: TasksRepository? = null
        internal set

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}
