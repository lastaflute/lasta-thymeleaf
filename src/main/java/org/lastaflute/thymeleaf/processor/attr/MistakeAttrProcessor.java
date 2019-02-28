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

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * @author jflute
 */
public class MistakeAttrProcessor extends AbstractAttributeTagProcessor {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MistakeAttrProcessor(String dialectPrefix, String attrName) {
        super(TemplateMode.HTML, dialectPrefix, /*elementName*/null //
                , /*prefixElementName*/false, attrName, /*prefixAttributeName*/false //
                , /*precedence*/1, true);
    }

    // ===================================================================================
    //                                                                             Process
    //                                                                             =======
    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue,
            IElementTagStructureHandler structureHandler) {
        if (isOutOfTarget(tag, attributeName, attributeValue)) { // e.g. <meta property="sea">
            return;
        }
        final String name = attributeName.getAttributeName();
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Mistaking prefix for the Lasta Thymeleaf name.");
        br.addItem("Advice");
        br.addElement("Use formal prefix for Lasta Thymeleaf 'la:' like this:");
        br.addElement("  (x):");
        br.addElement("    th:" + name + "=\"...\" // *Bad");
        br.addElement("  (o):");
        br.addElement("    la:" + name + "=\"...\" // Good");
        br.addItem("Template File");
        br.addElement(tag.getTemplateName());
        br.addItem("Tag Name");
        br.addElement(tag.getElementDefinition());
        br.addItem("Attribute on Tag");
        br.addElement(attributeName + "=\"" + tag.getAttributeValue(attributeName) + "\"");
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected boolean isOutOfTarget(IProcessableElementTag tag, AttributeName attributeName, String attributeValue) {
        final String tagPlainName = tag.getElementCompleteName();
        final IAttribute attribute = tag.getAttribute(attributeName);
        if (attribute == null) { // no way, just in case (because of additional process here)
            return false;
        }
        final String attrPlainName = attribute.getAttributeCompleteName();
        if (isMetaTagPropertyAttr(tagPlainName, attrPlainName)) { // e.g. <meta property="sea"> 
            // "property=..." is also coming here since thymeleaf3
            // but cannot determine "th:property=..." and just "property=..."
            // so ignore check at the case
            return true;
        }
        return false;
    }

    protected boolean isMetaTagPropertyAttr(String tagPlainName, String attrPlainName) {
        return "meta".equalsIgnoreCase(tagPlainName) && "property".equalsIgnoreCase(attrPlainName);
    }
}
