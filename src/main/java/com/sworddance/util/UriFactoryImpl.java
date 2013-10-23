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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.*;
import static com.sworddance.util.ApplicationNullPointerException.*;

/**
 * @author patmoore
 *
 * TODO: Most of these methods should be rolled into a UriSourceImplementor.
 * TODO: investigate google's URI checking code.
 *
 * TODO: Too simplistic wrt Ipv6 addresses. Roll together with UriTranslator/UriParser.
 *
 *
 */
public class UriFactoryImpl {

    public static final String HTTPS_SCHEME = "https";
    public static final String HTTP_SCHEME = "http";
    public static final String FTP_SCHEME = "ftp";
    public static final Pattern KNOWN_GOOD_SCHEMES = Pattern.compile("^("+HTTP_SCHEME+"|"+HTTPS_SCHEME+"|"+FTP_SCHEME+")$", Pattern.CASE_INSENSITIVE);
    public static final Pattern HTTP_SCHEMES =  Pattern.compile("^("+HTTP_SCHEME+"|"+HTTPS_SCHEME+")$", Pattern.CASE_INSENSITIVE);
    public static final String MAILTO_SCHEME = "mailto";


    public static final String MAILTO = "mailto:";
    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;
    /**
     *
     */
    private static final String PATH_SEPARATOR = "/";
    /**
     *
     */
    private static final String DEFAULT_FILENAME = "index.html";
    /**
     * pattern to strip off all the protocol from a URI. Useful for forcing the URI protocol to a specific protocol.
     */
    public static final Pattern stripProtocol = Pattern.compile("^\\p{Alpha}+://(.+)$");

