package com.github.jameshnsears.quoteunquote.database.history;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ReportedDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void markAsReported(ReportedEntity reportedEntity);

    @Query("SELECT COUNT(*) FROM REPORTED WHERE DIGEST = :digest")
    Integer isReported(String digest);

    @Nullable
    @Query("SELECT COUNT(*) FROM REPORTED")
    Integer countReported();

    @Query("DELETE FROM REPORTED")
    void erase();
}
