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
package org.lastaflute.thymeleaf.customizer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jflute
 * @since 0.3.0 (2017/03/22 Wednesday)
 */
public class ThymeleafAdditionalExpressionResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Map<String, Object> expressionObjectMap = new LinkedHashMap<String, Object>();
    protected final Set<String> cacheableExpressionObjectNames = new LinkedHashSet<String>();

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    public void registerExpressionObject(String key, Object expressionObject) {
        expressionObjectMap.put(key, expressionObject);
    }

    public void registerExpressionObject(String key, Object expressionObject, boolean cacheable) {
        expressionObjectMap.put(key, expressionObject);
        if (cacheable) {
            cacheableExpressionObjectNames.add(key);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, Object> getExpressionObjectMap() {
        return Collections.unmodifiableMap(expressionObjectMap);
    }

    public Set<String> getCacheableExpressionObjectNames() {
        return Collections.unmodifiableSet(cacheableExpressionObjectNames);
    }
}
