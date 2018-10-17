package com.example.android.architecture.blueprints.todoapp.tasks

import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [TasksPresenter].
 */
@Module
abstract class TasksModule {
    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun tasksFragment(): TasksFragment

    @ActivityScoped
    @Binds
    internal abstract fun taskPresenter(presenter: TasksPresenter): TasksContract.Presenter
}
