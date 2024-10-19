package com.github.jameshnsears.quoteunquote.utils;

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.google.common.hash.Hashing;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public LinkedHashSet<QuotationEntity> importFortune(
            String filePath,
            InputStream inputStream
    ) throws ImportHelperException {
        LinkedHashSet<QuotationEntity> quotationEntityLinkedHashSet = new LinkedHashSet<>();
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            int fortuneLineNumber = 0;
            int quotationLineNumber = 0;

            try {
                StringBuilder fortune = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("%")) {
                        fortuneLineNumber += 1;

                        addFortune(
                                fortuneLineNumber,
                                filePath,
                                fortune.toString().trim(),
                                quotationEntityLinkedHashSet
                        );

                        fortune.setLength(0);
                        quotationLineNumber = 0;
                    } else {
                        fortune.append(line).append("\n");
                        quotationLineNumber += 1;
                    }

                    if (quotationLineNumber > 50) {
                        Timber.e(filePath);
                        throw new ImportHelperException(quotationLineNumber,
                                "quotation too big / invalid format"
                        );
                    }
                }

                if (fortune.length() > 0 && !(fortune.toString().equals("\n\n"))) {
                    fortuneLineNumber += 1;

                    addFortune(
                            fortuneLineNumber,
                            filePath,
                            fortune.toString().trim(),
                            quotationEntityLinkedHashSet
                    );
                }

                if (quotationEntityLinkedHashSet.isEmpty()) {
                    throw new ImportHelperException(quotationLineNumber, "no quotations found");
                }

            } catch (IOException exception) {
                Timber.e("%s", exception.getMessage());
                throw new ImportHelperException(fortuneLineNumber, exception.getMessage());
            }

            return quotationEntityLinkedHashSet;
        }
    }

    private void addFortune(
            int fortuneLineNumber,
            String filePath,
            String fortune,
            LinkedHashSet<QuotationEntity> quotationEntityLinkedHashSet
    ) {
        if (!fortune.isEmpty() && !fortune.equals('%')) {
            Timber.d("fortuneLineNumber=%d; fortune=%s", fortuneLineNumber, fortune);

            if (quotationEntityLinkedHashSet.size() == 0) {
                fortuneLineNumber = 1;
            }

            QuotationEntity quotationEntity = new QuotationEntity(
                    makeDigest(fortuneLineNumber, filePath, fortune),
                    "?",
                    filePath.substring(1 + filePath.lastIndexOf('/')),
                    fortune);

            if (!quotationEntityLinkedHashSet.contains(quotationEntity)) {
                quotationEntityLinkedHashSet.add(quotationEntity);
            }
        }
    }

    public LinkedHashSet<QuotationEntity> importCsv(
            InputStream inputStream
    ) throws ImportHelperException {
        CSVParser parser = null;

        LinkedHashSet<QuotationEntity> quotationEntityLinkedHashSet = new LinkedHashSet<>();

        int lineNumber = 0;

        try {
            parser = CSVParser.parse(inputStream, Charset.defaultCharset(), getCsvFormatForImport());
            for (CSVRecord record : parser) {
                lineNumber += 1;

                String author = record.get("Author");
                testNotEmptyAuthor(lineNumber, author);

                String quotation = record.get("Quotation");
                testNotEmptyQuotation(lineNumber, quotation);

                QuotationEntity quotationEntity = new QuotationEntity(
                        makeDigest(lineNumber, author, quotation),
                        "?",
                        author,
                        quotation);
                if (!quotationEntityLinkedHashSet.contains(quotationEntity)) {
                    quotationEntityLinkedHashSet.add(quotationEntity);
                }
            }
        } catch (IllegalStateException | IllegalArgumentException | IOException exception) {
            Timber.e("%s", exception.getMessage());
            throw new ImportHelperException(lineNumber, exception.getMessage());
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
            throw new ImportHelperException(-1, "empty file");
        }

        return quotationEntityLinkedHashSet;
    }

    private String makeDigest(int recordCount, String author, String quotation) {
        String digest;
        if (recordCount == 1) {
            digest = DEFAULT_DIGEST;
        } else {
            digest = makeDigest(quotation, author);
        }
        return digest;
    }

    private void testNotEmptyQuotation(int lineNumber, String quotation) throws ImportHelperException {
        if (quotation.isEmpty()) {
            throw new ImportHelperException(lineNumber, "empty quotation");
        }
    }

    private void testNotEmptyAuthor(int lineNumber, String author) throws ImportHelperException {
        if (author.isEmpty()) {
            throw new ImportHelperException(lineNumber, "empty author");
        }
    }

    public class ImportHelperException extends Exception {
        public int lineNumber;

        public ImportHelperException(int lineNumber, String errorMessage) {
            super(errorMessage);
            this.lineNumber = lineNumber;
        }
    }
}
