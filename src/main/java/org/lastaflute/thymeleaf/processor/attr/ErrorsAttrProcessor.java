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
package org.lastaflute.thymeleaf.processor.attr;

import java.util.HashMap;
import java.util.Map;

import org.dbflute.util.Srl;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

/**
 * Token Attribute Processor.
 * <pre>
 * Usage:
 *   &lt;span class="errors" <b>la:errors="sea"</b>/&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;span class="errors"&gt;<b>is required</b>&lt;/span&gt;
 * </pre>
 * @author jflute
 */
public class ErrorsAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTRIBUTE_NAME = "errors";
    public static final String DEFAULT_STYLE = "errors";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ErrorsAttrProcessor() {
        super(ATTRIBUTE_NAME);
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    public int getPrecedence() {
        return 950;
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final String specifiedValue = extractSpecifiedValue(arguments, element, attributeName);
        final Map<String, String> values = new HashMap<String, String>();
        values.put("class", prepareOverridingStyle(element));
        final String eachValue;
        if ("all".equalsIgnoreCase(specifiedValue)) {
            eachValue = "er : ${errors.all}";
        } else {
            eachValue = "er : ${errors.part('" + specifiedValue + "')}";
        }
        values.put("th:each", eachValue);
        values.put("th:text", "${er.message}");
        return values;
    }

    protected String extractSpecifiedValue(Arguments arguments, Element element, String attributeName) {
        final Configuration configuration = arguments.getConfiguration();
        final String attributeValue = element.getAttributeValue(attributeName);
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
        return expression.execute(configuration, arguments).toString();
    }

    protected String prepareOverridingStyle(Element element) {
        final String classAttr = element.getAttributeValueFromNormalizedName("class");
        final String embeddedStyle = getErrorsEmbeddedStyle();
        final String overridingStyle;
        if (classAttr != null && !classAttr.isEmpty()) {
            if (Srl.splitList(classAttr, " ").contains(embeddedStyle)) { // already defined
                overridingStyle = classAttr; // keep existing, already contains same style
            } else {
                overridingStyle = classAttr + " " + embeddedStyle; // e.g. class="sea errors"
            }
        } else {
            overridingStyle = embeddedStyle; // e.g. class="errors"
        }
        return overridingStyle;
    }

    protected String getErrorsEmbeddedStyle() {
        return DEFAULT_STYLE;
    }

    // ===================================================================================
    //                                                                     Option Override
    //                                                                     ===============
    @Override
    protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return ModificationType.SUBSTITUTION;
    }

    @Override
    protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String attributeName, String newAttributeName) {
        return true;
    }

    @Override
    protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
        return true;
    }
}
