/*
 * Copyright 2015-2016 the original author or authors.
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
package org.lastaflute.thymeleaf.processor.expression;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.Srl;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.dbflute.exception.ProvidedClassificationNotFoundException;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Utility methods for Classification objects.<br>
 * <pre>
 * Usage:
 *     &lt;select&gt;
 *       &lt;option th:each="cdef : ${#cls.listAll('MemberStatus')}" th:value="${cdef.code()}" th:text="${cdef.alias()}"&gt;&lt;/option&gt;
 *     &lt;/select&gt;
 *
 *   The result of processing this example will be as expected.
 *     &lt;select&gt;
 *       &lt;option value="FML"&gt;Formalized&lt;/option&gt;
 *       &lt;option value="WDL"&gt;Withdrawal&lt;/option&gt;
 *       &lt;option value="PRV"&gt;Provisional&lt;/option&gt;
 *     &lt;/select&gt;
 * </pre>
 * @author schatten
 * @author jflute
 */
public class ClassificationExpressionProcessor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String GROUP_DELIMITER = ".";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final IProcessingContext processingContext;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClassificationExpressionProcessor(IProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    // ===================================================================================
    //                                                                          Expression
    //                                                                          ==========
    // -----------------------------------------------------
    //                                                list()
    //                                                ------
    /**
     * Get list of specified classification.
     * @param classificationName The name of classification, can contain group name by delimiter. (NotNull)
     * @return The list of all classification. (NotNull)
     */
    public List<Classification> list(String classificationName) {
        assertArgumentNotNull("classificationName", classificationName);
        final String delimiter = GROUP_DELIMITER;
        final String pureName;
        final String groupName;
        if (classificationName.contains(delimiter)) { // e.g. sea.land or maihamadb-sea.land
            pureName = Srl.substringFirstFront(classificationName, delimiter); // e.g. sea or maihamadb-sea
            groupName = Srl.substringFirstRear(classificationName, delimiter); // e.g. land
        } else { // e.g. sea or maihamadb-sea
            pureName = classificationName;
            groupName = null;
        }
        final ClassificationMeta meta = findClassificationMeta(pureName, () -> {
            return "list('" + classificationName + "')";
        });
        if (groupName != null) {
            final List<Classification> groupOfList = meta.groupOf(groupName);
            if (groupOfList.isEmpty()) { // means not found
                throw new TemplateProcessingException("Not found the classification group: " + groupName + " of " + pureName);
            }
            return groupOfList;
        } else {
            return meta.listAll();
        }
    }

    // -----------------------------------------------------
    //                                             listAll()
    //                                             ---------
    /**
     * Get list of all classification.
     * @param classificationName The name of classification. (NotNull)
     * @return The list of all classification. (NotNull)
     */
    public List<Classification> listAll(String classificationName) {
        assertArgumentNotNull("classificationName", classificationName);
        return findClassificationMeta(classificationName, () -> {
            return "listAll('" + classificationName + "')";
        }).listAll();
    }

    // -----------------------------------------------------
    //                                               alias()
    //                                               -------
    /**
     * Get Classification alias.
     * @param cls The instance of classification to get code. (NotNull)
     * @return The alias of classification. (NotNull: if not classification, throws exception)
     */
    public String alias(Object cls) {
        assertArgumentNotNull("cls", cls);
        assertCanBeClassification(cls);
        return findClassificationAlias((Classification) cls);
    }

    // should be by-code, and may be unneeded by native property in form
    ///**
    // * Get classification alias.
    // * @param classificationName The name of classification. (NotNull)
    // * @param elementName The name of classification element. (NotNull)
    // * @return classification alias (NotNull: if not found, throws exception)
    // */
    //public String alias(String classificationName, String elementName) {
    //    assertArgumentNotNull("classificationName", classificationName);
    //    assertArgumentNotNull("elementName", elementName);
    //    final ClassificationMeta meta = findClassificationMeta((String) classificationName, () -> {
    //        return "alias('" + classificationName + "', '" + elementName + "')";
    //    });
    //    final Classification cls = meta.nameOf(elementName);
    //    assertClassificationByNameExists(classificationName, elementName, cls);
    //    return findClassificationAlias(cls);
    //}

    // -----------------------------------------------------
    //                                                code()
    //                                                ------
    /**
     * Get classification code.
     * @param cls The instance of classification to get code. (NotNull)
     * @return The code of classification. (NotNull: if not classification, throws exception)
     */
    public String code(Object cls) {
        assertArgumentNotNull("cls", cls);
        assertCanBeClassification(cls);
        return ((Classification) cls).code();
    }

    /**
     * Get classification code.
     * @param classificationName The name of classification. (NotNull)
     * @param elementName The name of classification element. (NotNull)
     * @return The found code of classification. (NotNull: if not found, throws exception)
     */
    public String code(String classificationName, String elementName) {
        assertArgumentNotNull("classificationName", classificationName);
        assertArgumentNotNull("elementName", elementName);
        final ClassificationMeta meta = findClassificationMeta((String) classificationName, () -> {
            return "code('" + classificationName + "', '" + elementName + "')";
        });
        final Classification cls = meta.nameOf(elementName);
        assertClassificationByNameExists(classificationName, elementName, cls);
        return cls.code();
    }

    // -----------------------------------------------------
    //                                               ...Of()
    //                                               -------
    /**
     * Get classification by code.
     * @param classificationName The name of classification. (NotNull)
     * @param code The code of classification to find. (NotNull)
     * @return The found instance of classification for the code. (NotNull: if not found, throws exception)
     */
    public Classification codeOf(String classificationName, String code) {
        assertArgumentNotNull("elementName", classificationName);
        assertArgumentNotNull("code", code);
        return findClassificationMeta(classificationName, () -> {
            return "codeOf('" + classificationName + "', '" + code + "')";
        }).codeOf(code);
    }

    /**
     * Get classification by name.
     * @param classificationName The name of classification. (NotNull)
     * @param name The name of classification to find. (NotNull)
     * @return The found instance of classification for the code. (NotNull: if not found, throws exception)
     */
    public Classification nameOf(String classificationName, String name) {
        return findClassificationMeta((String) classificationName, () -> {
            return "nameOf('" + classificationName + "', '" + name + "')";
        }).nameOf(name);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    protected IProcessingContext getProcessingContext() {
        return processingContext;
    }

    protected Locale getUserLocale() {
        final IProcessingContext context = getProcessingContext();
        return context != null ? context.getContext().getLocale() : Locale.getDefault();
    }

    protected String getRequestTemplatePath() {
        final IProcessingContext context = getProcessingContext();
        return context instanceof Arguments ? ((Arguments) context).getTemplateName() : null;
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    protected ClassificationMeta findClassificationMeta(String classificationName, Supplier<Object> callerInfo) {
        return provideClassificationMeta(getListedClassificationProvider(), classificationName, callerInfo);
    }

    protected String findClassificationAlias(Classification cls) {
        return determineClassificationAliasKey().map(key -> {
            return (String) cls.subItemMap().get(key);
        }).orElse(cls.alias());
    }

    protected OptionalThing<String> determineClassificationAliasKey() {
        return getListedClassificationProvider().determineAlias(getUserLocale());
    }

    protected ListedClassificationProvider getListedClassificationProvider() {
        return getAssistantDirector().assistDbDirection().assistListedClassificationProvider();
    }

    protected ClassificationMeta provideClassificationMeta(ListedClassificationProvider provider, String classificationName,
            Supplier<Object> callerInfo) {
        try {
            return provider.provide(classificationName);
        } catch (ProvidedClassificationNotFoundException e) {
            throwListedClassificationNotFoundException(classificationName, callerInfo, e);
            return null; // unreachable
        }
    }

    protected void throwListedClassificationNotFoundException(String classificationName, Supplier<Object> callerInfo,
            ProvidedClassificationNotFoundException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification for the list.");
        br.addItem("Requested Template Path");
        br.addElement(getRequestTemplatePath());
        br.addItem("Target Expression");
        br.addElement(callerInfo.get());
        br.addItem("Classification Name");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new TemplateProcessingException(msg, cause);
    }

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected FwAssistantDirector getAssistantDirector() {
        return getComponent(FwAssistantDirector.class);
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) {
        return ContainerUtil.getComponent(type);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertCanBeClassification(Object cls) {
        if (!(cls instanceof Classification)) {
            throwNonClassificationObjectSpecifiedException(cls);
        }
    }

    protected void throwNonClassificationObjectSpecifiedException(Object cls) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Non classification object specified.");
        br.addItem("Specified Object");
        br.addElement(cls != null ? cls.getClass() : null);
        br.addElement(cls);
        final String msg = br.buildExceptionMessage();
        throw new TemplateProcessingException(msg);
    }

    protected void assertClassificationByNameExists(String classificationName, String elementName, Classification cls) {
        if (cls == null) {
            throwClassificationByNameNotFoundException(classificationName, elementName);
        }
    }

    protected void throwClassificationByNameNotFoundException(String classificationName, String elementName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Non found the classification by the name.");
        br.addItem("Classification Name");
        br.addElement(classificationName);
        br.addItem("Specified Name");
        br.addElement(elementName);
        final String msg = br.buildExceptionMessage();
        throw new TemplateProcessingException(msg);
    }

    protected void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The argument 'variableName' should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
