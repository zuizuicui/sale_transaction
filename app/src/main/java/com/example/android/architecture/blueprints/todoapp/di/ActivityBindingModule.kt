package com.example.android.architecture.blueprints.todoapp.di

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskModule
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsActivity
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModule
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailPresenterModule
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModule

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module ActivityBindingModule is on,
 * in our case that will be AppComponent. The beautiful part about this setup is that you never need to tell AppComponent that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that AppComponent exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the specified modules and be aware of a scope annotation @ActivityScoped
 * When Dagger.Android annotation processor runs it will create 4 subcomponents for us.
 */
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(TasksModule::class))
    internal abstract fun tasksActivity(): TasksActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(AddEditTaskModule::class))
    internal abstract fun addEditTaskActivity(): AddEditTaskActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(StatisticsModule::class))
    internal abstract fun statisticsActivity(): StatisticsActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(TaskDetailPresenterModule::class))
    internal abstract fun taskDetailActivity(): TaskDetailActivity
}
