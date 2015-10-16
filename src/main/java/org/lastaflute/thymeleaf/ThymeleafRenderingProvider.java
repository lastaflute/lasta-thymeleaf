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

import java.util.HashSet;

import org.lastaflute.thymeleaf.processor.LastaThymeleafDialect;
import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;
import org.lastaflute.web.util.LaServletContextUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Thymeleaf rendering provider of Lastaflute.
 * @author schatten
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
    private TemplateEngine cachedTemplateEngine;

    // ===================================================================================
    //                                                                             Provide
    //                                                                             =======
    /**
     * @param runtime The runtime of current requested action. (NotNull)
     * @param journey The journey to next stage. (NotNull)
     * @return The renderer to render HTML. (NotNull)
     * @see org.lastaflute.web.ruts.renderer.HtmlRenderingProvider#provideRenderer(org.lastaflute.web.callback.ActionRuntime, org.lastaflute.web.ruts.NextJourney)
     */
    @Override
    public HtmlRenderer provideRenderer(ActionRuntime runtime, NextJourney journey) {
        return new ThymeleafHtmlRenderer(getTemplateEngine());
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
        setupTemplateEngin(engine);
        return engine;
    }

    protected TemplateEngine newTemplateEngine() {
        return new TemplateEngine();
    }

    protected void setupTemplateEngin(TemplateEngine engine) {
        engine.addTemplateResolver(createTemplateResolver());

        final StandardMessageResolver standardMessageResolver = new StandardMessageResolver();
        standardMessageResolver.setOrder(1);
        engine.addMessageResolver(standardMessageResolver);

        final LastaThymeleafMessageResolver lastaThymeleafMessageResolver = new LastaThymeleafMessageResolver();
        lastaThymeleafMessageResolver.setOrder(10);
        engine.addMessageResolver(lastaThymeleafMessageResolver);

        LastaThymeleafDialect dialect = new LastaThymeleafDialect(engine.getConfiguration());
        engine.addDialect(dialect);

        IDialect standard = engine.getDialectsByPrefix().get(StandardDialect.PREFIX);
        if (standard instanceof StandardDialect) {
            // Add to standard prefix access
            ((StandardDialect)standard).setAdditionalProcessors(new HashSet<IProcessor>(dialect.getProcessors()));
        }

    }

    protected TemplateResolver createTemplateResolver() {
        final ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
        resolver.setPrefix(LaServletContextUtil.getJspViewPrefix());
        resolver.setTemplateMode(getTemplateMode());
        resolver.setCharacterEncoding(getEncoding());
        return resolver;
    }

    protected String getTemplateMode() {
        return DEFAULT_TEMPLATE_MODE;
    }

    protected String getEncoding() {
        return DEFAULT_TEMPLATE_ENCODING;
    }
}
