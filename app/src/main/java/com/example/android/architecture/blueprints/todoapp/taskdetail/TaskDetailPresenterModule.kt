package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity.Companion.EXTRA_TASK_ID

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [TaskDetailPresenter].
 */
@Module
abstract class TaskDetailPresenterModule {


    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun taskDetailFragment(): TaskDetailFragment

    @ActivityScoped
    @Binds
    internal abstract fun statitsticsPresenter(presenter: TaskDetailPresenter): TaskDetailContract.Presenter

    @Module
    companion object {

        @JvmStatic
        @Provides
        @ActivityScoped
        internal fun provideTaskId(activity: TaskDetailActivity): String {
            return activity.intent.getStringExtra(EXTRA_TASK_ID)
        }
    }
}
