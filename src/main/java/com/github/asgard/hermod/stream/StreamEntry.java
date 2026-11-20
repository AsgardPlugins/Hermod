package com.github.asgard.hermod.stream;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record StreamEntry(String user, String link, String login) {

    private static final Pattern TWITCH = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?twitch\\.tv/([A-Za-z0-9_]{3,25})/?(?:\\?.*)?$", Pattern.CASE_INSENSITIVE);

    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{3,25}$");

    public static StreamEntry parse(String user, String link) {
        String text = link.trim();
        String login;
        Matcher matcher = TWITCH.matcher(text);
        if (matcher.matches()) {
            login = matcher.group(1).toLowerCase(Locale.ROOT);
        } else if (NAME.matcher(text).matches()) {
            login = text.toLowerCase(Locale.ROOT);
        } else {
            return null;
        }
        return new StreamEntry(user, "https://twitch.tv/" + login, login);
    }
}
