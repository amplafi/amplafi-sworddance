/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.sworddance.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sworddance.util.UriFactoryImpl.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestUriFactoryImpl {
    /**
     *
     */
    private static final String NEW_TOP = "new_top";
    /**
     *
     */
    private static final String REMOVE_FIRST_SLASH = "remove/first/slash";

    @Test
    public void testCreateUriForRedirect(){
        String host = "http://test.com";
        URI redirect = createUriForRedirect(null, host);
        assertEquals(redirect.toString(), "http://test.com/");

        redirect = createUriForRedirect("", host);
        assertEquals(redirect.toString(), "http://test.com/");

        redirect = createUriForRedirect("/", host);
        assertEquals(redirect.toString(), "http://test.com/");

        redirect = createUriForRedirect("/page.1", host);
        assertEquals(redirect.toString(), "http://test.com/page.1");

        redirect = createUriForRedirect("page", host);
        assertEquals(redirect.toString(), "http://test.com/page");

        redirect = createUriForRedirect("page/", host);
        assertEquals(redirect.toString(), "http://test.com/page/");
    }
    /**
     * Test to make sure a bad uri ( with spaces ) is encoded correctly AND make sure a valid URI is not reencoded!
     */
    @Test
    public void testCreateUriCycle() {
        String uriStr = "http://localhost:8080/Application for scholarship%+.pdf";
        URI uri0 = createUri(uriStr,true);
        String encodedUriString = uri0.toString();
        assertEquals(encodedUriString, "http://localhost:8080/Application%20for%20scholarship%25+.pdf");
        URI uri1 = createUri(encodedUriString, true);
        assertEquals(uri1.toString(), "http://localhost:8080/Application%20for%20scholarship%25+.pdf");
    }

    /**
     * Test to make sure reserved characters are not encoded.
     */
    @Test
    public void testPercentEncoding() {
        String uriStr = "http://localhost:8080/abc.pdf";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc.pdf");

        uriStr = "http://localhost:8080/abc%";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc%25");

        uriStr = "http://localhost:8080/abc%1T";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc%251T");

        uriStr = "http://localhost:8080/abc%251T";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc%251T");

        uriStr = "http://localhost:8080/abc!$'()*+,-._";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc!$'()*+,-._");

        uriStr = "http://localhost:8080/abc ";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc");
        uriStr = "http://localhost:8080/abc c";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc%20c");

        uriStr = "http://localhost:8080/abc#";
        assertEquals(createUri(uriStr,true).toString(), "http://localhost:8080/abc#");

    }

    @Test
    public void testQueryMap() throws Exception {
        URI uri = createUri("http://yuattorney.com/path/file.html?q=q_value&r=r_value&s=s_value&t=t_value");
        assertEquals(uri.toString(), "http://yuattorney.com/path/file.html?q=q_value&r=r_value&s=s_value&t=t_value");
        assertEquals(uri.getPath(), "/path/file.html");
        assertEquals(uri.getQuery(), "q=q_value&r=r_value&s=s_value&t=t_value");
        Map<String, String> qmap = getQueryMap(uri);
        assertEquals(qmap.get("q"), "q_value");
        assertEquals(qmap.get("r"), "r_value");
        assertEquals(qmap.get("s"), "s_value");
        assertEquals(qmap.get("t"), "t_value");
    }
    @Test(dataProvider="queryMapUris")
    public void testQueryMap(URI uri) {
        Map<String, String> queryMapUri = getQueryMap(uri);

    }
    @DataProvider(name="queryMapUris")
    protected Object[][] getQueryMapUris() throws URISyntaxException {
        return new Object[][] {
            new Object[] { new URI("/foo?_jquery") },
            new Object[] { new URI("/?&") },
            new Object[] { new URI("http://example.com?_") },
            new Object[] { new URI("?_=1&__=")}
        };
    }
    @Test
    public void testUriResolutionWithBase() throws Exception {
        URI uri = new URI("http://amplafi.net/us/msg");

        assertEquals(absolutize("test.html",uri,"msg").toString(), "http://amplafi.net/us/msg/test.html");
        assertEquals(absolutize("local events.html",uri,"msg").toString(), "http://amplafi.net/us/msg/local%20events.html");
        assertEquals(absolutize("http://amplafi.com",uri,"msg").toString(), "http://amplafi.com");
        assertEquals(absolutize("http://amplafi.com/",uri,"msg").toString(), "http://amplafi.com/");
        assertEquals(absolutize("javascript:alert();",uri,"msg").toString(), "javascript:alert();");
        assertEquals(absolutize(null,uri,"msg").toString(), "http://amplafi.net/us/msg/");
    }

    @Test
    public void testUriResolutionWithoutBase() throws Exception {
        URI uri = new URI("http://amplafi.net/us/msg");

        assertEquals(absolutize("test.html", uri, null).toString(), "http://amplafi.net/us/test.html");
        assertEquals(absolutize("http://amplafi.com", uri, null).toString(), "http://amplafi.com");
        assertEquals(absolutize("http://amplafi.com/", uri, null).toString(), "http://amplafi.com/");
        assertEquals(absolutize("javascript:alert();", uri, null).toString(), "javascript:alert();");
        assertEquals(absolutize(null, uri, null).toString(), "http://amplafi.net/us/");
    }

    @Test
    public void testResolve() throws Exception {
        URI uri = new URI("http://amplafi.net/us/msg");

        assertEquals(resolveWithDefaultFile(uri, "test.html").toString(), "http://amplafi.net/us/test.html");
        assertEquals(resolveWithDefaultFile(uri, "local events.html").toString(), "http://amplafi.net/us/local%20events.html");
    }

    @Test
    public void testSanitizePath() {
        String sanitized = sanitizePath("/" + REMOVE_FIRST_SLASH);
        assertEquals(sanitized, REMOVE_FIRST_SLASH);
        // check meaningless './'
        sanitized = sanitizePath("././././"+REMOVE_FIRST_SLASH);
        assertEquals(sanitized, REMOVE_FIRST_SLASH);
        // check for '../' legally embedded.
        sanitized = sanitizePath("top/middle/../.././" + NEW_TOP);
        assertEquals(sanitized, NEW_TOP);
        // check for '../' ILlegally embedded.
        sanitized = sanitizePath("../top/../middle/../.././" + NEW_TOP);
        assertEquals(sanitized, NEW_TOP);
    }

    /**
     * make sure that the relative uri's are not accidently converted absolute paths.
     */
    @Test
    public void testCreateUriScheme() {
        URI uri = createUriWithSchema("foo.com/path/index.html");
        assertEquals(uri.toString(), "//foo.com/path/index.html");
        uri = createUriWithSchema("/path/index.html");
        assertEquals(uri.toString(), "/path/index.html");
    }

    @Test
    public void testCreateUriParsing() {
        ExtendedMatchResult result = UriParser.URL.partsOnly("http://example.com/?test");
        assertNotNull(result);
    }
    @Test
    public void testCustomUriParsing() {
        UriParser uriParser = new UriParser(UriParser.HTTP_S_SCHEME_PATTERN_STR, "(?:.(?:jpe?g)|(?:gif))", "good $3");
        CharSequence inputString = "here is a http://example.com/image.jpg answer";
        CharSequence expected = "here is a good example.com answer";
        CharSequence result = uriParser.replace(inputString);
        String resultStr = result.toString();
        assertEquals(resultStr,expected);
    }
    @Test
    public void testCustomUriParsingWithEndingSlash() {
        UriParser uriParser = new UriParser(/*UriParser.HTTP_S_SCHEME_PATTERN_STR, null,*/ "<a href=\"$0\">$0</a>");
        CharSequence inputString = "here is a " +
        		"http://example.com/" +
        		" or maybe over here " +
        		"http://here.com" +
        		" as well.";
        CharSequence expected = "here is a " +
        "<a href=\""+
        "http://example.com/" +
        "\">" +
        "http://example.com/" +
        "</a>"+
        " or maybe over here " +
        "<a href=\""+
        "http://here.com" +
        "\">" +
        "http://here.com" +
        "</a>"+
        " as well.";
        CharSequence result = uriParser.replace(inputString);
        String resultStr = result.toString();
        assertEquals(resultStr,expected);
    }
    @Test
    public void testPattern() {
        String regexWithout ="([a-z0-9+.-]+):" +
        		"(?:" +
        		"//" +
        		"(?:((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?" +
        		"((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)" +
        		"(?::(\\d+))?" +
        		"(/(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?" +
        		"|" +
        		"(/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?" +
        		")"+
        		"(?:" +
        		"\\?((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)" +
        		")?" +
        		"(?:" +
        		"#((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)" +
        		")?";
        assertEquals(UriParser.REGEX_URI, regexWithout);
        String regexWithDelim ="(?:([a-z0-9+.-]+://)((?:(?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)(:(?:\\d+))?(/(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?|([a-z0-9+.-]+:)(/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?)(\\?(?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)?(#(?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)?";
        assertEquals(UriParser.REGEX_URI_DELIM, regexWithDelim);
        for(String regex: new String[] {regexWithout, regexWithDelim}) {
            Pattern pattern = Pattern.compile(regex);
            for(String input: new String[] { "http://example.com/", "http://example.com"}) {
                Matcher matcher = pattern.matcher(" "+input+" ");
                assertTrue(matcher.find());
                String group = matcher.group();
                assertEquals(group, input);
            }
        }
    }

    /**
     * Ensure that domains that are 3-deep like the uk domains are handled correctly
     * Ensure that 'www.' is properly stripped
     */
    @Test
    public void testDomainHandling() {
        assertEquals(getDomain(createUri("http://bbc.co.uk")),"bbc.co.uk");
        assertEquals(getDomain(createUri("http://www.farreach.es")),"farreach.es");
        assertEquals(getDomain(createUri("wwwbbc.co.uk")),"wwwbbc.co.uk");
        assertEquals(getDomain(createUri("www.bbc.co.uk")),"bbc.co.uk");
        assertEquals(getDomain(createUri("wWw.bbc.co.uk")),"bbc.co.uk");
        assertEquals(getDomain(createUri("wWw.BBc.Co.uk")),"bbc.co.uk");
        assertEquals(getDomain(createUri("www.uk")),"www.uk");
    }

    @Test(dataProvider="hostAddresses")
    public void testLocalUri(String uriStr, boolean remote) {
        assertEquals(isNonLocalUri(createUriWithOptions(uriStr, true, false)), remote);
    }

    @DataProvider(name="hostAddresses")
    public Object[][] getHostAddresses() {
        return new Object[][] {
            new Object[] { "//localhost", false },
            // internationalized domain names: http://en.wikipedia.org/wiki/Internationalized_country_code_top-level_domain
            new Object[] { "//xn--80ahbyhddgf2au1c.xn--p1ai/", true },
            new Object[] { "xn--80ahbyhddgf2au1c.xn--p1ai", true },
            new Object[] { "10.0.0.com", true },

            new Object[] { "0.0.0.0", false },
            new Object[] { "0.255.255.255", false },
            new Object[] { "1.0.0.0", true },

            new Object[] { "9.255.255.255", true },
            new Object[] { "10.0.0.0", false },
            new Object[] { "10.255.255.255", false },
            new Object[] { "11.0.0.0", true },

            new Object[] { "100.63.255.255", true },
            new Object[] { "100.64.0.0", false },
            new Object[] { "100.127.255.255", false },
            new Object[] { "100.128.0.0", true },

            new Object[] { "126.255.255.255", true },
            new Object[] { "127.0.0.1", false },
            new Object[] { "127.0.0.0", false },
            new Object[] { "127.255.255.255", false },
            new Object[] { "128.0.0.0", true },

            new Object[] { "169.253.255.255", true },
            new Object[] { "169.254.0.0", false },
            new Object[] { "169.254.255.255", false },
            new Object[] { "169.255.0.0", true },

            new Object[] { "172.15.255.255", true },
            new Object[] { "172.16.0.0", false },
            new Object[] { "172.31.255.255", false },
            new Object[] { "172.32.0.0", true },

            new Object[] { "191.255.255.255", true },
            new Object[] { "192.0.0.0", false },
            new Object[] { "192.0.0.7", false },
            new Object[] { "192.0.0.8", true },

            new Object[] { "191.0.1.255", true },
            new Object[] { "192.0.2.0", false },
            new Object[] { "192.0.2.255", false },
            new Object[] { "192.0.3.0", true },

            new Object[] { "192.88.98.255", true },
            new Object[] { "192.88.99.0", false },
            new Object[] { "192.88.99.255", false },
            new Object[] { "192.88.100.0", true },

            new Object[] { "192.167.255.255", true },
            new Object[] { "192.168.0.0", false },
            new Object[] { "192.168.255.255", false },
            new Object[] { "192.169.0.0", true },

            new Object[] { "198.17.255.255", true },
            new Object[] { "198.18.0.0", false },
            new Object[] { "198.19.255.255", false },
            new Object[] { "198.20.0.0", true },

            new Object[] { "198.51.99.255", true },
            new Object[] { "198.51.100.0", false },
            new Object[] { "198.51.100.255", false },
            new Object[] { "198.51.101.0", true },

            new Object[] { "203.0.112.255", true },
            new Object[] { "203.0.113.0", false },
            new Object[] { "203.0.113.255", false },
            new Object[] { "203.0.114.0", true },

            new Object[] { "223.0.255.255", true },
            new Object[] { "224.0.0.0", false },
            new Object[] { "255.255.255.255", false },
        };
    }
}
