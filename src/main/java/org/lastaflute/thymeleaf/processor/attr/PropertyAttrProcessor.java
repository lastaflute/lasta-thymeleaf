/*
 * Copyright 2015-2017 the original author or authors.
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
package org.lastaflute.thymeleaf.processor.attr;

import java.util.HashMap;
import java.util.Map;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * Property Attribute Processor.
 * <pre>
 * Usage:
 *   &lt;input type="text" <b>la:property="memberName"</b>/&gt;
 *
 * The result of processing this example will be as expected.
 *   - MemberName with set to "Ariel"
 *     &lt;input type="text" <b>name="memberName" value="Ariel"</b>/&gt;
 *   - When MemberName have validation error.
 *     &lt;input type="text" <b>name="memberName" value="" class="validError"</b>/&gt;
 * </pre>
 * @author schatten
 * @author jflute
 */
public class PropertyAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTRIBUTE_NAME = "property";
    private static final String APPEND_ERROR_STYLE_CLASS = "${errors.exists('%s')} ? 'validError'";
    private static final String APPEND_ERROR_STYLE_CLASS_ATTRAPEND = "class=(${errors.exists('%s')} ? ' validError')";

    protected static final String SELECT_PROPERTY_NAME = "la:selectPropertyName";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PropertyAttrProcessor() {
        super(ATTRIBUTE_NAME);
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    /**
     * {@inheritDoc}
     * @see org.thymeleaf.processor.AbstractProcessor#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 950;
    }

    /**
     * {@inheritDoc}
     * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#getModifiedAttributeValues(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     */
    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final Configuration configuration = arguments.getConfiguration();

        // Obtain the attribute value
        final String attributeValue = element.getAttributeValue(attributeName);

        boolean hasThName = element.hasNormalizedAttribute(StandardDialect.PREFIX, "name");
        boolean hasThText = element.hasNormalizedAttribute(StandardDialect.PREFIX, "text");
        boolean hasThValue = element.hasNormalizedAttribute(StandardDialect.PREFIX, "value");
        boolean hasThClassAppend = element.hasNormalizedAttribute(StandardDialect.PREFIX, "classappend");
        boolean hasThAttrAppend = element.hasNormalizedAttribute(StandardDialect.PREFIX, "attrappend");

        // Obtain the Thymeleaf Standard Expression parser
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);

        // Parse the attribute value as a Thymeleaf Standard Expression
        final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);

        String propertyName = expression.execute(configuration, arguments).toString();
        final Map<String, String> values = new HashMap<String, String>();
        String propertyFieldName = getPropertyFieldName(arguments, element, configuration, propertyName);
        switch (element.getNormalizedName()) {
        case "input":
            if (!hasThName) {
                values.put("th:name", propertyFieldName);
            }
            if (!hasThValue) {
                String type = element.getAttributeValueFromNormalizedName("type");
                if (!("checkbox".equals(type) || "radio".equals(type))) {
                    values.put("th:value", "${" + propertyName + "}");
                }
            }
            break;
        case "select":
            if (!hasThName) {
                values.put("th:name", propertyFieldName);
            }
            element.setNodeProperty(SELECT_PROPERTY_NAME, propertyName);
            break;
        case "textarea":
            if (!hasThName) {
                values.put("th:name", propertyFieldName);
            }
            // not break.
        default:
            if (!hasThText) {
                values.put("th:text", "${" + propertyName + "}");
            }
            break;
        }
        if (!hasThClassAppend) {
            values.put("th:classappend", String.format(APPEND_ERROR_STYLE_CLASS, propertyFieldName));
        } else if (!hasThAttrAppend) {
            values.put("th:attrappend", String.format(APPEND_ERROR_STYLE_CLASS_ATTRAPEND, propertyFieldName));
        }

        return values;
    }

    protected String getPropertyFieldName(Arguments arguments, Element element, final Configuration configuration, final String name) {
        if (arguments.hasLocalVariable(ForEachAttrProcessor.FORM_PROPERTY_PATH_VER)) {
            String formName = (String) arguments.getLocalVariable(ForEachAttrProcessor.FORM_PROPERTY_PATH_VER) + ".";
            int indexOf = name.indexOf(".");
            if (indexOf < 0) {
                formName += name;
            } else {
                formName += name.substring(indexOf + 1);
            }
            return formName;
        }

        return name;
    }

    /**
     * {@inheritDoc}
     * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#getModificationType(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String, java.lang.String)
     */
    @Override
    protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return ModificationType.SUBSTITUTION;
    }

    /**
     * {@inheritDoc}
     * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#removeAttributeIfEmpty(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String, java.lang.String)
     */
    @Override
    protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return true;
    }

    /**
     * {@inheritDoc}
     * @see org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor#recomputeProcessorsAfterExecution(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     */
    @Override
    protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
        return true;
    }
}
