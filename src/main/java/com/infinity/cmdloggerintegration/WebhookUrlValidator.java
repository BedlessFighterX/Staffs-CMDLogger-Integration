package com.infinity.cmdloggerintegration;

import java.net.URI;

/** Strict, side-effect-free validation for Discord webhook URLs. */
final class WebhookUrlValidator {
    private WebhookUrlValidator() { }

    static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty() ||
                "your-webhook-url".equalsIgnoreCase(value.trim())) return false;
        try {
            URI uri = URI.create(value.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null) return false;
            if (uri.getRawUserInfo() != null || uri.getRawQuery() != null ||
                    uri.getRawFragment() != null) return false;
            if (uri.getPort() != -1 && uri.getPort() != 443) return false;
            if (!"discord.com".equalsIgnoreCase(host) &&
                    !"discordapp.com".equalsIgnoreCase(host)) return false;
            return uri.getPath() != null &&
                    uri.getPath().matches("/api/webhooks/[0-9]{5,25}/[A-Za-z0-9._-]{1,200}/?");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
