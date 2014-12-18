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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class is responsible for saving space-before/space-after attributes
 * history and adding spacing to established candidates (i.e. attributes) or
 * accumulation spacing in case of candidate absence.
 */
public class RtfSpaceManager {
    /** Stack for saving rtf block-level attributes. */
    private LinkedList blockAttributes = new LinkedList();

    /** Stack for saving rtf inline-level attributes. */
    private LinkedList inlineAttributes = new LinkedList();

    /**
     * Keeps value of accumulated space in twips. For example if block has
     * nonzero space-before or space-after properties and has no plain text
     * inside, then the next block should has increased value of space-before
     * property.
     */
    private int accumulatedSpace;

    /**
     * Construct a newly allocated <code>RtfSpaceManager</code> object.
     */
    public RtfSpaceManager() {
    }

    /**
     * Iterates block-level stack (i.e. all open blocks) and stops updating
     * candidate for adding space-before/space-after attribute in case of
     * candidate presence.
     */
    public void stopUpdatingSpaceBefore() {
        for (Iterator it = blockAttributes.iterator(); it.hasNext();) {
            RtfSpaceSplitter splitter = (RtfSpaceSplitter) it.next();
            if (splitter.isBeforeCandidateSet()) {
                splitter.stopUpdatingSpaceBefore();
            }
        }
    }

    /**
     * Set attributes as candidate for space attributes inheritance.
     *
     * @param attrs  attributes to set
     */
    public void setCandidate(RtfAttributes attrs) {
        for (Iterator it = blockAttributes.iterator(); it.hasNext();) {
            RtfSpaceSplitter splitter = (RtfSpaceSplitter) it.next();
            splitter.setSpaceBeforeCandidate(attrs);
            splitter.setSpaceAfterCandidate(attrs);
        }
    }

    /**
     * Builds RtfSpaceSplitter on <code>attrs</code> and adds it to the
     * block-level stack. Any attribute values that duplicate eeffective values
     * of the parent block are removed.
     *
     * @param attrs  RtfAttribute to add
     * @return instance of RtfSpaceSplitter
     */
    public RtfSpaceSplitter pushRtfSpaceSplitter(RtfAttributes attrs) {
        RtfSpaceSplitter splitter;
        splitter = new RtfSpaceSplitter(attrs, accumulatedSpace);
        
        // Prevent nested block from writting duplicate attributes
        removeEffectiveBlockAttributes(splitter.getCommonAttributes());

        // set accumulatedSpace to 0, because now accumulatedSpace used
        // in splitter
        accumulatedSpace = 0;
       
        blockAttributes.addLast(splitter);
        return splitter;
    }

    /**
     * Removes RtfSpaceSplitter from top of block-level stack.
     */
    public void popRtfSpaceSplitter() {
        if (!blockAttributes.isEmpty()) {
            RtfSpaceSplitter splitter;
            splitter = (RtfSpaceSplitter) blockAttributes.removeLast();
            
            accumulatedSpace += splitter.flush();
        }
    }

    /**
     * Pushes inline attributes to inline-level stack. Any attribute values
     * that duplicate effective attribute values of the current block are
     * removed.
     *
     * @param attrs attributes to add
     */
    public void pushInlineAttributes(RtfAttributes attrs) {
        removeEffectiveBlockAttributes(attrs);
        inlineAttributes.addLast(attrs);
    }

    /**
     * Pops inline attributes from inline-level stack.
     */
    public void popInlineAttributes() {
        if (!inlineAttributes.isEmpty()) {
            inlineAttributes.removeLast();
        }
    }

    /**
     * Peeks at inline-level attribute stack.
     *
     * @return RtfAttributes from top of inline-level stack
     */
    public RtfAttributes getLastInlineAttribute() {
        return !inlineAttributes.isEmpty()
                ? (RtfAttributes) inlineAttributes.getLast()
                : null;
    }

    /**
     * Removes attributes with values that would duplicate currently effective 
     * rtf block-level attribute values.
     * 
     * @param attrs The attributes from which duplicates are to be removed.
     */
    private void removeEffectiveBlockAttributes(RtfAttributes attrs) {
        RtfAttributes blockAttrs = getEffectiveBlockAttributes();
        if (attrs == null || blockAttrs == null) return;
        
        for (Iterator nameIt = blockAttrs.nameIterator(); nameIt.hasNext();) {
            String name = (String)nameIt.next();
            Object value = attrs.getValue(name);
            Object commonValue = blockAttrs.getValue(name);
            if (commonValue == null ? value == null : commonValue.equals(value)) {
                attrs.unset(name);
            }
        }
    }
    
    private RtfAttributes getEffectiveBlockAttributes()
    {
        if (blockAttributes.isEmpty()) return null;
        
        RtfSpaceSplitter splitter = ((RtfSpaceSplitter)blockAttributes.getFirst());
        RtfAttributes result = splitter.getCommonAttributes();
        
        if (blockAttributes.size() > 1) {
            try {
                result = (RtfAttributes)result.clone();
                for(ListIterator it = blockAttributes.listIterator(1); it.hasNext();) {
                    splitter = (RtfSpaceSplitter)it.next();
                    result.set(splitter.getCommonAttributes());
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
