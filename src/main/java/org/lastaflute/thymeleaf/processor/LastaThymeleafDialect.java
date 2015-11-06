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
    private final Set<IProcessor> additionalProcessors = new LinkedHashSet<IProcessor>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafDialect(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    /**
     * Get prefix of attribute name.
     * @see org.thymeleaf.dialect.IDialect#getPrefix()
     */
    @Override
    public String getPrefix() {
        return LASTA_TYMELEAF_DIALECT_PREFIX;
    }

    /**
     * @see org.thymeleaf.dialect.AbstractDialect#getProcessors()
     */
    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = createLastaProcessorsSet();
        final Set<IProcessor> dialectAdditionalProcessors = getAdditionalProcessors();

        if (!dialectAdditionalProcessors.isEmpty()) {
            processors.addAll(dialectAdditionalProcessors);
        }

        return new LinkedHashSet<IProcessor>(processors);
    }

    /**
     * @see org.thymeleaf.dialect.IExpressionEnhancingDialect#getAdditionalExpressionObjects(org.thymeleaf.context.IProcessingContext)
     */
    @Override
    public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
        Map<String, Object> map = new HashMap<>();
        map.put("handydate", new ExpressionHandyDateProcessor());
        map.put("cdef", new ExpressionCDefProcessor(processingContext));
        return map;
    }

    public static Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(new PropertyAttrProcessor());
        processors.add(new OptionClsAttrProcessor());
        processors.add(new ForEachAttrProcessor());
        return processors;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Set<IProcessor> getAdditionalProcessors() {
        return Collections.unmodifiableSet(this.additionalProcessors);
    }

    public void addAdditionalProcessor(IProcessor processor) {
        this.additionalProcessors.add(processor);
    }
}
