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
package org.lastaflute.thymeleaf.processor;

import java.util.HashMap;
import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.token.DoubleSubmitManager;
import org.lastaflute.web.util.LaActionRuntimeUtil;
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
 *   &lt;input type="hidden" <b>la:token="true"</b>/&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;input type="text" <b>name="lastaflute.action.TRANSACTION_TOKEN" value="f229f48b0bb7986656cdd5f9d86338e3"</b>/&gt;
 * </pre>
 * @author jflute
 */
public class TokenAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PROPERTY_ATTRIBUTE_NAME = "token";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TokenAttrProcessor() {
        super(PROPERTY_ATTRIBUTE_NAME);
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
        final Map<String, String> values = new HashMap<String, String>(4);

        final ActionRuntime runtime = LaActionRuntimeUtil.getActionRuntime();
        final String tagName = element.getNormalizedName();
        switch (tagName) {
        case "input":
            final String inputType = element.getAttributeValueFromNormalizedName("type");
            if (!"hidden".equals(inputType)) {
                throwThymeleafTokenNotHiddenTypeException(runtime, inputType);
            }
            if ("true".equalsIgnoreCase(specifiedValue)) { // #thinking how to remove this tag when false?

                final DoubleSubmitManager doubleSubmitManager = getDoubleSubmitManager();
                final String token = doubleSubmitManager.getSessionTokenMap().orElseThrow(() -> {
                    return createThymeleafSessionTokenMapNotFoundException(runtime);
                }).get(runtime.getActionType()).orElseThrow(() -> { // #thinking token group setting?
                    return createThymeleafGroupTokenNotFoundException(runtime);
                });
                values.put("th:name", DoubleSubmitManager.TOKEN_KEY);
                values.put("th:value", token);
            }
            break;
        default:
            throwThymeleafTokenNotInputTypeException(runtime, tagName);
        }
        return values;
    }

    protected String extractSpecifiedValue(Arguments arguments, Element element, String attributeName) {
        final Configuration configuration = arguments.getConfiguration();
        final String attributeValue = element.getAttributeValue(attributeName);
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
        return expression.execute(configuration, arguments).toString();
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected DoubleSubmitManager getDoubleSubmitManager() { // #pending want to cache
        return ContainerUtil.getComponent(DoubleSubmitManager.class);
    }

    protected IllegalStateException createThymeleafSessionTokenMapNotFoundException(ActionRuntime runtime) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the session token map for the hidden token");
        br.addItem("Advice");
        br.addElement("Call saveToken() in your action for the view");
        br.addElement("if you use la:token.");
        br.addItem("Action");
        br.addElement(runtime);
        final String msg = br.buildExceptionMessage();
        return new IllegalStateException(msg);
    }

    protected IllegalStateException createThymeleafGroupTokenNotFoundException(ActionRuntime runtime) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the token group for the action type.");
        br.addItem("Action");
        br.addElement(runtime);
        final String msg = br.buildExceptionMessage();
        return new IllegalStateException(msg);
    }

    protected void throwThymeleafTokenNotHiddenTypeException(ActionRuntime runtime, String inputType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the token attribute except hidden type.");
        br.addItem("Advice");
        br.addElement("The la:token attribute should be used at hidden type like this:");
        br.addElement("  (x):");
        br.addElement("    <input type=\"text\" th:token=\"true\"/> // *Bad");
        br.addElement("  (o):");
        br.addElement("    <input type=\"hidden\" th:token=\"true\"/> // Good");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Input Type");
        br.addElement(inputType);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwThymeleafTokenNotInputTypeException(ActionRuntime runtime, String tagName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the token attribute except input tag.");
        br.addItem("Advice");
        br.addElement("The la:token attribute should be used at input type like this:");
        br.addElement("  (x):");
        br.addElement("    <form th:action=\"...\" th:token=\"true\"/> // *Bad");
        br.addElement("  (o):");
        br.addElement("    <input type=\"hidden\" th:token=\"true\"/> // Good");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Tag Name");
        br.addElement(tagName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
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
