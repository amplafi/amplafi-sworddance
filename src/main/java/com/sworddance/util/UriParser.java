package com.sworddance.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import static com.sworddance.util.CUtilities.*;

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
    public static final String REGEX_URI = "(" +
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
    public static UriParser URI = new UriParser(REGEX_URI, null);

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
    public static final String REGEX_URI_DELIM = "(?:(" +
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
    public static final String REGEX_URI_DELIM_FORMAT_STR = "{1}{6}{2}{3}{4}{5}{7}{8}{9}";
    public static final String REGEX_URI_DELIM_REPLACE_STR = "$1$6$2$3$4$5$7$8$9";
    private static final Pattern regexUriDelimOnly = onlyPattern(REGEX_URI_DELIM);
    private static final Pattern regexUriDelim= withinPattern(REGEX_URI_DELIM);
    public static UriParser URI_DELIM = new UriParser(REGEX_URI_DELIM, REGEX_URI_DELIM_FORMAT_STR);

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

    public static final String REGEX_URL_FORMAT_STR = "{1}://{2}:{3}{4}?{5}#{6}";
    public static final String REGEX_URL_REPLACE_STR = "$1://$2:$3$4?$5#$6";
    public static UriParser URL = new UriParser(REGEX_URL, REGEX_URL_FORMAT_STR);

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
    public static final String REGEX_MAILTO_FORMAT_STR = "{1}:{2}?{3}";
    public static final String REGEX_MAILTO_REPLACE_STR = "$1:$2?$3";
    public static UriParser MAIL_TO = new UriParser(REGEX_MAILTO, REGEX_MAILTO_REPLACE_STR);

    private final Pattern uriOnly;
    private final Pattern uriWithin;
    private final String replaceFormatStr;


    public UriParser(String replaceFormat) {
        this(regexUriDelimOnly, regexUriDelim, replaceFormat);
    }
    public UriParser(String schemePattern, String fileExtensionPattern, String replaceFormat) {
        this.replaceFormatStr = replaceFormat == null?REGEX_URI_DELIM_REPLACE_STR:replaceFormat;
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
        this.uriOnly = onlyPattern(fullPatternString);
        this.uriWithin = withinPattern(fullPatternString);
    }
    public UriParser(String regex, String replaceFormat) {
        this(onlyPattern(regex), withinPattern(regex), replaceFormat);
    }

    public UriParser(Pattern uriOnly, Pattern uriWithin, String replaceFormatStr) {
        this.uriOnly = uriOnly;
        this.uriWithin = uriWithin;
        this.replaceFormatStr = replaceFormatStr;
    }
    public ExtendedMatchResult partsOnly(String uriStr) {
        Pattern pattern = this.uriOnly;
        Matcher matcher = pattern.matcher(uriStr);

        if ( matcher.find()) {
            return new ExtendedMatchResultImpl(matcher.toMatchResult());
        } else {
            return null;
        }
    }
    public List<ExtendedMatchResult> partsWithin(String input) {
        Pattern pattern = this.uriWithin;
        Matcher matcher = pattern.matcher(input);
        return ExtendedMatchResultImpl.toExtendedMatchResult(matcher);
    }

    public CharSequence replace(CharSequence inputString) {
        return replace(inputString, this.replaceFormatStr);
    }
    /**
     * TODO: use {@link Matcher#replaceAll(String)}
     * @param inputString
     * @param replacement
     * @return the replaced string
     */
    public CharSequence replace(CharSequence inputString, String replacement) {
        Pattern pattern = this.uriWithin;
        Matcher matcher = pattern.matcher(inputString);

        if ( matcher.find()) {
            ApplicationNullPointerException.notNull(replacement, "cannot do a replace because there is no format");
            String output = matcher.replaceAll(replacement);

            return output;
        } else {
            return inputString;
        }
    }

    /**
     * TODO: use {@link Matcher#replaceAll(String)}
     * @param inputString
     * @param replaceFormat
     * @return the replaced string
     */
    public CharSequence replaceMessageFormatString(CharSequence inputString, String replaceFormat) {
        Pattern pattern = this.uriWithin;
        Matcher matcher = pattern.matcher(inputString);

        if ( matcher.find()) {
            int start = 0;
            ApplicationNullPointerException.notNull(replaceFormat, "cannot do a replace because there is no format");
            MessageFormat messageFormat = new MessageFormat(replaceFormat);
            StringBuilder stringBuilder = new StringBuilder();

            int groupCount = matcher.groupCount();
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
    public boolean isOnly(CharSequence input) {
        return this.uriOnly.matcher(input).find();
    }
}