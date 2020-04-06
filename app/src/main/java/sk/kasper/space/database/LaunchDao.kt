package sk.kasper.space.database

import androidx.room.*
import sk.kasper.space.database.entity.FalconCoreEntity
import sk.kasper.space.database.entity.LaunchDetailEntity
import sk.kasper.space.database.entity.LaunchEntity

@Dao
abstract class LaunchDao {

    @Transaction
    @Query("""
        SELECT
            launch.id,
            launch.launchName,
            launch.launchTs,
            launch.rocketId,
            launch.accurateDate,
            launch.accurateTime,
            launch.description,
            launch.mainPhotoUrl,
            launch.hashTag,
            launch.payloadMass,
            launch.videoUrl,
            rocket.rocketName,
            manufacturer.manufacturerName
        FROM
            launch
        LEFT JOIN
            rocket ON launch.rocketId = rocket.id
        LEFT JOIN
            manufacturer ON rocket.manufacturerId = manufacturer.id
        ORDER BY
            launch.launchTs
        """)
    abstract suspend fun getLaunches(): List<LaunchDetailEntity>

    @Transaction
    @Query("""
        SELECT
            launch.id,
            launch.launchName,
            launch.launchTs,
            launch.rocketId,
            launch.accurateDate,
            launch.accurateTime,
            launch.videoUrl,
            launch.description,
            launch.mainPhotoUrl,
            launch.hashTag,
            launch.payloadMass,
            rocket.rocketName,
            manufacturer.manufacturerName
        FROM
            launch
        LEFT JOIN
            rocket ON launch.rocketId = rocket.id
        LEFT JOIN
            manufacturer ON rocket.manufacturerId = manufacturer.id
        WHERE
            :launchId = launch.id
            """)
    abstract fun getLaunch(launchId: Long): LaunchDetailEntity

    @Query(
       """
        SELECT
            launch.falconCore_reused AS reused,
            launch.falconCore_block AS block,
            launch.falconCore_flights AS flights,
            launch.falconCore_landingType AS landingType,
            launch.falconCore_landingVehicle AS landingVehicle
        FROM
            launch
        WHERE
            :launchId = launch.id
        """)
    abstract suspend fun getFalconCore(launchId: Long): FalconCoreEntity

    @Query("""
        SELECT
            launch.orbit
        FROM
            launch
        WHERE
            :launchId = launch.id
    """)
    abstract fun getOrbit(launchId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(vararg launches: LaunchEntity): List<Long>

    @Query("DELETE FROM launch")
    abstract fun clear()

}

