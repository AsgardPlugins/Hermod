package com.github.asgard.hermod.stream;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record StreamEntry(String user, String link, String login) {

    private static final Pattern TWITCH = Pattern.compile("^(?:https?://)?(?:www\\.|m\\.)?twitch\\.tv/([A-Za-z0-9_]{3,25})/?(?:\\?.*)?$", Pattern.CASE_INSENSITIVE);

    public static StreamEntry parse(String user, String link) {
        Matcher matcher = TWITCH.matcher(link.trim());
        if (!matcher.matches()) {
            return null;
        }
        String login = matcher.group(1).toLowerCase(Locale.ROOT);
        return new StreamEntry(user, "https://twitch.tv/" + login, login);
    }
}
