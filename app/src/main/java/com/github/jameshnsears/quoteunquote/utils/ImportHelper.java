package com.github.jameshnsears.quoteunquote.utils;

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.hash.Hashing;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import timber.log.Timber;

public class ImportHelper {
    public static String DEFAULT_DIGEST = "00000000";
    private static String[] headers = {"Author", "Quotation"};

    public static String makeDigest(String quotation, String author) {
        String rawString = quotation + author;
        return Hashing.sha256()
                .hashBytes(rawString.getBytes(StandardCharsets.UTF_8))
                .toString().substring(0, 8);
    }

    public void csvExport(FileOutputStream fileOutputStream, ArrayList<QuotationEntity> exportableFavourites) throws IOException {
        OutputStreamWriter outputStreamWriter = null;
        CSVPrinter csvPrinter = null;

        try {
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            csvPrinter = new CSVPrinter(outputStreamWriter, getCsvFormatForExport());

            for (final QuotationEntity quotationEntityFavourite : exportableFavourites) {
                csvPrinter.printRecord(
                        quotationEntityFavourite.author,
                        quotationEntityFavourite.quotation);
            }
        } finally {
            if (csvPrinter != null) {
                csvPrinter.flush();
                csvPrinter.close();
            }

            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        }
    }

    private CSVFormat getCsvFormatForExport() {
        return CSVFormat.Builder.create()
                .setDelimiter("||")
                .setRecordSeparator("\n")
                .setEscape('\\')
                .setQuoteMode(QuoteMode.NONE)
                .setHeader(headers)
                .setSkipHeaderRecord(true)
                .build();
    }

    /*
    does not support newlines; use .setQuoteMode(QuoteMode.NONE) and rm .setQuote(null)
     */
    private CSVFormat getCsvFormatForImport() {
        return CSVFormat.Builder.create()
                .setDelimiter("||")
                .setEscape('\\')
                .setQuote(null)
                .setHeader(headers)
                .build();
    }

    public LinkedHashSet<QuotationEntity> csvImportDatabase(InputStream inputStream) throws ImportHelperException {
        CSVParser parser = null;

        LinkedHashSet<QuotationEntity> quotationEntityLinkedHashSet = new LinkedHashSet<>();

        int recordCount = 0;

        try {
            parser = CSVParser.parse(inputStream, Charset.defaultCharset(), getCsvFormatForImport());
            for (CSVRecord record : parser) {
                String author = record.get("Author");
                testNotEmptyAuthor(author);

                String quotation = record.get("Quotation");
                testNotEmptyQuotation(quotation);


                String digest = makeDigest(recordCount, author, quotation);

                recordCount += 1;
                Timber.d("recordCount=%d", recordCount);

                QuotationEntity q = new QuotationEntity(
                        digest,
                        "?",
                        author,
                        quotation);
                if (!quotationEntityLinkedHashSet.contains(q)) {
                    quotationEntityLinkedHashSet.add(q);
                }
            }
        } catch (IllegalStateException | IllegalArgumentException | IOException exception) {
            Timber.e("%s", exception.getMessage());
            throw new ImportHelperException(recordCount + " : " + exception.getMessage());
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    Timber.e(e.getMessage());
                }
            }
        }

        if (quotationEntityLinkedHashSet.size() == 0) {
            throw new ImportHelperException("empty file");
        }

        return quotationEntityLinkedHashSet;
    }

    private String makeDigest(int recordCount, String author, String quotation) {
        String digest;
        if (recordCount == 0) {
            digest = DEFAULT_DIGEST;
        } else {
            digest = makeDigest(quotation, author);
        }
        return digest;
    }

    private void testNotEmptyQuotation(String quotation) throws ImportHelperException {
        if (quotation.equals("") || quotation.length() <= 1) {
            throw new ImportHelperException("empty quotation");
        }
    }

    private void testNotEmptyAuthor(String author) throws ImportHelperException {
        if (author.equals("")) {
            throw new ImportHelperException("empty author");
        }
    }

    public class ImportHelperException extends Exception {
        public ImportHelperException(String errorMessage) {
            super(errorMessage);
        }
    }
}
