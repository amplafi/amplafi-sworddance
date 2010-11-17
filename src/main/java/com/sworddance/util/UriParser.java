package com.sworddance.util;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * borrowed from http://snipplr.com/view.php?codeview&id=6889
 *
 * TODO: Hook these patterns up and use more
 */
public class UriParser {
    /**
     *
     */
    public static final String HTTP_S_SCHEME_PATTERN_STR = "https?";
    /**
     *
     */
    private static final String USERINFO = "(?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})";
    /**
     *
     */
    private static final String HOST = "((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)";
    /**
     *
     */
    private static final String PATH_CHAR_INCLUDE_SLASH = "(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})";
    /**
     *
     */
    private static final String PATH_CHAR_EXCLUDE_SLASH = "(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})";

    /**
     *
     */
    private static final String PATH_OPTIONAL = "*)?";
    /**
     *
     */
    private static final String BASE_PATH_WHEN_NO_USERINFO = "(/?" +
        PATH_CHAR_EXCLUDE_SLASH +
        "+" +
        PATH_CHAR_INCLUDE_SLASH;
    private static final String PATH_WHEN_NO_USERINFO = BASE_PATH_WHEN_NO_USERINFO + PATH_OPTIONAL;
    /**
     *
     */
    private static final String BASE_PATH_WHEN_USERINFO = "(/" +
    		PATH_CHAR_INCLUDE_SLASH;
    private static final String PATH_WHEN_USERINFO = BASE_PATH_WHEN_USERINFO +          PATH_OPTIONAL;
    /**
     *
     */
    private static final String PORT_WITHOUT_DELIM = "(?::(\\d+))?";
    /**
     *
     */
    private static final String PORT_WITH_DELIM = "(:(?:\\d+))?";
    /**
     *
     */
    private static final String QUERY_WITH_DELIMETER = "(\\?(?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)?";
    /**
     * pattern that retrieves query without the ? delimeter
     */
    private static final String QUERY_WITHOUT_DELIM = "(?:\\?((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*))?";
    /**
     *
     */
    private static final String FRAGMENT_WITH_DELIMETER = "(#(?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)?";
    /**
     *
     */
    private static final String FRAGMENT_WITHOUT_DELIM = "(?:#((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*))?";
    /**
     * General pattern to match all schemes
     */
    private static final String SCHEME_PATTERN_STR = "[a-z0-9+.-]+";


    private static Pattern only(String regex) {
        return Pattern.compile("^"+regex+"$", Pattern.CASE_INSENSITIVE);
    }
    private static Pattern within(String regex) {
        return Pattern.compile("\\b"+regex+"\\b", Pattern.CASE_INSENSITIVE);
    }
    //replace() can be used to parse the URI. For example, to get the path:
    //  path = uri.replace(regexUri, "$5$6");

    //****************************************************//
    //***************** Validate a URI *******************//
    //****************************************************//
    //- The different parts are kept in their own groups and can be recombined
    //  depending on the scheme:
    //  - http as $1://$3:$4$5?$7#$8
    //  - ftp as $1://$2@$3:$4$5
    //  - mailto as $1:$6?$7
    //- groups are as follows:
    //  1   == scheme
    //  2   == userinfo
    //  3   == host
    //  4   == port
    //  5,6 == path (5 if it has an authority, 6 if it doesn't)
    //  7   == query
    //  8   == fragment
        /*composed as follows:
        ^
        ([a-z0-9+.-]+):                         #scheme
        (?:
            //                          #it has an authority:
            (?:((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?  #userinfo
            ((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)     #host
            (?::(\d*))?                     #port  PATM: why is number optional if : present? I changed to require a port number (makes it identical to #REGEX_URL/REGEX_URI_DELIM pattern )
            (/(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?    #path
            |
                                        #it doesn't have an authority:
            (/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?    #path
        )
        (?:
            \?((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)   #query string
        )?
        (?:
            #((?:[a-z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)    #fragment
        )?
        $
    */
    private static final String REGEX_URI = "(" +
    		SCHEME_PATTERN_STR +
    		"):(?://" +
    		"(?:(" +
    		USERINFO + "*)@)?" +
    		HOST +
    		PORT_WITHOUT_DELIM +
    		PATH_WHEN_USERINFO +
    		"|" +
    		PATH_WHEN_NO_USERINFO + ")" +
    		QUERY_WITHOUT_DELIM +
    		FRAGMENT_WITHOUT_DELIM;
    /**
     * {@link #REGEX_URI} only in the string
     */
    public static final Pattern regexUriOnly = only(REGEX_URI);
    /**
     * {@link #REGEX_URI} with word boundary separating from the rest of the string.
     */
    public static final Pattern regexUri = within(REGEX_URI);

    //****************************************************//
    //** Validate a URI (includes delimiters in groups) **//
    //****************************************************//
    //- The different parts--along with their delimiters--are kept in their own
    //  groups and can be recombined as $1$6$2$3$4$5$7$8$9
    //- groups are as follows:
    //  1,6 == scheme:// or scheme:
    //  2   == userinfo@
    //  3   == host
    //  4   == :port
    //  5,7 == path (5 if it has an authority, 7 if it doesn't)
    //  8   == ?query
    //  9   == #fragment
    private static final String REGEX_URI_DELIM = "(?:(" +
    		SCHEME_PATTERN_STR +
    		"://)((?:" +
    		USERINFO + "*)@)?" +
    		HOST +
    		PORT_WITH_DELIM +
    		PATH_WHEN_USERINFO +
    		"|(" +
    		SCHEME_PATTERN_STR +
    		":)" +
    		PATH_WHEN_NO_USERINFO + ")" +
    		QUERY_WITH_DELIMETER +
    		FRAGMENT_WITH_DELIMETER;
    public static final Pattern regexUriDelimOnly = only(REGEX_URI_DELIM);
    public static final Pattern regexUriDelim= within(REGEX_URI_DELIM);
    public static final String REGEX_URI_DELIM_FORMAT_STR = "{1}{6}{2}{3}{4}{5}{7}{8}{9}";

