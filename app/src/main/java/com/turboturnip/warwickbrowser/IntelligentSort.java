package com.turboturnip.warwickbrowser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntelligentSort {
    private IntelligentSort(){}

    public static int compare(String a, String b, Comparator<Integer> intCompare) {
        // Splits strings into tokens of (integer | name), ignoring other chars
        // Performs a lexical comparison of as many tokens of matching type as possible
        // i.e. if t1 = [int, name, int] and t2 = [int, name] the first 2 tokens of each list will be compared
        // this means lecture_N, lectureN, and lecture_N_extra will sort with descending N,
        // even without zero-padding.

        List<Token> tokens1 = tokenize(a);
        List<Token> tokens2 = tokenize(b);

        int i;
        for (i = 0; i < tokens1.size() && i < tokens2.size(); i++) {
            if (tokens1.get(i).type != tokens2.get(i).type)
                break;
        }
        int firstNMatchingTypes = i;

        if (firstNMatchingTypes == 0) {
            return a.compareTo(b);
        }

        // There's a partial type match
        for (i = 0; i < firstNMatchingTypes; i++) {
            int comparison = 0;
            Token t1 = tokens1.get(i), t2 = tokens2.get(i);
            if (t1.type == 0) {
                comparison = intCompare.compare(Integer.parseInt(t1.token), Integer.parseInt(t2.token));
            } else {
                comparison = t1.token.toLowerCase().compareTo(t2.token.toLowerCase());
            }
            if (comparison != 0)
                return comparison;
        }
        int comparison = tokens1.size() - tokens2.size();
        if (comparison == 0)
            return a.compareTo(b);
        return comparison;
    }

    static class Token {
        int type; // 0 for int, 1 for string
        String token;

        Token(int type, String token) {
            this.type = type;
            this.token = token;
        }
    }

    static List<Token> tokenize(String data) {
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
}
