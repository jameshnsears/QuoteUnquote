package com.github.jameshnsears.quoteunquote.database.quotation;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface QuotationDAO {
    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS")
    List<QuotationEntity> getAllQuotations();

    @Insert
    void insertQuotation(QuotationEntity quotationEntity);

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE DIGEST = :digest ORDER BY AUTHOR ASC")
    QuotationEntity getQuotation(String digest);

    @Query("SELECT COUNT(QUOTATION) AS QUOTATION_COUNT FROM QUOTATIONS")
    Single<Integer> countAll();

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR LIKE '%' || :exclusion || '%' COLLATE NOCASE")
    List<String> getExclusionDigests(String exclusion);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author ORDER BY ROWID ASC")
    List<String> getDigestsForAuthor(String author);

    @Query("SELECT DISTINCT COUNT(AUTHOR) AS C FROM QUOTATIONS GROUP BY AUTHOR ORDER BY C ASC")
    Single<List<Integer>> getAuthorsQuotationCount();

    @Query("SELECT AUTHOR, COUNT(*) AS QUOTATION_COUNT FROM QUOTATIONS GROUP BY AUTHOR HAVING QUOTATION_COUNT >= :quotationCount ORDER BY AUTHOR ASC")
    Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts(int quotationCount);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getNextAuthorDigest(String author);

    @Query("SELECT DIGEST FROM QUOTATIONS ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getNextAllDigests();

    @Query("DELETE FROM QUOTATIONS")
    void eraseQuotations();

    @Query("DELETE FROM QUOTATIONS WHERE DIGEST = :digest")
    void eraseQuotations(String digest);
}
