package com.github.asgard.hermod.stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class HelixSource implements LiveSource {

    private static final int BATCH = 100;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String clientId;
    private final String clientSecret;
    private String token;

    public HelixSource(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Set<String> liveLogins(Collection<String> logins) throws IOException, InterruptedException {
        Set<String> live = new HashSet<>();
        List<String> list = new ArrayList<>(logins);
        for (int from = 0; from < list.size(); from += BATCH) {
            live.addAll(query(list.subList(from, Math.min(list.size(), from + BATCH))));
        }
        return live;
    }

    private Set<String> query(List<String> logins) throws IOException, InterruptedException {
        StringBuilder url = new StringBuilder("https://api.twitch.tv/helix/streams?first=100");
        for (String login : logins) {
            url.append("&user_login=").append(URLEncoder.encode(login, StandardCharsets.UTF_8));
        }
        HttpResponse<String> response = get(url.toString());
        if (response.statusCode() == 401) {
            token = null;
            response = get(url.toString());
        }
        if (response.statusCode() != 200) {
            throw new IOException("Twitch answered " + response.statusCode());
        }
        Set<String> live = new HashSet<>();
        JsonArray data = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("data");
        for (JsonElement element : data) {
            JsonObject stream = element.getAsJsonObject();
            if ("live".equals(stream.get("type").getAsString())) {
                live.add(stream.get("user_login").getAsString().toLowerCase(Locale.ROOT));
            }
        }
        return live;
    }

    private HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Client-Id", clientId)
                .header("Authorization", "Bearer " + token())
                .GET()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String token() throws IOException, InterruptedException {
        if (token != null) {
            return token;
        }
        String form = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                + "&grant_type=client_credentials";
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://id.twitch.tv/oauth2/token"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Twitch refused the client credentials (" + response.statusCode() + ")");
        }
        token = JsonParser.parseString(response.body()).getAsJsonObject().get("access_token").getAsString();
        return token;
    }
}
