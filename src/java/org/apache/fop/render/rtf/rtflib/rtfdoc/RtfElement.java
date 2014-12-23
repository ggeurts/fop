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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * <p>Base class for all elements of an RTF file.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Andreas Putz (a.putz@skynamics.com).</p>
 */
public abstract class RtfElement {
    /** parent element */
    protected final RtfContainer parent;
    /** attributes of the element */
    protected final RtfAttributes attrib;
    private boolean closed;
    private final int id;
    private static int idCounter;

    /** Create an RTF element as a child of given container
     * @param parent the value of parent */
    RtfElement(RtfContainer parent) throws IOException {
        this(parent, null);
    }

    /** Create an RTF element as a child of given container with given attributes
     * @param parent the value of parent
     * @param attr the RTF attributes 
     */
    RtfElement(RtfContainer parent, RtfAttributes attr) throws IOException {

        id = idCounter++;
        this.parent = parent;
        attrib = (attr != null ? attr : new RtfAttributes());
        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }

    /**
     * Does nothing, meant to allow elements to write themselves without waiting
     * for write(), but not implemented yet
     * @throws IOException for I/O problems
     */
    public final void close() throws IOException {
        closed = true;
    }

    /**
     * Write the RTF code of this element to our Writer
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    public final void writeRtf(RtfWriter w) throws IOException {
        if (w != null && !isEmpty()) {
            writeRtfPrefix(w);
            writeRtfContent(w);
            writeRtfSuffix(w);
        }
    }

    /**
     * Called before writeRtfContent()
     * @param w the {@link RtfWriter} to write to
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
    }

    /**
     * Must be implemented to write RTF content to m_writer
     * @param w the {@link RtfWriter} to write to
     * @throws IOException for I/O problems
     */
    protected abstract void writeRtfContent(RtfWriter w) throws IOException;

    /**
     * Called after writeRtfContent()
     * @param w the {@link RtfWriter} to write to
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
    }

    /** debugging to given PrintWriter */
    void dump(Writer w, int indent)
    throws IOException {
        for (int i = 0; i < indent; i++) {
            w.write(' ');
        }
        w.write(this.toString());
        w.write('\n');
        w.flush();
    }

    /**
     * minimal debugging display
     * @return String representation of object
     */
    public String toString() {
        return (this == null) ? "null" : (this.getClass().getName() + " #" + id);
    }

    /** true if close() has been called */
    boolean isClosed() {
        return closed;
    }

    /** access our RtfFile, which is always the topmost parent */
    RtfFile getRtfFile() {
        // go up the chain of parents until we find the topmost one
        RtfElement result = this;
        while (result.parent != null) {
            result = result.parent;
        }

        // topmost parent must be an RtfFile
        // a ClassCastException here would mean that the parent-child structure is not as expected
        return (RtfFile)result;
    }

    /** find the first parent where c.isAssignableFrom(parent.getClass()) is true
     *  @return null if not found
     */
    public RtfElement getParentOfClass(Class c) {
        RtfElement result = null;
        RtfElement current = this;
        while (current.parent != null) {
            current = current.parent;
            if (c.isAssignableFrom(current.getClass())) {
                result = current;
                break;
            }
        }
        return result;
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public abstract boolean isEmpty();

    /**
     * Gets the RTF attributes for this element.
     * @return The {@link RtfAttributes}.
     */
    public RtfAttributes getRtfAttributes() {
        return attrib;
    }
}
