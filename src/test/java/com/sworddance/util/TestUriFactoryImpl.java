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

import org.testng.annotations.Test;

import static com.sworddance.util.UriFactoryImpl.*;
import static org.testng.Assert.*;

/**
 * @author patmoore
 *
 */
public class TestUriFactoryImpl {
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
        URI uri0 = createUri(uriStr);
        String encodedUriString = uri0.toString();
        assertEquals(encodedUriString, "http://localhost:8080/Application+for+scholarship%25%2B.pdf");
        URI uri1 = createUri(encodedUriString);
        assertEquals(uri1.toString(), "http://localhost:8080/Application+for+scholarship%25%2B.pdf");
    }
}
