package com.example.todolist

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todolist.data.worker.DataRefreshWorker
import com.example.todolist.data.worker.WorkerFactory
import com.example.todolist.di.AppComponent
import com.example.todolist.di.DaggerAppComponent
import com.example.todolist.presentation.util.TIME_REFRESH_DATA
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Custom Application class
 * as long as application lives.
 */
class ToDoAppApplication: Application() {

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var workerFactory: WorkerFactory
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(
            this, applicationContext
        )
        appComponent.inject(this)

        val configuration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(this, configuration)

        startWorker()
    }

    private fun startWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(DataRefreshWorker::class.java, TIME_REFRESH_DATA, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueue(periodicWorkRequest)
    }
}
