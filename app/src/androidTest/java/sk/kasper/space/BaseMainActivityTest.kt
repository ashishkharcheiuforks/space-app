package sk.kasper.space

import android.content.Context
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import sk.kasper.space.api.entity.RemoteLaunch
import sk.kasper.space.api.entity.RemoteTag
import sk.kasper.space.di.MockRemoteApi
import sk.kasper.space.mainactivity.MainActivity
import sk.kasper.space.robot.droid.LaunchDroid
import timber.log.Timber
import java.util.concurrent.TimeUnit


open class BaseMainActivityTest {

    private lateinit var mockRemoteApi: MockRemoteApi
    private lateinit var mainActivityIdlingResource: MainActivityIdlingResource

    @Rule @JvmField
    var activityScenario: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val spaceApp = getApplicationContext<SpaceApp>()
        clearSharedPrefs(spaceApp)
        mockRemoteApi = spaceApp.remoteApi as MockRemoteApi

        activityScenario.scenario.onActivity { activity ->
            mainActivityIdlingResource = MainActivityIdlingResource(activity)
            Espresso.registerIdlingResources(mainActivityIdlingResource)
        }
    }

    @After
    fun tearDown() {
        Espresso.unregisterIdlingResources(mainActivityIdlingResource)
    }

    fun fromServerReturnLaunches(droidLaunches: List<LaunchDroid>) {
        droidLaunches.forEach {
            Timber.d("fromServerReturnLaunches $it")
        }

        val launches = droidLaunches.mapIndexed { index: Int, launchDroid: LaunchDroid ->
            RemoteLaunch(
                    index.toLong(),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(index.toLong() + 1),
                    launchDroid.name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    true,
                    true,
                    null,
                    null,
                    launchDroid.tags.map { RemoteTag(it) },
                    null)
        }

        mockRemoteApi.listLaunchesReturn(launches)
    }

    /**
     * Clears everything in the SharedPreferences
     */
    private fun clearSharedPrefs(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .clear()
                .commit()
    }

    class MainActivityIdlingResource(mainActivity: MainActivity) : IdlingResource {

        private var callback: IdlingResource.ResourceCallback? = null

        init {
            mainActivity.isIdleListener = object: MainActivity.IdleListener {
                override fun onIdleStatusChanged(isIdle: Boolean) {
                    this@MainActivityIdlingResource.isIdle = isIdle

                    if (isIdle) {
                        callback?.onTransitionToIdle()
                    }
                }
            }
        }

        private var isIdle = false

        override fun getName() = "Idle resource from MainActivity"

        override fun isIdleNow() = isIdle

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
            this.callback = callback
        }

    }

}
