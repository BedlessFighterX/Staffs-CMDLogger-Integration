package com.infinity.cmdloggerintegration;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
class CommandFilterTest {
 @Test void matchesCommandAndArguments() { assertTrue(CommandFilter.shouldIgnore("/plugins list", Collections.singletonList("plugins"))); }
 @Test void matchesCaseInsensitivelyAndNamespacedCommands() { assertTrue(CommandFilter.shouldIgnore("/minecraft:PLUGINS", Arrays.asList("plugins"))); }
 @Test void doesNotMatchPartialCommandNames() { assertFalse(CommandFilter.shouldIgnore("/pluginsomething", Collections.singletonList("plugins"))); }
 @Test void safelyHandlesMissingValues() { assertFalse(CommandFilter.shouldIgnore(null, Collections.singletonList("plugins"))); assertFalse(CommandFilter.shouldIgnore("/plugins", null)); }
}
