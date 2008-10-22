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
package com.google.code.scriptengines.js.javascript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;

import com.google.code.scriptengines.js.util.ScriptEngineFactoryBase;

/**
 * Factory to create EmbeddedRhinoScriptEngine
 *
 */
public class EmbeddedRhinoScriptEngineFactory extends ScriptEngineFactoryBase {

    private Properties properties;
    private boolean initialized;
        
    public EmbeddedRhinoScriptEngineFactory() {
    }
    
    public List<String> getExtensions() {
        return extensions;
    }
    
    public List<String> getMimeTypes() {
        return mimeTypes;
    }
    
    public List<String> getNames() {
        return names;
    }
    
    public Object getParameter(String key) {
        if (key.equals(ScriptEngine.NAME)) {
            return "embedded-javascript";
        } else if (key.equals(ScriptEngine.ENGINE)) {
            return "Mozilla Rhino";
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return "1.6 release 2";
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return "EmbeddedECMAScript";
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return "1.6";
        } else if (key.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ScriptEngine getScriptEngine() {
        EmbeddedRhinoScriptEngine ret = new EmbeddedRhinoScriptEngine();
        ret.setEngineFactory(this);
        return ret;
    }
            
    public String getMethodCallSyntax(String obj, String method, String... args) {
        
        String ret = obj + "." + method + "(";
        int len = args.length;
        if (len == 0) {
            ret += ")";
            return ret;
        }
        
        for (int i = 0; i < len; i++) {
            ret += args[i];
            if (i != len - 1) {
                ret += ",";
            } else {
                ret += ")";
            }
        }
        return ret;
    }
    
    public String getOutputStatement(String toDisplay) {
        return "print(" + toDisplay + ")";
    }
    
    public String getProgram(String... statements) {
        int len = statements.length;
        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += statements[i] + ";";
        }
        
        return ret;
    }
    
    public static void main(String[] args) {
        EmbeddedRhinoScriptEngineFactory fact = new EmbeddedRhinoScriptEngineFactory();
        System.out.println(fact.getParameter(ScriptEngine.ENGINE_VERSION));
    }
    
    private static List<String> names;
    private static List<String> mimeTypes;
    private static List<String> extensions;
    
    static {
        names = new ArrayList<String>(6);
        names.add("ejs");
        names.add("EmbeddedJavaScript");
        names.add("embeddedjavascript");
        names = Collections.unmodifiableList(names);

        mimeTypes = new ArrayList<String>(4);
        mimeTypes.add("application/embeddedjavascript");
        mimeTypes.add("text/embeddedjavascript");
        mimeTypes = Collections.unmodifiableList(mimeTypes);

        extensions = new ArrayList<String>(1);
        extensions.add("ejs");
        extensions = Collections.unmodifiableList(extensions);
    }
}
