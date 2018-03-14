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

import org.lastaflute.thymeleaf.customizer.ThymeleafAdditionalExpressionSetupper;
import org.lastaflute.thymeleaf.processor.LastaThymeleafDialect;
import org.lastaflute.thymeleaf.processor.LastaThymeleafMistakeDialect;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;
import org.lastaflute.web.util.LaServletContextUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Thymeleaf rendering provider of Lastaflute.
 * @author schatten
 * @author jflute
 */
public class ThymeleafRenderingProvider implements HtmlRenderingProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_TEMPLATE_MODE = "HTML5";
    public static final String DEFAULT_TEMPLATE_ENCODING = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean development;
    protected ThymeleafAdditionalExpressionSetupper additionalExpressionSetupper; // null allowed
    private TemplateEngine cachedTemplateEngine;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public ThymeleafRenderingProvider asDevelopment(boolean development) {
        this.development = development;
        return this;
    }

    public ThymeleafRenderingProvider additionalExpression(ThymeleafAdditionalExpressionSetupper additionalExpressionSetupper) {
        if (additionalExpressionSetupper == null) {
            throw new IllegalArgumentException("The argument 'additionalExpressionSetupper' should not be null.");
        }
        this.additionalExpressionSetupper = additionalExpressionSetupper;
        return this;
    }

    // ===================================================================================
    //                                                                             Provide
    //                                                                             =======
    /**
     * @param runtime The runtime of current requested action. (NotNull)
     * @param journey The journey to next stage. (NotNull)
     * @return The renderer to render HTML. (NotNull)
     * @see org.lastaflute.web.ruts.renderer.HtmlRenderingProvider#provideRenderer(org.lastaflute.web.ruts.process.ActionRuntime, org.lastaflute.web.ruts.NextJourney)
     */
    @Override
    public HtmlRenderer provideRenderer(ActionRuntime runtime, NextJourney journey) {
        if (journey.getRoutingPath().endsWith(".jsp")) {
            return DEFAULT_RENDERER;
        }
        return createThymeleafHtmlRenderer();
    }

    protected ThymeleafHtmlRenderer createThymeleafHtmlRenderer() {
        return new ThymeleafHtmlRenderer(getTemplateEngine());
    }

    @Override
    public HtmlResponse provideShowErrorsResponse(ActionRuntime runtime) {
        return HtmlResponse.fromForwardPath("/error/show_errors.html");
    }

    // ===================================================================================
    //                                                                     Template Engine
    //                                                                     ===============
    protected TemplateEngine getTemplateEngine() {
        if (cachedTemplateEngine != null) {
            return cachedTemplateEngine;
        }
        synchronized (this) {
            if (cachedTemplateEngine != null) {
                return cachedTemplateEngine;
            }
            cachedTemplateEngine = createTemplateEngine();
        }
        return cachedTemplateEngine;
    }

    protected TemplateEngine createTemplateEngine() {
        final TemplateEngine engine = newTemplateEngine();
        setupTemplateEngine(engine);
        return engine;
    }

    protected TemplateEngine newTemplateEngine() {
        return new TemplateEngine();
    }

    protected void setupTemplateEngine(TemplateEngine engine) {
        engine.addTemplateResolver(createTemplateResolver());
        engine.addMessageResolver(createStandardMessageResolver());
        engine.addMessageResolver(createLastaThymeleafMessageResolver());
        engine.addDialect(createLastaThymeleafDialect(engine));
        engine.addDialect(createLastaThymeleafMistakeDialect(engine));
    }

    // -----------------------------------------------------
    //                                     Template Resolver
    //                                     -----------------
    protected ITemplateResolver createTemplateResolver() {
        final ServletContextTemplateResolver resolver = newServletContextTemplateResolver();
        resolver.setPrefix(getHtmlViewPrefix());
        resolver.setTemplateMode(getTemplateMode());
        resolver.setCharacterEncoding(getEncoding());
        resolver.setCacheable(isCacheable());
        return resolver;
    }

    protected ServletContextTemplateResolver newServletContextTemplateResolver() {
        return new ServletContextTemplateResolver(LaServletContextUtil.getServletContext());
    }

    protected String getHtmlViewPrefix() {
        return LaServletContextUtil.getHtmlViewPrefix();
    }

    protected String getTemplateMode() {
        return DEFAULT_TEMPLATE_MODE;
    }

    protected String getEncoding() {
        return DEFAULT_TEMPLATE_ENCODING;
    }

    protected boolean isCacheable() {
        return !development;
    }

    // -----------------------------------------------------
    //                                      Message Resolver
    //                                      ----------------
    protected StandardMessageResolver createStandardMessageResolver() {
        final StandardMessageResolver resolver = new StandardMessageResolver();
        resolver.setOrder(1);
        return resolver;
    }

    protected LastaThymeleafMessageResolver createLastaThymeleafMessageResolver() {
        final LastaThymeleafMessageResolver resolver = newLastaThymeleafMessageResolver();
        resolver.setOrder(10);
        return resolver;
    }

    protected LastaThymeleafMessageResolver newLastaThymeleafMessageResolver() {
        return new LastaThymeleafMessageResolver();
    }

    // -----------------------------------------------------
    //                                          Main Dialect
    //                                          ------------
    protected LastaThymeleafDialect createLastaThymeleafDialect(TemplateEngine engine) {
        final LastaThymeleafDialect dialect = newLastaThymeleafDialect();
        if (additionalExpressionSetupper != null) {
            dialect.additionalExpression(additionalExpressionSetupper);
        }
        return dialect;
    }

    protected LastaThymeleafDialect newLastaThymeleafDialect() {
        return new LastaThymeleafDialect();
    }

    // -----------------------------------------------------
    //                                       Mistake Dialect
    //                                       ---------------
    protected LastaThymeleafMistakeDialect createLastaThymeleafMistakeDialect(TemplateEngine engine) {
        return new LastaThymeleafMistakeDialect();
    }
}
