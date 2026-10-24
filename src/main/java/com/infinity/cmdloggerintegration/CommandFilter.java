package com.infinity.cmdloggerintegration;
import java.util.List;
import java.util.Locale;
final class CommandFilter {
 private CommandFilter() { }
 static boolean shouldIgnore(String line, List<String> ignored) {
  if (line == null || ignored == null || ignored.isEmpty()) return false;
  String value = line.trim().toLowerCase(Locale.ROOT);
  if (value.startsWith("/")) value = value.substring(1);
  int space = value.indexOf(' ');
  String root = space < 0 ? value : value.substring(0, space);
  int colon = root.indexOf(':');
  String plain = colon < 0 ? root : root.substring(colon + 1);
  for (String item : ignored) {
   if (item == null) continue;
   String candidate = item.trim().toLowerCase(Locale.ROOT);
   while (candidate.startsWith("/")) candidate = candidate.substring(1);
   if (!candidate.isEmpty() && (candidate.equals(root) || candidate.equals(plain))) return true;
  }
  return false;
 }
}
