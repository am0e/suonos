/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package suonos.httpserver;

import java.lang.ref.SoftReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A utility class for parsing and formatting HTTP dates as used in cookies and
 * other headers. This class handles dates as defined by RFC 2616 section 3.3.1
 * as well as some other common non-standard formats.
 * 
 *
 * @since 4.0
 */
public final class DateUtils {

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime() format.
     */
    public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    private static final String[] DEFAULT_PATTERNS = new String[] { PATTERN_RFC1036, PATTERN_RFC1123, PATTERN_ASCTIME };

    public static final int RFC1036 = 0;
    public static final int RFC1123 = 1;
    public static final int ASCTIME = 2;

    private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(GMT);
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
    }

    /**
     * Parses a date value. The formats used for parsing the date value are
     * retrieved from the default http params.
     *
     * @param dateValue
     *            the date value to parse
     * 
     * @return the parsed date
     *
     * @throws DateParseException
     *             if the value could not be parsed using any of the supported
     *             date formats
     */
    public static Date parseDate(String dateValue) throws DateParseException {
        return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     * 
     * @param dateValue
     *            the date value to parse
     * @param dateFormats
     *            the date formats to use
     * 
     * @return the parsed date
     * 
     * @throws DateParseException
     *             if none of the dataFormats could parse the dateValue
     */
    public static Date parseDate(final String dateValue, String[] dateFormats) throws DateParseException {
        return parseDate(dateValue, dateFormats, null);
    }

    /**
     * Parses the date value using the given date formats.
     * 
     * @param dateValue
     *            the date value to parse
     * @param dateFormats
     *            the date formats to use
     * @param startDate
     *            During parsing, two digit years will be placed in the range
     *            <code>startDate to startDate + 100 years. This value may be
     *            <code>null. When null is given as a parameter, year <code>2000
     *            will be used.
     * 
     * @return the parsed date
     * 
     * @throws DateParseException
     *             if none of the dataFormats could parse the dateValue
     */
    public static Date parseDate(String dateValue, String[] dateFormats, Date startDate) throws DateParseException {

        if (dateValue == null) {
            throw new IllegalArgumentException("dateValue is null");
        }
        if (dateFormats == null) {
            dateFormats = DEFAULT_PATTERNS;
        }
        if (startDate == null) {
            startDate = DEFAULT_TWO_DIGIT_YEAR_START;
        }
        // trim single quotes around date if present
        // see issue #5279
        if (dateValue.length() > 1 && dateValue.startsWith("'") && dateValue.endsWith("'")) {
            dateValue = dateValue.substring(1, dateValue.length() - 1);
        }

        for (int i = 0; i != DEFAULT_PATTERNS.length; i++) {
            SimpleDateFormat dateParser = DateFormatHolder.formatFor(i);
            dateParser.set2DigitYearStart(startDate);

            try {
                return dateParser.parse(dateValue);

            } catch (ParseException pe) {
                // ignore this exception, we will try the next format
            }
        }

        // we were unable to parse the date
        throw new DateParseException("Unable to parse the date " + dateValue);
    }

    /**
     * Formats the given date according to the RFC 1123 pattern.
     * 
     * @param date
     *            The date to format.
     * @return An RFC 1123 formatted date string.
     * 
     * @see #PATTERN_RFC1123
     */
    public static String formatDate(Date date) {
        return formatDate(date, 1);
    }

    public static String formatDate(Date date, int i) {
        if (date == null)
            throw new IllegalArgumentException("date is null");

        SimpleDateFormat formatter = DateFormatHolder.formatFor(i);
        return formatter.format(date);
    }

    /** This class should not be instantiated. */
    private DateUtils() {
    }

    /**
     * A factory for {@link SimpleDateFormat}s. The instances are stored in a
     * threadlocal way because SimpleDateFormat is not threadsafe as noted in
     * {@link SimpleDateFormat its javadoc}.
     * 
     */
    final static class DateFormatHolder {

        private static final ThreadLocal<SoftReference<SimpleDateFormat[]>> THREADLOCAL_FORMATS = new ThreadLocal<>();

        /**
         * creates a {@link SimpleDateFormat} for the requested format string.
         * 
         * @param pattern
         *            a non-<code>null format String according to
         *            {@link SimpleDateFormat}. The format is not checked
         *            against <code>null since all paths go through
         *            {@link DateUtils}.
         * @return the requested format. This simple dateformat should not be
         *         used to {@link SimpleDateFormat#applyPattern(String) apply}
         *         to a different pattern.
         */
        public static SimpleDateFormat formatFor(int i) {
            SoftReference<SimpleDateFormat[]> ref = THREADLOCAL_FORMATS.get();
            SimpleDateFormat[] formats = null;

            if (ref != null) {
                formats = ref.get();
            }

            if (formats == null) {
                formats = new SimpleDateFormat[3];
                formats[0] = new SimpleDateFormat(DEFAULT_PATTERNS[0], Locale.US);
                formats[1] = new SimpleDateFormat(DEFAULT_PATTERNS[1], Locale.US);
                formats[2] = new SimpleDateFormat(DEFAULT_PATTERNS[2], Locale.US);
                formats[0].setTimeZone(TimeZone.getTimeZone("GMT"));
                formats[1].setTimeZone(TimeZone.getTimeZone("GMT"));
                formats[2].setTimeZone(TimeZone.getTimeZone("GMT"));
                THREADLOCAL_FORMATS.set(new SoftReference<>(formats));
            }

            return formats[i];
        }

    }

}