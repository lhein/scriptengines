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

import java.util.LinkedList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class ScriptEnginePool {
    
    private static final int DEFAULT_CAPACITY = 10;
    private int capacity;
    private int size;
    private LinkedList pool;
    private ScriptEngineFactory fact;
    private boolean multiThreaded;
    
    public ScriptEnginePool(ScriptEngineFactory fact, int capacity) {
        
        pool = new LinkedList();
        this.capacity = capacity;
        this.fact = fact;
        this.size = 0;
        String value = (String)fact.getParameter("THREADING");
        if (value.equals("THREAD-ISOLATED") || value.equals("STATELESS")) {
            multiThreaded = true;
            //just use a single engine
            capacity = 1;
        } else {
            multiThreaded = false;
        }
    }

    public ScriptEnginePool(ScriptEngineFactory fact) {
        this(fact, DEFAULT_CAPACITY);
    }
    
    public ScriptEnginePool(ScriptEngineFactory fact, ScriptEngine eng) {
        this(fact);
        synchronized(this) {
            pool.addLast(eng);
        }
    }
    
    public synchronized ScriptEngine checkOut() {
        
        if (pool.size() > 0) {
            //there engines in pool awaiting reuse
            if (multiThreaded) {
                //always return first (only) engine.. do not remove
                return (ScriptEngine)pool.getFirst();
            } else {
                return (ScriptEngine)pool.removeFirst();
            }
        } else if (size < capacity) {
            //create a new engine
            size++;
            ScriptEngine ret = fact.getScriptEngine();
            if (multiThreaded) {
                //size and capacity are now both 1.  Add the engine to
                //the pool for everyone to use. 
                pool.add(ret);
            }
            return ret;
        } else {
            //won't get here in multiThreaded case
            while (pool.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            return (ScriptEngine)pool.removeFirst();
        }
    }
    
    public synchronized void checkIn(ScriptEngine eng) {
        
        if (multiThreaded) {
            //pool always contatins exactly one engine
            return;
        }
        
        pool.addLast(eng);
        notify();
    }
}
        
    
    
