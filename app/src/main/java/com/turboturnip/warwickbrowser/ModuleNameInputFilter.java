package com.turboturnip.warwickbrowser;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleNameInputFilter implements InputFilter {
    public static final Pattern moduleNamePattern = Pattern.compile("[A-Za-z]{2}[A-Za-z0-9]{3}");

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Matcher matcher = moduleNamePattern.matcher(source);
        if (!matcher.matches())
            return "";

        return null;
    }
}
