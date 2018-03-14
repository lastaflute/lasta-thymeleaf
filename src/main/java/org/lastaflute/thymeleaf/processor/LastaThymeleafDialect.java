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
package org.lastaflute.thymeleaf.processor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.lastaflute.thymeleaf.customizer.ThymeleafAdditionalExpressionSetupper;
import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.expression.HandyDateExpressionProcessor;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * Lasta Thymeleaf Dialect.
 * @author schatten
 * @author jflute
 */
public class LastaThymeleafDialect extends AbstractProcessorDialect {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LASTA_THYMELEAF_DIALECT_PREFIX = "la";
    protected static final HandyDateExpressionProcessor HANDY_DATE_EXPRESSION_PROCESSOR = new HandyDateExpressionProcessor();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<IProcessor> additionalProcessors = new LinkedHashSet<IProcessor>();
    protected ThymeleafAdditionalExpressionSetupper additionalExpressionSetupper; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafDialect() {
        super("lasta", LASTA_THYMELEAF_DIALECT_PREFIX, 1000);
    }

    public LastaThymeleafDialect additionalExpression(ThymeleafAdditionalExpressionSetupper additionalExpressionSetupper) {
        if (additionalExpressionSetupper == null) {
            throw new IllegalArgumentException("The argument 'additionalExpressionSetupper' should not be null.");
        }
        this.additionalExpressionSetupper = additionalExpressionSetupper;
        return this;
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = createLastaProcessorsSet();
        processors.addAll(getAdditionalProcessors());
        return processors;
    }

    protected Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(createErrorsAttrProcessor());
        // TODO jflute #thymeleaf3 processors.add(createForEachAttrProcessor()) (2017/11/30)
        //processors.add(createForEachAttrProcessor());
        //processors.add(createOptionClsAttrProcessor());
        //processors.add(createPropertyAttrProcessor());
        //processors.add(createTokenAttrProcessor());
        return processors;
    }

    protected ErrorsAttrProcessor createErrorsAttrProcessor() {
        return new ErrorsAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX);
    }

    // TODO jflute #thymeleaf3 protected ForEachAttrProcessor createForEachAttrProcessor() (2017/11/30)
    //protected ForEachAttrProcessor createForEachAttrProcessor() {
    //    return new ForEachAttrProcessor();
    //}
    //
    //protected OptionClsAttrProcessor createOptionClsAttrProcessor() {
    //    return new OptionClsAttrProcessor();
    //}
    //
    //protected PropertyAttrProcessor createPropertyAttrProcessor() {
    //    return new PropertyAttrProcessor();
    //}
    //
    //protected TokenAttrProcessor createTokenAttrProcessor() {
    //    return new TokenAttrProcessor();
    //}

    // TODO jflute #thymeleaf3 getAdditionalExpressionObjects() (2017/11/30)
    //@Override
    //public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
    //    final Map<String, Object> processorMap = new HashMap<>();
    //    processorMap.put("handy", HANDY_DATE_EXPRESSION_PROCESSOR); // stateless so recycle
    //    processorMap.put("cls", createClassificationExpressionProcessor(processingContext));
    //    if (additionalExpressionSetupper != null) {
    //        final ThymeleafAdditionalExpressionResource resource = createThymeleafCustomExpressionResource(processingContext);
    //        additionalExpressionSetupper.setup(resource);
    //        resource.getProcessorMap().forEach((key, processor) -> {
    //            if (processorMap.containsKey(key)) {
    //                String msg = "The processor key already exists in processor map: " + key + ", " + processorMap.keySet();
    //                throw new IllegalStateException(msg);
    //            }
    //            processorMap.put(key, processor);
    //        });
    //    }
    //    return processorMap;
    //}
    //
    //protected ClassificationExpressionProcessor createClassificationExpressionProcessor(IProcessingContext processingContext) {
    //    return new ClassificationExpressionProcessor(processingContext);
    //}
    //
    //protected ThymeleafAdditionalExpressionResource createThymeleafCustomExpressionResource(IProcessingContext processingContext) {
    //    return new ThymeleafAdditionalExpressionResource(processingContext);
    //}

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
