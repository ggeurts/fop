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

/* $Id$ */

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.util.HashMap;
import java.util.Iterator;


/**
 * <p>Attributes for RtfText.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch).</p>
 */

public class RtfAttributes implements Cloneable {

    private final HashMap values;

    /** Creates empty RTF attribute set */
    public RtfAttributes() {
        values = new HashMap();
    }
    
    /** 
     * Creates clone of other RTF attribute set 
     * @param other The RTF attributes to clone
     */
    public RtfAttributes(RtfAttributes other)
    {
        this.values = (HashMap)other.values.clone();
    }
    
    /**
     * Set attributes from another attributes object
     * @param attrs RtfAttributes object whose elements will be copied into this
     *        instance
     * @return this object, for chaining calls
     */
    public RtfAttributes set(RtfAttributes attrs) {
        if (attrs != null) {
            Iterator it = attrs.nameIterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                if (attrs.getValue(name) instanceof Integer) {
                    Integer value = (Integer)attrs.getValue(name);
                    if (value == null) {
                        set(name);
                    }  else {
                        set(name, value.intValue());
                    }
                } else if (attrs.getValue(name) instanceof String) {
                    String value = (String)attrs.getValue(name);
                    if (value == null) {
                        set(name);
                    } else {
                        set(name, value);
                    }
                } else {
                    set(name);
                }
            }
        }
        return this;
    }

    /**
     * set an attribute that has no value.
     * @param name name of attribute to set
     * @return this object, for chaining calls
     */
    public RtfAttributes set(String name) {
        values.put(name, null);
        return this;
    }

    /**
     * unset an attribute that has no value
     * @param name name of attribute to unset
     * @return this object, for chaining calls
     */
    public RtfAttributes unset(String name) {
        values.remove(name);
        return this;
    }

    /**
     * debugging log
     * @return String representation of object
     */
    public String toString() {
        return values.toString() + "(" + super.toString() + ")";
    }

    /** {@inheritDoc} */
    public Object clone() throws CloneNotSupportedException {
        return new RtfAttributes(this);
    }

    /**
     * Set an attribute that has an integer value
     * @param name name of attribute
     * @param value value of attribute
     * @return this (which now contains the new entry), for chaining calls
     */
    public RtfAttributes set(String name, int value) {
        values.put(name, new Integer(value));
        return this;
    }

    /**
     * Set an attribute that has a String value
     * @param name name of attribute
     * @param type value of attribute
     * @return this (which now contains the new entry)
     */
    public RtfAttributes set(String name, String type) {
        values.put(name, type);
        return this;
    }

    /**
     * Set an attribute that has nested attributes as value
     * @param name name of attribute
     * @param value value of the nested attributes
     * @return this (which now contains the new entry)
     */
    public RtfAttributes set(String name, RtfAttributes value) {
        values.put(name, value);
        return this;
    }

    /**
     * @param name String containing attribute name
     * @return the value of an attribute, null if not found
     */
    public Object getValue(String name) {
        return values.get(name);
    }

    /**
     * Returns a value as an Integer. The value is simply cast to an Integer.
     * @param name String containing attribute name
     * @return the value of an attribute, null if not found
     */
    public Integer getValueAsInteger(String name) {
        return (Integer)values.get(name);
    }

    /**
     * @return true if no attributes have been set.
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    /**
     * @param name String containing attribute name
     * @return true if given attribute is set
     */
    public boolean isSet(String name) {
        return values.containsKey(name);
    }

    /** @return an Iterator on all names that are set */
    public Iterator nameIterator() {
        return values.keySet().iterator();
    }

    /**
     * Add integer value <code>addValue</code> to attribute with name <code>name</code>.
     * If there is no such setted attribute, then value of this attribure is equal to
     * <code>addValue</code>.
     * @param addValue the increment of value
     * @param name the name of attribute
     */
    public void addIntegerValue(int addValue, String name) {
        Integer value = (Integer) getValue(name);
        int v = (value != null) ? value.intValue() : 0;
        set(name, v + addValue);
    }
}
