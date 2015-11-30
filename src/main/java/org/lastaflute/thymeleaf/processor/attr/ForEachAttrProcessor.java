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

import org.lastaflute.thymeleaf.internal.processor.attr.AbstractIterationAttrProcessor;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.Each;
import org.thymeleaf.standard.expression.EachUtils;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.SelectionVariableExpression;
import org.thymeleaf.standard.expression.VariableExpression;
import org.thymeleaf.util.StringUtils;

/**
 * Foreach Attribute Processor.
 * Base function is Same to {@link org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor StandardEachAttrProcessor}.<br>
 * Add Support to write array name variable with list form property to name attribute.
 * <pre>
 * Usage: e.g.
 *
 *   [HTML]
 *     &lt;dl la:foreach="item : <b>${items}</b>"&gt;
 *       &lt;dt la:property="item.product.name"&gt;Sample Name&lt;/dt&gt;
 *       &lt;dd&gt;Qty:&lt;input la:property="item.quantity"/&gt;&lt;/dd&gt;
 *     &lt;/dl&gt;
 *
 *   [Form Bean]
 *     public class PurchaseForm {
 *         public List&lt;CartProduct&gt; items;
 *         ...
 *     }
 *     public class CartProduct {
 *         public Product product;
 *         public Integer quantity;
 *         ...
 *     }
 *
 * The result of processing this example will be as expected.
 *     &lt;dl&gt;
 *       &lt;dt&gt;Rosemary (100g)&lt;/dt&gt;
 *       &lt;dd&gt;Qty:&lt;input <b>name="items[0].quantity"</b> value=""/&gt;&lt;/dd&gt;
 *     &lt;/dl&gt;
 *     &lt;dl&gt;
 *       &lt;dt&gt;Lemon Grass (100g)&lt;/dt&gt;
 *       &lt;dd&gt;Qty:&lt;input <b>name="items[1].quantity"</b> value=""/&gt;&lt;/dd&gt;
 *     &lt;/dl&gt;
 *
 * </pre>
 * @author schatten
 * @see org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor StandardEachAttrProcessor(Original)
 */
