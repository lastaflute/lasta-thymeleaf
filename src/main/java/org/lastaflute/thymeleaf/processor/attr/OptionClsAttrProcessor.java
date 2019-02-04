/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.List;

import org.lastaflute.thymeleaf.processor.attr.option.ExpressionAttributeTagInitOption;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.StringUtils;

/**
 * Processor for Option Attribute of Select Tag with Classification Definition.
 * <pre>
 * Usage:
 *   &lt;select <b>la:property="status"</b>&gt;
 *     &lt;option&gt;&lt;/option&gt;
 *     &lt;option <b>la:optionCls="'MemberStatus'"</b>&gt;&lt;/option&gt;
 *   &lt;/select&gt;
 *
 * This means is :
 *   &lt;select name="status"&gt;
 *     &lt;option&gt;&lt;/option&gt;
 *     &lt;option <b>th:each="cdef : ${#cls.listAll('MemberStatus')}" th:value="${#cls.code(cdef)}" th:text="${#cls.alias(status)}"</b> th:selected="${cdef} == ${status}"&gt;&lt;/option&gt;
 *   &lt;/select&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;select name="status"&gt;
 *     &lt;option&gt;&lt;/option&gt;
 *     &lt;option selected="selected" value="FML"&gt;Formalized&lt;/option&gt;
 *     &lt;option value="WDL"&gt;Withdrawal&lt;/option&gt;
 *     &lt;option value="PRV"&gt;Provisional&lt;/option&gt;
 *   &lt;/select&gt;
 *
 * 2.If Custom text use.("prod" is iteration variable)
 *   &lt;select la:proparty="status"&gt;
 *     &lt;option&gt;&lt;/option&gt;
 *     &lt;option <b>la:optionCls="prod : 'MemberStatus'"</b> th:text="${#cls.code(prod)} + ' : ' + ${#cls.alias(prod)}"&gt;&lt;/option&gt;
 *   &lt;/select&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;select name="status"&gt;
 *     &lt;option&gt;&lt;/option&gt;
 *     &lt;option selected="selected" value="FML"&gt;FML : Formalized&lt;/option&gt;
 *     &lt;option value="WDL"&gt;WDL : Withdrawal&lt;/option&gt;
 *     &lt;option value="PRV"&gt;PRV : Provisional&lt;/option&gt;
 *   &lt;/select&gt;
 * </pre>
 * @author schatten
 * @author jflute
 * @author p1us2er0
 */
public class OptionClsAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTR_NAME = "optionCls";
    public static final int PRECEDENCE = 200;
    public static final boolean REMOVE_ATTRIBUTE = true;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OptionClsAttrProcessor(String dialectPrefix, ExpressionAttributeTagInitOption option) {
        super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, REMOVE_ATTRIBUTE, option.isRestrictedExpressionExecution());
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue,
            Object expressionResult, IElementTagStructureHandler structureHandler) {
        final String optionClsName = expressionResult.toString();
        structureHandler.setAttribute("th:each", String.format("cdef : ${#cls.listAll('%s')}", optionClsName));
        structureHandler.setAttribute("th:value", "${cdef.code()}");
        structureHandler.setAttribute("th:text", "${cdef.alias()}");

        List<IProcessableElementTag> elementStack = context.getElementStack();
        if (elementStack.size() >= 2) {
            IProcessableElementTag parentTag = elementStack.get(elementStack.size() - 2);
            String propertyName = parentTag.getAttributeValue(getDialectPrefix(), "property");
            if (!StringUtils.isEmpty(propertyName)) {
                structureHandler.setAttribute("th:selected", String.format("${cdef} == ${%s}", propertyName));
            }
        }
    }
}
