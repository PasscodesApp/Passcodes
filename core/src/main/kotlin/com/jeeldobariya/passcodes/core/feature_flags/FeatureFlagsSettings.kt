package com.jeeldobariya.passcodes.core.feature_flags

// This file contains a solution as commented code... ignore it.. if you don't entertain it.


import android.content.Context
// import androidx.datastore.core.DataMigration
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable

val Context.featureFlagsDatastore by dataStore(
    fileName = "feature-flags-settings.json",
    serializer = FeatureFlagsSettingsSerializer,
    // produceMigrations = { listOf(DataStoreMigration1_2) }
)

@Serializable
data class FeatureFlagsSettings(
    val version: Int = 1,
    val isPreviewFeaturesEnabled: Boolean = false,
    val isPreviewLayoutEnabled: Boolean = false
)

/*
Let say we have added a new feature flag, for custom backends..
like this in Feature Flag Settings.

```kotlin
@Serializable
data class FeatureFlagsSettings(
    val version: Int = 2,
    val isPreviewFeaturesEnabled: Boolean = false,
    val isPreviewLayoutEnabled: Boolean = false,
    val isCustomBackendFeatureEnabled: Boolean = false,
)
```

Then we can create a migration like this, and data will be migrated

```kotlin
data object DataStoreMigration1_2: DataMigration<FeatureFlagsSettings> {
    override suspend fun cleanUp() {
        // I don't think this is need in our case (according to rules written below..)
        TODO("Not yet implemented")
    }

    override suspend fun migrate(currentData: FeatureFlagsSettings): FeatureFlagsSettings {
        return FeatureFlagsSettings(
            // this is previous version flags this will be need to restore users current data.
            isPreviewFeaturesEnabled = currentData.isPreviewFeaturesEnabled,
            isPreviewLayoutEnabled = currentData.isPreviewLayoutEnabled,

            // this is new flag has already have default value, but we will explicitly declare it for clarity reasons....
            isCustomBackendFeatureEnabled = false

            // [!WARNING]
            // this should not restore version field from current data....
            // because if we do `version = currentData.version` then it will break the system in next run of migrations.
        )
    }

    override suspend fun shouldMigrate(currentData: FeatureFlagsSettings): Boolean {
        return currentData.version < 2
    }
}
```

## Conclusion:

I don't know weather this will work or not... let say it will..
we will eventually find out...

Here I am just proposing the solution that work perfectly in my mind..
I have not tested this solution nor do, I have seen this anywhere so I don't know weather this work or not..
But here are some links that I have refer to, while coming up with the solution...

- https://medium.com/androiddevelopers/datastore-and-data-migration-fdca806eb1aa
- https://stackoverflow.com/questions/69457920/how-to-perform-version-migrations-in-android-jetpack-datastore
- https://github.com/yogeshpaliyal/KeyPass/blob/master/common/src/main/java/com/yogeshpaliyal/common/utils/SharedPreferenceUtils.kt
- https://github.com/PasscodesApp/Passcodes/pull/52

they all use preferenceDataStore. which i don;t entertain using... because i guess it too complex..

### Rules (Assumptions):

- To this solution to work we can only add the new field...
- we can't change existing field nor do we can change it's datatype.
- we need to provide the default value to each and every field...
- we can only extend it can;t modify it....

**Main Assumption**: the under-lying datastore androidx library, will pass the currentData in `migrate()` function..
with old field restore and new field will have default value.

I think for this use case, we won;t need to modify field, so this solution work here..
*/
