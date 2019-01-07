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
import org.lastaflute.thymeleaf.processor.attr.TokenAttrProcessor;
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

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<String> allExpressionObjectNames;
    protected final Set<String> cacheableExpressionObjectNames;
    protected final HandyDateExpressionObject handyDateExpressionObject;
    protected final LastaExpressionObjectFactory expressionObjectFactory;
    protected final Set<IProcessor> additionalProcessors = new LinkedHashSet<IProcessor>();

    protected ThymeleafAdditionalExpressionSetupper additionalExpressionSetupper; // null allowed
    protected Map<String, Object> additionalExpressionObjectMap; // not null after initialization (lazy-loaded)

    // #thinking pri.B want to use JsonManager in JS serialization (2018/11/28)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafDialect() {
        super("lasta", LASTA_THYMELEAF_DIALECT_PREFIX, 1000);
        allExpressionObjectNames = prepareAllExpressionObjectNames();
        cacheableExpressionObjectNames = prepareCacheableExpressionObjectNames();
        handyDateExpressionObject = newHandyDateExpressionObject();
        expressionObjectFactory = newLastaExpressionObjectFactory();
    }

    protected Set<String> prepareAllExpressionObjectNames() {
        return DfCollectionUtil.newHashSet(EXPRESSION_OBJECT_CLASSIFICATION, EXPRESSION_OBJECT_HANDY);
    }

    protected Set<String> prepareCacheableExpressionObjectNames() {
        return DfCollectionUtil.newHashSet();
    }

    protected HandyDateExpressionObject newHandyDateExpressionObject() {
        return new HandyDateExpressionObject();
    }

    protected LastaExpressionObjectFactory newLastaExpressionObjectFactory() {
        return new LastaExpressionObjectFactory();
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
    public Set<IProcessor> getProcessors(String dialectPrefix) { // only once called when first access
        final Set<IProcessor> processors = createLastaProcessorsSet();
        processors.addAll(getAdditionalProcessors());
        return processors;
    }

    protected Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(createErrorsAttrProcessor());
        processors.add(createPropertyAttrProcessor());
        processors.add(createOptionClsAttrProcessor());
        processors.add(createTokenAttrProcessor());
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

    protected TokenAttrProcessor createTokenAttrProcessor() {
        return new TokenAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX);
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() { // only once called when first access
        return expressionObjectFactory;
    }

    public class LastaExpressionObjectFactory implements IExpressionObjectFactory {

        @Override
        public Set<String> getAllExpressionObjectNames() {
            initializeAdditionalExpressionIfNeeds(); // reflecting the names set to additional expressions
            return allExpressionObjectNames;
        }

        @Override
        public Object buildObject(IExpressionContext context, String expressionObjectName) {
            if ("cls".equals(expressionObjectName)) {
                return createClassificationExpressionObject(context);
            } else if ("handy".equals(expressionObjectName)) {
                return handyDateExpressionObject;
            }
            initializeAdditionalExpressionIfNeeds(); // preparing the object map
            final Object additionalObject = additionalExpressionObjectMap.get(expressionObjectName);
            if (additionalObject != null) {
                return additionalObject;
            }
            return null;
        }

        @Override
        public boolean isCacheable(String expressionObjectName) {
            // if not specified, false for safety
            return cacheableExpressionObjectNames.contains(expressionObjectName);
        }
    }

    protected ClassificationExpressionObject createClassificationExpressionObject(IExpressionContext context) {
        return new ClassificationExpressionObject(context);
    }

    protected ThymeleafAdditionalExpressionResource createThymeleafCustomExpressionResource() {
        return new ThymeleafAdditionalExpressionResource();
    }

    protected void initializeAdditionalExpressionIfNeeds() {
        if (additionalExpressionObjectMap != null) {
            return;
        }
        synchronized (this) {
            if (additionalExpressionObjectMap != null) {
                return;
            }
            if (additionalExpressionSetupper != null) {
                final ThymeleafAdditionalExpressionResource resource = createThymeleafCustomExpressionResource();
                additionalExpressionSetupper.setup(resource);
                final Map<String, Object> expressionObjectMap = resource.getExpressionObjectMap(); // read-only
                expressionObjectMap.keySet().forEach(name -> {
                    allExpressionObjectNames.add(name); // required since thymeleaf3
                });
                additionalExpressionObjectMap = expressionObjectMap;
                cacheableExpressionObjectNames.addAll(resource.getCacheableExpressionObjectNames());
            } else {
                additionalExpressionObjectMap = Collections.emptyMap();
            }
        }
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
