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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.lastaflute.core.message.UserMessages;
import org.lastaflute.di.helper.beans.PropertyDesc;
import org.lastaflute.thymeleaf.exception.ThymeleafFormPropertyConflictingWithRegisteredDataException;
import org.lastaflute.thymeleaf.exception.ThymeleafFormPropertyUsingReservedWordException;
import org.lastaflute.thymeleaf.exception.ThymeleafResisteredDataUsingReservedWordException;
import org.lastaflute.thymeleaf.message.ErrorMessages;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.exception.RequestForwardFailureException;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.VirtualForm;
import org.lastaflute.web.ruts.config.ActionFormMeta;
import org.lastaflute.web.ruts.config.ActionFormProperty;
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

    // avoid conflicting with form property as best one can
    public static final String VARIABLE_ERRORS = "errors";
    public static final String VARIABLE_VERSION_QUERY = "vq";

    protected static final Set<String> reservedWordSet;

    static {
        final Set<String> makingSet = new LinkedHashSet<String>();
        makingSet.add(VARIABLE_ERRORS);
        makingSet.add(VARIABLE_VERSION_QUERY);
        reservedWordSet = Collections.unmodifiableSet(makingSet);
    }

    public static final String VERSION_QUERY = "?v=" + System.currentTimeMillis();

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
        final Locale locale = requestManager.getUserLocale();
        return new WebContext(request, response, servletContext, locale);
    }

    // -----------------------------------------------------
    //                                         Export Errors
    //                                         -------------
    protected void exportErrorsToContext(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        setupVariableErrors(requestManager, context, runtime);
        setupVariableVersionQuery(requestManager, context, runtime);
    }

    protected void setupVariableErrors(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        final String varKey = VARIABLE_ERRORS;
        checkRegisteredDataUsingReservedWord(runtime, context, varKey);
        context.setVariable(varKey, createErrorMessages(requestManager));
    }

    protected ErrorMessages createErrorMessages(RequestManager requestManager) {
        return new ErrorMessages(extractActionErrors(requestManager), requestManager);
    }

    protected UserMessages extractActionErrors(RequestManager requestManager) { // from request and session
        final String attributeKey = getMessagesAttributeKey();
        final Class<UserMessages> attributeType = UserMessages.class;
        return requestManager.getAttribute(attributeKey, UserMessages.class).orElseGet(() -> {
            return requestManager.getSessionManager().getAttribute(attributeKey, attributeType).orElseGet(() -> {
                return createEmptyMessages();
            });
        });
    }

    protected String getMessagesAttributeKey() {
        return LastaWebKey.ACTION_ERRORS_KEY;
    }

    protected UserMessages createEmptyMessages() {
        return new UserMessages();
    }

    protected void setupVariableVersionQuery(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        final String varKey = VARIABLE_VERSION_QUERY;
        checkRegisteredDataUsingReservedWord(runtime, context, varKey);
        context.setVariable(varKey, VERSION_QUERY);
    }

    // -----------------------------------------------------
    //                                         Reserved Word
    //                                         -------------
    // Thymeleaf's reserved words are already checked in Thymeleaf process so no check them here
    protected void checkRegisteredDataUsingReservedWord(ActionRuntime runtime, WebContext context, String varKey) {
        if (isSuppressRegisteredDataUsingReservedWordCheck()) {
            return;
        }
        if (context.getVariableNames().contains(varKey)) {
            throwThymeleafRegisteredDataUsingReservedWordException(runtime, context, varKey);
        }
    }

    protected boolean isSuppressRegisteredDataUsingReservedWordCheck() {
        return false;
    }

    protected void throwThymeleafRegisteredDataUsingReservedWordException(ActionRuntime runtime, WebContext context, String dataKey) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the data key '" + dataKey + "' .");
        br.addItem("Advice");
        br.addElement("The word is already reserved in Lasta Thymeleaf.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    return asHtml(...).renderWith(data -> {");
        br.addElement("        data.register(\"" + dataKey + "\", ...); // *Bad");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    return asHtml(...).renderWith(data -> {");
        br.addElement("        data.register(\"sea\", ...); // Good");
        br.addElement("    });");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Variable Map");
        for (String variableName : context.getVariableNames()) {
            br.addElement(variableName + " = " + context.getVariable(variableName));
        }
        br.addItem("Bad DataKey");
        br.addElement(dataKey);
        br.addItem("Reserved Word");
        br.addElement(reservedWordSet);
        final String msg = br.buildExceptionMessage();
        throw new ThymeleafResisteredDataUsingReservedWordException(msg);
    }

    // ===================================================================================
    //                                                                         Export Form
    //                                                                         ===========
    protected void exportFormPropertyToContext(RequestManager requestManager, WebContext context, ActionRuntime runtime) {
        runtime.getActionForm().ifPresent(virtualForm -> {
            final ActionFormMeta meta = virtualForm.getFormMeta();
            final Collection<ActionFormProperty> properties = meta.properties();
            if (properties.isEmpty()) {
                return;
            }
            for (ActionFormProperty property : properties) {
                if (isExportableProperty(property.getPropertyDesc())) {
                    final String propertyName = property.getPropertyName();
                    checkFormPropertyUsingReservedWord(runtime, virtualForm, propertyName);
                    checkFormPropertyConflictingWithRegisteredData(runtime, context, propertyName);
                    final Object propertyValue = virtualForm.getPropertyValue(property);
                    if (propertyValue != null) {
                        context.setVariable(propertyName, propertyValue);
                    }
                }
            }
        });
    }

    protected boolean isExportableProperty(PropertyDesc pd) {
        return !pd.getPropertyType().getName().startsWith("javax.servlet");
    }

    // -----------------------------------------------------
    //                                         Reserved Word
    //                                         -------------
    protected void checkFormPropertyUsingReservedWord(ActionRuntime runtime, VirtualForm virtualForm, final String propertyName) {
        if (isSuppressFormPropertyUsingReservedWordCheck()) {
            return;
        }
        if (reservedWordSet.contains(propertyName)) {
            throwThymeleafFormPropertyUsingReservedWordException(runtime, virtualForm, propertyName);
        }
    }

    protected boolean isSuppressFormPropertyUsingReservedWordCheck() {
        return false;
    }

    protected void throwThymeleafFormPropertyUsingReservedWordException(ActionRuntime runtime, VirtualForm virtualForm,
            String propertyName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the property name '" + propertyName + "' in form.");
        br.addItem("Advice");
        br.addElement("The word is already reserved in Lasta Thymeleaf.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public String " + propertyName + "; // *Bad");
        br.addElement("  (o):");
        br.addElement("    public String sea; // Good");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Form");
        br.addElement(virtualForm);
        br.addItem("Bad Property");
        br.addElement(propertyName);
        br.addItem("Reserved Word");
        br.addElement(reservedWordSet);
        final String msg = br.buildExceptionMessage();
        throw new ThymeleafFormPropertyUsingReservedWordException(msg);
    }

    // -----------------------------------------------------
    //                                    Conflict with Data
    //                                    ------------------
    protected void checkFormPropertyConflictingWithRegisteredData(ActionRuntime runtime, WebContext context, String propertyName) {
        if (context.getVariableNames().contains(propertyName)) {
            throwThymeleafFormPropertyConflictingWithRegisteredDataException(runtime, context, propertyName);
        }
    }

    protected void throwThymeleafFormPropertyConflictingWithRegisteredDataException(ActionRuntime runtime, WebContext context,
            String conflictedName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Conflicting between form property and registered data.");
        br.addItem("Advice");
        br.addElement("Use unique names for form property or registered data.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    public String sea; // *Bad");
        br.addElement("    ...");
        br.addElement("    return asHtml(...).renderWith(data -> {");
        br.addElement("        data.register(\"sea\", ...); // *Bad");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    public String sea; // Good");
        br.addElement("    ...");
        br.addElement("    return asHtml(...).renderWith(data -> {");
        br.addElement("        data.register(\"land\", ...); // Good");
        br.addElement("    });");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Variable Map");
        for (String variableName : context.getVariableNames()) {
            br.addElement(variableName + " = " + context.getVariable(variableName));
        }
        br.addItem("Conflicting Name");
        br.addElement(conflictedName);
        final String msg = br.buildExceptionMessage();
        throw new ThymeleafFormPropertyConflictingWithRegisteredDataException(msg);
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
