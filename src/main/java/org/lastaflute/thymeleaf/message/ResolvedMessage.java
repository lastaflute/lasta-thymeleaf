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
package org.lastaflute.thymeleaf.message;

import java.io.Serializable;
import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.core.message.UserMessage;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author Toshi504
 * @author jflute
 */
public class ResolvedMessage implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UserMessage message;
    protected final RequestManager requestManager;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResolvedMessage(UserMessage origin, RequestManager requestManager) {
        this.message = origin;
        this.requestManager = requestManager;
    }

    // ===================================================================================
    //                                                                    Resolved Message
    //                                                                    ================
    /**
     * @return The resolved message about message resources. (NotNull)
     */
    public String getMessage() { // called by thymeleaf templates e.g. th:text="${er.message}"
        final String messageKey = message.getMessageKey();
        if (message.isResource()) {
            final Locale locale = requestManager.getUserLocale();
            final MessageManager messageManager = requestManager.getMessageManager();
            final Object[] values = message.getValues();
            if (values != null && values.length > 0) {
                return messageManager.getMessage(locale, messageKey, values);
            } else {
                return messageManager.getMessage(locale, messageKey);
            }
        } else {
            return messageKey;
        }
    }
}
