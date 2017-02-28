/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.thymeleaf.processor.expression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dbflute.helper.HandyDate;
import org.dbflute.util.Srl;
import org.lastaflute.core.direction.AccessibleConfig;
import org.lastaflute.core.util.ContainerUtil;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Utility methods for LocalDate and more Date objects.<br>
 * Inner work uses org.dbflute.helper.HandyDate.
 * <pre>
 * Usage:
 *     &lt;span th:text="${#handy.format(member.birthdate)}"&gt;20XX-XX-XX&lt;/span&gt;
 *     &lt;span th:text="${#handy.format(member.birthdate,'yyyy/MM/dd')}"&gt;20XX-XX-XX&lt;/span&gt;
 *     &lt;span th:text="${#handy.date(member.birthdate).addYear(10).toDisp('yyyy-MM-dd')}"&gt;20XX-XX-XX&lt;/span&gt;
 *
 *   The result of processing this example will be as expected.
 *     &lt;span&gt;2006-09-26&lt;/span&gt;
 *     &lt;span&gt;2006/09/26&lt;/span&gt;
 *     &lt;span&gt;2016-09-26&lt;/span&gt;
 * </pre>
 *
 * @author schatten
 * @author jflute
 */
public class HandyDateExpressionProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Date pattern used as default in format(), defined at your [app]_config.properties. */
    public static final String KEY_OF_DATE_PATTERN = "app.standard.date.pattern";

    /** Datetime pattern used as default in format(), defined at your [app]_config.properties. */
    public static final String KEY_OF_DATETIME_PATTERN = "app.standard.datetime.pattern";

    /** Time pattern used as default in format(), defined at your [app]_config.properties. */
    public static final String KEY_OF_TIME_PATTERN = "app.standard.time.pattern";

    /** Default date pattern for format(). */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /** Default date-time pattern for format(). */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** Default time pattern for format(). */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * The cached configuration of application, which can be lazy-loaded when you get it.
     * Don't use these variables directly, you should use the getter. (NotNull: after lazy-load)
     */
    protected AccessibleConfig cachedApplicationConfig;

    // ===================================================================================
    //                                                                          Handy Date
    //                                                                          ==========
    /**
     * Create HandyDate object.
     * @param expression Date expression.(LocalDate, LocalDateTime, Date String)
     * @return HandyDate
     */
    public HandyDate date(Object expression) {
        if (expression instanceof LocalDate) {
            return create((LocalDate) expression);
        }
        if (expression instanceof LocalDateTime) {
            return create((LocalDateTime) expression);
        }
        if (expression instanceof Date) {
            return create((Date) expression);
        }
        if (expression instanceof String) {
            return create((String) expression);
        }
        String msg = "First argument as one argument should be LocalDate or LocalDateTime or Date or String(expression): " + expression;
        throw new TemplateProcessingException(msg);
    }

    /**
     * Create HandyDate object.
     * @param expression Date expression.(LocalDate, LocalDateTime, Date String)
     * @param arg2 TimeZone or Date String parse pattern.
     * @return HandyDate
     */
    public HandyDate date(Object expression, Object arg2) {
        if (arg2 instanceof TimeZone) {
            if (expression instanceof LocalDate) {
                return create((LocalDate) expression, (TimeZone) arg2);
            }
            if (expression instanceof LocalDateTime) {
                return create((LocalDateTime) expression, (TimeZone) arg2);
            }
            if (expression instanceof String) {
                return create((String) expression, (TimeZone) arg2);
            }
            String msg =
                    "First argument as two arguments should be LocalDate or LocalDateTime or String(expression) when second argument is TimeZone: "
                            + expression;
            throw new TemplateProcessingException(msg);
        }
        if (arg2 instanceof String) {
            if (expression instanceof String) {
                return create((String) expression, (String) arg2);
            }
            String msg = "First argument as two arguments should be String when second argument is String(pattern): " + expression;
            throw new TemplateProcessingException(msg);
        }
        String msg = "Second argument as two arguments should be TimeZone or String(pattern): " + arg2;
        throw new TemplateProcessingException(msg);
    }

    /**
     * Create HandyDate object.
     * @param expression String date (String)
     * @param pattern date format pattern (String)
     * @param locale date locale (Locale)
     * @return HandyDate
     */
    public HandyDate date(Object expression, Object pattern, Object locale) {
        if (!(expression instanceof String)) {
            String msg = "First argument as three arguments should be String(expression): " + expression;
            throw new TemplateProcessingException(msg);
        }
        if (!(pattern instanceof String)) {
            String msg = "Second argument as three arguments should be TimeZone or String(pattern): " + pattern;
            throw new TemplateProcessingException(msg);
        }
        if (!(locale instanceof Locale)) {
            String msg = "Third argument as three arguments should be Locale: " + locale;
            throw new TemplateProcessingException(msg);
        }
        return create((String) expression, (String) pattern, (Locale) locale);
    }

    /**
     * Create HandyDate object.
     * @param expression String date (String)
     * @param timeZone Time zone (TimeZone)
     * @param pattern date format pattern (String)
     * @param locale dat locale (Locale)
     * @return HandyDate
     */
    public HandyDate date(Object expression, Object timeZone, Object pattern, Object locale) {
        if (!(expression instanceof String)) {
            String msg = "First argument as four arguments should be String(expression): " + expression;
            throw new TemplateProcessingException(msg);
        }
        if (!(timeZone instanceof TimeZone)) {
            String msg = "Second argument as four arguments should be TimeZone: " + timeZone;
            throw new TemplateProcessingException(msg);
        }
        if (!(pattern instanceof String)) {
            String msg = "Third argument as four arguments should be TimeZone or String(pattern): " + pattern;
            throw new TemplateProcessingException(msg);
        }
        if (!(locale instanceof Locale)) {
            String msg = "Fourth argument as four arguments should be Locale: " + locale;
            throw new TemplateProcessingException(msg);
        }
        return create((String) expression, (TimeZone) timeZone, (String) pattern, (Locale) locale);
    }

    // ===================================================================================
    //                                                                              Format
    //                                                                              ======
    /**
     * Get formatted date string, using application standard pattern or default pattern.
     * @param expression Date expression. (NullAllowed: if null, returns null)
     * @return formatted date string. (NullAllowed: if expression is null)
     */
    public String format(Object expression) {
        return format(expression, chooseDateFormatPattern(expression));
    }

    protected String chooseDateFormatPattern(Object expression) {
        if (expression == null) {
            return DEFAULT_DATETIME_PATTERN; // unused: returns null
        }
        final AccessibleConfig config = getApplicationConfig();
        if (expression instanceof LocalDate) {
            return getAppStandardPatternDate(config);
        } else if (expression instanceof LocalDateTime) {
            return getAppStandardPatternDatetime(config);
        } else if (expression instanceof LocalTime) {
            return getAppStandardPatternTime(config);
        } else if (expression instanceof java.sql.Timestamp) {
            return getAppStandardPatternDatetime(config);
        } else if (expression instanceof java.sql.Time) {
            return getAppStandardPatternTime(config);
        } else if (expression instanceof java.util.Date) {
            return getAppStandardPatternDate(config);
        } else { // unknown expression
            return DEFAULT_DATETIME_PATTERN; // unused: basicaly error by other process
        }
    }

    /**
     * Get formatted date string.
     * @param expression Date expression. (NullAllowed: if null, returns null)
     * @param objPattern date format pattern. (NotNull)
     * @return formatted date string. (NullAllowed: if expression is null.)
     */
    public String format(Object expression, Object objPattern) {
        if (objPattern instanceof String) {
            final String pattern = filterPattern((String) objPattern);
            if (expression == null) {
                return null;
            }
            if (expression instanceof LocalDate) {
                return create((LocalDate) expression).toDisp(pattern);
            }
            if (expression instanceof LocalDateTime) {
                return create((LocalDateTime) expression).toDisp(pattern);
            }
            if (expression instanceof java.util.Date) {
                return create((java.util.Date) expression).toDisp(pattern);
            }
            if (expression instanceof String) {
                return create((String) expression).toDisp(pattern);
            }
            String msg =
                    "First argument as two arguments should be LocalDate or LocalDateTime or Date or String(expression): " + expression;
            throw new TemplateProcessingException(msg);
        }
        String msg = "Second argument as two arguments should be String(pattern): objPattern=" + objPattern;
        throw new TemplateProcessingException(msg);
    }

    protected String filterPattern(String pattern) {
        if (pattern.contains("$$")) {
            final AccessibleConfig config = getApplicationConfig();
            String filtered = pattern;
            filtered = Srl.replace(filtered, "$$date$$", getAppStandardPatternDate(config));
            filtered = Srl.replace(filtered, "$$datetime$$", getAppStandardPatternDatetime(config));
            filtered = Srl.replace(filtered, "$$time$$", getAppStandardPatternTime(config));
            return filtered;
        } else {
            return pattern;
        }
    }

    // -----------------------------------------------------
    //                                  Application Standard
    //                                  --------------------
    // #for_now application standard date patterns are only format() (not used at parsing date)
    protected String getAppStandardPatternDate(AccessibleConfig config) {
        return config.getOrDefault(KEY_OF_DATE_PATTERN, DEFAULT_DATE_PATTERN);
    }

    protected String getAppStandardPatternDatetime(AccessibleConfig config) {
        return config.getOrDefault(KEY_OF_DATETIME_PATTERN, DEFAULT_DATETIME_PATTERN);
    }

    protected String getAppStandardPatternTime(AccessibleConfig config) {
        return config.getOrDefault(KEY_OF_TIME_PATTERN, DEFAULT_TIME_PATTERN);
    }

    // ===================================================================================
    //                                                                    Delegate Utility
    //                                                                    ================
    protected static HandyDate create(LocalDate localDate) {
        return new HandyDate(localDate);
    }

    protected static HandyDate create(LocalDate localDate, TimeZone timeZone) {
        return new HandyDate(localDate, timeZone);
    }

    protected static HandyDate create(LocalDateTime localDateTime) {
        return new HandyDate(localDateTime);
    }

    protected static HandyDate create(LocalDateTime localDateTime, TimeZone timeZone) {
        return new HandyDate(localDateTime, timeZone);
    }

    protected static HandyDate create(Date date) {
        return new HandyDate(date);
    }

    protected static HandyDate create(String exp) {
        return new HandyDate(exp);
    }

    protected static HandyDate create(String exp, TimeZone timeZone) {
        return new HandyDate(exp, timeZone);
    }

    protected static HandyDate create(String exp, String pattern) {
        return new HandyDate(exp, pattern);
    }

    protected static HandyDate create(String exp, String pattern, Locale locale) {
        return new HandyDate(exp, pattern, locale);
    }

    protected static HandyDate create(String exp, TimeZone timeZone, String pattern, Locale locale) {
        return new HandyDate(exp, timeZone, pattern, locale);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected AccessibleConfig getApplicationConfig() {
        if (cachedApplicationConfig != null) {
            return cachedApplicationConfig;
        }
        synchronized (this) {
            if (cachedApplicationConfig != null) {
                return cachedApplicationConfig;
            }
            cachedApplicationConfig = ContainerUtil.getComponent(AccessibleConfig.class);
        }
        return cachedApplicationConfig;
    }
}
