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

import static com.sworddance.util.ApplicationNullPointerException.notNull;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.left;
import static org.apache.commons.lang.StringUtils.substringAfterLast;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;

/**
 * @author patmoore
 *
 * TODO: Most of these methods should be rolled into a UriSourceImplementor.
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

    public static URI createUriWithOptions(Object uriStr, boolean schemaRequired, boolean pathRequired) {
        URI uri = createUri(uriStr);
        if (uri != null && uriStr != null) {
            String newUriStr = uriStr.toString();
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
    public static URI createUriWithPath(Object uriStr) {
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
     * One more note. Why this couldn't be done in a regular method {@link #createUriWithSchemaAndPath(Object)} ,
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
     * @param uriStr object to compose URI object from
     * @return {@link java.net.URI} instance produced from the string passed.
     * @throws com.sworddance.util.ApplicationIllegalStateException if URI is not parsable.
     */
    public static URI createUriWithSchemaAndPath(Object uriStr) {
        return createUriWithOptions(uriStr, true, true);
    }

    /**
     *  Odd comment : can't resolve because this is also used for "mailto:" </br>
     *
     *  This method creates the {@link URI} from the given argument.
     *
     *  The default behaviour of this method is changed on (25-Feb-2010), now
     *  create URI encodes the URI. No thorough testing has been done for existing
     *  callers of this method. In case you face any issues with existing callers
     *  of this method and you want to disable encoding,
     *  please call {@link UriFactoryImpl#createUri(Object, boolean)}
     *
     * @param uriStr {@link Object} from which {@link URI} has to be created
     * @return uri, percent encoded {@link URI}
     */
    public static URI createUri(Object uriStr) {
        return createUri(uriStr, true);
    }

    /**
     * This method creates the {@link URI} from the given argument.
     *
     * @param uriStr {@link Object} from which {@link URI} has to be created
     * @param forceEncoding true if URI has to be encoded
     * @return {@link URI}
     */
    public static URI createUri(Object uriStr, boolean forceEncoding) {
        URI uri;
        if (uriStr == null) {
            return null;
        }
        if (uriStr instanceof URI) {
            uri = (URI) uriStr;
            // TODO handle UriSource
        } else {
            String uriString = uriStr.toString().trim();
            if (isNotBlank(uriString)) {
                try {
                    if (forceEncoding) {
                        uri = new URI(percentEncoding(uriString));
                    } else {
                        uri = new URI(uriString);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
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
    public static URI createUriWithSchema(Object uriStr) {
        return createUriWithOptions(uriStr, true, false);
    }
    public static URI createUriWithSchema(Object uriStr, Object path) {

        URI baseUri = createUriWithOptions(uriStr, true, false);
        if ( path == null) {
            return baseUri;
        } else {
            return baseUri.resolve(path.toString());
        }
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

    /**
     * TODO need to figure out what parts of WebLocationImpl belong here.
     * @param uri
     * @return uri
     */
    public static URI getNormalizedUri(URI uri) {
        try {
            String path = uri.getPath();

            // see WebLocationImpl ( this implementation here may not be correct )
            return new URI(uri.getScheme(), uri.getHost(), path==null?"/":"/"+ path, uri.getQuery());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @param originalUri
     * @param uri
     * @return true if the uri's have the same host / domain. Always returns false if either uri has a "mailTo" scheme.
     */
    public static boolean isSameHost(URI originalUri, URI uri) {
        if ( isWebUri(originalUri) || isWebUri(uri)) {
            return false;
        } else {
            String originalHost = originalUri.getHost();
            String host = uri.getHost();
            return equalsIgnoreCase(originalHost, host);
        }
    }

    /**
     * @param uri
     * @return true if uri is relative ( so scheme not supplied ), or http/https protocol.
     */
    public static boolean isWebUri(URI uri) {
        if ( uri == null ) {
            return false;
        } else {
            String scheme = uri.getScheme();
            return !uri.isAbsolute() || "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        }
    }
    public static boolean isNonLocalUri(URI uri) {
        if ( uri == null ) {
            return false;
        } else {
            String host = uri.getHost();
            if ( isBlank(host) || !uri.isAbsolute() ) {
                return false;
            } else {
                // top-level domains ( .info, .com, .org, etc )  are at most 4 characters long + 1 for the dot.
                // so checking for a '.' in the last 5 characters is a reasonable quick test to make sure the domain is
                // a real domain.
                int dotPos = host.substring(Math.max(host.length()-5, 0)).indexOf('.');
                return dotPos >=0;
            }
        }
    }
    /**
     * Chops uri to the max length supplied if it is longer.
     *
     * @param uri          URI object to chop
     * @param maxUriLength max number of characters acceptable
     * @return corrected or initial URI object
     */
    public static URI chopUri(URI uri, int maxUriLength) {
        String initialURIStr = uri.toString();
        String cutURIStr = left(initialURIStr, maxUriLength);
        if (!cutURIStr.equals(initialURIStr)) {
            uri = createUri(cutURIStr);
        }
        return uri;
    }

    /**
     * convert uri.getQuery() into a map.
     * @param uri
     * @return map
     */
    public static Map<String, String> getQueryMap(URI uri) {
        String queryStr = uri.getQuery();
        Map<String, String> queryParametersMap = new HashMap<String, String>();
        if ( isNotBlank(queryStr)) {
            String[] queryParameters = queryStr.split("&");
            for(String queryParameterStr: queryParameters) {
                String[] queryParameter = queryParameterStr.split("=");

                queryParametersMap.put(queryParameter[0], queryParameter.length == 1?"": queryParameter[1]);
            }
        }
        return queryParametersMap;
    }

    public static String createQueryString(Map<String,String> map) {
        int i=0;
        StringBuilder uriBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            String keyStr = UriFactoryImpl.percentEncoding(entry.getKey());
            String valueStr = UriFactoryImpl.percentEncoding(entry.getValue());
            uriBuilder.append(keyStr).append("=").append(valueStr);
            if ( ++i < map.size()) {
                uriBuilder.append("&");
            }
        }
        return uriBuilder.toString();
    }

    /**
     * Sorted list of characters which can be used without percent encoding in the URI.
     * Please also note that all the alphanumerics can also be used without percent
     * encoding in the URI
     * {@linkplain "http://www.ietf.org/rfc/rfc1738.txt"} (section 2.2)
     */
    private static final char[] safe = new char[] { '!', '$', '\'', '(', ')', '*', '+', ',', '-', '.', '_' };

    /**
     * Sorted list of characters which are reserved in a URI for a special meaning.
     * These characters should be percent encoded only if they are used for a meaning
     * other than their special meaning in URI. When reserved characters are used for
     * special meaning in URI they must not be encoded.
     *
     * {@linkplain "http://www.ietf.org/rfc/rfc1738.txt"} (section 2.2)
     */
    private static final char[] reserved = new char[] { '&', '/', ':', ';', '=', '?', '@'};

    /**
     * This method encodes URI string into a percent encoded string. If a URI string
     * or part of it is already percent encoded, that part of URI string is skipped.
     * This method is implemented as per specifications in RFC 1738 (section 2).
     *
     * I also had a look at <br/>
     * {@link URLEncoder} does not meet out requirements <br/>
     * {@link URI} also does not meet our requirements <br/>
     *
     *
     *  TODO There is one issue in this implementation, RFC 1738 section 2.2,
     *  states that a reserved character in a URI must be encoded if it is used
     *  for a purpose other than its reserved purpose. (For example :- If a
     *  reserved character is used in the name of file in a URI, then that reserved
     *  character must be encoded). As of now in the current implementation we are
     *  not encoding any reserved characters. For us it is difficult to determine
     *  whether a reserved character is used for its reserved purpose of some other
     *  purpose. </br>
     *
     *  One possible (but costly) solution to above limitation could be,
     *  to start encoding all the possible combinations of reserved characters
     *  in the URI one at a time and see if URI starts working.
     *
     * @param input URI string which needs to be percent encoded
     * @return percent encoded string
     * {@linkplain "http://java.sun.com/javaee/6/docs/api/javax/ws/rs/core/UriBuilder.html"},
     * probably URIBuilder does the same work we want to do below. But no of
     * dependencies(maven) on URI are huge. So it does not look worth the effort
     * to use URIBuilder
     * {@linkplain "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars"}
     * {@linkplain "http://www.ietf.org/rfc/rfc1738.txt"} (section 2.2)

     */
    public static String percentEncoding(String input){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '%') {
                boolean inputHasAtleastTwoMoreChars = i + 2 < input.length();
                if (inputHasAtleastTwoMoreChars) {
                    String twoCharsAfterPercent = input.substring(i + 1, i + 3);
                    if (isHexAfterPercent(twoCharsAfterPercent)) {
                        i = i + 2;
                        sb.append("%").append(twoCharsAfterPercent);
                        continue;
                    }
                }
            }
            boolean isLetterOrDigit = Character.isLetterOrDigit(c);
            boolean isSafe = Arrays.binarySearch(safe, c)>= 0;
            boolean isReserved = Arrays.binarySearch(reserved, c) >= 0;
            if(isLetterOrDigit || isSafe || isReserved){
                sb.append(c);
            } else if(c <= 0x007F) { // convert all other ASCII to percent encoding
                sb.append("%").append(Integer.toHexString(c));
            } else if (c <= 0x07FF) {      // non-ASCII <= 0x7FF (UTF 2 byte)
                sb.append("%").append(Integer.toHexString(0xc0 | (c >> 6)));
                sb.append("%").append(Integer.toHexString(0x80 | (c & 0x3F)));
           } else {                  // 0x7FF < ch <= 0xFFFF (UTF 3 bytes)
               sb.append("%").append(Integer.toHexString(0xe0 | (c >> 12)));
               sb.append("%").append(Integer.toHexString(0x80 | ((c >> 6) & 0x3F)));
               sb.append("%").append(Integer.toHexString(0x80 | (c & 0x3F)));
           }
        }
        return sb.toString();
    }

    private static boolean isHexAfterPercent(String twoCharsAfterPercent) {
        boolean isHexAfterPercent = true;
        try {
            Integer.valueOf(twoCharsAfterPercent, 16);
        } catch (NumberFormatException nfe) {
            isHexAfterPercent = false;
        }
        return isHexAfterPercent;
    }
    /**
     *
     * @param baseUri
     * @param relativeUriString may be an absolute uri when converted to a URI
     * @return absolute Uri
     */
    public static String absolutize(URI baseUri, String relativeUriString) {
        URI relativeUri = UriFactoryImpl.createUri(relativeUriString);
        if ( relativeUri != null && relativeUri.isAbsolute()) {
            return relativeUri.toString();
        }

        notNull(baseUri, "uri should not be null");
        if (relativeUriString == null) {
            ApplicationIllegalArgumentException.valid(baseUri.isAbsolute(),baseUri,": uri must be absolute because there is no relativeUriString.");
            return baseUri.resolve(".").toString();
        }
        return baseUri.resolve(relativeUri).toString();
    }
    /**
     * Converts the given href value to an absolute uri.
     * The html document's baseHtmlElement and
     * baseUri are used in order to derive the
     * correct location of the resource.
     *
     * @param href accepts null
     * @param baseUri
     * @param baseHtmlElement
     * @return the URI
     *
     * @throws IllegalArgumentException if the uri resolution encounters
     * {@link URI#create(String) problems}.
     */
    public static URI absolutize(String href, URI baseUri, String baseHtmlElement) {
        if (href==null) {
            href="";
        }
        URI uri = createUri(href);
        if (uri != null && uri.isAbsolute()) {
            return uri;
        }

        String start = absolutize(baseUri, baseHtmlElement);
        if (!start.endsWith("/")) {
            start += "/";
        }
        URI startUri = createUri(start);
        if ( uri != null ) {
            return startUri.resolve(uri);
        } else {
            return startUri;
        }
    }

    /**
     * @param root
     * @param filePath
     * @return {@link #resolveWithDefaultFile(Object, Object, String)} - using "index.html" as the defaultFileName
     */
    public static URI resolve(Object root, Object filePath) {
        return resolveWithDefaultFile(root, filePath, "index.html");
    }
    /**
     * @param root
     * @param filePath
     * @return resolvedUri
     */
    public static URI resolveWithDefaultFile(Object root, Object filePath, String defaultFileName) {
        URI rootUri = createUriWithSchemaAndPath(root);
        ApplicationNullPointerException.notNull(rootUri, root);

        String filePathStr = ObjectUtils.toString(filePath);
        URI uri;
        if ( isNotBlank(filePathStr)) {
            uri = rootUri.resolve("./"+filePathStr);
        } else {
            uri = rootUri;
        }
        if ( uri.toString().endsWith("/")) {
            uri = uri.resolve("./"+defaultFileName);
        }
        return uri;
    }
}
