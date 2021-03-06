package sk.kasper.space.sync

import android.content.Context
import androidx.work.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import sk.kasper.domain.model.SyncLaunches
import sk.kasper.space.BuildConfig
import sk.kasper.space.work.ChildWorkerFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncWorker @AssistedInject constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val syncLaunches: SyncLaunches)
    : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val UNIQUE_PERIODIC_WORK_NAME = "Sync launches work"

        fun startPeriodicWork(context: Context) {
            val constrains = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(BuildConfig.SYNC_INTERVAL_HOURS, TimeUnit.HOURS)
                    .setConstraints(constrains)
                    .build()

            WorkManager
                    .getInstance(context)
                    .enqueueUniquePeriodicWork(UNIQUE_PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, syncRequest)

            Timber.d("periodic launches sync work request started")
        }

    }

    override suspend fun doWork(): Result {
        Timber.d("doWork")
        return if (syncLaunches.doSync(force = true)) {
            Timber.d("doWork - success")
            Result.success()
        } else {
            Timber.d("doWork - failure")
            Result.failure()
        }
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory

}