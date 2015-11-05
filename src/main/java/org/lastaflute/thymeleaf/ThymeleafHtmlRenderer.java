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
import org.lastaflute.di.helper.beans.PropertyDesc;
import org.lastaflute.thymeleaf.wrapper.ActionMessagesWrapper;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.exception.RequestForwardFailureException;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.config.ActionFormMeta;
import org.lastaflute.web.ruts.config.ActionFormProperty;
import org.lastaflute.web.ruts.message.ActionMessages;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
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
        final WebContext context = createTemplateContext(requestManager);
        exportErrorsToContext(requestManager, context);
        exportFormPropertyToContext(context, runtime); // form to context
        final String html = createResponseBody(templateEngine, context, runtime, journey);
        write(requestManager, html);
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
    protected void exportErrorsToContext(RequestManager requestManager, WebContext context) {
        context.setVariable("errors", new ActionMessagesWrapper(extractActionErrors(requestManager)));
    }

    protected ActionMessages extractActionErrors(RequestManager requestManager) {
        return requestManager.getAttribute(getMessagesAttributeKey(), ActionMessages.class).orElseGet(() -> {
            return new ActionMessages();
        });
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    // -----------------------------------------------------
    //                                           Export Form
    //                                           -----------
    protected void exportFormPropertyToContext(WebContext context, ActionRuntime runtime) {
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
