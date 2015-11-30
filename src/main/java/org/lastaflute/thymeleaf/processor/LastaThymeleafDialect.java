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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.ForEachAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.OptionClsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.PropertyAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.TokenAttrProcessor;
import org.lastaflute.thymeleaf.processor.expression.ClassificationExpressionProcessor;
import org.lastaflute.thymeleaf.processor.expression.HandyDateExpressionProcessor;
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
public class LastaThymeleafDialect extends AbstractXHTMLEnabledDialect implements IExpressionEnhancingDialect {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String LASTA_TYMELEAF_DIALECT_PREFIX = "la";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Configuration configuration;
    protected final Set<IProcessor> additionalProcessors = new LinkedHashSet<IProcessor>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafDialect(Configuration configuration) {
        this.configuration = configuration;
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    /**
     * Get prefix of attribute name.
     */
    @Override
    public String getPrefix() {
        return LASTA_TYMELEAF_DIALECT_PREFIX;
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = createLastaProcessorsSet();
        processors.addAll(getAdditionalProcessors());
        return new LinkedHashSet<IProcessor>(processors);
    }

    protected Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(createErrorsAttrProcessor());
        processors.add(createForEachAttrProcessor());
        processors.add(createOptionClsAttrProcessor());
        processors.add(createPropertyAttrProcessor());
        processors.add(createTokenAttrProcessor());
        return processors;
    }

    protected ErrorsAttrProcessor createErrorsAttrProcessor() {
        return new ErrorsAttrProcessor();
    }

    protected ForEachAttrProcessor createForEachAttrProcessor() {
        return new ForEachAttrProcessor();
    }

    protected OptionClsAttrProcessor createOptionClsAttrProcessor() {
        return new OptionClsAttrProcessor();
    }

    protected PropertyAttrProcessor createPropertyAttrProcessor() {
        return new PropertyAttrProcessor();
    }

    protected TokenAttrProcessor createTokenAttrProcessor() {
        return new TokenAttrProcessor();
    }

    @Override
    public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
        final Map<String, Object> map = new HashMap<>();
        map.put("handy", new HandyDateExpressionProcessor());
        map.put("cls", new ClassificationExpressionProcessor(processingContext));
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Set<IProcessor> getAdditionalProcessors() {
        return Collections.unmodifiableSet(additionalProcessors);
    }

    public void addAdditionalProcessor(IProcessor processor) {
        this.additionalProcessors.add(processor);
    }
}
