/*
 * Copyright 2014-2015 the original author or authors.
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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.dbflute.helper.HandyDate;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Utility methods for LocalDate and more Date objects.<br>
 * Inner work uses org.dbflute.helper.HandyDate.
 * <pre>
 * Usage:
 *     &lt;span th:text="${#handydate.format(member.birthdate)}"&gt;20XX-XX-XX&lt;/span&gt;
 *     &lt;span th:text="${#handydate.format(member.birthdate,'yyyy/MM/dd')}"&gt;20XX-XX-XX&lt;/span&gt;
 *     &lt;span th:text="${#handydate.create(member.birthdate).addYear(10).toDisp('yyyy-MM-dd')}"&gt;20XX-XX-XX&lt;/span&gt;
 *
 *   The result of processing this example will be as expected.
 *     &lt;span&gt;2006-09-26&lt;/span&gt;
 *     &lt;span&gt;2006/09/26&lt;/span&gt;
 *     &lt;span&gt;2016-09-26&lt;/span&gt;
 * </pre>
 *
 * @author schatten
 */
public class HandyDateExpressionProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /**
     * Default date format pattern.
     * HTML input type date is uses this pattern.
     */
    public static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    // ===================================================================================
    //                                                                          Expression
    //                                                                          ==========
    /**
     * Create HandyDate object.
     * @param expression Date expression.(LocalDate, LocalDateTime, Date String)
     * @return HandyDate
     */
    public HandyDate create(Object expression) {
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
        String msg = "First argument as one argument should be LocalDate or LocalDateTime or Date or String(expression).";
        throw new TemplateProcessingException(msg);
    }

    /**
     * Create HandyDate object.
     * @param expression Date expression.(LocalDate, LocalDateTime, Date String)
     * @param arg2 TimeZone or Date String parse pattern.
     * @return HandyDate
     */
    public HandyDate create(Object expression, Object arg2) {
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
                    "First argument as two arguments should be LocalDate or LocalDateTime or String(expression) when second argument is TimeZone.";
            throw new TemplateProcessingException(msg);
        }
        if (arg2 instanceof String) {
            if (expression instanceof String) {
                return create((String) expression, (String) arg2);
            }
            String msg = "First argument as two arguments should be String when second argument is String(pattern).";
            throw new TemplateProcessingException(msg);
        }
        String msg = "Second argument as two arguments should be TimeZone or String(pattern).";
        throw new TemplateProcessingException(msg);
    }

    /**
     * Create HandyDate object.
     * @param expression String date (String)
     * @param pattern date format pattern (String)
     * @param locale date locale (Locale)
     * @return HandyDate
     */
    public HandyDate create(Object expression, Object pattern, Object locale) {
        if (!(expression instanceof String)) {
            String msg = "First argument as three arguments should be String(expression).";
            throw new TemplateProcessingException(msg);
        }
        if (!(pattern instanceof String)) {
            String msg = "Second argument as three arguments should be TimeZone or String(pattern).";
            throw new TemplateProcessingException(msg);
        }
        if (!(locale instanceof Locale)) {
            String msg = "Third argument as three arguments should be Locale.";
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
    public HandyDate create(Object expression, Object timeZone, Object pattern, Object locale) {
        if (!(expression instanceof String)) {
            String msg = "First argument as four arguments should be String(expression).";
            throw new TemplateProcessingException(msg);
        }
        if (!(timeZone instanceof TimeZone)) {
            String msg = "Second argument as four arguments should be TimeZone.";
            throw new TemplateProcessingException(msg);
        }
        if (!(pattern instanceof String)) {
            String msg = "Third argument as four arguments should be TimeZone or String(pattern).";
            throw new TemplateProcessingException(msg);
        }
        if (!(locale instanceof Locale)) {
            String msg = "Fourth argument as four arguments should be Locale.";
            throw new TemplateProcessingException(msg);
        }
        return create((String) expression, (TimeZone) timeZone, (String) pattern, (Locale) locale);
    }

    /**
     * Get formatted date string.
     * Using pattern of default.
     * @param expression Date expression.
     * @return formatted date string.(Return null if expression is null.)
     */
    public String format(Object expression) {
        return format(expression, DEFAULT_DATE_FORMAT_PATTERN);
    }

    /**
     * Get formatted date string.
     * @param expression Date expression.
     * @param pattern date format pattern.
     * @return formatted date string.(Return null if expression is null.)
     */
    public String format(Object expression, Object pattern) {
        if (pattern instanceof String) {
            if (expression == null) {
                return null;
            }
            if (expression instanceof LocalDate) {
                return create((LocalDate) expression).toDisp((String) pattern);
            }
            if (expression instanceof LocalDateTime) {
                return create((LocalDateTime) expression).toDisp((String) pattern);
            }
            if (expression instanceof Date) {
                return create((Date) expression).toDisp((String) pattern);
            }
            if (expression instanceof String) {
                return create((String) expression).toDisp((String) pattern);
            }
            String msg = "First argument as two arguments should be LocalDate or LocalDateTime or Date or String(expression).";
            throw new TemplateProcessingException(msg);
        }
        String msg = "Second argument as two arguments should be String(pattern).";
        throw new TemplateProcessingException(msg);
    }

    // ===================================================================================
    //                                                                    Delegate Utility
    //                                                                    ================
    /**
     * Create HandyDate from LocalDate.
     * @param localDate
     * @return HandyDate
     * @see HandyDate#HandyDate(LocalDate)
     */
    public static HandyDate create(LocalDate localDate) {
        return new HandyDate(localDate);
    }

    /**
     * Create HandyDate from LocalDate.
     * @param localDate
     * @param timeZone
     * @return HandyDate
     * @see HandyDate#HandyDate(LocalDate, TimeZone)
     */
    public static HandyDate create(LocalDate localDate, TimeZone timeZone) {
        return new HandyDate(localDate, timeZone);
    }

    /**
     * Create HandyDate from LocalDateTime.
     * @param localDateTime
     * @return HandyDate
     * @see HandyDate#HandyDate(LocalDateTime)
     */
    public static HandyDate create(LocalDateTime localDateTime) {
        return new HandyDate(localDateTime);
    }

    /**
     * Create HandyDate from LocalDateTime.
     * @param localDateTime
     * @param timeZone
     * @return HandyDate
     * @see HandyDate#HandyDate(LocalDateTime, TimeZone)
     */
    public static HandyDate create(LocalDateTime localDateTime, TimeZone timeZone) {
        return new HandyDate(localDateTime, timeZone);
    }

    /**
     * Create HandyDate from Date.
     * @param date
     * @return HandyDate
     * @see HandyDate#HandyDate(Date)
     */
    public static HandyDate create(Date date) {
        return new HandyDate(date);
    }

    /**
     * Create HandyDate from String.
     * @param exp
     * @return HandyDate
     * @see HandyDate#HandyDate(String)
     */
    public static HandyDate create(String exp) {
        return new HandyDate(exp);
    }

    /**
     * Create HandyDate from string with TimeZone.
     * @param exp
     * @param timeZone
     * @return HandyDate
     * @see HandyDate#HandyDate(String, TimeZone)
     */
    public static HandyDate create(String exp, TimeZone timeZone) {
        return new HandyDate(exp, timeZone);
    }

    /**
     * Create HandyDate from date string with parse pattern.
     * @param exp
     * @param pattern
     * @return HandyDate
     * @see HandyDate#HandyDate(String, String)
     */
    public static HandyDate create(String exp, String pattern) {
        return new HandyDate(exp, pattern);
    }

    /**
     * Create HandyDate from date string with parse pattern and locale.
     * @param exp
     * @param pattern
     * @param locale
     * @return HandyDate
     * @see HandyDate#HandyDate(String, String, Locale)
     */
    public static HandyDate create(String exp, String pattern, Locale locale) {
        return new HandyDate(exp, pattern, locale);
    }

    /**
     * Create HandyDate from date string with TimeZone, parse pattern and locale.
     * @param exp
     * @param timeZone
     * @param pattern
     * @param locale
     * @return HandyDate
     * @see HandyDate#HandyDate(String, TimeZone, String, Locale)
     */
    public static HandyDate create(String exp, TimeZone timeZone, String pattern, Locale locale) {
        return new HandyDate(exp, timeZone, pattern, locale);
    }
}
