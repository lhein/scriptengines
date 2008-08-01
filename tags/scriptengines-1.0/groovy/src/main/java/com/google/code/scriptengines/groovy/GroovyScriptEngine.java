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

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.io.Reader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import javax.script.Compilable;
import javax.script.AbstractScriptEngine;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.script.ScriptEngineFactory;
import javax.script.CompiledScript;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.MetaClassHelper;
import groovy.lang.GroovyClassLoader;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;
import groovy.lang.Binding;
import groovy.lang.DelegatingMetaClass;
import groovy.lang.MetaClass;
import groovy.lang.Tuple;
import groovy.lang.MissingMethodException;
import groovy.lang.Closure;

public class GroovyScriptEngine extends AbstractScriptEngine implements Compilable, Invocable {

    private Map<String, Class> classMap = new ConcurrentHashMap<String, Class>();
    private Map<String, MethodClosure> globalClosures = new ConcurrentHashMap<String, MethodClosure>();
    private GroovyClassLoader loader;
    private volatile GroovyScriptEngineFactory factory;
    private static int counter = 0;

    public GroovyScriptEngine() {
        loader = new GroovyClassLoader(getParentLoader(), new CompilerConfiguration());
    }

    public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
        return eval(readFully(reader), ctx);
    }

    public Object eval(String script, ScriptContext ctx) throws ScriptException {
        try {
            return eval(getScriptClass(script), ctx);
        } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(), e.getSourceLocator(), e.getLine());
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = new GroovyScriptEngineFactory();
                }
            }
        }
        return factory;
    }

    public CompiledScript compile(String scriptSource) throws ScriptException {
        try {
            return new GroovyCompiledScript(this, getScriptClass(scriptSource));
        } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(), e.getSourceLocator(), e.getLine());
        } catch (IOException e) {
            throw new ScriptException(e);
        } catch (CompilationFailedException ee) {
            throw new ScriptException(ee);
        }
    }

    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(readFully(reader));
    }

    public Object invokeFunction(String name, Object args[]) throws ScriptException, NoSuchMethodException {
        return invokeImpl(null, name, args);
    }

    public Object invokeMethod(Object thiz, String name, Object args[]) throws ScriptException, NoSuchMethodException {
        if (thiz == null) {
            throw new IllegalArgumentException("script object is null");
        } else {
            return invokeImpl(thiz, name, args);
        }
    }

    public Object getInterface(Class clasz) {
        return makeInterface(null, clasz);
    }

    public Object getInterface(Object thiz, Class clasz) {
        if (thiz == null)
            throw new IllegalArgumentException("script object is null");
        else
            return makeInterface(thiz, clasz);
    }

    Object eval(Class scriptClass, final ScriptContext ctx) throws ScriptException {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        java.io.Writer writer = ctx.getWriter();
        ctx.setAttribute("out", writer instanceof PrintWriter ? writer : new PrintWriter(writer), ScriptContext.ENGINE_SCOPE);
        Binding binding = new Binding() {
            public Object getVariable(String name) {
                synchronized (ctx) {
                    int scope = ctx.getAttributesScope(name);
                    if (scope != -1) {
                        return ctx.getAttribute(name, scope);
                    }
                    throw new MissingPropertyException(name, getClass());
                }
            }
            public void setVariable(String name, Object value) {
                synchronized (ctx) {
                    int scope = ctx.getAttributesScope(name);
                    if (scope == -1) {
                        scope = ScriptContext.ENGINE_SCOPE;
                    }
                    ctx.setAttribute(name, value, scope);
                }
            }
        };
        try {
            Script scriptObject = InvokerHelper.createScript(scriptClass, binding);
            Method methods[] = scriptClass.getMethods();
            Map<String, MethodClosure> closures = new HashMap<String, MethodClosure>();
            for (Method m : methods) {
                String name = m.getName();
                closures.put(name, new MethodClosure(scriptObject, name));
            }

            globalClosures.putAll(closures);
            final MetaClass oldMetaClass = scriptObject.getMetaClass();
            scriptObject.setMetaClass(new DelegatingMetaClass(oldMetaClass) {
                public Object invokeMethod(Object object, String name, Object args) {
                    if (args == null) {
                        return invokeMethod(object, name, MetaClassHelper.EMPTY_ARRAY);
                    } else if (args instanceof Tuple) {
                        return invokeMethod(object, name, ((Tuple) args).toArray());
                    } else  if (args instanceof Object[]) {
                        return invokeMethod(object, name, (Object[]) (Object[]) args);
                    } else {
                        return invokeMethod(object, name, new Object[] { args });
                    }
                }
                public Object invokeMethod(Object object, String name, Object args[]) {
                    try {
                        return super.invokeMethod(object, name, args);
                    } catch (MissingMethodException mme) {
                        return callGlobal(name, args, ctx);
                    }
                }
                public Object invokeStaticMethod(Object object, String name, Object args[]) {
                    try {
                        return super.invokeStaticMethod(object, name, args);
                    } catch (MissingMethodException mme) {
                        return callGlobal(name, args, ctx);
                    }
                }
            });
            return scriptObject.run();
        }
        catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    Class getScriptClass(String script) throws SyntaxException, CompilationFailedException, IOException {
        Class clazz = (Class) classMap.get(script);
        if (clazz != null) {
            return clazz;
        } else {
            java.io.InputStream stream = new ByteArrayInputStream(script.getBytes());
            clazz = loader.parseClass(stream, generateScriptName());
            classMap.put(script, clazz);
            return clazz;
        }
    }

    private Object invokeImpl(Object thiz, String name, Object args[]) throws ScriptException, NoSuchMethodException {
        if (name == null) {
            throw new NullPointerException("method name is null");
        }
        try {
            if (thiz != null) {
                return InvokerHelper.invokeMethod(thiz, name, ((Object) (args)));
            }
        } catch (MissingMethodException mme) {
            throw new NoSuchMethodException(mme.getMessage());
        } catch (Exception e) {
            throw new ScriptException(e);
        }
        return callGlobal(name, args);
    }

    private Object callGlobal(String name, Object args[]) {
        return callGlobal(name, args, context);
    }

    private Object callGlobal(String name, Object args[], ScriptContext ctx) {
        Closure closure = globalClosures.get(name);
        if (closure != null) {
            return closure.call(args);
        }
        Object value = ctx.getAttribute(name);
        if (value instanceof Closure) {
            return ((Closure) value).call(args);
        } else {
            throw new MissingMethodException(name, getClass(), args);
        }
    }

    private synchronized String generateScriptName() {
        return "Script" + ++counter + ".groovy";
    }

    private Object makeInterface(final Object obj, Class clazz) {
        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        } else {
            return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                public Object invoke(Object proxy, Method m, Object args[]) throws Throwable {
                    return invokeImpl(obj, m.getName(), args);
                }
            });
        }
    }

    private ClassLoader getParentLoader() {
        ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class c = ctxtLoader.loadClass("groovy.lang.Script");
            if (c == groovy.lang.Script.class) {
                return ctxtLoader;
            }
        } catch (ClassNotFoundException cnfe) {
        }
        return groovy.lang.Script.class.getClassLoader();
    }

    private String readFully(Reader reader) throws ScriptException {
        char arr[] = new char[8192];
        StringBuilder buf = new StringBuilder();
        int numChars;
        try {
            while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
                buf.append(arr, 0, numChars);
            }
        } catch (IOException exp) {
            throw new ScriptException(exp);
        }
        return buf.toString();
    }

}
