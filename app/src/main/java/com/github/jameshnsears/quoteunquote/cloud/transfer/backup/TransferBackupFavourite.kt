package com.github.jameshnsears.quoteunquote.cloud.transfer.backup

import com.github.jameshnsears.quoteunquote.cloud.transfer.Favourite
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository

class TransferBackupFavourite {
    fun favourite(databaseRepository: DatabaseRepository): List<Favourite> {
        val favouriteList = mutableListOf<Favourite>()

        for (favourite in databaseRepository.favourites) {
            favouriteList.add(
                Favourite(
                    favourite.digest, favourite.navigation
                )
            )
        }

        return favouriteList
    }
}
