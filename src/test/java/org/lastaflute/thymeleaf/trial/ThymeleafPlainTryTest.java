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
package org.lastaflute.thymeleaf.trial;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dbflute.utflute.core.PlainTestCase;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * @author jflute
 */
public class ThymeleafPlainTryTest extends PlainTestCase {

    private static final String BASE_DIR = "/target/test-classes/try-templates";

    public void test_trial1() throws IOException {
        // ## Arrange ##
        TemplateEngine engine = new TemplateEngine();
        engine.addTemplateResolver(new FileTemplateResolver());

        // ## Act ##
        String canonicalPath = getProjectDir().getCanonicalPath();
        String template = canonicalPath + BASE_DIR + "/thymeleaf-try1-no-th.html";
        Context context = new Context();
        String processed = engine.process(template, context);

        // ## Assert ##
        log(ln() + processed);
    }

    public void test_trial2() throws Exception {
        // ## Arrange ##
        TemplateEngine engine = new TemplateEngine();
        engine.addTemplateResolver(new FileTemplateResolver());

        // ## Act ##
        String canonicalPath = getProjectDir().getCanonicalPath();

        Map<String, Object> variableMap = new HashMap<>();
        HeaderBean headerBean = new HeaderBean();
        headerBean.isLogin = true;
        headerBean.memberName = "jflute";
        variableMap.put("title", "try's contents");
        variableMap.put("headerBean", headerBean);

        IContext context = new Context(Locale.ENGLISH, variableMap);
        String template = canonicalPath + BASE_DIR + "/thymeleaf-try2-simple-th.html";
        String processed = engine.process(template, context);

        // ## Assert ##
        log(ln() + processed);
    }

    public static class HeaderBean {

        public boolean isLogin;
        public String memberName;
    }
}
