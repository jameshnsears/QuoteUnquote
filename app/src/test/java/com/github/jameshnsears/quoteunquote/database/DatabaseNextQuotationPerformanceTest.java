package com.github.jameshnsears.quoteunquote.database;

import androidx.annotation.NonNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DatabaseNextQuotationPerformanceTest {
    // https://javarevisited.blogspot.com/2012/11/difference-between-treeset-hashset-vs-linkedhashset-java.html?m=1
    // https://stackoverflow.com/questions/2043783/how-to-efficiently-performance-remove-many-items-from-list-in-java
    // https://medium.com/@sadia.islam.badhon/big-o-notation-into-overview-f83bc792e344
    // https://github.com/google/guava

    static Random random = new Random();
    static List<String> initDigests;
    static int maxPreviousDigests = 30000;
    static int maxAllDigests = 30000;

    static List<String> getInitDigests() {
        if (initDigests == null) {
            initDigests = getAllDigests();
        }
        return initDigests;
    }

    /*
    maxPreviousDigests = maxAllDigests = 30000
    3828ms - jdk
    3914ms - guave:removeAll
    3976ms - guava:iterator
    14ms - jdk:Set

    maxPreviousDigests = maxAllDigests = 10000
    264ms - jdk
    248ms - guave:removeAll
    240ms - guava:iterator
    5ms - jdk:Set

    maxPreviousDigests = maxAllDigests = 5000
    78ms - jdk
    57ms - guave:removeAll
    58ms - guava:iterator
    2ms - jdk:Set
     */

    @Test
    public void jdkHashSet() {
        // insertion order, fast
        LinkedHashSet<String> allDigests = new LinkedHashSet<>(getInitDigests());

        // no order, very fast
        HashSet<String> previousDigests = getPreviousDigests(allDigests);

        long startTime = System.currentTimeMillis();
        allDigests.removeAll(previousDigests);
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + "ms - jdk:Set");

        assertAllMinusPrevious(allDigests, previousDigests);
    }

    @NonNull
    private HashSet<String> getPreviousDigests(LinkedHashSet<String> allDigests) {
        HashSet<String> previousDigests = new HashSet();

        List<Integer> rndIndexes = randomAllDigestsIndexes();

        for (int i = 0; i < maxPreviousDigests; i++) {
            int rndIndex = rndIndexes.get(i);

            int rndIndexCount = 0;
            for (String digest : allDigests) {
                if (rndIndexCount == rndIndex) {
                    previousDigests.add(digest);
                    break;
                }
                rndIndexCount++;
            }
        }

        return previousDigests;
    }

    @Test
    public void jdkArrayList() {
        List<String> allDigests = new ArrayList<>(getInitDigests());
        List<String> previousDigests = getPreviousDigests(allDigests);

        long startTime = System.currentTimeMillis();
        allDigests.removeAll(previousDigests);
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + "ms - jdk");

        assertAllMinusPrevious(allDigests, previousDigests);
    }

    @Test
    public void guavaRemoveAll() {
        List<String> allDigests = Lists.newArrayList(new ArrayList<>(getInitDigests()));
        List<String> previousDigests = Lists.newArrayList(getPreviousDigests(allDigests));

        long startTime = System.currentTimeMillis();
        allDigests.removeAll(previousDigests);
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + "ms - guave:removeAll");

        assertAllMinusPrevious(allDigests, previousDigests);
    }

    @Test
    public void guavaIterator() {
        List<String> allDigests = Lists.newArrayList(new ArrayList<>(getInitDigests()));
        List<String> previousDigests = Lists.newArrayList(getPreviousDigests(allDigests));

        long startTime = System.currentTimeMillis();
        Iterables.removeIf(allDigests, input -> previousDigests.contains(input) ? true : false);
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + "ms - guava:iterator");

        assertAllMinusPrevious(allDigests, previousDigests);
    }

    private void assertAllMinusPrevious(LinkedHashSet<String> allDigests, HashSet<String> previousDigests) {
        for (String previousDigest : previousDigests
        ) {
            if (allDigests.contains(previousDigest)) {
                fail(previousDigest);
            }
        }
        assertEquals(maxAllDigests - maxPreviousDigests, allDigests.size());
    }

    private void assertAllMinusPrevious(List<String> allDigests, List<String> previousDigests) {
        for (String previousDigest : previousDigests
        ) {
            if (allDigests.contains(previousDigest)) {
                fail(previousDigest);
            }
        }
        assertEquals(maxAllDigests - maxPreviousDigests, allDigests.size());
    }

    @NonNull
    private List<String> getPreviousDigests(List<String> allDigests) {
        List<String> previousDigests = new ArrayList();

        List<Integer> rndIndexes = randomAllDigestsIndexes();

        for (int i = 0; i < maxPreviousDigests; i++) {
            previousDigests.add(allDigests.get(rndIndexes.get(i)));
        }
        return previousDigests;
    }

    @NonNull
    private List<Integer> randomAllDigestsIndexes() {
        List<Integer> rndIndexes = new ArrayList<>();
        for (int i = 0; i < maxAllDigests; i++) {
            rndIndexes.add(i);
        }
        Collections.shuffle(rndIndexes);
        return rndIndexes;
    }

    @NonNull
    private static List<String> getAllDigests() {
        List<String> allDigests = new ArrayList();
        for (int i = 0; i < maxAllDigests; i++) {
            allDigests.add(rndString());
        }
        return allDigests;
    }

    @NonNull
    private static String rndString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
