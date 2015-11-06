/*
 * Copyright 2014-2015 the original author or authors.
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
package org.lastaflute.thymeleaf.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.ruts.message.ActionMessages;

/**
 * Read-only Action Messages Wrapper. <br>
 * Accessed by Thymeleaf templates, so cannot easily refactor method names.
 * @author schatten
 * @author jflute
 */
public class ErrorMessages implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ActionMessages messages;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ErrorMessages(ActionMessages origin) {
        this.messages = origin;
    }

    // ===================================================================================
    //                                                                      Convert Access
    //                                                                      ==============
    public List<ActionMessage> getAll() { // e.g. th:each="error : ${errors.all}"
        List<ActionMessage> list = new ArrayList<ActionMessage>();
        messages.accessByFlatIterator().forEachRemaining(message -> list.add(message));
        return list;
    }

    public List<ActionMessage> part(String property) { // e.g. th:each="error : ${errors.part('seaName')}"
        List<ActionMessage> list = new ArrayList<ActionMessage>();
        messages.accessByIteratorOf(property).forEachRemaining(message -> list.add(message));
        return list;
    }

    // ===================================================================================
    //                                                                     Delegate Access
    //                                                                     ===============
    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#hasMessageOf(java.lang.String)
     */
    public boolean exists(String property) { // e.g. th:unless="${errors.exists('seaName')}"
        return messages.hasMessageOf(property);
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#hasMessageOf(java.lang.String, java.lang.String)
     */
    public boolean exists(String property, String key) { // e.g. th:unless="${errors.exists('seaName', 'errors.required')}"
        return messages.hasMessageOf(property, key);
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#isEmpty()
     */
    public boolean isEmpty() { // e.g. th:unless="${errors.empty}"
        return messages.isEmpty();
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#isAccessed()
     */
    public boolean isAccessed() { // e.g. th:unless="${errors.accessed}"
        return messages.isAccessed();
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#size()
     */
    public int size() {
        return messages.size();
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#size(java.lang.String)
     */
    public int size(String property) {
        return messages.size(property);
    }
}
