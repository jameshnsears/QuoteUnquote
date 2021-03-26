package com.github.jameshnsears.quoteunquote.database.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ReportedDAO {
    @Insert
    void markAsReported(ReportedEntity reportedEntity);

    @Query("SELECT COUNT(*) FROM REPORTED WHERE DIGEST = :digest")
    Integer countIsReported(String digest);

    @Query("SELECT COUNT(*) FROM REPORTED")
    Integer countReported();

    @Query("DELETE FROM REPORTED")
    void deleteReported();
}
