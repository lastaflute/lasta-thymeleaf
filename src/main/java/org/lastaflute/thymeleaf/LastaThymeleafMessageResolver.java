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
package org.lastaflute.thymeleaf;

import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.core.util.ContainerUtil;
import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;

/**
 * @author schatten
 * @author jflute
 */
public class LastaThymeleafMessageResolver extends AbstractMessageResolver {

    protected MessageManager messageManager;

    @Override
    protected void initializeSpecific() {
        messageManager = ContainerUtil.getComponent(MessageManager.class);
    }

    @Override
    public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
        checkInitialized();
        final Locale locale = arguments.getContext().getLocale(); // #thinking requestManager.getUserLocale()?
        return messageManager.findMessage(locale, key, messageParameters).map(message -> {
            return new MessageResolution(message);
        }).orElse(null);
    }
}
