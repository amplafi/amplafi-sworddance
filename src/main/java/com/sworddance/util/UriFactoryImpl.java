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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.*;
import static com.sworddance.util.ApplicationNullPointerException.notNull;

/**
 * @author patmoore
 *
 */
public class UriFactoryImpl {

    public static String getFilename(URI uri) {
        return uri == null ? "" : substringAfterLast(uri.getPath(), "/");
    }

    public static URI createUriWithSpecificSchema(String uriStr, String forcedSchema) {
        Matcher matcher = stripProtocol.matcher(uriStr);
        String baseString;
        if ( matcher.find()) {
            baseString = matcher.group(1);
        } else {
            baseString = uriStr;
        }
        return createUri(forcedSchema+"://"+baseString);
    }

    public static URI createUriWithOptions(String uriStr, boolean schemaRequired, boolean pathRequired) {
        URI uri = createUri(uriStr);
        if (uri != null) {
            String newUriStr = uriStr;
            String path = uri.getRawPath();
            if (pathRequired && isEmpty(path)) {
                String rawQuery = uri.getRawQuery();
                newUriStr = newUriStr + "/" + (rawQuery == null ? "" : rawQuery);
            }
            if (schemaRequired && isEmpty(uri.getScheme())) {
                newUriStr = "http://" + newUriStr;
            }
            //noinspection StringEquality
            if (uriStr != newUriStr) {
                uri = createUri(newUriStr);
            }
        }
        return uri;
    }

    /**
     * Creates uri and ensures path is not empty. 'www.amplafi.com' -> 'www.amplafi.com/'
     *
     * @param uriStr input uri string
     * @return converted URI
     */
    public static URI createUriWithPath(String uriStr) {
        return createUriWithOptions(uriStr, false, true);
    }

    /**
     * Purpose of this method is to process redirect case. A lot of sites are NOT compatible to HTTP RFCs. It means
     * that they may specify redirect in wrong way. For correct way refer to http://www.ietf.org/rfc/rfc2616.txt 14.30.
     *
     * Let's say we received redirect directive from external server after requested http://www.w3.org/Whois page.
     * Below are the variants we may've received and the ways we will treat them:
     * <code>
     * <ol>
     * <li> "http://www.w3.org/pub/WWW/People.html" - correct redirect, no questions
     * <li> "/pub/WWW/People.html" - resolve to http://www.w3.org/pub/WWW/People.html
     * <li> "pub/WWW/People.html" - resolve to http://www.w3.org/pub/WWW/People.html
     * <li> "" - resolve to http://www.w3.org/
     * </ol>
     * </code>
     *
     * Please add the cases if you found anything else we may suffer from on redirect matter.
     *
     * One more note. Why this couldn't be done in a regular method {@link #createUriWithSchemaAndPath(String)} ,
     * why we need separate one. Regular one deals with user input with no URI context built in. Redirect case
     * ALWAYS has context - the page it was redirected from. User meaning of something.page is (try it in browser!)
     * http://something.page. Redirect meaning of the same is most likely http://theHostRedirectCameFrom/something.page.
     *
     * @param redirectTo string saying where we should be redirected
     * @param hostRedirectedFrom host we received redirect from
     * @return resolved URI
     */
    public static URI createUriForRedirect(String redirectTo, String hostRedirectedFrom){
        // empty redirect. Redirect to the main page.
        if(isEmpty(redirectTo)){
            return createUriWithPath(hostRedirectedFrom);
        }

        URI idealCaseURI = createUriWithPath(redirectTo);
        if(idealCaseURI.getScheme() != null && idealCaseURI.getHost() != null && idealCaseURI.getPath() != null){
            // that's it. Thanks our remote server for following RFC!
            return idealCaseURI;
        }

        // if we're failed with ideal case - let's try other ways
        notNull(hostRedirectedFrom, "Host we came from shouldn't be null");
        if(!redirectTo.startsWith("/")){
            redirectTo = "/" + redirectTo;
        }
        return createUriWithSchema(hostRedirectedFrom + redirectTo);
    }

    /**
     * Creates URI object from String and verifies that scheme is not null. For null schemes it
     * constructs new URI with default "http" scheme. If path requested is null (amplafi.com) it will append default
     * http path: http://amplafi.com/
     *
     * @param uriStr string to compose URI object from
     * @return {@link java.net.URI} instance produced from the string passed.
     * @throws com.sworddance.util.ApplicationIllegalStateException if URI is not parsable.
     */
    public static URI createUriWithSchemaAndPath(String uriStr) {
        return createUriWithOptions(uriStr, true, true);
    }

    /**
     * Odd comment : can't resolve because this is also used for "mailto:"
     *
     * @param uriStr
     * @return uri
     */
    public static URI createUri(Object uriStr) {
        URI uri;
        if (uriStr == null) {
            return null;
        }
        if (uriStr instanceof URI) {
            uri = (URI) uriStr;
        } else {
            String uriString = uriStr.toString().trim();
            if (isNotBlank(uriString)) {
                uri = URI.create(uriString);
            } else {
                return null;
            }
        }
        return uri;
    }

    /**
     * pattern to strip off all the protocol from a URI. Useful for forcing the URI protocol to a specific protocol.
     */
    public static final Pattern stripProtocol = Pattern.compile("^\\p{Alpha}+://(.+)$");

    /**
     * Creates URI object from String and verifies that scheme is not null. For null schemes it
     * constructs new URI with default "http" scheme.
     *
     * @param uriStr string to compose URI object from
     * @return {@link java.net.URI} instance produced from the string passed.
     * @throws com.sworddance.util.ApplicationIllegalStateException if URI is not parsable.
     */
    public static URI createUriWithSchema(String uriStr) {
        return createUriWithOptions(uriStr, true, false);
    }

    /**
     * get the domain portion of the uri. For example, "http://www.goo.com/index.html" would return
     * "goo.com"
     *
     * @param uri
     * @return the domain
     */
    public static String getDomain(URI uri) {
        String domain = null;
        if (uri != null) {
            domain = uri.getHost();
            if (isNotBlank(domain)) {
                String[] parts = domain.split("\\.");
                if (parts.length > 2) {
                    return parts[parts.length - 2] + "." + parts[parts.length - 1];
                }
            }
        }
        return domain;
    }

}
