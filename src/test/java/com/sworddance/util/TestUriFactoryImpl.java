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
        assertEquals(uri.toString(), "http://foo.com/path/index.html");
        uri = createUriWithSchema("/path/index.html");
        assertEquals(uri.toString(), "/path/index.html");
    }
}
