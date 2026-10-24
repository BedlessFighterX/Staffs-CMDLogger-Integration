package com.infinity.cmdloggerintegration;

import java.util.List;
import java.util.Locale;

final class CommandFilter {
    private CommandFilter() { }

    static boolean shouldIgnore(String line, List<String> ignored) { return matchesRoot(line, ignored); }

    static boolean matchesRoot(String line, List<String> roots) {
        if (line == null || roots == null || roots.isEmpty()) return false;
        String value = line.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("/")) value = value.substring(1);
        int space = firstWhitespace(value);
        String root = space < 0 ? value : value.substring(0, space);
        int colon = root.indexOf(':');
        String plain = colon < 0 ? root : root.substring(colon + 1);

        for (String item : roots) {
            if (item == null) continue;
            String candidate = item.trim().toLowerCase(Locale.ROOT);
            while (candidate.startsWith("/")) candidate = candidate.substring(1);
            if (!candidate.isEmpty() && (candidate.equals(root) || candidate.equals(plain))) return true;
        }
        return false;
    }

    static String redactArguments(String commandLine, List<String> redactedRoots) {
        if (commandLine == null || !matchesRoot(commandLine, redactedRoots)) return commandLine;
        String value = commandLine.trim();
        if (value.startsWith("/")) value = value.substring(1);
        int space = firstWhitespace(value);
        String root = space < 0 ? value : value.substring(0, space);
        return "/" + root + (space < 0 ? "" : " [arguments redacted]");
    }

    static String redactAllArguments(String commandLine) {
        if (commandLine == null) return null;
        String value = commandLine.trim();
        if (value.isEmpty()) return value;
        if (value.startsWith("/")) value = value.substring(1);
        int space = firstWhitespace(value);
        String root = space < 0 ? value : value.substring(0, space);
        return "/" + root + (space < 0 ? "" : " [arguments redacted]");
    }

    /**
     * Makes untrusted command text safe to display inside a Discord embed.
     * Mentions are disabled at payload level too; escaping prevents visual spoofing
     * with Markdown and collapses control characters into a single line.
     */
    static String sanitizeForDiscord(String value) {
        if (value == null) return "(unknown)";
        StringBuilder safe = new StringBuilder(Math.min(value.length() + 16, 2048));
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isISOControl(c)) {
                if (Character.isWhitespace(c) && (safe.length() == 0 || safe.charAt(safe.length() - 1) != ' ')) {
                    safe.append(' ');
                }
                continue;
            }
            if ("\\*_~`|>#[]()@".indexOf(c) >= 0) safe.append('\\');
            safe.append(c);
        }
        return safe.toString().trim();
    }

    private static int firstWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) if (Character.isWhitespace(value.charAt(i))) return i;
        return -1;
    }
}
