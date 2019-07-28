
package chatty;

import chatty.util.StringUtil;
import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tduva
 */
public class HelperTest {
    
    @Test
    public void validateChannelTest() {
        assertTrue(Helper.isValidChannel("joshimuz"));
        assertTrue(Helper.isValidChannel("51nn3r"));
        assertTrue(Helper.isValidChannel("#joshimuz"));
        assertFalse(Helper.isValidChannel("##joshimuz"));
        assertFalse(Helper.isValidChannel(""));
        assertFalse(Helper.isValidChannel(" "));
        assertFalse(Helper.isValidChannel("abc$"));
        
        assertTrue(Helper.isValidChannelStrict("#joshimuz"));
        assertFalse(Helper.isValidChannelStrict("joshimuz"));
        
        assertTrue(Helper.isRegularChannel("#joshimuz"));
        assertTrue(Helper.isRegularChannel("joshimuz"));
        assertFalse(Helper.isRegularChannel(""));
        assertFalse(Helper.isRegularChannel("#chatrooms:1234:abc-def"));
        assertFalse(Helper.isRegularChannel("chatrooms:"));
        
        assertTrue(Helper.isRegularChannelStrict("#joshimuz"));
        assertTrue(Helper.isRegularChannelStrict("#underscore_"));
        assertFalse(Helper.isRegularChannelStrict("joshimuz"));
        assertFalse(Helper.isRegularChannelStrict("#chatrooms:1234:abc-def"));
        
        assertTrue(Helper.isValidStream("joshimuz"));
        assertTrue(Helper.isValidStream("51nn3r"));
        assertFalse(Helper.isValidStream("chatrooms:1234:abc-def"));
        assertFalse(Helper.isValidStream(null));
        assertFalse(Helper.isValidStream(""));
        assertFalse(Helper.isValidStream(" "));
        
        assertEquals(Helper.toStream("#channel"), "channel");
        assertEquals(Helper.toStream(""), "");
        assertEquals(Helper.toStream("#chatrooms:1234:abc-def"), "chatrooms:1234:abc-def");
        
        assertEquals(Helper.toValidStream("#channel"), "channel");
        assertEquals(Helper.toValidStream(""), null);
        assertEquals(Helper.toValidStream("#chatrooms:1234:abc-def"), null);
        
        assertEquals(Helper.toValidChannel("#channel"), "#channel");
        assertEquals(Helper.toValidChannel("channel"), "#channel");
        assertEquals(Helper.toValidChannel("$channel"), null);
        assertEquals(Helper.toValidChannel("chatrooms:1234:abc-def"), "#chatrooms:1234:abc-def");
        assertEquals(Helper.toValidChannel("abc"), "#abc");
        assertNull(Helper.toValidChannel(""));
        assertNull(Helper.toValidChannel("#"));
        assertNull(Helper.toValidChannel(" 1"));
        assertEquals(Helper.toValidChannel("#abc"), "#abc");
    }
    
    @Test
    public void removeDuplicateWhitespaceTest() {
        assertEquals(StringUtil.removeDuplicateWhitespace(" ")," ");
        assertEquals(StringUtil.removeDuplicateWhitespace(""), "");
        assertEquals(StringUtil.removeDuplicateWhitespace("abc"),"abc");
        assertEquals(StringUtil.removeDuplicateWhitespace("a  b"), "a b");
        assertEquals(StringUtil.removeDuplicateWhitespace("       "), " ");
        assertEquals(StringUtil.removeDuplicateWhitespace(" a  b  "), " a b ");
    }
    
    @Test
    public void htmlspecialchars_encodeTest() {
        assertEquals(Helper.htmlspecialchars_encode("&"), "&amp;");
        assertEquals(Helper.htmlspecialchars_encode("&amp;"), "&amp;amp;");
        assertEquals(Helper.htmlspecialchars_encode("hello john & everyone else"), "hello john &amp; everyone else");
        assertEquals(Helper.htmlspecialchars_encode("<"), "&lt;");
        assertEquals(Helper.htmlspecialchars_encode(">"), "&gt;");
        assertEquals(Helper.htmlspecialchars_encode("\""), "&quot;");
        assertEquals(Helper.htmlspecialchars_encode("& >"), "&amp; &gt;");
    }
    
