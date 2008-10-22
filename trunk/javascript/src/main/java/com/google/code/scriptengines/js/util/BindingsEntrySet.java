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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

public class BindingsEntrySet extends AbstractSet<Map.Entry<String, Object>> {
    
    private BindingsBase base;
    private String[] keys;
    
    public BindingsEntrySet(BindingsBase base) {
        this.base = base;
        keys = base.getNames();
    }
    
    public int size() {
        return keys.length;
    }
    
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new BindingsIterator();
    }
    
    public class BindingsEntry implements Map.Entry<String, Object> {
        private String key;
        public BindingsEntry(String key) {
            this.key = key;
        }
        
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }
        
        public String getKey() {
            return key;
        }
        
        public Object getValue() {
            return base.get(key);
        }
        
    }
    
    public class BindingsIterator implements Iterator<Map.Entry<String, Object>> {
        
        private int current = 0;
        private boolean stale = false;
        
        public boolean hasNext() {
            return (current < keys.length);
        }
       
        public BindingsEntry next() {
            stale = false;
            return new BindingsEntry(keys[current++]);
        }
        
        public void remove() {
            if (stale || current == 0) {
                throw new IllegalStateException();
            }
            
            stale = true;
            base.remove(keys[current - 1]);
        }
        
    }
    
}