    public static String getFilename(URI uri, String defaultFileName) {
        String fileName = null;
        if (  uri != null ) {
            fileName = substringAfterLast(uri.getPath(), PATH_SEPARATOR);
        }
        if ( isNotBlank(fileName)) {
            return fileName;
        } else {
            return defaultFileName;
        }
    }
    public static String getFilename(URI uri) {
        return getFilename(uri, DEFAULT_FILENAME);
    }
    /**
     * @param httpUri
     * @return root URI
     */
    public static URI getRootDirectory(URI httpUri) {
        String path = httpUri.getPath();
        URI base = httpUri.resolve(substringBeforeLast(path, PATH_SEPARATOR) + PATH_SEPARATOR);
        return base;
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
                newUriStr += PATH_SEPARATOR + (rawQuery == null ? "" : rawQuery);
            }
            if (schemaRequired && !uri.isAbsolute() && !newUriStr.startsWith(PATH_SEPARATOR)) {
                // TODO: check for a relative uri! will produce something like http:/httpdocs/demo if newUriStr does not have host information.
                newUriStr = HTTP_SCHEME+"://" + newUriStr;
            }
            //noinspection StringEquality
            if (uriStr != newUriStr) {
                uri = createUri(newUriStr);
            }
        }
        return uri;
    }

    /**
     * suitable for namespacing
     * @return the uri with relative schema ( ex. //facebook.com/path/to/stuff?query )
     */
    public static URI createNamespaceUri(Object uriStr) {
        URI temp = createUriWithOptions(uriStr, true, true);
        try {
            URI namesUri = new URI(null,
                temp.getUserInfo(), temp.getHost(), -1,
                temp.getPath(), temp.getQuery(), temp.getFragment());
            return namesUri;
        } catch (URISyntaxException e) {
            return null;
        }
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
        notNull(hostRedirectedFrom, "Host we came from shouldn't be null because redirectTo doesn't have host information. redirectTo=", redirectTo);
        if(!redirectTo.startsWith(PATH_SEPARATOR)){
            redirectTo = PATH_SEPARATOR + redirectTo;
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
     * TODO: need a createUri that checks result has {@link #isNonLocalUri(URI)} is true
     * @param uriStr {@link Object} from which {@link URI} has to be created
     * @return uri, percent encoded {@link URI}
     *
     */
    public static URI createUri(Object uriStr) {
        return createUri(uriStr, true);
    }

    /**
     * This method creates the {@link URI} from the given argument.
     *
     * TODO! Need to check for path information in the URI in particular use of ../ to try to game the urls on amplafi servers.
     *
     * @param uriStr {@link Object} from which {@link URI} has to be created. Allowed to be relative URI.
     * @param forceEncoding true if URI has to be encoded
     * @return {@link URI}
     */
    public static URI createUri(Object uriStr, boolean forceEncoding) {
        URI uri = null;
        if (uriStr == null || uriStr instanceof URI) {
            // TODO handle UriSource
            uri = (URI) uriStr;
        } else if ( uriStr instanceof URL ) {
            try {
                uri = ((URL)uriStr).toURI();
            } catch (URISyntaxException e) {
                // just ignore, uri may be coming from user input.
            }
        } else {
            String uriString = uriStr.toString().trim();
            if (forceEncoding) {
                uriString = percentEncoding(uriString);
            }
            if (isNotBlank(uriString)) {
                try {
                    // TODO: (see UriParser - normalize to
                    uri = new URI(uriString);
                } catch (URISyntaxException e) {
                    // just ignore, uri may be coming from user input.
                }
            }
        }
        return uri;
    }

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
            uri = createUriWithSchema(uri);
            // TODO: possible internationalization problem (Turkey's I and i )
            domain = uri.getHost().toLowerCase();
            // the domain splitting is to look out for some domain like "www.es" where the domain itself is 'www'
            String[] domainComponents = domain.split("\\.");
            if(domainComponents.length > 2 && domainComponents[0].equalsIgnoreCase("www")) {
                // exclude the 'www.' part
                domain = domain.substring(4);
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
            return new URI(uri.getScheme(), uri.getHost(), path==null?PATH_SEPARATOR:PATH_SEPARATOR+ path, uri.getQuery());
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
            return !uri.isAbsolute() || isHttpProtocol(uri);
        }
    }
    public static boolean hasQuestionableScheme(URI uri) {
        if ( uri.isAbsolute()) {
            String scheme = uri.getScheme();
            return !KNOWN_GOOD_SCHEMES.matcher(scheme).find();
        } else {
            return false;
        }
    }

    /**
     */
    private static final Pattern IPV4_ADDRESSES = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
    /**
     * NOT checking for !uri.{@link java.net.URI#isAbsolute()} because protocol-less uri ( '//example.com' ) is not absolute.
     * @param uri
     * @return false if uri == null or the uri.getHost() has no top-level-domain (no '.' in the last 8 characters )
     */
    public static boolean isNonLocalUri(URI uri) {
        if ( uri != null ) {
            String host = uri.getHost();
            if ( isNotBlank(host) ) {
                // top-level domains ( .info, .com, .org, etc )  are at most 4 characters long + 1 for the dot.
                // so checking for a '.' in the last 8 characters is a reasonable quick test to make sure the domain is
                // a real domain.
                // Above is not true: internationalized domain names (e.g. xn--80ahbyhddgf2au1c.xn--p1ai )
                int dotPos = host.lastIndexOf('.');
                if (dotPos >=0) {
                    // now look for ip address ranges that are declared local
                    Matcher matcher = IPV4_ADDRESSES.matcher(host);
                    if ( matcher.matches()) {
                        // ipv4 address
                        //  http://en.wikipedia.org/wiki/Reserved_IP_addresses
                        int first = Integer.parseInt(matcher.group(1));
                        int second = Integer.parseInt(matcher.group(2));
                        int third = Integer.parseInt(matcher.group(3));
                        int fourth =  Integer.parseInt(matcher.group(4));
                        if ( first >= 224 ) {
                            // 224.0.0.0 - 255.255.255.255 : various multicast and reserved blocks
                            return false;
                        } else if ( first < 0 || first > 255 || second < 0 || second > 255 || third < 0 || third > 255 || fourth < 0 || fourth > 255) {
                            // some out of bounds number for ip address : lets just declare it bad.
                            return false;
                        } else {
                            switch(first) {
                            case 0: // 0.0.0.0 – 0.255.255.255 : all reserved
                                return false;
                            case 10: // 10.0.0.0 – 10.255.255.255 : all reserved
                                return false;
                            case 100:
                                // 100.64.0.0 – 100.127.255.255
                                // communication between service providers
                                return second < 64 || second > 127;
                            case 127:// 127.0.0.0 – 127.255.255.255 : all reserved
                                return false;
                            case 169:
                                // 169.254.0.0 – 169.254.255.255
                                return second != 254;
                            case 172:
                                // 172.16.0.0 – 172.31.255.255
                                return second < 16 || second > 31;
                            case 192:
                                switch(second) {
                                case 0:
                                    if ( third == 0 ) {
                                        // 192.0.0.0 – 192.0.0.7
                                        return fourth > 7;
                                    } else {
                                        // 192.0.2.0 – 192.0.2.255
                                        return third != 2;
                                    }
                                case 88:
                                    // 192.88.99.0 – 192.88.99.255
                                    return third != 99;
                                case 168:
                                    // 192.168.0.0 – 192.168.255.255
                                    return false;
                                default:
                                    return true;
                                }
                            case 198:
                                switch(second) {
                                case 18:
                                case 19:
                                    // 198.18.0.0 – 198.19.255.255
                                    return false;
                                case 51:
                                    // 198.51.100.0 – 198.51.100.255
                                    return third != 100;
                                default:
                                    return true;
                                }
                            case 203:
                                // 203.0.113.0 – 203.0.113.255
                                return second !=0 || third != 113;
                            }
                            return true;
                        }
                    } else if (false/*test for ipv6 */) {

                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
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
                if (isNotBlank(queryParameterStr)) {
                    String[] queryParameter = queryParameterStr.split("=");
                    queryParametersMap.put(queryParameter[0], queryParameter.length < 2 ?"": queryParameter[1]);
                }
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
    private static final char[] safe;
    static {
        safe = new char[] { '!', '$', '\'', '(', ')', '*', '+', ',', '-', '.', '_' };
        Arrays.sort(safe);
    }

    /**
     * Sorted list of characters which are reserved in a URI for a special meaning.
     * These characters should be percent encoded only if they are used for a meaning
     * other than their special meaning in URI. When reserved characters are used for
     * special meaning in URI they must not be encoded.
     *
     * {@linkplain "http://www.ietf.org/rfc/rfc1738.txt"} (section 2.2)
     */
    private static final char[] reserved;
    static {
        reserved = new char[] { '&', '/', ':', ';', '=', '?', '@', '#'};
        Arrays.sort(reserved);
    }

    /**
     * This method encodes URI string into a percent encoded string. If a URI string
     * or part of it is already percent encoded, that part of URI string is skipped.
     * This method is implemented as per specifications in RFC 1738 (section 2).
     *
     * I also had a look at <br/>
     * {@link java.net.URLEncoder} does not meet out requirements <br/>
     * {@link java.net.URI} also does not meet our requirements <br/>
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
        if (!start.endsWith(PATH_SEPARATOR)) {
            start += PATH_SEPARATOR;
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
    public static URI resolveWithDefaultFile(Object root, Object filePath) {
        return resolveWithDefaultFile(root, filePath, DEFAULT_FILENAME);
    }
    /**
     * @param root
     * @param filePath
     * @param defaultFileName
     * @return resolvedUri
     */
    public static URI resolveWithDefaultFile(Object root, Object filePath, String defaultFileName) {
        URI rootUri = createUriWithSchemaAndPath(root);
        ApplicationNullPointerException.notNull(rootUri, root);

        String filePathStr = sanitizePath(filePath);
        URI uri;
        if ( isNotBlank(filePathStr)) {
            uri = rootUri.resolve("./"+percentEncoding(filePathStr));
        } else {
            uri = rootUri;
        }
        if ( uri.toString().endsWith(PATH_SEPARATOR) && isNotBlank(defaultFileName)) {
            uri = uri.resolve("."+PATH_SEPARATOR+percentEncoding(defaultFileName));
        }
        return uri;
    }
    /**
     * manually handle '..' path elements to ensure that the path cannot be used to access
     * a directory above its root.
     * '.' and blank elements are removed.
     *
     * '../foo/./index.html' becomes '/foo/index.html'
     * '/foo/../../index.html' becomes '/index.html'
     * 'foo////bar.html' becomes 'foo/bar.html'
     *
     * @param filePath
     * @return the sanitized path with no blank, '..' or '.' components.
     */
    public static String sanitizePath(Object filePath) {
        if ( filePath != null ) {
        	//Let's make sure that filePath is really just path and nothing more.
            // TO_KOSTYA PATM 19 July 2011 --
                // Please create comment as to why this necessary ( showing input )
        			//This was to prevent generation of links like following:
        				//http://amplafi.net/http:/amplafi.net/rss/ampbp_2/ampmep_6.xml
        			//Input.. I guess: http://amplafi.net/rss/ampbp_2/ampmep_6.xml

            // HACK we can't do this - because if we have any problems with uri path then we get an exception.
            // and then look at UriParser.
            // It looks like we should hook up UriParser soon rather than later.
            // Also Google guava has some uri validation code as well.
        		//        	URI create = URI.create(filePath.toString());
            String path = filePath.toString();//create.getRawPath();
            // chop up path based on \ or / characters.
            String[] pathParts = path.split("[/\\\\]");
            // and look for '..' and '.' and resolve the final path,
            // looking for attempts to navigate to the root directory of 'filePath'
            List<String> pathArr = new ArrayList<String>();
            int index = -1;
            for(String pathPart: pathParts) {
                pathPart = pathPart.trim();
                if ( pathPart.isEmpty() || ".".equals(pathPart)) {
                	// a '//' in the path or '/./'
                    continue;
                } else if ( "..".equals(pathPart)) {
                    if ( index >= 0 ) {
                        pathArr.remove(index--);
                    } else {
                    	// TODO: possible security violation
                    }
                } else {
                	// TODO: look for forbidden elements : "etc" "passwd" "shadow" a file name that starts with a "." like ".htaccess"
                    pathArr.add(pathPart);
                    index++;
                }
            }
            return join(pathArr, PATH_SEPARATOR);
        } else {
            return "";
        }
    }

    /**
     * Utility method to calculate port based on defaults for different schemas.
     *
     * @param uri uri to calculate port for
     * @return integer port presentation
     */
    public static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            String scheme = uri.getScheme();
            if (HTTPS_SCHEME.equalsIgnoreCase(scheme)) {
                port = DEFAULT_HTTPS_PORT;
            } else {
                port = DEFAULT_HTTP_PORT;
            }
        }
        return port;
    }

    public static boolean isHttpProtocol(URI uri) {
        if ( uri != null) {
            String scheme = uri.getScheme();
            if ( scheme != null) {
                return HTTP_SCHEMES.matcher(scheme).find();
            }
        }
        return false;
    }
	public static URI createUriWithQuery(URI uri, Map<String, String> parameters) {
		Map<String, String> queryMap = getQueryMap(uri);
		queryMap.putAll(parameters);
		String noQueryUriString = uri.toString();
		int queryDelimiterIndex = noQueryUriString.indexOf('?');
		if (queryDelimiterIndex > -1) {
			noQueryUriString = noQueryUriString.substring(0, queryDelimiterIndex);
		}
		URI noQueryUri = createUriWithSchemaAndPath(noQueryUriString);
		String queryString = createQueryString(queryMap);
		return URI.create(noQueryUri + "?" + queryString);
	}

}
