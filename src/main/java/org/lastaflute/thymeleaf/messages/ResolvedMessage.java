package org.lastaflute.thymeleaf.messages;

import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * 
 * @author Toshi504
 */
public class ResolvedMessage extends ActionMessage {
    
    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected MessageManager messageManager;
    protected RequestManager requestManager;
    
    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResolvedMessage(String key, boolean resource) {
        super(key, resource);
    }
    
    public String getMessage() {
        Locale locale = requestManager.getUserLocale();
        return super.isResource() ? messageManager.getMessage(locale, super.key) : super.key;
    }

}
