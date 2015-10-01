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

import javax.servlet.ServletException;

import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * @author jflute
 */
public class ThymeleafHtmlRenderer implements HtmlRenderer {

    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        final TemplateEngine engine = createTemplateEngine();
        final IContext context = createTemplateContext(); // #thinking form to context
        final String html = engine.process(journey.getRoutingPath(), context);
        write(requestManager, html);
    }

    protected TemplateEngine createTemplateEngine() {
        final TemplateEngine engine = newTemplateEngine();
        engine.addTemplateResolver(createTemplateResolver());
        return engine; // #thinking needs instancee cache?
    }

    protected TemplateEngine newTemplateEngine() {
        return new TemplateEngine();
    }

    protected TemplateResolver createTemplateResolver() {
        return new ClassLoaderTemplateResolver();
    }

    protected Context createTemplateContext() {
        return new Context();
    }

    protected void write(RequestManager requestManager, String html) {
        requestManager.getResponseManager().write(html, "text/html", getEncoding());
    }

    protected String getEncoding() {
        return "UTF-8";
    }
}
