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
package org.lastaflute.thymeleaf.processor.attr;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.thymeleaf.processor.attr.exception.ThymeleafTokenNotHiddenTypeException;
import org.lastaflute.thymeleaf.processor.attr.exception.ThymeleafTokenNotInputTypeException;
import org.lastaflute.thymeleaf.processor.attr.option.ExpressionAttributeTagInitOption;
import org.lastaflute.web.LastaWebKey;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.token.DoubleSubmitManager;
import org.lastaflute.web.util.LaActionRuntimeUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.AbstractStandardExpressionAttributeTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Token Attribute Processor.
 * <pre>
 * Usage:
 *   &lt;input type="hidden" <b>la:token="true"</b>/&gt;
 *
 * The result of processing this example will be as expected.
 *   &lt;input type="text" <b>name="lastaflute.action.TRANSACTION_TOKEN" value="f229f48b0bb7986656cdd5f9d86338e3"</b>/&gt;
 * </pre>
 * @author jflute
 */
public class TokenAttrProcessor extends AbstractStandardExpressionAttributeTagProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ATTR_NAME = "token";
    public static final int PRECEDENCE = 950;
    public static final boolean REMOVE_ATTRIBUTE = true;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TokenAttrProcessor(String dialectPrefix, ExpressionAttributeTagInitOption option) {
        super(TemplateMode.HTML, dialectPrefix, ATTR_NAME, PRECEDENCE, REMOVE_ATTRIBUTE, option.isRestrictedExpressionExecution());
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue,
            Object expressionResult, IElementTagStructureHandler structureHandler) {
        final ActionRuntime runtime = LaActionRuntimeUtil.getActionRuntime();
        switch (tag.getElementCompleteName()) {
        case "input":
            final String inputType = tag.getAttributeValue("type");
            if (!"hidden".equals(inputType)) {
                throwThymeleafTokenNotHiddenTypeException(runtime, inputType);
            }
            if (Boolean.TRUE.equals(expressionResult)) {
                structureHandler.setAttribute("th:name", LastaWebKey.TRANSACTION_TOKEN_KEY);
                structureHandler.setAttribute("th:value", prepareTransactionToken(runtime));
            } else {
                structureHandler.removeElement();
            }
            break;
        default:
            throwThymeleafTokenNotInputTypeException(runtime, tag.getElementCompleteName());
        }
    }

    protected String prepareTransactionToken(ActionRuntime runtime) {
        final String token = getDoubleSubmitManager().getSessionTokenMap().flatMap(tokenMap -> {
            return tokenMap.get(runtime.getActionType());
        }).orElse("none");
        // *cannot check saveToken() call because of validation error after double-submitted
        //final String token;
        //if (doubleSubmitManager.isDoubleSubmittedRequest()) { // rendering after error here
        //    token = "none"; // verified when submit by verifyToken()
        //} else {
        //    token = doubleSubmitManager.getSessionTokenMap().orElseThrow(() -> {
        //        return createThymeleafSessionTokenMapNotFoundException(runtime);
        //    }).get(runtime.getActionType()).orElseThrow(() -> {
        //        return createThymeleafGroupTokenNotFoundException(runtime);
        //    });
        //}
        return token;
    }

    protected DoubleSubmitManager getDoubleSubmitManager() { // #pending want to cache
        return ContainerUtil.getComponent(DoubleSubmitManager.class);
    }

    //// ===================================================================================
    ////                                                                    Exception Helper
    ////                                                                    ================
    //// *see getModifiedAttributeValues()
    ////protected RuntimeException createThymeleafSessionTokenMapNotFoundException(ActionRuntime runtime) {
    ////    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    ////    br.addNotice("Not found the session token map for the hidden token.");
    ////    br.addItem("Advice");
    ////    br.addElement("Call saveToken() in your action for the view");
    ////    br.addElement("if you use <input type=\"hidden\" la:token=\"true\"/>.");
    ////    br.addElement("For example:");
    ////    br.addElement("  (o):");
    ////    br.addElement("    public HtmlResponse index(Integer memberId) {");
    ////    br.addElement("        saveToken(); // Good");
    ////    br.addElement("        return asHtml(...);");
    ////    br.addElement("    }");
    ////    br.addElement("");
    ////    br.addElement("If that helps, you should call verifyToken() after validate()");
    ////    br.addElement("or this exception is thrown. (confirm the stack trace)");
    ////    br.addElement("For example:");
    ////    br.addElement("  (x):");
    ////    br.addElement("    public HtmlResponse update(Integer memberId) {");
    ////    br.addElement("        verifyToken(...); // *Bad: session token is deleted here");
    ////    br.addElement("        validate(form, messages -> {}, () -> { // may be this exception if validation error");
    ////    br.addElement("            return asHtml(path_...); // the html may need token...");
    ////    br.addElement("        });");
    ////    br.addElement("        ...");
    ////    br.addElement("    }");
    ////    br.addElement("  (o):");
    ////    br.addElement("    public HtmlResponse update(Integer memberId) {");
    ////    br.addElement("        validate(form, messages -> {}, () -> {");
    ////    br.addElement("            return asHtml(path_...); // session token remains");
    ////    br.addElement("        });");
    ////    br.addElement("        verifyToken(...); // Good");
    ////    br.addElement("        ...");
    ////    br.addElement("    }");
    ////    br.addItem("Action");
    ////    br.addElement(runtime);
    ////    final String msg = br.buildExceptionMessage();
    ////    return new ThymeleafSessionTokenMapNotFoundException(msg);
    ////}
    ////
    ////protected RuntimeException createThymeleafGroupTokenNotFoundException(ActionRuntime runtime) {
    ////    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    ////    br.addNotice("Not found the token group for the action type.");
    ////    br.addItem("Action");
    ////    br.addElement(runtime);
    ////    final String msg = br.buildExceptionMessage();
    ////    return new ThymeleafGroupTokenNotFoundException(msg);
    ////}

    protected void throwThymeleafTokenNotHiddenTypeException(ActionRuntime runtime, String inputType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the token attribute except hidden type.");
        br.addItem("Advice");
        br.addElement("The la:token attribute should be used at hidden type like this:");
        br.addElement("  (x):");
        br.addElement("    <input type=\"text\" th:token=\"true\"/> // *Bad");
        br.addElement("  (o):");
        br.addElement("    <input type=\"hidden\" th:token=\"true\"/> // Good");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Input Type");
        br.addElement(inputType);
        final String msg = br.buildExceptionMessage();
        throw new ThymeleafTokenNotHiddenTypeException(msg);
    }

    protected void throwThymeleafTokenNotInputTypeException(ActionRuntime runtime, String tagName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot use the token attribute except input tag.");
        br.addItem("Advice");
        br.addElement("The la:token attribute should be used at input type like this:");
        br.addElement("  (x):");
        br.addElement("    <form th:action=\"...\" th:token=\"true\"/> // *Bad");
        br.addElement("  (o):");
        br.addElement("    <input type=\"hidden\" th:token=\"true\"/> // Good");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Tag Name");
        br.addElement(tagName);
        final String msg = br.buildExceptionMessage();
        throw new ThymeleafTokenNotInputTypeException(msg);
    }
}
