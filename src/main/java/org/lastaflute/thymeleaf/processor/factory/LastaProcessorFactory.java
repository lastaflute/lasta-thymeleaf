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
package org.lastaflute.thymeleaf.processor.factory;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lastaflute.thymeleaf.processor.attr.ErrorsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.OptionClsAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.PropertyAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.TokenAttrProcessor;
import org.lastaflute.thymeleaf.processor.attr.option.ExpressionAttributeTagInitOption;
import org.thymeleaf.processor.IProcessor;

/**
 * @author jflute
 * @since 0.4.0 (2019/01/18 Friday)
 */
public class LastaProcessorFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String dialectPrefix; // not null
    protected final ExpressionAttributeTagInitOption expressionAttributeTagInitOption; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LastaProcessorFactory(String dialectPrefix) {
        this.dialectPrefix = dialectPrefix;
        this.expressionAttributeTagInitOption = prepareLastaExpressionAttributeTagInitOption();
    }

    protected ExpressionAttributeTagInitOption prepareLastaExpressionAttributeTagInitOption() {
        // tags that Lasta Thymeleaf provides do not need request parameter access
        // and also for security, so restricted here
        return new ExpressionAttributeTagInitOption().restrictExpressionExecution();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Set<IProcessor> createLastaProcessorsSet() { // not null
        final Set<IProcessor> processors = new LinkedHashSet<IProcessor>();
        processors.add(createErrorsAttrProcessor());
        processors.add(createPropertyAttrProcessor());
        processors.add(createOptionClsAttrProcessor());
        processors.add(createTokenAttrProcessor());
        return processors;
    }

    protected ErrorsAttrProcessor createErrorsAttrProcessor() {
        return new ErrorsAttrProcessor(dialectPrefix, expressionAttributeTagInitOption);
    }

    protected PropertyAttrProcessor createPropertyAttrProcessor() {
        return new PropertyAttrProcessor(dialectPrefix, expressionAttributeTagInitOption);
    }

    protected OptionClsAttrProcessor createOptionClsAttrProcessor() {
        return new OptionClsAttrProcessor(dialectPrefix, expressionAttributeTagInitOption);
    }

    protected TokenAttrProcessor createTokenAttrProcessor() {
        return new TokenAttrProcessor(dialectPrefix, expressionAttributeTagInitOption);
    }
}
