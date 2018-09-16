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
package org.lastaflute.thymeleaf.message.resolver;

import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.core.util.ContainerUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;

/**
 * @author jflute
 */
public class ManagedMessageResolver extends AbstractMessageResolver {

    protected final MessageManager messageManager;

    public ManagedMessageResolver() {
        messageManager = ContainerUtil.getComponent(MessageManager.class);
    }

    @Override
    public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        final Locale locale = context.getLocale(); // #thinking is the same as requestManager's user locacle? by jflute (2018/09/17)
        return messageManager.findMessage(locale, key, messageParameters).orElse(null);
    }

    @Override
    public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
        // #thinking #thymeleaf3 what should I do? by jflute
        return null;
    }
}