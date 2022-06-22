package com.github.jameshnsears.quoteunquote.utils;

import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class CSVHelper {
    public void csvWriteFavourites(FileOutputStream fileOutputStream, ArrayList<QuotationEntity> exportableFavourites) throws IOException {
        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setDelimiter("||")
                .setRecordSeparator("\n")
                .setEscape('\\')
                .setQuoteMode(QuoteMode.NONE)
                .setHeader("Author","Quotation")
                .build();

        final CSVPrinter csvPrinter = new CSVPrinter(outputStreamWriter, csvFormat);

        for (final QuotationEntity quotationEntityFavourite : exportableFavourites) {
            csvPrinter.printRecord(
                    quotationEntityFavourite.author,
                    quotationEntityFavourite.quotation);
        }

        csvPrinter.flush();
        csvPrinter.close();
        outputStreamWriter.close();
    }
}
