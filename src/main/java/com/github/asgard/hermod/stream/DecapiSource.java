package com.github.asgard.hermod.stream;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class DecapiSource implements LiveSource {

    private static final Pattern UPTIME = Pattern.compile("^\\d+ (second|minute|hour|day|week|month|year)");
    private static final long PAUSE_MILLIS = 250L;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Override
    public Set<String> liveLogins(Collection<String> logins) throws IOException, InterruptedException {
        Set<String> live = new HashSet<>();
        for (String login : logins) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://decapi.me/twitch/uptime/" + URLEncoder.encode(login, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Hermod")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new IOException("decapi.me is rate limiting, set up a Twitch app to avoid this");
            }
            if (response.statusCode() != 200) {
                throw new IOException("decapi.me answered " + response.statusCode());
            }
            if (UPTIME.matcher(response.body().trim()).find()) {
                live.add(login);
            }
            Thread.sleep(PAUSE_MILLIS);
        }
        return live;
    }
}
