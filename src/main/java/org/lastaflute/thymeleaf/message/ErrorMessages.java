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
package org.lastaflute.thymeleaf.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dbflute.util.DfTypeUtil;
import org.lastaflute.core.message.UserMessage;
import org.lastaflute.core.message.UserMessages;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * Read-only Action Messages Wrapper. <br>
 * Accessed by Thymeleaf templates, so cannot easily refactor method names.
 * @author schatten
 * @author jflute
 * @author Toshi504
 */
public class ErrorMessages implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UserMessages messages;
    protected final RequestManager requestManager;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ErrorMessages(UserMessages origin, RequestManager requestManager) {
        this.messages = origin;
        this.requestManager = requestManager;
    }

    // ===================================================================================
    //                                                                      Convert Access
    //                                                                      ==============
    public List<ResolvedMessage> getAll() { // e.g. th:each="error : ${errors.all}"
        List<ResolvedMessage> list = new ArrayList<ResolvedMessage>();
        messages.accessByFlatIterator().forEachRemaining(message -> {
            list.add(createResolvedMessage(message));
        });
        return list;
    }

    public List<ResolvedMessage> part(String property) { // e.g. th:each="error : ${errors.part('seaName')}"
        List<ResolvedMessage> list = new ArrayList<ResolvedMessage>();
        messages.accessByIteratorOf(property).forEachRemaining(message -> {
            list.add(createResolvedMessage(message));
        });
        return list;
    }

    protected ResolvedMessage createResolvedMessage(UserMessage message) {
        return new ResolvedMessage(message, requestManager);
    }

    // ===================================================================================
    //                                                                     Delegate Access
    //                                                                     ===============
    public boolean exists(String property) { // e.g. th:unless="${errors.exists('seaName')}"
        return messages.hasMessageOf(property);
    }

    public boolean exists(String property, String key) { // e.g. th:unless="${errors.exists('seaName', 'errors.required')}"
        return messages.hasMessageOf(property, key);
    }

    public boolean isEmpty() { // e.g. th:unless="${errors.empty}"
        return messages.isEmpty();
    }

    public boolean isAccessed() { // e.g. th:unless="${errors.accessed}"
        return messages.isAccessed();
    }

    public int size() {
        return messages.size();
    }

    public int size(String property) {
        return messages.size(property);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() { // for request attribute logging
        return DfTypeUtil.toClassTitle(this) + ":{messages=" + messages + "}@" + Integer.toHexString(hashCode());
    }
}
