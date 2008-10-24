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

import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

public class GroovyCompiledScript extends CompiledScript {

    private GroovyScriptEngine engine;
    private Class clasz;

    public GroovyCompiledScript(GroovyScriptEngine engine, Class clasz) {
        this.engine = engine;
        this.clasz = clasz;
    }

    public Object eval(ScriptContext context) throws ScriptException {
        return engine.eval(clasz, context);
    }

    public ScriptEngine getEngine() {
        return engine;
    }

}
