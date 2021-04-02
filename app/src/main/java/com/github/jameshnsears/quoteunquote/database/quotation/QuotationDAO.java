package com.github.jameshnsears.quoteunquote.database.quotation;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface QuotationDAO {
    @Insert
    void insertQuotation(QuotationEntity quotationEntity);

    @Query("SELECT AUTHOR, QUOTATION, DIGEST FROM QUOTATIONS WHERE DIGEST = :digest ORDER BY AUTHOR ASC")
    QuotationEntity getQuotation(String digest);

    @Query("SELECT COUNT(QUOTATION) AS QUOTATION_COUNT FROM QUOTATIONS")
    Single<Integer> countAll();

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author ORDER BY ROWID ASC")
    List<String> getDigestsForAuthor(String author);

    @Query("SELECT AUTHOR, COUNT(*) AS QUOTATION_COUNT FROM QUOTATIONS GROUP BY AUTHOR HAVING QUOTATION_COUNT >= :quotationCount ORDER BY AUTHOR ASC")
    Single<List<AuthorPOJO>> getAuthorsAndQuotationCounts(int quotationCount);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE AUTHOR = :author AND DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getNextAuthorDigest(String author, List<String> digests);

    @Query("SELECT COUNT(QUOTATION) FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text)")
    Integer countSearchText(String text);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getNextAllDigests(List<String> digests);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getSearchTextDigests(String text);

    @Query("SELECT DIGEST FROM QUOTATIONS WHERE (QUOTATION LIKE :text OR AUTHOR LIKE :text) AND DIGEST NOT IN (:digests) ORDER BY AUTHOR ASC, ROWID ASC")
    List<String> getNextSearchTextDigests(String text, List<String> digests);

    @Query("DELETE FROM QUOTATIONS")
    void erase();
}
