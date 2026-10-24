package com.infinity.cmdloggerintegration;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscordWebhookJsonTest {
    @Test
    void escapesQuotesBackslashesAndControlCharacters() {
        assertEquals("\"a\\\"b\\\\c\\n\"", DiscordWebhook.json("a\"b\\c\n"));
    }

    @Test
    void encodesNullAsJsonNull() {
        assertEquals("null", DiscordWebhook.json(null));
    }

    @Test
    void rejectsEmbedFieldsBeyondDiscordLimits() {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        assertThrows(IllegalArgumentException.class, () -> embed.addField("x".repeat(257), "value", false));
        assertThrows(IllegalArgumentException.class, () -> embed.addField("name", "x".repeat(1025), false));
    }

    @Test
    void rejectsMoreThanTwentyFiveFields() {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        for (int i = 0; i < 25; i++) embed.addField("field", "value", false);
        assertThrows(IllegalArgumentException.class, () -> embed.addField("extra", "value", false));
    }

    @Test
    void supportsTimestampsAndNormalEmbedMetadata() {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                .setTitle("Audit event")
                .setTimestamp(Instant.parse("2026-01-01T00:00:00Z"))
                .setFooter("Staff audit", null);
        embed.addField("Command", "/spawn", false);
    }
}
