package com.github.jameshnsears.quoteunquote.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface FavouriteDAO {
    @Insert
    void markAsFavourite(FavouriteEntity favouriteEntity);

    @Query("SELECT COUNT(*) FROM FAVOURITE")
    Single<Integer> countFavourites();

    @Query("SELECT COUNT(*) FROM FAVOURITE WHERE DIGEST = :digest")
    Integer isFavourite(String digest);

    @Query("SELECT DIGEST FROM FAVOURITE ORDER BY NAVIGATION DESC")
    List<String> getFavouriteDigests();

    @Query("SELECT DIGEST FROM FAVOURITE WHERE DIGEST NOT IN (:digests)")
    List<String> getNextFavouriteDigests(List<String> digests);

    @Query("DELETE FROM FAVOURITE WHERE DIGEST = :digest")
    void deleteFavourite(String digest);

    @Query("DELETE FROM FAVOURITE")
    void erase();
}
