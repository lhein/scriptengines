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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.google.code.scriptengines.js.util.DeTagifier;

public class EmbeddedRhinoScriptEngine extends RhinoScriptEngine {
    
    protected DeTagifier detagifier;
    
    public EmbeddedRhinoScriptEngine() {
        detagifier = new DeTagifier("context.getWriter().write(\"",
                                    "\");\n",
                                    "context.getWriter().write(",
                                    ");\n");
    }
    
    protected Reader preProcessScriptSource(Reader reader) throws ScriptException {
        try {
            String s = detagifier.parse(reader);
            return new StringReader(s);
        }
        catch (IOException ee) {
            throw new ScriptException(ee);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No file specified");
            return;
        }
        
        InputStreamReader r = new InputStreamReader(new FileInputStream(args[0]));
        ScriptEngine engine = new EmbeddedRhinoScriptEngine();
        
        SimpleScriptContext context = new SimpleScriptContext();
        engine.put(ScriptEngine.FILENAME, args[0]);
        engine.eval(r, context);
        context.getWriter().flush();
    }
}
