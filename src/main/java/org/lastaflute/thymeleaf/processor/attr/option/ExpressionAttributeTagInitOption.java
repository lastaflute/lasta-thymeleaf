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
package org.lastaflute.thymeleaf.processor.attr.option;

/**
 * @author jflute
 * @since 0.4.0 (2019/01/18 at broadway theatre)
 */
public class ExpressionAttributeTagInitOption {

    // suppress la:property="${param.sea}" ('param' means request parameters) or not
    protected boolean restrictedExpressionExecution;

    public ExpressionAttributeTagInitOption restrictExpressionExecution() {
        this.restrictedExpressionExecution = true;
        return this;
    }

    public boolean isRestrictedExpressionExecution() {
        return restrictedExpressionExecution;
    }
}