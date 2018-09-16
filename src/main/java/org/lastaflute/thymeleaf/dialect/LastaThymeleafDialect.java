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
package org.lastaflute.thymeleaf.dialect;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.dbflute.util.DfCollectionUtil;
import org.lastaflute.thymeleaf.customizer.ThymeleafAdditionalExpressionResource;
import org.lastaflute.thymeleaf.customizer.ThymeleafAdditionalExpressionSetupper;
import org.lastaflute.thymeleaf.expression.ClassificationExpressionObject;
import org.lastaflute.thymeleaf.expression.HandyDateExpressionObject;
import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.OptionClsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.PropertyAttrProcessor;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;

/**
 * Lasta Thymeleaf Dialect.
 * @author schatten
 * @author jflute
 * @author p1us2er0
 */
public class LastaThymeleafDialect extends AbstractProcessorDialect implements IExpressionObjectDialect {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String LASTA_THYMELEAF_DIALECT_PREFIX = "la";

    protected static final String EXPRESSION_OBJECT_CLASSIFICATION = "cls";
    protected static final String EXPRESSION_OBJECT_HANDY = "handy";
    protected static final Set<String> allExpressionObjectNames;
    static {
        allExpressionObjectNames = DfCollectionUtil.newHashSet(EXPRESSION_OBJECT_CLASSIFICATION, EXPRESSION_OBJECT_HANDY);
    }
    protected static final HandyDateExpressionObject HANDY_DATE_EXPRESSION_OBJECT = new HandyDateExpressionObject();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<IProcessor> additionalProcessors = new LinkedHashSet<IProcessor>();
    protected final LastaExpressionObjectFactory expressionObjectFactory = new LastaExpressionObjectFactory();
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
        processors.add(createPropertyAttrProcessor());
        processors.add(createOptionClsAttrProcessor());
        // TODO jflute #thymeleaf3 pri.B processors.add(createTokenAttrProcessor()) (2017/11/30)
        //processors.add(createTokenAttrProcessor());
        return processors;
    }

    protected ErrorsAttrProcessor createErrorsAttrProcessor() {
        return new ErrorsAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX);
    }

    protected PropertyAttrProcessor createPropertyAttrProcessor() {
        return new PropertyAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX);
    }

    protected OptionClsAttrProcessor createOptionClsAttrProcessor() {
        return new OptionClsAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX);
    }

    // migrating now
    //protected TokenAttrProcessor createTokenAttrProcessor() {
    //    return new TokenAttrProcessor();
    //}

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return expressionObjectFactory;
    }

    public class LastaExpressionObjectFactory implements IExpressionObjectFactory {

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return allExpressionObjectNames;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if ("cls".equals(expressionObjectName)) {
                return createClassificationExpressionObject(context);
            } else if ("handy".equals(expressionObjectName)) {
                return HANDY_DATE_EXPRESSION_OBJECT;
            }
            // #for_now may need to improve performance? by jflute
            final Map<String, Object> processorMap = new HashMap<>();
            if (additionalExpressionSetupper != null) {
                final ThymeleafAdditionalExpressionResource resource = createThymeleafCustomExpressionResource(context);
                additionalExpressionSetupper.setup(resource);
                resource.getExpressionObjectMap().forEach((key, processor) -> {
                    if (processorMap.containsKey(key)) {
                        String msg = "The processor key already exists in processor map: " + key + ", " + processorMap.keySet();
                        throw new IllegalStateException(msg);
                    }
                    processorMap.put(key, processor);
                });
            }
            final Object additionalObject = processorMap.get(expressionObjectName);
            if (additionalObject != null) {
                return additionalObject;
            }
            return null;
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            return false; // #thinking #thymeleaf3 all right? by jflute
        }
    }

    protected ClassificationExpressionObject createClassificationExpressionObject(IExpressionContext context) {
        return new ClassificationExpressionObject(context);
    }

    protected ThymeleafAdditionalExpressionResource createThymeleafCustomExpressionResource(IExpressionContext context) {
        return new ThymeleafAdditionalExpressionResource(context);
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
