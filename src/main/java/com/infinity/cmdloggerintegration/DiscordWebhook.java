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
    public void addEmbed(EmbedObject embed) { if (embed != null) embeds.add(embed); }

    public void execute() throws IOException {
        if (embeds.isEmpty()) throw new IllegalArgumentException("At least one embed is required");
        if (embeds.size() > 10) throw new IllegalArgumentException("Discord allows at most 10 embeds per webhook");
        if (!WebhookUrlValidator.isValid(url)) throw new IllegalArgumentException("Webhook URL is not a valid Discord webhook URL");

        StringBuilder body = new StringBuilder("{\"username\":").append(json(username))
                .append(",\"tts\":").append(tts)
                .append(",\"allowed_mentions\":{\"parse\":[]},\"embeds\":[");
        for (int i = 0; i < embeds.size(); i++) {
            if (i > 0) body.append(',');
            EmbedObject e = embeds.get(i);
            body.append("{\"title\":").append(json(e.title));
            if (e.color != null) {
                int rgb = (e.color.getRed() << 16) | (e.color.getGreen() << 8) | e.color.getBlue();
                body.append(",\"color\":").append(rgb);
            }
            if (e.timestamp != null) body.append(",\"timestamp\":").append(json(DateTimeFormatter.ISO_INSTANT.format(e.timestamp)));
            if (e.footer != null) body.append(",\"footer\":{\"text\":").append(json(e.footer)).append('}');
            if (e.fields.size() > 25) throw new IllegalArgumentException("Discord allows at most 25 fields per embed");
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

        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        IOException lastFailure = null;

        // Retry only transient responses. The caller already runs on the bounded
        // delivery executor, so backoff never blocks the Minecraft server thread.
        for (int attempt = 1; attempt <= 3; attempt++) {
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setUseCaches(false);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("User-Agent", "CMDLoggerIntegration/3.3");
                connection.setFixedLengthStreamingMode(payload.length);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(payload);
                }
                int status = connection.getResponseCode();
                if (status >= 200 && status < 300) {
                    InputStream response = connection.getInputStream();
                    if (response != null) response.close();
                    return;
                }

                InputStream error = connection.getErrorStream();
                if (error != null) error.close();
                IOException failure = new IOException("Discord webhook returned HTTP " + status);
                if (!isRetryable(status) || attempt == 3) throw failure;
                lastFailure = failure;
                long delay = retryDelayMillis(connection, attempt);
                connection.disconnect();
                connection = null;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to retry Discord delivery", interrupted);
                }
            } catch (IOException ex) {
                if (attempt == 3 || !isTransientNetworkFailure(ex)) throw ex;
                lastFailure = ex;
                try {
                    Thread.sleep(250L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting to retry Discord delivery", interrupted);
                }
            } finally {
                if (connection != null) connection.disconnect();
            }
        }
        throw lastFailure == null ? new IOException("Discord webhook delivery failed") : lastFailure;
    }

    private static boolean isRetryable(int status) {
        return status == 429 || status >= 500 && status <= 599;
    }

    private static boolean isTransientNetworkFailure(IOException ex) {
        // HTTP status failures are handled explicitly above; IO failures such as
        // timeouts and connection resets are safe to retry a limited number of times.
        return !(ex.getMessage() != null && ex.getMessage().startsWith("Discord webhook returned HTTP "));
    }

    private static long retryDelayMillis(HttpsURLConnection connection, int attempt) {
        String retryAfter = connection.getHeaderField("Retry-After");
        if (retryAfter != null) {
            try {
                long seconds = Long.parseLong(retryAfter.trim());
                return Math.max(0L, Math.min(seconds * 1000L, 5000L));
            } catch (NumberFormatException ignored) {
                // Discord may omit or change this header; use bounded backoff below.
            }
        }
        return Math.min(250L * (1L << (attempt - 1)), 2000L);
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
        return out.append('\"').toString();
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
        public EmbedObject setFooter(String text, String ignoredIcon) {
            if (text != null && text.length() > 2048) throw new IllegalArgumentException("Embed footer exceeds 2048 characters");
            this.footer = text;
            return this;
        }
        public EmbedObject setColor(Color color) { this.color = color; return this; }
        public EmbedObject setTimestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
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
        private Field(String name, String value, boolean inline) { this.name = name; this.value = value; this.inline = inline; }
    }
}
