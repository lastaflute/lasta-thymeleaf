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
package org.lastaflute.thymeleaf;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.lastaflute.di.helper.beans.PropertyDesc;
import org.lastaflute.thymeleaf.messages.ErrorMessages;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.exception.RequestForwardFailureException;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.config.ActionFormMeta;
import org.lastaflute.web.ruts.config.ActionFormProperty;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

/**
 * @author jflute
 * @author schatten
 */
public class ThymeleafHtmlRenderer implements HtmlRenderer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(ThymeleafHtmlRenderer.class);
    public static final String DEFAULT_HTML_ENCODING = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TemplateEngine templateEngine;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ThymeleafHtmlRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // ===================================================================================
    //                                                                              Render
    //                                                                              ======
    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        showRendering(journey);
        final WebContext context = createTemplateContext(requestManager);
        exportErrorsToContext(requestManager, context, runtime);
        exportFormPropertyToContext(requestManager, context, runtime);
        final String html = createResponseBody(templateEngine, context, runtime, journey);
        write(requestManager, html);
    }

    protected void showRendering(NextJourney journey) {
        if (logger.isDebugEnabled()) {
            final String pureName = Srl.substringLastRear(journey.getRoutingPath(), "/");
            logger.debug("#flow ...Rendering {} by #thymeleaf template", pureName);
        }
    }

    // -----------------------------------------------------
    //                                      Template Context
    //                                      ----------------
    protected WebContext createTemplateContext(RequestManager requestManager) {
        final HttpServletRequest request = requestManager.getRequest();
        final HttpServletResponse response = requestManager.getResponseManager().getResponse();
        final ServletContext servletContext = request.getServletContext();
        final Locale locale = request.getLocale();
        return new WebContext(request, response, servletContext, locale);
    }

    // -----------------------------------------------------
    //                                         Export Errors
    //                                         -------------
    protected void exportErrorsToContext(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        context.setVariable("errors", createErrorMessages(requestManager));
    }

    protected ErrorMessages createErrorMessages(RequestManager requestManager) {
        return new ErrorMessages(extractActionErrors(requestManager), requestManager);
    }

    protected ActionMessages extractActionErrors(RequestManager requestManager) { // from request and session
        final String attributeKey = getMessagesAttributeKey();
        final Class<ActionMessages> attributeType = ActionMessages.class;
        return requestManager.getAttribute(attributeKey, ActionMessages.class).orElseGet(() -> {
            return requestManager.getSessionManager().getAttribute(attributeKey, attributeType).orElseGet(() -> {
                return createEmptyMessages();
            });
        });
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    protected ActionMessages createEmptyMessages() {
        return new ActionMessages();
    }

    // -----------------------------------------------------
    //                                           Export Form
    //                                           -----------
    protected void exportFormPropertyToContext(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        runtime.getActionForm().ifPresent(virtualForm -> {
            final ActionFormMeta meta = virtualForm.getFormMeta();
            final Collection<ActionFormProperty> properties = meta.properties();
            if (properties.isEmpty()) {
                return;
            }
            final Object form = virtualForm.getRealForm();
            for (ActionFormProperty property : properties) {
                if (isExportableProperty(property.getPropertyDesc())) {
                    final Object propertyValue = property.getPropertyValue(form);
                    if (propertyValue != null) {
                        context.setVariable(property.getPropertyName(), propertyValue);
                    }
                }
            }
        });
    }

    protected boolean isExportableProperty(PropertyDesc pd) {
        return !pd.getPropertyType().getName().startsWith("javax.servlet");
    }

    // -----------------------------------------------------
    //                                         Response Body
    //                                         -------------
    protected String createResponseBody(TemplateEngine engine, WebContext context, ActionRuntime runtime, NextJourney journey) {
        try {
            return engine.process(journey.getRoutingPath(), context);
        } catch (RuntimeException e) {
            return throwRequestForwardFailureException(runtime, journey, e);
        }
    }

    protected void write(RequestManager requestManager, String html) {
        requestManager.getResponseManager().write(html, "text/html", getEncoding());
    }

    protected String getEncoding() {
        return DEFAULT_HTML_ENCODING;
    }

    protected String throwRequestForwardFailureException(ActionRuntime runtime, NextJourney journey, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to forward the request to the path.");
        br.addItem("Advice");
        br.addElement("Read the nested exception message.");
        br.addItem("Action Runtime");
        br.addElement(runtime);
        br.addItem("Forward Journey");
        br.addElement(journey);
        final String msg = br.buildExceptionMessage();
        throw new RequestForwardFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
