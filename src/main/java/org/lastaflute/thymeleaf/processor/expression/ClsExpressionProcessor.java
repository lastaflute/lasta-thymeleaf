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
package org.lastaflute.thymeleaf.processor.expression;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.optional.OptionalThing;
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
 *       &lt;option th:each="def : ${#cdef.values('MemberStatus')}" th:value="${#cdef.code(def)}" th:text="${#cdef.alias(def)}"&gt;&lt;/option&gt;
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
 */
public class ClsExpressionProcessor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final IProcessingContext processingContext;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ClsExpressionProcessor(IProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    // ===================================================================================
    //                                                                          Expression
    //                                                                          ==========
    // -----------------------------------------------------
    //                                              values()
    //                                              --------
    /**
     * Get classification values.
     * @param classificationName The name of classification (NotNull)
     * @return The list of the classification. (NotNull)
     */
    public List<Classification> values(String classificationName) {
        final ClassificationMeta meta = findClassificationMeta((String) classificationName, () -> {
            return "${#cdef.values('" + classificationName + "')}";
        });
        return meta.listAll();
    }

    // -----------------------------------------------------
    //                                               alias()
    //                                               -------
    /**
     * Get Classification alias.
     * @param def The instance of classification to get code. (NotNull)
     * @return classification alias. (non classification is return null)
     */
    public String alias(Object def) {
        if (def instanceof Classification) {
            return findClassificationAlias((Classification) def);
        }
        // TODO jflute exception? (2015/11/29)
        return null;
    }

    /**
     * Get classification alias.
     * @param classificationName The name of classification. (NotNull)
     * @param elementName The name of classification element. (NotNull)
     * @return classification alias (NullAllowed: if not found)
     */
    public String alias(String classificationName, String elementName) {
        final ClassificationMeta meta = findClassificationMeta((String) classificationName, () -> {
            return "${#cdef.alias('" + classificationName + "', '" + elementName + "')}";
        });
        final Classification def = meta.nameOf(elementName);
        return def == null ? null : findClassificationAlias(def);
    }

    // -----------------------------------------------------
    //                                                code()
    //                                                ------
    /**
     * Get classification code.
     * @param def The instance of classification to get code. (NotNull)
     * @return The code of classification. (NullAllowed: if not classification)
     */
    public String code(Object def) {
        return def instanceof Classification ? ((Classification) def).code() : null;
    }

    /**
     * Get classification code.
     * @param classificationName The name of classification. (NotNull)
     * @param elementName The name of classification element. (NotNull)
     * @return The found code of classification. (NullAllowed: if not found)
     */
    public String code(String classificationName, String elementName) {
        final ClassificationMeta meta = findClassificationMeta((String) classificationName, () -> {
            return "${#cdef.code('" + classificationName + "', '" + elementName + "')}";
        });
        final Classification def = meta.nameOf(elementName);
        return def == null ? null : def.code();
    }

    // -----------------------------------------------------
    //                                               ...Of()
    //                                               -------
    /**
     * Get Classification by code.
     * @param type classification-name
     * @param code classification element code
     * @return Classification
     */
    public Classification codeOf(String type, String code) {
        try {
            final ClassificationMeta meta = findClassificationMeta((String) type, new Supplier<Object>() {
                public Object get() {
                    return "${#cdef.codeOf('" + type + "', '" + code + "')}";
                }
            });
            return meta.codeOf(code);
        } catch (Exception e) {
            throw new TemplateProcessingException(e.getMessage(), e);
        }
    }

    /**
     * Get Classification by name.
     * @param type classification-name
     * @param name classification element name
     * @return Classification
     */
    public Classification nameOf(String type, String name) {
        try {
            final ClassificationMeta meta = findClassificationMeta((String) type, new Supplier<Object>() {
                public Object get() {
                    return "${#cdef.nameOf('" + type + "', '" + name + "')}";
                }
            });
            return meta.nameOf(name);
        } catch (Exception e) {
            throw new TemplateProcessingException(e.getMessage(), e);
        }
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
            throwListedClassificationNotFoundException(classificationName, callerInfo);
            return null; // unreachable
        }
    }

    protected void throwListedClassificationNotFoundException(String classificationName, Supplier<Object> callerInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the classification for the list.");
        br.addItem("Requested Template Path");
        br.addElement(getRequestTemplatePath());
        br.addItem("Target Expression");
        br.addElement(callerInfo.get());
        br.addItem("Classification Name");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new TemplateProcessingException(msg);
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
}
