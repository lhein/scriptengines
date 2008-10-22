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

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.google.code.scriptengines.js.util.ExtendedScriptException;

final class RhinoCompiledScript extends CompiledScript {
    
    private RhinoScriptEngine engine;
    private Script script;
    private final static boolean DEBUG = RhinoScriptEngine.DEBUG;
    
    RhinoCompiledScript(RhinoScriptEngine engine, Script script) {
        this.engine = engine;
        this.script = script;
    }
    
    public Object eval(ScriptContext context) throws ScriptException {
        
        Object result = null;
        Context cx = RhinoScriptEngine.enterContext();
        try {
            
            Scriptable scope = engine.getRuntimeScope(context);
            Object ret = script.exec(cx, scope);
            result = engine.unwrapReturnValue(ret);
        } catch (JavaScriptException jse) {
            if (DEBUG) jse.printStackTrace();
            int line = (line = jse.lineNumber()) == 0 ? -1 : line;
            Object value = jse.getValue();
            String str = (value != null && value.getClass().getName().equals("org.mozilla.javascript.NativeError") ?
                          value.toString() :
                          jse.toString());
            throw new ExtendedScriptException(jse, str, jse.sourceName(), line);
        } catch (RhinoException re) {
            if (DEBUG) re.printStackTrace();
            int line = (line = re.lineNumber()) == 0 ? -1 : line;
            throw new ExtendedScriptException(re, re.toString(), re.sourceName(), line);
        } finally {
            Context.exit();
        }
        
        return result;
    }
    
    public ScriptEngine getEngine() {
        return engine;
    }
    
}
