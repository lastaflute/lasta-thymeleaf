/*
 * Copyright 2015-2024 the original author or authors.
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

import java.util.LinkedHashSet;
import java.util.Set;

import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.MistakeAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.OptionClsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.PropertyAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.TokenAttrProcessor;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * @author jflute
 * @author p1us2er0
 */
public class LastaThymeleafMistakeDialect extends AbstractProcessorDialect {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String LASTA_THYMELEAF_DIALECT_PREFIX = "th";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaThymeleafMistakeDialect() {
        super("lasta-mistake", LASTA_THYMELEAF_DIALECT_PREFIX, 1000);
    }

    // ===================================================================================
    //                                                                          Implements
    //                                                                          ==========
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return createLastaProcessorsSet();
    }

    protected Set<IProcessor> createLastaProcessorsSet() {
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(createMistakeAttrProcessor(ErrorsAttrProcessor.ATTR_NAME));
        processors.add(createMistakeAttrProcessor(PropertyAttrProcessor.ATTR_NAME));
        processors.add(createMistakeAttrProcessor(OptionClsAttrProcessor.ATTR_NAME));
        processors.add(createMistakeAttrProcessor(TokenAttrProcessor.ATTR_NAME));
        return processors;
    }

    protected MistakeAttrProcessor createMistakeAttrProcessor(String attrName) {
        return new MistakeAttrProcessor(LASTA_THYMELEAF_DIALECT_PREFIX, attrName);
    }
}
