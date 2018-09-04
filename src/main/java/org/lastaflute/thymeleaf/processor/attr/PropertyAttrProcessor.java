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

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

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
public class PropertyAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTR_NAME = "property";
    public static final int PRECEDENCE = 950;
    public static final boolean REMOVE_ATTRIBUTE = true;
    public static final boolean RESTRICTED_EXPRESSION_EXECUTION = false; // #thinking can be true? need to research behavior when thymeleaf2 by jflute

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public PropertyAttrProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, REMOVE_ATTRIBUTE, RESTRICTED_EXPRESSION_EXECUTION);
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue,
            Object expressionResult, IElementTagStructureHandler structureHandler) {
        // #thinking nest property. by p1us2er0 (2018/09/04)
        final String propertyName = expressionResult.toString();
        boolean hasThName = tag.hasAttribute(StandardDialect.PREFIX, "name");
        boolean hasThText = tag.hasAttribute(StandardDialect.PREFIX, "text");
        boolean hasThValue = tag.hasAttribute(StandardDialect.PREFIX, "value");

        if (!hasThName) {
            structureHandler.setAttribute("th:name", propertyName);
        }

        switch (tag.getElementCompleteName()) {
        case "input":
            if (!hasThName) {
                structureHandler.setAttribute("th:name", propertyName);
            }
            if (!hasThValue) {
                String type = tag.getAttributeValue("type");
                if (!("checkbox".equals(type) || "radio".equals(type))) {
                    structureHandler.setAttribute("th:value", "${" + propertyName + "}");
                }
            }
            break;
        case "select":
            if (!hasThName) {
                structureHandler.setAttribute("th:name", propertyName);
            }
            // #thinking organize. by p1us2er0 (2018/09/04)
            //element.setNodeProperty(SELECT_PROPERTY_NAME, propertyName);
            break;
        case "textarea":
            if (!hasThName) {
                structureHandler.setAttribute("th:name", propertyName);
            }
            if (!hasThText) {
                structureHandler.setAttribute("th:text", "${" + propertyName + "}");
            }
            break;
        default:
            if (!hasThText) {
                structureHandler.setAttribute("th:text", "${" + propertyName + "}");
            }
            break;
        }
    }
}
