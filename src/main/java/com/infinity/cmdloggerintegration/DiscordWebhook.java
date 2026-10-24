package com.infinity.cmdloggerintegration;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Minimal Discord webhook client with bounded network timeouts and JSON escaping. */
public final class DiscordWebhook {
    private final String url;
    private String username;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<>();

    public DiscordWebhook(String url) { this.url = url; }
    public void setUsername(String username) { this.username = username; }
    public void setTts(boolean tts) { this.tts = tts; }
    public void addEmbed(EmbedObject embed) {
        if (embed != null) {
            if (embeds.size() >= 10) throw new IllegalArgumentException("Discord allows at most 10 embeds per message");
            embeds.add(embed);
        }
    }

    public void execute() throws IOException {
        if (embeds.isEmpty()) throw new IllegalArgumentException("At least one embed is required");
        if (!WebhookUrlValidator.isValid(url)) throw new IllegalArgumentException("Webhook URL is not a valid Discord webhook URL");

        StringBuilder body = new StringBuilder("{\"username\":").append(json(username))
                .append(",\"tts\":").append(tts)
                // Command text must never be allowed to ping @everyone, @here, or roles.
                .append(",\"allowed_mentions\":{\"parse\":[]},\"embeds\":[");
        for (int i = 0; i < embeds.size(); i++) {
            if (i > 0) body.append(',');
            EmbedObject e = embeds.get(i);
            body.append("{\"title\":").append(json(e.title));
            if (e.color != null) {
                int rgb = (e.color.getRed() << 16) | (e.color.getGreen() << 8) | e.color.getBlue();
                body.append(",\"color\":").append(rgb);
            }
            if (e.timestamp != null) {
                body.append(",\"timestamp\":").append(json(DateTimeFormatter.ISO_INSTANT.format(e.timestamp)));
            }
            if (e.footer != null) body.append(",\"footer\":{\"text\":").append(json(e.footer)).append('}');
            body.append(",\"fields\":[");
            for (int j = 0; j < e.fields.size(); j++) {
                if (j > 0) body.append(',');
                Field f = e.fields.get(j);
                body.append("{\"name\":").append(json(f.name))
                        .append(",\"value\":").append(json(f.value))
                        .append(",\"inline\":").append(f.inline).append('}');
            }
            body.append("]}");
        }
        body.append("]}");

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("User-Agent", "CMDLoggerIntegration/3.1");
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(payload.length);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                InputStream error = connection.getErrorStream();
                if (error != null) error.close();
                // Do not include the remote body; it may contain sensitive or unexpected content.
                throw new IOException("Discord webhook returned HTTP " + status);
            }
            InputStream response = connection.getInputStream();
            if (response != null) response.close();
        } finally {
            connection.disconnect();
        }
    }

    static String json(String value) {
        if (value == null) return "null";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.append('"').toString();
    }

    public static final class EmbedObject {
        private String title, footer;
        private Color color;
        private Instant timestamp;
        private final List<Field> fields = new ArrayList<>();
        public EmbedObject setTitle(String title) {
            if (title != null && title.length() > 256) throw new IllegalArgumentException("Embed title exceeds 256 characters");
            this.title = title;
            return this;
        }
        public EmbedObject setTimestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public EmbedObject setFooter(String text, String ignoredIcon) {
            if (text != null && text.length() > 2048) throw new IllegalArgumentException("Embed footer exceeds 2048 characters");
            this.footer = text;
            return this;
        }
        public EmbedObject setColor(Color color) { this.color = color; return this; }
        public EmbedObject addField(String name, String value, boolean inline) {
            if (name == null || value == null || name.isEmpty() || value.isEmpty()) {
                throw new IllegalArgumentException("Embed field name and value are required");
            }
            if (name.length() > 256 || value.length() > 1024) {
                throw new IllegalArgumentException("Embed field name/value exceeds Discord limits");
            }
            if (fields.size() >= 25) throw new IllegalArgumentException("Discord allows at most 25 fields per embed");
            fields.add(new Field(name, value, inline));
            return this;
        }
    }

    private static final class Field {
        private final String name, value;
        private final boolean inline;
        private Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }
}
