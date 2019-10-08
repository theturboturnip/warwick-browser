package com.turboturnip.warwickbrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.room.TypeConverter;
import androidx.transition.Visibility;

public enum SortBy {
    NameAscending(0),
    NameDescending(1),
    DateAscending(2),
    DateDescending(3),
    IntelligentName(4),
    IntelligentNameInvNumbers(5),
    ;

    // Create a dummy class to avoid generic array creation
    private static abstract class FileComparator implements Comparator<File> {};
    private static final FileComparator[] comparators = new FileComparator[]{
            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Name Ascending
                    return o1.getName().compareTo(o2.getName());
                }
            },

            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Name Descending
                    return o2.getName().compareTo(o1.getName());
                }
            },

            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Date Ascending
                    return (int)(o2.lastModified()/1000000 - o1.lastModified()/1000000);
                }
            },

            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Date Descending
                    return (int)(o1.lastModified()/1000000 - o2.lastModified()/1000000);
                }
            },

            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Intelligent comparison
                    return IntelligentSort.compare(o1.getName(), o2.getName(), Integer::compare);
                }
            },
            new FileComparator() {
                @Override
                public int compare(@NonNull File o1, @NonNull File o2) {
                    // Intelligent comparison with the numbers inverted
                    return IntelligentSort.compare(o1.getName(), o2.getName(), new Comparator<Integer>() {
                        @Override
                        public int compare(Integer t1, Integer t2) {
                            // Inverse number comparison
                            return t2 - t1;
                        }
                    });
                }
            },
    };

    public static final SortBy Default = SortBy.IntelligentNameInvNumbers;

    public final int SQLIndex;
    SortBy(int SQLIndex) {
        this.SQLIndex = SQLIndex;
    }

    public Comparator<File> getComparator() {
        return SortBy.comparators[SQLIndex];
    }

    @TypeConverter
    public static SortBy toSortBy(int status) {
        return SortBy.values()[status];
    }
    @TypeConverter
    public static int toInt(SortBy sortBy) {
        return sortBy.SQLIndex;
    }
}

