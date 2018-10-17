package com.example.android.architecture.blueprints.todoapp.data.source

import android.app.Application
import android.arch.persistence.room.Room

import com.example.android.architecture.blueprints.todoapp.data.FakeTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors
import com.example.android.architecture.blueprints.todoapp.util.DiskIOThreadExecutor

import java.util.concurrent.Executors

import javax.inject.Singleton

import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * This is used by Dagger to inject the required arguments into the [TasksRepository].
 */
@Module
abstract class TasksRepositoryModule {

    @Singleton
    @Binds
    @Local
    internal abstract fun provideTasksLocalDataSource(dataSource: TasksLocalDataSource): TasksDataSource

    @Singleton
    @Binds
    @Remote
    internal abstract fun provideTasksRemoteDataSource(dataSource: FakeTasksRemoteDataSource): TasksDataSource

    companion object {

        private val THREAD_COUNT = 3

        @Singleton
        @Provides
        internal fun provideDb(context: Application): ToDoDatabase {
            return Room.databaseBuilder<ToDoDatabase>(context.applicationContext, ToDoDatabase::class.java!!, "Tasks.db")
                    .build()
        }

        @Singleton
        @Provides
        internal fun provideTasksDao(db: ToDoDatabase): TasksDao {
            return db.taskDao()
        }

        @Singleton
        @Provides
        internal fun provideAppExecutors(): AppExecutors {
            return AppExecutors(DiskIOThreadExecutor(),
                    Executors.newFixedThreadPool(THREAD_COUNT),
                    AppExecutors.MainThreadExecutor())
        }
    }
}
