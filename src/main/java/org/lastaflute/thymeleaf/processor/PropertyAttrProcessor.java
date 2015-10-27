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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Attribute;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.NestableNode;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.standard.processor.attr.StandardEachAttrProcessor;

/**
 * Property Attribute Processor.
 * <pre>
 * Usage:
 *   &lt;input type="text" la:property="memberName"/&gt;
 *
 * The result of processing this example will be as expected.
 *   - MemberName with set to "Ariel"
 *     &lt;input type="text" name="memberName" value="Ariel"/&gt;
 *   - When MemberName have validation error.
 *     &lt;input type="text" name="memberName" value="" class="validError"/&gt;
 * </pre>
 * @author schatten
 */
public class PropertyAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    private static final String PROPERTY_ATTRIBUTE_NAME = "property";
    private static final String EACH_PROPERTY_FORM_NAME = "'%s[' + ${%s.index} + '].%s'";
    private static final String APPEND_ERROR_STYLE_CLASS = "${errors.hasMessageOf('%s')} ? 'validError'";
    private static final String APPEND_ERROR_STYLE_CLASS_ATTRAPEND = "class=(${errors.hasMessageOf('%s')} ? ' validError')";
    private static final String EACH_ATTER_NAME = String.format("%s:%s", StandardDialect.PREFIX, StandardEachAttrProcessor.ATTR_NAME);

    protected static final String SELECT_PROPERTY_NAME = "la:selectPropertyName";

    public PropertyAttrProcessor() {
        super(PROPERTY_ATTRIBUTE_NAME);
    }

    @Override
    public int getPrecedence() {
        return 950;
    }

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
        if (name.indexOf(".") > 0) {
            String eachName = name.substring(0, name.indexOf(".")).trim();
            String statusVariable = eachName + "Stat";
            String eachAttr = getParentEachValue(element, eachName);
            if (eachAttr == null) {
                return name;
            }
            statusVariable = getProdStatusNameFromEachAttributeValue(eachAttr);
            String expressionPropertyName = getExpressionPropertyName(eachAttr);
            if (expressionPropertyName != null) {
                if (expressionPropertyName.indexOf(".") > 0) {
                    eachName = expressionPropertyName.substring(expressionPropertyName.lastIndexOf(".") + 1);
                } else {
                    eachName = expressionPropertyName;
                }
            }

            String nextName = name.substring(name.indexOf(".") + 1);
            return String.format(EACH_PROPERTY_FORM_NAME, eachName, statusVariable, nextName);
        }
        return name;
    }

    protected String getParentEachValue(Element element, String eachName) {
        NestableNode parent = element.getParent();
        if (parent instanceof Element) {
            // Thymeleaf bug ?? NestableAttributeHolderNode.attributesLen is every access result zero.
            //  if (((Element) parent).hasAttribute(EACH_ATTER_NAME)) {
            //      String attVal = ((Element) parent).getAttributeValueFromNormalizedName(EACH_ATTER_NAME);
            //      if (eachName.equals(getProdNameFromEachAttributeValue(attVal))) {
            //          return attVal;
            //      }
            //  }
            Attribute[] attributes = ((Element) parent).unsafeGetAttributes();
            if (attributes != null) {
                for (Attribute attribute : attributes) {
                    if (attribute != null && EACH_ATTER_NAME.equals(attribute.getNormalizedName())) {
                        if (eachName.equals(getProdNameFromEachAttributeValue(attribute.getValue()))) {
                            return attribute.getValue();
                        }
                    }
                }
            }
            if (parent.hasParent()) {
                return getParentEachValue((Element) parent, eachName);
            }
        }
        return null;
    }

    private String getProdNameFromEachAttributeValue(String eachAttrVal) {
        if (eachAttrVal == null) {
            return null;
        }
        int separate = eachAttrVal.indexOf(":");
        String prod = eachAttrVal.substring(0, separate);
        int statusProd = prod.indexOf(",");
        if (statusProd > 0) {
            return prod.substring(0, statusProd).trim();
        }
        return prod.trim();
    }

    private String getProdStatusNameFromEachAttributeValue(String eachAttrVal) {
        if (eachAttrVal == null) {
            return null;
        }
        int separate = eachAttrVal.indexOf(":");
        String prod = eachAttrVal.substring(0, separate);
        int statusProd = prod.indexOf(",");
        if (statusProd > 0) {
            return prod.substring(statusProd).trim();
        }
        return prod.trim() + "Stat";
    }

    private String getExpressionPropertyName(String eachAttrVal) {
        if (eachAttrVal == null) {
            return null;
        }
        int separate = eachAttrVal.indexOf(":");
        String expression = eachAttrVal.substring(separate + 1).trim();
        Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z_0-9.]+)\\}");
        Matcher matcher = pattern.matcher(expression);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

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
