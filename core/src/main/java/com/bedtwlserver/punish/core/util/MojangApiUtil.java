package com.bedtwlserver.punish.core.util;

import com.bedtwlserver.punish.core.model.MojangProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class MojangApiUtil {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private MojangApiUtil() {
    }

    public static MojangProfile resolveProfile(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return new MojangProfile(online.getName(), online.getUniqueId());
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                return null;
            }

            String body = response.body();
            if (body == null || body.isBlank()) {
                return null;
            }

            JsonObject obj = new JsonParser().parse(new StringReader(body)).getAsJsonObject();
            if (!obj.has("id") || !obj.has("name")) {
                return null;
            }

            return new MojangProfile(obj.get("name").getAsString(), parseUuid(obj.get("id").getAsString()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static UUID parseUuid(String raw) {
        String formatted = raw.replaceFirst(
                "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(formatted);
    }

}
