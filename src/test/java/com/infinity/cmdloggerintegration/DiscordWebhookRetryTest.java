package com.infinity.cmdloggerintegration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscordWebhookRetryTest {
    @Test
    void retriesRateLimitsAndServerErrorsOnly() {
        assertTrue(DiscordWebhook.isRetryable(429));
        assertTrue(DiscordWebhook.isRetryable(500));
        assertTrue(DiscordWebhook.isRetryable(502));
        assertTrue(DiscordWebhook.isRetryable(599));
        assertFalse(DiscordWebhook.isRetryable(200));
        assertFalse(DiscordWebhook.isRetryable(400));
        assertFalse(DiscordWebhook.isRetryable(401));
        assertFalse(DiscordWebhook.isRetryable(404));
    }
}