public class ForEachAttrProcessor extends AbstractIterationAttrProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTRIBUTE_NAME = "foreach";
    public static final int ATTR_PRECEDENCE = 200;

    protected static final String ITERATION_SPEC_VAR_SUFFIX = "Spec";
    public static final String FORM_PROPERTY_PATH_VER = "foreach_form_property_path";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ForEachAttrProcessor() {
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
        return ATTR_PRECEDENCE;
    }

    /**
     * @see org.lastaflute.thymeleaf.internal.processor.attr.AbstractIterationAttrProcessor#processClonedHostIterationElement(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     * @see org.thymeleaf.standard.processor.attr.AbstractStandardIterationAttrProcessor#processClonedHostIterationElement(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     */
    @Override
    protected void processClonedHostIterationElement(Arguments arguments, Element iteratedChild, String attributeName) {
        // Nothing to be done here, no additional processings for iterated elements
    }

    /**
     * @see org.lastaflute.thymeleaf.internal.processor.attr.AbstractIterationAttrProcessor#getIterationSpec(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     * @see org.thymeleaf.standard.processor.attr.AbstractStandardIterationAttrProcessor#getIterationSpec(org.thymeleaf.Arguments, org.thymeleaf.dom.Element, java.lang.String)
     */
    @Override
    protected final IterationSpec getIterationSpec(final Arguments arguments, final Element element, final String attributeName) {

        final String attributeValue = element.getAttributeValue(attributeName);

        final Configuration configuration = arguments.getConfiguration();

        final Each each = EachUtils.parseEach(configuration, arguments, attributeValue);

        final IStandardExpression iterVarExpr = each.getIterVar();
        final Object iterVarValue = iterVarExpr.execute(configuration, arguments);

        final IStandardExpression statusVarExpr = each.getStatusVar();
        final Object statusVarValue;
        if (statusVarExpr != null) {
            statusVarValue = statusVarExpr.execute(configuration, arguments);
        } else {
            statusVarValue = null;
        }

        final IStandardExpression iterableExpr = each.getIterable();
        final Object iteratedValue = iterableExpr.execute(configuration, arguments);

        final String iterVarName = (iterVarValue == null ? null : iterVarValue.toString());
        if (StringUtils.isEmptyOrWhitespace(iterVarName)) {
            throw new TemplateProcessingException("Iteration variable name expression evaluated as null: \"" + iterVarExpr + "\"");
        }

        final String statusVarName = (statusVarValue == null ? null : statusVarValue.toString());
        if (statusVarExpr != null && StringUtils.isEmptyOrWhitespace(statusVarName)) {
            throw new TemplateProcessingException("Status variable name expression evaluated as null or empty: \"" + statusVarExpr + "\"");
        }

        // Extends form property path access.
        String currentPropertyName = getCurrentPropertyNameFromIterVarExpr(iterableExpr);
        if (currentPropertyName != null) {
            String parentPath = (String) arguments.getLocalVariable(FORM_PROPERTY_PATH_VER);
            String propertyPath = parentPath == null ? currentPropertyName : parentPath + "." + currentPropertyName;
            return new ForEachIterationSpec(iterVarName, statusVarName, iteratedValue, propertyPath);
        }
        return new IterationSpec(iterVarName, statusVarName, iteratedValue);

    }

    protected String getCurrentPropertyNameFromIterVarExpr(IStandardExpression iterVarExpr) {
        String expression;
        if (iterVarExpr instanceof SelectionVariableExpression) {
            expression = ((SelectionVariableExpression) iterVarExpr).getExpression();
        } else if (iterVarExpr instanceof VariableExpression) {
            expression = ((VariableExpression) iterVarExpr).getExpression();
        } else {
            return null;
        }
        int lastIndexOf = expression.lastIndexOf(".");
        if (lastIndexOf < 0) {
            return expression;
        }
        return expression.substring(lastIndexOf + 1);
    }

    /**
     * @see org.lastaflute.thymeleaf.internal.processor.attr.AbstractIterationAttrProcessor#prepareLocalVariablesForEachIterationItem(org.thymeleaf.dom.Element, org.lastaflute.thymeleaf.internal.processor.attr.AbstractIterationAttrProcessor.IterationSpec, int, int, java.lang.Object) Internal
     * @see org.thymeleaf.processor.attr.AbstractIterationAttrProcessor#processAttribute(Arguments, Element, String) Original
     */
    @Override
    protected void prepareLocalVariablesForEachIterationItem(final Element clonedElement, final IterationSpec iterationSpec, final int size,
            int index, final Object obj) {
        final String iterVar = iterationSpec.getIterVarName();
        final String statusVar = iterationSpec.getStatusVarName();
        /*
         * Prepare local variables that will be available for each iteration item
         */
        clonedElement.setNodeLocalVariable(iterVar, obj);
        StatusVar status;
        if (iterationSpec instanceof ForEachIterationSpec) {
            String formNamePath = String.format("%s[%d]", ((ForEachIterationSpec) iterationSpec).getPropertyPath(), index);
            status = new ForEachStatusVar(index, index + 1, size, obj, formNamePath);
            clonedElement.setNodeLocalVariable(FORM_PROPERTY_PATH_VER, formNamePath);
        } else {
            status = new StatusVar(index, index + 1, size, obj);
        }
        if (statusVar != null) {
            clonedElement.setNodeLocalVariable(statusVar, status);
        } else {
            clonedElement.setNodeLocalVariable(iterVar + DEFAULT_STATUS_VAR_SUFFIX, status);
        }
        clonedElement.setNodeLocalVariable(iterVar + ITERATION_SPEC_VAR_SUFFIX, iterationSpec);
    }

    // ===================================================================================
    //                                                                     Internal Object
    //                                                                     ===============
    public static class ForEachStatusVar extends StatusVar {

        private final String propertyPath;

        public ForEachStatusVar(int index, int count, int size, Object current) {
            super(index, count, size, current);
            this.propertyPath = null;
        }

        public ForEachStatusVar(int index, int count, int size, Object current, String propertyPath) {
            super(index, count, size, current);
            this.propertyPath = propertyPath;
        }

        public String getPropertyPath() {
            return propertyPath;
        }
    }

    public static class ForEachIterationSpec extends IterationSpec {
        private final String propertyPath;

        public ForEachIterationSpec(String iterVarName, String statusVarName, Object iteratedObject, String propertyPath) {
            super(iterVarName, statusVarName, iteratedObject);
            this.propertyPath = propertyPath;
        }

        public String getPropertyPath() {
            return this.propertyPath;
        }
    }
}
