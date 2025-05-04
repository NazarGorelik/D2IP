package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockingKeyGenerator {
    private static final Pattern pattern = Pattern.compile("\\w+\\s\\w+\\d+");

    public static String generateBlockingKey(String text) {
        Matcher matcher = pattern.matcher(text);
        List<String> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(matcher.group().toLowerCase());
        }
        Collections.sort(matches);
        return String.join(" ", matches);
    }
}
