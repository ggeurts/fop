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
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

/**
 * <p>An RtfElement that can contain other elements.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch).</p>
 */

public class RtfContainer extends RtfElement {
    private final LinkedList children; 

    /** Create an RTF container as a child of given container */
    RtfContainer(RtfContainer parent) {
        this(parent, null);
    }

    /** Create an RTF container as a child of given container with given attributes */
    RtfContainer(RtfContainer parent, RtfAttributes attr) {
        super(parent, attr);
        children = new LinkedList();
    }

    /**
     * add a child element to this
     * @param e child element to add
     */
    protected void addChild(RtfElement e) {
        if (isClosed()) {
            // No childs should be added to a container that has been closed
            final StringBuffer sb = new StringBuffer();
            sb.append("addChild: container already closed (parent=");
            sb.append(this.getClass().getName());
            sb.append(" child=");
            sb.append(e.getClass().getName());
            sb.append(")");
            throw new RuntimeException(sb.toString());
        }

        children.add(e);
    }

    /**
     * @return our children's list
     */
    public List getChildren() {
        return children;
    }

    
    
    /**
     * @return the number of children
     */
    public int getChildCount() {
        return children.size();
    }
    
    /**
     * @return The last child added to this container, or null otherwise.
     */
    protected RtfElement getLastChild()
    {
        return children.isEmpty() ? null : (RtfElement)children.getLast();
    }

    /**
     * Find the passed child in the current container
     * @param aChild the child element
     * @return the depth (nested level) inside the current container
     */
    public int findChildren(RtfElement aChild) {
        for (Iterator it = this.getChildren().iterator(); it.hasNext();) {
          final RtfElement e = (RtfElement)it.next();
          if (aChild == e) {
              return 0;
          } else if (e instanceof RtfContainer) {
              int recursiveResult = ((RtfContainer)e).findChildren(aChild);
              if (recursiveResult >= 0) {
                  return recursiveResult + 1;
              }
          }
        }
        return -1;
    }

    /**
     * write RTF code of all our children
     * @param w the {@link RtwWriter} to write to
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent(RtfWriter w)
    throws IOException {
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.writeRtf(w);
        }
    }

    /** true if this (recursively) contains at least one RtfText object */
    boolean containsText() {
        boolean result = false;
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfText) {
                result = !e.isEmpty();
            } else if (e instanceof RtfContainer) {
                if (((RtfContainer)e).containsText()) {
                    result = true;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }

    /** debugging to given Writer */
    void dump(Writer w, int indent)
    throws IOException {
        super.dump(w, indent);
        for (Iterator it = children.iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            e.dump(w, indent + 1);
        }
    }

    /**
     * minimal debugging display
     * @return String representation of object contents
     */
    public String toString() {
        return super.toString() + " (" + getChildCount() + " children)";
    }

    /** {@inheritDoc} */
    public void close() {
        if (!isClosed()) {
            super.close();
            for (Iterator it = children.iterator(); it.hasNext();) {
                ((RtfElement)it.next()).close();
            }
        }
    }

    /**
     * @return true if this element would generate no "useful" RTF content,
     * i.e. (for RtfContainer) true if it has no children where isEmpty() is false
     */
    public boolean isEmpty() {
        if (!children.isEmpty()) {
            for (Iterator it = children.iterator(); it.hasNext();) {
                final RtfElement e = (RtfElement)it.next();
                if (!e.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