    @Test
    public void htmlspecialchars_decodeTest() {
        assertEquals(Helper.htmlspecialchars_decode("&amp;"), "&");
        assertEquals(Helper.htmlspecialchars_decode("&quot;"), "\"");
        assertEquals(Helper.htmlspecialchars_decode("&lt;"), "<");
        assertEquals(Helper.htmlspecialchars_decode("&gt;"), ">");
        assertEquals(Helper.htmlspecialchars_decode("abc &amp; test"), "abc & test");
    }
    
    @Test
    public void tagsvalue_decodeTest() {
        assertEquals(Helper.tagsvalue_decode("\\s"), " ");
        assertEquals(Helper.tagsvalue_decode("\\:"), ";");
        assertEquals(Helper.tagsvalue_decode("\\n"), "\n");
        assertEquals(Helper.tagsvalue_decode("\\\\s"), "\\s");
        assertEquals(Helper.tagsvalue_decode("abc\\stest"), "abc test");
        assertEquals(Helper.tagsvalue_decode(""), "");
        assertEquals(Helper.tagsvalue_decode(null), null);
        assertEquals(Helper.tagsvalue_decode(" "), " ");
        assertEquals(Helper.tagsvalue_decode("\\\\s\\s\\:"), "\\s ;");
    }
    
    @Test
    public void testUrls() {
        
        //---------------
        // Just matching
        //---------------
        String[] shouldMatch = new String[]{
            "twitch.tv",
            "Twitch.TV",
            "twitch.tv/abc",
            "twitch.tv/ABC",
            "http://twitch.tv",
            "https://twitch.tv",
            "amazon.de/ääh",
            "https://google.de",
            "google.com/abc(blah)",
            "http://börse.de",
            "www.twitch.tv/",
            "twitch.tv🐁"
        };
        
        /**
         * Trying to match the full string, so it's not necessary to check the
         * matches. Ofc it would find an URL in most of those.
         */
        String[] shouldNotMatch = new String[]{
            "twitch.tv.",
            "twitch.tv ",
            " twitch.tv",
            " twitch.tv ",
            "https:/twitch.tv",
            "https://twitch.tv:",
            "twitch.tv:",
            "twitch.tv,",
            "www.twitch.tv,",
            "www.twitch.tv/,",
            "asdas.abc",
            "http://",
            "http://www.",
            "https://",
            "https://www."
        };
        
        for (String test : shouldMatch) {
            assertTrue("Should match: "+test, Helper.getUrlPattern().matcher(test).matches());
        }
        
        for (String test : shouldNotMatch) {
            assertFalse("Should not match: "+test, Helper.getUrlPattern().matcher(test).matches());
        }
        
        //-------------------
        // Finding in String
        //-------------------
        String find = "Abc http://example.com http://example.com/abc(test) dumdidum(http://example.com) [http://example.com] "
                + "(http://example.com)[github.io] [github.io] (github.io) 🐣twitch.tv🐣 https:// www. not.a/domain "
                + "twitch.tv/🐁 twitch.tv github.io $ chatty.github.io Other text and whatnot https://chatty.github.io";
        String[] findTarget = new String[]{
            "http://example.com",
            "http://example.com/abc(test)",
            "http://example.com)",
            "http://example.com]",
            "http://example.com)[github.io]",
            "github.io]",
            "github.io)",
            "twitch.tv🐣",
            "twitch.tv/🐁",
            "twitch.tv",
            "github.io",
            "chatty.github.io",
            "https://chatty.github.io"
        };

        Matcher m = Helper.getUrlPattern().matcher(find);
        int i = 0;
        while (m.find()) {
            String found = m.group();
            String target = findTarget[i];
            assertTrue("Should have found: "+target+" found: "+found,
                    found.equals(target));
            i++;
        }
    }
    
    @Test
    public void removeEmojiVariationSelectorTest() {
        assertEquals(Helper.removeEmojiVariationSelector(null), null);
        assertEquals(Helper.removeEmojiVariationSelector("❤︎"), "❤");
        assertEquals(Helper.removeEmojiVariationSelector("❤︎︎"), "❤");
        assertEquals(Helper.removeEmojiVariationSelector("\uFE0F❤︎︎"), "❤");
        assertEquals(Helper.removeEmojiVariationSelector("\uFE0F\uFE0E"), "");
        assertEquals(Helper.removeEmojiVariationSelector("❤️ 	❤ 	❤︎"), "❤ 	❤ 	❤");
    }
}
