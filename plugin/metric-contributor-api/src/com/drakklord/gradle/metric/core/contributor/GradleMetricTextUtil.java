package com.drakklord.gradle.metric.core.contributor;

import com.drakklord.gradle.metric.core.contributor.model.GradleMetricEntryWrapper;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text helper functions.
 * Created by DrakkLord on 2016. 03. 14..
 */
public class GradleMetricTextUtil {

    private static final int MAX_PEEK_LENGTH_NUMCHARS = 128;

    /** Peek into the contents of the file showing the area that is part of the problem.
     * Generic rule is that hard limit, line break, or column end breaks the preview, whichever is met first. */
    public static String getTextPeek(GradleMetricEntryWrapper entry, PsiFile file) {
        int targetLine = entry.lineStart;

        String data = file.getText();
        String lines[] = data.split("\r?\n|\r");
        if (lines.length < entry.lineStart) {
            targetLine = 0;
        }

        int startColumn = Math.max(entry.columnStart, 0);
        int endColumn = entry.columnEnd < 2 ? lines[targetLine].length() - 2  : entry.columnEnd - 2;
        boolean truncateText = false;

        if (endColumn - startColumn >= MAX_PEEK_LENGTH_NUMCHARS) {
            endColumn -= 3;
            truncateText = true;
        }
        if (endColumn < lines[targetLine].length()) {
            truncateText = true;
        }

        final StringBuilder sb = new StringBuilder();
        if (startColumn > 0) {
            sb.append("[...]");
        }
        sb.append(lines[targetLine].substring(startColumn, endColumn));
        if (truncateText) {
            sb.append("[...]");
        }
        return sb.toString().trim();
    }

    /**
     * Splits a String according to a regex, keeping the splitter at the end of each substring
     * @param input The input String
     * @param regex The regular expression upon which to split the input
     * @param offset Shifts the split point by this number of characters to the left: should be equal or less than the splitter length
     * @return An array of Strings
     */
    static String[] splitAndKeep(String input, String regex, int offset) {
        ArrayList<String> res = new ArrayList<String>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        int pos = 0;
        while (m.find()) {
            res.add(input.substring(pos, m.end() - offset));
            pos = m.end() - offset;
        }
        if(pos < input.length()) res.add(input.substring(pos));
        return res.toArray(new String[res.size()]);
    }

    /**
     * Splits a String according to a regex, keeping the splitter at the end of each substring
     * @param input The input String
     * @param regex The regular expression upon which to split the input
     * @return An array of Strings
     */
    static String[] splitAndKeep(String input, String regex) {
        return splitAndKeep(input, regex, 0);
    }
}
