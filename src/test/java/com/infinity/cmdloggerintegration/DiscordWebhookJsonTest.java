package com.infinity.cmdloggerintegration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
class DiscordWebhookJsonTest {
 @Test void escapesQuotesSlashesAndControlCharacters() {
  assertEquals("\"a\\\"b\\\\c\\n\"", DiscordWebhook.json("a\"b\\c\n"));
 }
 @Test void encodesNullAsJsonNull() { assertEquals("null", DiscordWebhook.json(null)); }
}
