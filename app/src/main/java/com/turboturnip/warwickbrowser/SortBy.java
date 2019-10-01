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
                    // Intelligent Name

                    // Splits strings into tokens of (integer | name), ignoring other chars
                    // Performs a lexical comparison of as many tokens of matching type as possible
                    // i.e. if t1 = [int, name, int] and t2 = [int, name] the first 2 tokens of each list will be compared
                    // this means lecture_N, lectureN, and lecture_N_extra will sort with descending N,
                    // even without zero-padding.

                    List<Token> tokens1 = tokenize(o1.getName());
                    List<Token> tokens2 = tokenize(o2.getName());

                    //
                    int i;
                    for (i = 0; i < tokens1.size() && i < tokens2.size(); i++) {
                        if (tokens1.get(i).type != tokens2.get(i).type)
                            break;
                    }
                    int firstNMatchingTypes = i;

                    if (firstNMatchingTypes == 0) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    // There's a partial type match
                    for (i = 0; i < tokens1.size() && i < tokens2.size(); i++) {
                        int comparison = 0;
                        Token t1 = tokens1.get(i), t2 = tokens2.get(i);
                        if (t1.type == 0) {
                            comparison = Integer.parseInt(t1.token) - Integer.parseInt(t2.token);
                        } else {
                            comparison = t1.token.compareTo(t2.token);
                        }
                        if (comparison != 0)
                            return comparison;
                    }
                    return tokens1.size() - tokens2.size();
                }

                class Token {
                    int type; // 0 for int, 1 for string
                    String token;

                    Token(int type, String token) {
                        this.type = type;
                        this.token = token;
                    }
                }

                List<Token> tokenize(String data) {
                    // Strip extension
                    int pos = data.lastIndexOf(".");
                    if (pos > 0) {
                        data = data.substring(0, pos);
                    }

                    List<Token> tokens = new ArrayList<>();
                    Pattern name = Pattern.compile("[a-zA-Z]+");
                    Pattern number = Pattern.compile("[0-9]+");
                    Pattern other = Pattern.compile("[^a-zA-Z0-9]+");
                    while(data.length() != 0) {
                        Matcher m = name.matcher(data);
                        if (m.lookingAt()) {
                            tokens.add(new Token(1, m.group(0)));
                            data = data.substring(m.group(0).length());
                            continue;
                        }

                        m = number.matcher(data);
                        if (m.lookingAt()) {
                            tokens.add(new Token(0, m.group(0)));
                            data = data.substring(m.group(0).length());
                            continue;
                        }

                        m = other.matcher(data);
                        if (m.lookingAt()) {
                            data = data.substring(m.group(0).length());
                        }
                    }

                    return tokens;
                }
            },
    };

    public static final SortBy Default = SortBy.IntelligentName;

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

