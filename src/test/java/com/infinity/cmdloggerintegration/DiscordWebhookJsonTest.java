package com.infinity.cmdloggerintegration;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

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
    void rejectsOversizedTitleAndFooter() {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        assertThrows(IllegalArgumentException.class, () -> embed.setTitle("x".repeat(257)));
        assertThrows(IllegalArgumentException.class, () -> embed.setFooter("x".repeat(2049), null));
    }

    @Test
    void rejectsCombinedTextOverDiscordMessageLimitAcrossEmbeds() {
        DiscordWebhook.EmbedObject first = new DiscordWebhook.EmbedObject().setTitle("x".repeat(256));
        first.addField("name", "x".repeat(1024), false);
        DiscordWebhook.EmbedObject second = new DiscordWebhook.EmbedObject().setFooter("y".repeat(2048), null);
        second.addField("name", "z".repeat(1024), false);
        second.addField("name", "q".repeat(1024), false);
        second.addField("name", "r".repeat(1024), false);
        assertThrows(IllegalArgumentException.class,
                () -> DiscordWebhook.validateEmbedCharacterLimit(List.of(first, second)));
    }

    @Test
    void acceptsCombinedTextAtDiscordMessageLimit() {
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject().setTitle("x".repeat(256));
        embed.addField("n".repeat(256), "v".repeat(1024), false);
        embed.addField("n".repeat(256), "v".repeat(1024), false);
        embed.addField("n".repeat(256), "v".repeat(1024), false);
        embed.addField("n".repeat(256), "v".repeat(1024), false);
        embed.addField("n".repeat(256), "v".repeat(616), false);
        DiscordWebhook.validateEmbedCharacterLimit(List.of(embed));
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
