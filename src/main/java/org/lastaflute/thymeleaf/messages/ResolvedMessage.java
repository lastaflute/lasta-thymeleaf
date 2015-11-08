package org.lastaflute.thymeleaf.messages;

import java.io.Serializable;
import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * 
 * @author Toshi504
 */
public class ResolvedMessage implements Serializable {
    
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ActionMessage message;
    protected final RequestManager requestManager;
    
    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResolvedMessage(ActionMessage origin, RequestManager requestManager) {
        this.message = origin;
        this.requestManager = requestManager;
    }
    
    public String getMessage() {
        Locale locale = requestManager.getUserLocale();
        MessageManager messageManager = requestManager.getMessageManager();
        return message.isResource() ? messageManager.getMessage(locale, message.getKey()) :  message.getKey();
    }

}
