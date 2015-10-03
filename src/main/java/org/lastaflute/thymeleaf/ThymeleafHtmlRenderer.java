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
import org.dbflute.optional.OptionalThing;
import org.lastaflute.di.helper.beans.PropertyDesc;
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
 */
public class ThymeleafHtmlRenderer implements HtmlRenderer {

    protected TemplateEngine templateEngine;

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine engine) {
        this.templateEngine = engine;
    }

    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        final TemplateEngine engine = getTemplateEngine();
        final WebContext context = createTemplateContext(requestManager);
        exportErrorsToContext(requestManager, context);
        exportFormPropertyToContext(context, runtime); // form to context
        final String html = createResponseBody(engine, context, runtime, journey);
        write(requestManager, html);
    }

    protected WebContext createTemplateContext(RequestManager requestManager) {
        HttpServletRequest request = requestManager.getRequest();
        HttpServletResponse response = requestManager.getResponseManager().getResponse();
        ServletContext servletContext = request.getServletContext();
        Locale locale = request.getLocale();
        return new WebContext(request, response, servletContext, locale);
    }

    protected void exportErrorsToContext(RequestManager requestManager, WebContext context) {
        context.setVariable("errors", extractActionErrors(requestManager));
    }

    protected ActionMessages extractActionErrors(RequestManager requestManager) {
        OptionalThing<ActionMessages> errors = requestManager.getAttribute(getMessagesAttributeKey(), ActionMessages.class);
        return errors.isPresent() ? errors.get() : new ActionMessages();
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

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
        return "UTF-8";
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
}
