package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.db.DatabaseRepository

class TransferBackupFavourite {
    fun favourite(databaseRepository: DatabaseRepository): List<Favourite> {
        val internalDatabaseFavourites = getFavouritesFromDatabase(databaseRepository, true)
        val externalDatabaseFavourites = getFavouritesFromDatabase(databaseRepository, false)

        return internalDatabaseFavourites + externalDatabaseFavourites
    }

    private fun getFavouritesFromDatabase(
        databaseRepository: DatabaseRepository,
        useInternalDatabase: Boolean,
    ): List<Favourite> {
        val favouriteList = mutableListOf<Favourite>()

        for (favourite in databaseRepository.getFavourites(useInternalDatabase)) {
            favouriteList.add(
                Favourite(
                    favourite.digest,
                    favourite.navigation,
                    if (useInternalDatabase) {
                        "internal"
                    } else {
                        "external"
                    },
                ),
            )
        }

        return favouriteList
    }
}
