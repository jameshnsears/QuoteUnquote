package com.github.jameshnsears.quoteunquote.database.quotation;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public interface QuotationDAO {
    @Insert
    void insertQuotation(QuotationEntity quotationEntity);

    @Query("SELECT AUTHOR, QUOTATION, DIGEST FROM QUOTATIONS WHERE DIGEST = :digest ORDER BY AUTHOR ASC")
    QuotationEntity getQuotation(String digest);

    @Query("SELECT COUNT(QUOTATION) AS QUOTATION_COUNT FROM QUOTATIONS")
    Single<Integer> countAll();

    @Query("SELECT AUTHOR, COUNT(*) AS QUOTATION_COUNT FROM QUOTATIONS GROUP BY AUTHOR ORDER BY AUTHOR ASC")
    Single<List<AuthorPOJO>> authorsWithAtLeastFiveQuotations();

    @Query("SELECT AUTHOR, COUNT(*) AS QUOTATION_COUNT FROM QUOTATIONS GROUP BY AUTHOR ORDER BY AUTHOR ASC")
    Single<List<AuthorPOJO>> authors();

    @Query("SELECT COUNT(QUOTATION) FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text)")
    Integer countQuotationsText(String text);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getAll(List<String> digests);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author ORDER BY ROWID ASC")
    List<String> getAuthors(String author);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author AND DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getAuthors(String author, List<String> digests);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getQuotationText(String text);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text) AND DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getQuotationText(String text, List<String> digests);
}
