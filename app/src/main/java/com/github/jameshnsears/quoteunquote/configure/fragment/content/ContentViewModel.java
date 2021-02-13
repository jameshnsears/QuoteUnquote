package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.github.jameshnsears.quoteunquote.cloud.CloudFavouritesHelper;
import com.github.jameshnsears.quoteunquote.cloud.SaveRequest;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
import com.github.jameshnsears.quoteunquote.database.quotation.AuthorPOJO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.Single;
import timber.log.Timber;

public class ContentViewModel extends AndroidViewModel {
    @Nullable
    public List<AuthorPOJO> authorPOJOList;
    @Nullable
    public DatabaseRepository databaseRepository;

    public ContentViewModel(@NonNull final Application application) {
        super(application);
        databaseRepository = DatabaseRepository.getInstance(application.getApplicationContext());
    }

    @NonNull
    public Single<Integer> countAll() {
        return databaseRepository.countAll();
    }

    @NonNull
    public Single<List<AuthorPOJO>> authors() {
        return databaseRepository.getAuthors();
    }

    @NonNull
    public List<String> authorsSorted(@NonNull final List<AuthorPOJO> unsortedAuthorPOJOList) {
        this.authorPOJOList = unsortedAuthorPOJOList;

        Collections.sort(unsortedAuthorPOJOList);
        final ArrayList<String> authors = new ArrayList<>();
        for (final AuthorPOJO authorPOJO : unsortedAuthorPOJOList) {
            authors.add(authorPOJO.author);
        }
        return authors;
    }

    public int countAuthorQuotations(@NonNull final String author) {
        int countAuthorQuotations = 0;

        for (final AuthorPOJO authorPOJO : this.authorPOJOList) {
            if (authorPOJO.author.equals(author)) {
                countAuthorQuotations = authorPOJO.count;
                break;
            }
        }
        return countAuthorQuotations;
    }

    public int authorsIndex(@NonNull final String author) {
        int index = 0;
        for (final AuthorPOJO authorPOJO : this.authorPOJOList) {
            if (authorPOJO.author.equals(author)) {
                break;
            }
            index++;
        }
        return index;
    }

    @NonNull
    public Single<Integer> countFavourites() {
        return databaseRepository.countFavourites();
    }

    @NonNull
    public Integer countQuotationWithText(@NonNull final String text) {
        return databaseRepository.countQuotationsText(text);
    }

    @NonNull
    public String getFavouritesToSend() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        final Future<String> future = executorService.submit(() -> {
            SaveRequest saveRequest = new SaveRequest();
            saveRequest.code = localCode();
            saveRequest.digests = new ArrayList<>(databaseRepository.getFavourites());

            return CloudFavouritesHelper.jsonSendRequest(saveRequest);
        });

        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Timber.w(e.toString());
            Thread.currentThread().interrupt();
            return "";
        }
    }

    protected String localCode() {
        return CloudFavouritesHelper.getLocalCode();
    }
}
