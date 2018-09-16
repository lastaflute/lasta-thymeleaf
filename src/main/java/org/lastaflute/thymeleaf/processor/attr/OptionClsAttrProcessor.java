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

import java.util.List;

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
 */
public class OptionClsAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTR_NAME = "optionCls";
    public static final int PRECEDENCE = 200;
    public static final boolean REMOVE_ATTRIBUTE = true;
    public static final boolean RESTRICTED_EXPRESSION_EXECUTION = false; // #thinking can be true? need to research behavior when thymeleaf2 by jflute

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public OptionClsAttrProcessor(String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, REMOVE_ATTRIBUTE, RESTRICTED_EXPRESSION_EXECUTION);
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

    // TODO jflute #thymeleaf3 OptionClsAttrProcessor (2018/03/14)
    // extends AbstractAttributeModifierAttrProcessor
    //
    //// ===================================================================================
    ////                                                                          Definition
    ////                                                                          ==========
    //public static final String ATTRIBUTE_NAME = "optionCls";
    //private static final String ELEMENT_TARGET = "option";
    //private static final String DEFAULT_ITERATION_VALUE = "cdef";
    //
    //// ===================================================================================
    ////                                                                         Constructor
    ////                                                                         ===========
    //public OptionClsAttrProcessor() {
    //    super(new AttributeNameProcessorMatcher(ATTRIBUTE_NAME, ELEMENT_TARGET));
    //}
    //
    //// ===================================================================================
    ////                                                                          Implements
    ////                                                                          ==========
    //@Override
    //public int getPrecedence() {
    //    return 200;
    //}
    //
    //@Override
    //protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
    //    final IterationSpec spec = getIterationSpec(arguments, element, attributeName);
    //    final String iterVarName = spec.getIterVarName();
    //    final Map<String, String> values = new HashMap<String, String>();
    //    final String statusVarName = spec.getStatusVarName();
    //    final Object classificationName = spec.getClassificationName();
    //    values.put("th:each", String.format("%s, %s : ${#cls.list('%s')}", iterVarName, statusVarName, classificationName));
    //    if (!element.hasNormalizedAttribute(StandardDialect.PREFIX, "value")) {
    //        values.put("th:value", String.format("${#cls.code(%s)}", iterVarName));
    //    }
    //    if (!element.hasNormalizedAttribute(StandardDialect.PREFIX, "text")) {
    //        values.put("th:text", String.format("${#cls.alias(%s)}", iterVarName));
    //    }
    //    if (!element.hasNormalizedAttribute(StandardDialect.PREFIX, "selected")) {
    //        final String selectPropertyNamme = getParentSelectPropertyName(element);
    //        if (selectPropertyNamme != null) {
    //            values.put("th:selected", String.format("${%s} == ${%s}", iterVarName, selectPropertyNamme));
    //        }
    //    }
    //    return values;
    //}
    //
    //protected String getParentSelectPropertyName(Element element) {
    //    if (element.hasNodeProperty(PropertyAttrProcessor.SELECT_PROPERTY_NAME)) {
    //        return (String) element.getNodeProperty(PropertyAttrProcessor.SELECT_PROPERTY_NAME);
    //    }
    //    if ("select".equals(element.getNormalizedName())) {
    //        return null;
    //    }
    //    return getParentSelectPropertyName((Element) element.getParent());
    //}
    //
    //@Override
    //protected ModificationType getModificationType(Arguments arguments, Element element, String attributeName, String newAttributeName) {
    //    return ModificationType.SUBSTITUTION;
    //}
    //
    //@Override
    //protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String attributeName, String newAttributeName) {
    //    return true;
    //}
    //
    //@Override
    //protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String attributeName) {
    //    return true;
    //}
    //
    //protected IterationSpec getIterationSpec(final Arguments arguments, final Element element, final String attributeName) {
    //    final Configuration configuration = arguments.getConfiguration();
    //
    //    // Obtain the Thymeleaf Standard Expression parser
    //    final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
    //
    //    // Obtain the attribute value
    //    final String attributeValue = element.getAttributeValue(attributeName);
    //    int separateIndex = attributeValue.indexOf(":");
    //    if (separateIndex < 0) {
    //        // Parse the attribute value as a Thymeleaf Standard Expression
    //        final IStandardExpression expression = parser.parseExpression(configuration, arguments, attributeValue);
    //
    //        final String classificationName = expression.execute(configuration, arguments).toString();
    //        final String iterVarName = DEFAULT_ITERATION_VALUE;
    //        final String statusVarName = iterVarName + AbstractIterationAttrProcessor.DEFAULT_STATUS_VAR_SUFFIX;
    //        return new IterationSpec(iterVarName, statusVarName, classificationName);
    //    }
    //
    //    String classification = attributeValue.substring(separateIndex + 1).trim();
    //    String iterVarName = attributeValue.substring(0, separateIndex).trim();
    //    String statusVarName = null;
    //    int statusSeparate = iterVarName.indexOf(",");
    //    if (statusSeparate < 0) {
    //        statusVarName = iterVarName + AbstractIterationAttrProcessor.DEFAULT_STATUS_VAR_SUFFIX;
    //    } else {
    //        statusVarName = iterVarName.substring(statusSeparate + 1).trim();
    //        iterVarName = iterVarName.substring(0, statusSeparate).trim();
    //    }
    //    // Parse the attribute value as a Thymeleaf Standard Expression
    //    final IStandardExpression expression = parser.parseExpression(configuration, arguments, classification);
    //    final String classificationName = expression.execute(configuration, arguments).toString();
    //    return new IterationSpec(iterVarName, statusVarName, classificationName);
    //}
    //
    //// ===================================================================================
    ////                                                                     Internal Object
    ////                                                                     ===============
    //protected static class IterationSpec {
    //
    //    private final String iterVarName;
    //    private final String statusVarName;
    //    private final String classificationName;
    //
    //    public IterationSpec(final String iterVarName, final String statusVarName, final String classificationName) {
    //        super();
    //        Validate.notEmpty(iterVarName, "Iteration var name cannot be null or empty");
    //        this.iterVarName = iterVarName;
    //        this.statusVarName = statusVarName;
    //        this.classificationName = classificationName;
    //    }
    //
    //    public String getIterVarName() {
    //        return this.iterVarName;
    //    }
    //
    //    public String getStatusVarName() {
    //        return this.statusVarName;
    //    }
    //
    //    public Object getClassificationName() {
    //        return this.classificationName;
    //    }
    //}
}
