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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.ForEachAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.MistakeAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.OptionClsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.PropertyAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.TokenAttrProcessor;
import org.thymeleaf.Configuration;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractXHTMLEnabledDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * Lasta Thymeleaf Dialect.
 * @author schatten
 * @author jflute
 */
public class LastaThymeleafMistakeDialect extends AbstractXHTMLEnabledDialect implements IExpressionEnhancingDialect {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String LASTA_TYMELEAF_DIALECT_PREFIX = "th";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Configuration configuration;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafMistakeDialect(Configuration configuration) {
        this.configuration = configuration;
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    public String getPrefix() {
        return LASTA_TYMELEAF_DIALECT_PREFIX;
    }

    @Override
    public Set<IProcessor> getProcessors() {
        return createLastaProcessorsSet();
    }

    protected Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(newMistakeAttrProcessor(ErrorsAttrProcessor.ATTRIBUTE_NAME));
        processors.add(newMistakeAttrProcessor(ForEachAttrProcessor.ATTRIBUTE_NAME));
        processors.add(newMistakeAttrProcessor(OptionClsAttrProcessor.ATTRIBUTE_NAME));
        processors.add(newMistakeAttrProcessor(PropertyAttrProcessor.ATTRIBUTE_NAME));
        processors.add(newMistakeAttrProcessor(TokenAttrProcessor.ATTRIBUTE_NAME));
        return processors;
    }

    protected MistakeAttrProcessor newMistakeAttrProcessor(String name) {
        return new MistakeAttrProcessor(name);
    }

    @Override
    public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
        return new HashMap<>();
    }
}
