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
import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

/**
 * <p>Model of an RTF list item, which can contain RTF paragraphs.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Andreas Putz (a.putz@skynamics.com).</p>
 */
public class RtfListItem extends RtfContainer
        implements IRtfTextrunContainer,
                   IRtfListContainer,
                   IRtfParagraphContainer {

    private RtfList parentList;
    private RtfListStyle listStyle;
    private int number;

    /**
     * special RtfParagraph that writes list item setup code before its content
     */
    private class RtfListItemParagraph extends RtfParagraph {

        RtfListItemParagraph(RtfListItem rli, RtfAttributes attrs) {
            super(rli, attrs);
        }

        protected void writeRtfPrefix(RtfWriter w) throws IOException {
            super.writeRtfPrefix(w);
            getRtfListStyle().writeParagraphPrefix(w);
        }
    }

    /**
     * special RtfTextrun that is used as list item label
     */
    public class RtfListItemLabel extends RtfTextrun implements IRtfTextrunContainer {

        private RtfListItem rtfListItem;

        /**
         * Constructs the RtfListItemLabel
         * @param item The RtfListItem the label belongs to
         */
        public RtfListItemLabel(RtfListItem item) {
            super(null, null);

            rtfListItem = item;
        }

        /**
         * Returns the current RtfTextrun object.
         * Opens a new one if necessary.
         * @return The RtfTextrun object
         */
        public RtfTextrun getTextrun() {
            return this;
        }

        /**
         * Sets the content of the list item label.
         * @param s Content of the list item label.
         */
        public void addString(String s) {

            final String label = s.trim();
            if (label.length() > 0 && Character.isDigit(label.charAt(0))) {
                rtfListItem.setRtfListStyle(new RtfListStyleNumber());
            } else {
                rtfListItem.setRtfListStyle(new RtfListStyleText(label));
            }
        }
    }

    /** Create an RTF list item as a child of given container with default attributes */
    RtfListItem(RtfList parent) {
        super((RtfContainer)parent);
        parentList = parent;
    }

    /**
     * Close current paragraph if any and start a new one
     * @param attrs attributes of new paragraph
     * @return new RtfParagraph
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) {
        return new RtfListItemParagraph(this, attrs);
    }

    /**
     * Close current paragraph if any and start a new one with default attributes
     * @return new RtfParagraph
     */
    public RtfParagraph newParagraph() {
        return newParagraph(null);
    }

    /** Create an RTF list item as a child of given container with given attributes */
    RtfListItem(RtfList parent, RtfAttributes attr) {
        super((RtfContainer)parent, attr);
        parentList = parent;
    }


    /**
     * Get the current textrun.
     * @return current RtfTextrun object
     */
    public RtfTextrun getTextrun() {
        RtfTextrun textrun = RtfTextrun.getTextrun(this, null);
        textrun.setRtfListItem(this);
        return textrun;
    }

    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrs attributes of new RftList object
     * @return new RtfList
     */
    public RtfList newList(RtfAttributes attrs) {
        return new RtfList(this, attrs);
    }

    /** {@inheritDoc} 
     * Overridden to setup the list: start a group with appropriate attributes.
     */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {

        // pard causes word97 (and sometimes 2000 too) to crash if the list is nested in a table
        if (!parentList.getHasTableParent()) {
            w.writeControlWord("pard");
        }

        w.writeOneAttribute(RtfText.LEFT_INDENT_FIRST,
                "360"); //attrib.getValue(RtfListTable.LIST_INDENT));

        w.writeOneAttribute(RtfText.LEFT_INDENT_BODY,
                attrib.getValue(RtfText.LEFT_INDENT_BODY));

        // group for list setup info
        w.writeGroupMark(true);

        w.writeStarControlWord("pn");
        //Modified by Chris Scott
        //fixes second line indentation
        getRtfListStyle().writeListPrefix(w, this);

        w.writeGroupMark(false);
        w.writeOneAttribute(RtfListTable.LIST_NUMBER, new Integer(number));
    }

    /** {@inheritDoc}
     * End the list group
     */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        super.writeRtfSuffix(w);

        /* reset paragraph defaults to make sure list ends
         * but pard causes word97 (and sometimes 2000 too) to crash if the list
         * is nested in a table
         */
        if (!parentList.getHasTableParent()) {
            w.writeControlWord("pard");
        }
    }

    /**
     * Change list style
     * @param ls ListStyle to set
     */
    public void setRtfListStyle(RtfListStyle ls) {
        listStyle = ls;

        listStyle.setRtfListItem(this);
        number = getRtfFile().getListTable().addRtfListStyle(ls);
    }

    /**
     * Get list style
     * @return ListSytle of the List
     */
    public RtfListStyle getRtfListStyle() {
        if (listStyle == null) {
            return parentList.getRtfListStyle();
        } else {
            return listStyle;
        }
    }

    /**
     * Get the parent list.
     * @return the parent list
     */
    public RtfList getParentList() {
        return parentList;
    }

    /**
     * Returns the list number
     * @return list number
     */
    public int getNumber() {
        return number;
    }
    
    /** 
     * {@inheritDoc}
     * Closes any previous child.
     */
    protected void addChild(RtfElement e) {
        RtfElement previousChild = getLastChild();
        if (previousChild != null) {
            previousChild.close();
        }
        
        super.addChild(e);
    }
}
