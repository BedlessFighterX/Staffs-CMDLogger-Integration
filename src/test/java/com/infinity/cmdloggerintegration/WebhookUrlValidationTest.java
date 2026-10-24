package com.infinity.cmdloggerintegration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookUrlValidationTest {
    @Test
    void acceptsHttpsDiscordWebhookUrls() {
        assertTrue(WebhookUrlValidator.isValid(
                "https://discord.com/api/webhooks/123456789012345678/secret-token"));
        assertTrue(WebhookUrlValidator.isValid(
                "https://discordapp.com/api/webhooks/123456789012345678/secret-token/"));
        assertTrue(WebhookUrlValidator.isValid(
                "https://discord.com:443/api/webhooks/123456789012345678/secret-token"));
    }

    @Test
    void rejectsNonHttpsAndLookalikeHosts() {
        assertFalse(WebhookUrlValidator.isValid("http://discord.com/api/webhooks/123456789012345678/token"));
        assertFalse(WebhookUrlValidator.isValid("https://discord.com.evil.example/api/webhooks/123456789012345678/token"));
        assertFalse(WebhookUrlValidator.isValid("https://notdiscord.com/api/webhooks/123456789012345678/token"));
        assertFalse(WebhookUrlValidator.isValid("https://discord.com:8443/api/webhooks/123456789012345678/token"));
    }

    @Test
    void rejectsMalformedOrAmbiguousWebhookUrls() {
        assertFalse(WebhookUrlValidator.isValid(null));
        assertFalse(WebhookUrlValidator.isValid("your-webhook-url"));
        assertFalse(WebhookUrlValidator.isValid("https://discord.com/api/webhooks/not-a-number/token"));
        assertFalse(WebhookUrlValidator.isValid("https://discord.com/api/webhooks/123/token/extra"));
        assertFalse(WebhookUrlValidator.isValid("https://discord.com/api/webhooks/123456789012345678/token?next=https://evil.example"));
        assertFalse(WebhookUrlValidator.isValid("https://user@discord.com/api/webhooks/123456789012345678/token"));
    }
}