    //****************************************************//
    //***************** Validate a URL *******************//
    //****************************************************//
    //Validates a URI with an http or https scheme.
    //- The different parts are kept in their own groups and can be recombined as
    //  $1://$2:$3$4?$5#$6
    //- Does not validate the host portion (domain); just makes sure the string
    //  consists of valid characters (does not include IPv6 nor IPvFuture
    //  addresses as valid).
    private static final String REGEX_URL = "(" +
    		HTTP_S_SCHEME_PATTERN_STR +
    		")://((?:[a-z0-9.-]|%[0-9A-F]{2}){3,})" +
    		PORT_WITHOUT_DELIM +
    		"((?:/" +
    		PATH_CHAR_EXCLUDE_SLASH +
    		"*)*)" +
    		QUERY_WITHOUT_DELIM +
    		FRAGMENT_WITHOUT_DELIM;

    public static final Pattern regexUrlOnly = only(REGEX_URL);
    public static final Pattern regexUrl = within(REGEX_URL);
    public static final String REGEX_URL_FORMAT_STR = "{1}://{2}:{3}{4}?{5}#{6}";

    //****************************************************//
    //**************** Validate a Mailto *****************//
    //****************************************************//
    //Validates a URI with a mailto scheme.
    //- The different parts are kept in their own groups and can be recombined as
    //  $1:$2?$3
    //- Does not validate the email addresses themselves.
    private static final String REGEX_MAILTO = "(mailto):(" +
    		PATH_CHAR_EXCLUDE_SLASH +
    		"+)?" +
    		QUERY_WITHOUT_DELIM;

    public static final Pattern regexMailtoOnly = only(REGEX_MAILTO);
    public static final Pattern regexMailto = within(REGEX_MAILTO);
    public static final String REGEX_MAILTO_FORMAT_STR = "{1}:{2}?{3}";

    private Pattern uriOnly;
    private Pattern uriWithin;
    private String replaceFormatStr;

    public UriParser(String replaceFormat) {
        this(null, null, replaceFormat);
    }
    public UriParser(String schemePattern, String fileExtensionPattern, String replaceFormat) {
        this.replaceFormatStr = replaceFormat == null?REGEX_URI_DELIM_FORMAT_STR:new MessageFormat(replaceFormat).format(new String[] {REGEX_URI_DELIM_FORMAT_STR});
        if ( StringUtils.isBlank(schemePattern )) {
            schemePattern = SCHEME_PATTERN_STR;
        }
        String pathEnd;
        if ( StringUtils.isBlank(fileExtensionPattern)) {
            pathEnd= PATH_OPTIONAL;
        } else {
            pathEnd = "+" + fileExtensionPattern + ")";
        }
        String fullPatternString = "(?:(" +
            schemePattern +
            "://)((?:" +
            USERINFO + "*)@)?" +
            HOST +
            PORT_WITH_DELIM +
            BASE_PATH_WHEN_USERINFO + pathEnd +
            "|(" +
            schemePattern +
            ":)" +
            BASE_PATH_WHEN_NO_USERINFO + pathEnd + ")" +
            QUERY_WITH_DELIMETER +
            FRAGMENT_WITH_DELIMETER;
        this.uriOnly = only(fullPatternString);
        this.uriWithin = within(fullPatternString);
    }

    public static final String[] parts(String uriStr, Pattern pattern) {
        Matcher matcher = pattern.matcher(uriStr);

        int groupCount = matcher.groupCount();
        String[] result = null;

        if ( matcher.find()) {
            result = new String[groupCount];
            for(int i = 0; i < result.length; i++ ) {
                result[i] = matcher.group(i);
            }
        }
        return result;
    }

    public CharSequence replace(CharSequence inputString) {
        return replace(inputString, this.uriWithin, new MessageFormat(this.replaceFormatStr));
    }
    public static final CharSequence replace(CharSequence inputString, Pattern pattern, MessageFormat messageFormat) {
        Matcher matcher = pattern.matcher(inputString);
        StringBuilder stringBuilder = new StringBuilder();

        int groupCount = matcher.groupCount();

        int start = 0;
        if ( matcher.find()) {
            do {
                int end = matcher.start();
                stringBuilder.append(inputString.subSequence(start, end));
                String[] result = new String[groupCount];
                for(int i = 0; i < result.length; i++ ) {
                    result[i] = matcher.group(i);
                }
                String formatted = messageFormat.format(result);
                stringBuilder.append(formatted);
                start = matcher.end();

            } while ( matcher.find());
            return stringBuilder;
        } else {
            return inputString;
        }
    }
    public static final String[] regexUriParts(String uriStr) {
        return parts(uriStr, regexUri);
    }
    public static final String[] regexUriDelimParts(String uriStr) {
        return parts(uriStr, regexUriDelim);
    }
    public static final String[] regexUrlParts(String uriStr) {
        return parts(uriStr, regexUrl);
    }
    public static final String[] regexMailtoParts(String uriStr) {
        return parts(uriStr, regexMailto);
    }
}