/**
 * ﻿Original work: copyright 1999-2004 The Apache Software Foundation
 * (http://www.apache.org/)
 *
 * This project is based on the work licensed to the Apache Software
 * Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Modified work: copyright 2013-2019 Valery Silaev (http://vsilaev.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.javaflow.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClasspathResourceLoader implements VetoableResourceLoader {

    private final ClassLoader classLoader;

    public ClasspathResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public InputStream getResourceAsStream(String name) throws IOException {
        InputStream result = classLoader.getResourceAsStream(name);
        if (null == result) {
            throw new IOException("Unable to find resource " + name);
        }
        return result;
    }
    
    public ClassMatcher createVeto() throws IOException {
        List<ClassMatcher> matchers = new ArrayList<ClassMatcher>();
        Enumeration<URL> allResources = classLoader.getResources("/META-INF/net.tascalate.javaflow.veto.cmf");
        ClassMatcherFileParser parser = new ClassMatcherFileParser();
        while (allResources.hasMoreElements()) {
            URL resource = allResources.nextElement();
            ClassMatcher matcher = parser.parse(resource);
            if (null != matcher && ClassMatchers.MATCH_NONE != matcher) {
                matchers.add(matcher);
            }
        }
        return matchers.isEmpty() ? ClassMatchers.MATCH_NONE : ClassMatchers.whenAny(matchers);
    }
    
    /**
     * Check if <code>maybeParent</code> is a parent (probably inderect) of the <code>classLoader</code>
     * @param classLoader The classloader whose parents are checked, may not be null
     * @param maybeParent Possible parent, may be null for boot class loader
     * @return
     */
    public static boolean isClassLoaderParent(ClassLoader classLoader, ClassLoader maybeParent) {
        ClassLoader cl = classLoader;
        do {
            cl = cl.getParent();
            if (maybeParent == cl) {
                // Check includes null == null for bootstrap classloader
                return true;
            }
        } while (cl != null);
        return false;
    }
}
