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
package org.lastaflute.thymeleaf.wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.ruts.message.ActionMessages;

/**
 * Read-only Action Messages Wrapper.
 * @author schatten
 */
public class ActionMessagesWrapper implements Serializable {

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
    public ActionMessagesWrapper(ActionMessages origin) {
        this.messages = origin;
    }

    // ===================================================================================
    //                                                                      Access Message
    //                                                                      ==============
    public List<ActionMessage> getAllMessages() {
        List<ActionMessage> list = new ArrayList<ActionMessage>();
        messages.accessByFlatIterator().forEachRemaining((message) -> list.add(message));
        return list;
    }

    public List<ActionMessage> getMessages(String property) {
        List<ActionMessage> list = new ArrayList<ActionMessage>();
        messages.accessByIteratorOf(property).forEachRemaining((message) -> list.add(message));
        return list;
    }

    // ===================================================================================
    //                                                                     Delegate method
    //                                                                     ===============
    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#hasMessageOf(java.lang.String)
     */
    public boolean hasMessageOf(String property) {
        return messages.hasMessageOf(property);
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#hasMessageOf(java.lang.String, java.lang.String)
     */
    public boolean hasMessageOf(String property, String key) {
        return messages.hasMessageOf(property, key);
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#isEmpty()
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * @see org.lastaflute.web.ruts.message.ActionMessages#isAccessed()
     */
    public boolean isAccessed() {
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
