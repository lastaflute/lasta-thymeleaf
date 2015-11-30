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

import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;

/**
 * @author jflute
 */
public class MistakeAttrProcessor extends AbstractAttributeModifierAttrProcessor {

    protected final String name;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MistakeAttrProcessor(String name) {
        super(name);
        this.name = name;
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    public int getPrecedence() {
        return 1;
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Mistaking prefix for the Lasta Thymeleaf name.");
        br.addItem("Advice");
        br.addElement("Use formal prefix for Lasta Thymeleaf 'la:' like this:");
        br.addElement("  (x):");
        br.addElement("    th:" + name + "=\"...\" // *Bad");
        br.addElement("  (o):");
        br.addElement("    la:" + name + "=\"...\" // Good");
        br.addItem("Your Attribute");
        br.addElement(attributeName + "=\"" + element.getAttributeValue(attributeName) + "\"");
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
