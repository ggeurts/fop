/*
 * $Id: StringProperty.java,v 1.4 2003/03/05 21:48:01 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

/**
 * Exists primarily as a container for its Maker inner class, which is
 * extended by many string-based FO property classes.
 */
public class StringProperty extends Property {

    /**
     * Inner class for making instances of StringProperty
     */
    public static class Maker extends Property.Maker {

        /**
         * @param propName name of property for which to create a Maker
         */
        public Maker(String propName) {
            super(propName);
        }

        /**
         * Make a new StringProperty object
         * @param propertyList not used
         * @param value String value of the new object
         * @param fo not used
         * @return the StringProperty object
         */
        public Property make(PropertyList propertyList, String value,
                             FObj fo) {
            // Work around the fact that most String properties are not
            // specified as actual String literals (with "" or '') since
            // the attribute values themselves are Strings!
            // If the value starts with ' or ", make sure it also ends with
            // this character
            // Otherwise, just take the whole value as the String
            int vlen = value.length() - 1;
            if (vlen > 0) {
                char q1 = value.charAt(0);
                if (q1 == '"' || q1 == '\'') {
                    if (value.charAt(vlen) == q1) {
                        return new StringProperty(value.substring(1, vlen));
                    }
                    System.err.println("Warning String-valued property starts with quote"
                                       + " but doesn't end with quote: "
                                       + value);
                    // fall through and use the entire value, including first quote
                }
                String str = checkValueKeywords(value);
                if (str != null) {
                    value = str;
                }
            }
            return new StringProperty(value);
        }

    }    // end String.Maker

    private String str;

    /**
     * @param str String value to place in this object
     */
    public StringProperty(String str) {
        this.str = str;
        // System.err.println("Set StringProperty: " + str);
    }

    /**
     * @return the Object equivalent of this property
     */
    public Object getObject() {
        return this.str;
    }

    /**
     * @return the String equivalent of this property
     */
    public String getString() {
        return this.str;
    }

}
