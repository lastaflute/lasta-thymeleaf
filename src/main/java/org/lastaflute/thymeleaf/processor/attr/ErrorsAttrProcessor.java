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

import org.dbflute.util.Srl;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Errors Attribute Processor.
 * <pre>
 * Usage:
 *   &lt;span <b>la:errors="sea"</b>/&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;span class="errors"&gt;<b>is required</b>&lt;/span&gt;
 * </pre>
 * @author jflute
 * @author p1us2er0
 */
public class ErrorsAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                     Constructor Value
    //                                     -----------------
    public static final String ATTR_NAME = "errors"; // e.g. la:errors="sea"
    public static final int PRECEDENCE = 950;
    public static final boolean REMOVE_ATTRIBUTE = true;
    public static final boolean RESTRICTED_EXPRESSION_EXECUTION = false; // #thinking can be true? need to research behavior when thymeleaf2 by jflute

    // -----------------------------------------------------
    //                                      Changeable Value
    //                                      ----------------
    public static final String DEFAULT_STYLE = "errors"; // e.g. class="errors"

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ErrorsAttrProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, REMOVE_ATTRIBUTE, RESTRICTED_EXPRESSION_EXECUTION);
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue,
            Object expressionResult, IElementTagStructureHandler structureHandler) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // <span la:errors="sea"/>
        //  ||
        //  vv
        // <span class="errors" th:each="er : ${errors.part('sea')}" th:text="is required"/>
        //  ||
        //  vv
        // <span class="errors">is required</span>
        // <span class="errors">is funny</span>
        // ...
        // _/_/_/_/_/_/_/_/_/_/
        final String specifiedValue = expressionResult.toString();
        structureHandler.setAttribute("class", prepareOverridingStyle(tag));
        final String eachValue;
        if ("all".equalsIgnoreCase(specifiedValue)) {
            eachValue = "er : ${errors.all}";
        } else {
            eachValue = "er : ${errors.part('" + specifiedValue + "')}";
        }
        structureHandler.setAttribute("th:each", eachValue);
        structureHandler.setAttribute("th:text", "${er.message}");
    }

    protected String prepareOverridingStyle(IProcessableElementTag tag) {
        final String classAttr = tag.getAttributeValue("class");
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
    // done, cannot select it in this super class, default is SUBSTITUTION... by jflute (2018/04/11)
    // but la:errors may not be related to modification type so out of migration target
    //@Override
    //protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName, String newAttributeName) {
    //    return ModificationType.SUBSTITUTION;
    //}

    // #thinking what is recompute? by jflute
    //@Override
    //protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
    //    return true;
    //}
}
