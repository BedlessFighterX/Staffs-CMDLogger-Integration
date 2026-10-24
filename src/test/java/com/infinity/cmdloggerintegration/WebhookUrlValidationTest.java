package com.infinity.cmdloggerintegration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookUrlValidationTest {
    @Test
    void acceptsHttpsDiscordWebhookUrls() {
        assertTrue(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discord.com/api/webhooks/123456789012345678/secret-token"));
        assertTrue(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discordapp.com/api/webhooks/123456789012345678/secret-token/"));
    }

    @Test
    void rejectsNonHttpsAndLookalikeHosts() {
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "http://discord.com/api/webhooks/123/token"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discord.com.evil.example/api/webhooks/123/token"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://notdiscord.com/api/webhooks/123/token"));
    }

    @Test
    void rejectsMalformedOrAmbiguousWebhookUrls() {
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(null));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl("your-webhook-url"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discord.com/api/webhooks/not-a-number/token"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discord.com/api/webhooks/123/token/extra"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://discord.com/api/webhooks/123/token?next=https://evil.example"));
        assertFalse(CMDLoggerIntegration.isValidWebhookUrl(
                "https://user@discord.com/api/webhooks/123/token"));
    }
}
