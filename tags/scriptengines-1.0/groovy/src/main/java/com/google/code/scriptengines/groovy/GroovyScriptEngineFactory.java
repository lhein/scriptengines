/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.scriptengines.groovy;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngine;

import org.codehaus.groovy.runtime.InvokerHelper;

public class GroovyScriptEngineFactory implements ScriptEngineFactory {

    private static String VERSION = "1.5.6";
    private static List names;
    private static List extensions;
    private static List mimeTypes;

    static {
        names = new ArrayList(1);
        names.add("groovy");
        names = Collections.unmodifiableList(names);
        extensions = names;
        mimeTypes = new ArrayList(0);
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }

    public GroovyScriptEngineFactory() {
    }

    public String getEngineName() {
        return "groovy";
    }

    public String getEngineVersion() {
        return InvokerHelper.getVersion();
    }

    public String getLanguageName() {
        return "groovy";
    }

    public String getLanguageVersion() {
        return VERSION;
    }

    public List getExtensions() {
        return extensions;
    }

    public List getMimeTypes() {
        return mimeTypes;
    }

    public List getNames() {
        return names;
    }

    public Object getParameter(String key) {
        if ("javax.script.name".equals(key)) {
            return "Groovy";
        } else if ("javax.script.engine".equals(key)) {
            return "Groovy Script Engine";
        } else if ("javax.script.engine_version".equals(key)) {
            return InvokerHelper.getVersion();
        } else if ("javax.script.language".equals(key)) {
            return "Groovy";
        } else if ("javax.script.language_version".equals(key)) {
            return VERSION;
        } else if ("THREADING".equals(key)) {
            return "MULTITHREADED";
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    public ScriptEngine getScriptEngine() {
        return new GroovyScriptEngine();
    }

    public String getMethodCallSyntax(String obj, String method, String args[]) {
        StringBuilder buf = new StringBuilder();
        buf.append(obj).append(".").append(method).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
	            buf.append(",");
            }
            buf.append(args[i]);
        }
        buf.append(")");
        return buf.toString();
    }

    public String getOutputStatement(String toDisplay) {
        StringBuilder buf = new StringBuilder();
        buf.append("println(\"");
        int len = toDisplay.length();
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
                case '"':
                    buf.append("\\\"");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(ch);
                    break;
            }
        }
        buf.append("\")");
        return buf.toString();
    }

    public String getProgram(String statements[]) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < statements.length; i++) {
            ret.append(statements[i]);
            ret.append('\n');
        }
        return ret.toString();
    }

}
