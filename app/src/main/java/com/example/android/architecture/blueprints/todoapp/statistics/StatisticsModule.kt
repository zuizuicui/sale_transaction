package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [StatisticsPresenter].
 */
@Module
abstract class StatisticsModule {

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun statisticsFragment(): StatisticsFragment

    @ActivityScoped
    @Binds
    internal abstract fun statitsticsPresenter(presenter: StatisticsPresenter): StatisticsContract.Presenter
}
