package com.github.jameshnsears.quoteunquote.db.q;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;

import java.util.List;

import io.reactivex.Single;

@Dao
@RewriteQueriesToDropUnusedColumns
public interface QuotationDAO {
    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE digest NOT IN ('00000000', '1624c314') ORDER BY AUTHOR ASC")
    List<QuotationEntity> getAllQuotations();

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS ORDER BY AUTHOR, QUOTATION ASC")
    List<QuotationEntity> getAllQuotationsIncludingDefault();

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE DIGEST NOT IN ('00000000', '1624c314') ORDER BY COUNT(*) OVER (PARTITION BY AUTHOR) ASC, AUTHOR")
    List<QuotationEntity> getAllQuotationsAscendingSource();

    @Insert
    void insertQuotation(QuotationEntity quotationEntity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertQuotations(List<QuotationEntity> quotations);

    @Query("UPDATE QUOTATIONS SET author = :author, quotation = :quotation WHERE digest = :digest")
    void updateQuotationUsingDigest(String digest, String author, String quotation);

    @Query("UPDATE QUOTATIONS SET digest = :digest WHERE author = :author AND quotation = :quotation")
    void updateQuotationUsingAuthorQuotation(String digest, String author, String quotation);

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE DIGEST = :digest ORDER BY AUTHOR ASC")
    QuotationEntity getQuotation(String digest);

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE DIGEST IN (:digests)")
    List<QuotationEntity> getQuotations(List<String> digests);

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE AUTHOR = :author ORDER BY ROWID ASC")
    List<QuotationEntity> getQuotationsByAuthor(String author);

    @Query("SELECT COUNT(*) FROM QUOTATIONS")
    Single<Integer> countAll();

    @Query("SELECT DIGEST FROM QUOTATIONS")
    List<String> getDigests();

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

    @Query("SELECT AUTHOR, QUOTATION, WIKIPEDIA, DIGEST FROM QUOTATIONS WHERE INSTR(LOWER(AUTHOR || QUOTATION), LOWER(:text)) > 0")
    List<QuotationEntity> getQuotationsByText(@NonNull String text);

    @Query("SELECT COUNT(*) FROM QUOTATIONS WHERE INSTR(LOWER(AUTHOR || QUOTATION), LOWER(:text)) > 0")
    int countQuotationsByText(@NonNull String text);

    @Query("DELETE FROM QUOTATIONS")
    void eraseQuotations();

    @Query("DELETE FROM QUOTATIONS WHERE DIGEST = :digest")
    void eraseQuotations(String digest);
}
