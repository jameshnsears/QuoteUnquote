package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

class TransferBackupFavourite {
    fun favourite(databaseRepository: DatabaseRepository): List<Favourite> {
        val originalUseInternalDatabaseState = DatabaseRepository.useInternalDatabase

        val internalDatabaseFavourites = getFavouritesFromDatabase(databaseRepository, true)
        val externalDatabaseFavourites = getFavouritesFromDatabase(databaseRepository, false)

        DatabaseRepository.useInternalDatabase = originalUseInternalDatabaseState

        return internalDatabaseFavourites + externalDatabaseFavourites
    }

    private fun getFavouritesFromDatabase(
        databaseRepository: DatabaseRepository,
        useInternalDatabase: Boolean
    ): List<Favourite> {
        DatabaseRepository.useInternalDatabase = useInternalDatabase

        val favouriteList = mutableListOf<Favourite>()

        for (favourite in databaseRepository.favouritesEntity) {
            favouriteList.add(
                Favourite(
                    favourite.digest,
                    favourite.navigation,
                    if (useInternalDatabase) {
                        "internal"
                    } else {
                        "external"
                    }
                )
            )
        }

        return favouriteList
    }
}
