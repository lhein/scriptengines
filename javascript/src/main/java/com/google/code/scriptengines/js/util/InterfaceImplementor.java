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
package com.google.code.scriptengines.js.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.script.Invocable;
import javax.script.ScriptException;

public class InterfaceImplementor {
    
    private Invocable engine;
    
    /** Creates a new instance of Invocable */
    public InterfaceImplementor(Invocable engine) {
        this.engine = engine;
    }
    
    public class InterfaceImplementorInvocationHandler implements InvocationHandler {
        private Invocable engine;
        private Object thiz;
        public InterfaceImplementorInvocationHandler(Invocable engine, Object thiz) {
            
            this.engine = engine;
            this.thiz = thiz;
        }
        
        public Object invoke(Object proxy , Method method, Object[] args)
        throws java.lang.Throwable {
            // give chance to convert input args
            args = convertArguments(method, args);
            Object result = engine.invokeMethod(thiz, method.getName(), args);
            // give chance to convert the method result
            return convertResult(method, result);
        }
    }
    
    public <T> T getInterface(Object thiz, Class<T> iface)
    throws ScriptException {
        if (iface == null || !iface.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(),
                new Class[]{iface},
                new InterfaceImplementorInvocationHandler(engine, thiz)));
    }

    // called to convert method result after invoke
    protected Object convertResult(Method method, Object res) 
                                   throws ScriptException {
        // default is identity conversion
        return res;
    }

    // called to convert method arguments before invoke
    protected Object[] convertArguments(Method method, Object[] args)
                                      throws ScriptException {
        // default is identity conversion
        return args;
    }
}
