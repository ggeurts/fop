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

// Java
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Class which contains a linear text run. It has methods to add attributes,
 * text, paragraph breaks....</p>
 *
 * <p>This work was authored by Peter Herweg (pherweg@web.de).</p>
 */
public class RtfTextrun extends RtfContainer {

    /** Constant for no page break */
    public static final int BREAK_NONE = 0;
    /** Constant for a normal page break */
    public static final int BREAK_PAGE = 1;
    /** Constant for a column break */
    public static final int BREAK_COLUMN = 2;
    /** Constant for a even page break */
    public static final int BREAK_EVEN_PAGE = 3;
    /** Constant for a odd page break */
    public static final int BREAK_ODD_PAGE = 4;

    private boolean bSuppressLastPar;
    private RtfListItem rtfListItem;

    /**
     * logging instance
     */
    protected static final Log log = LogFactory.getLog(RtfTextrun.class);

    /** Manager for handling space-* property. */
    private RtfSpaceManager rtfSpaceManager = new RtfSpaceManager();

    /**  Class which represents the opening of a RTF group mark.*/
    private class RtfOpenGroupMark extends RtfElement {

        RtfOpenGroupMark(RtfContainer parent, RtfAttributes attr)
                throws IOException {
            super(parent, attr);
        }

        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return false;
        }

        /** {@inheritDoc} */
        protected void writeRtfContent(RtfWriter w) throws IOException {
            w.writeGroupMark(true);
            w.writeAttributes(getRtfAttributes(), null);
        }
    }

    /**  Class which represents the closing of a RTF group mark.*/
    private class RtfCloseGroupMark extends RtfElement {
        private int breakType = BREAK_NONE;

        RtfCloseGroupMark(RtfContainer parent, int breakType)
                  throws IOException {
            super(parent);
            this.breakType = breakType;
        }

        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return false;
        }

        /**
         * Returns the break type.
         * @return the break type (BREAK_* constants)
         */
        public int getBreakType() {
            return breakType;
        }

        /** {@inheritDoc} */
        protected void writeRtfContent(RtfWriter w) throws IOException {
            w.writeGroupMark(false);

            //Unknown behavior when a table starts a new section,
            //Word may crash
            if (breakType != BREAK_NONE) {
                boolean bHasTableCellParent = this.getParentOfClass(RtfTableCell.class) != null;
                if (!bHasTableCellParent) {
                    w.writeControlWord("sect");
                    /* The following modifiers don't seem to appear in the right place */
                    switch (breakType) {
                    case BREAK_EVEN_PAGE:
                        w.writeControlWord("sbkeven");
                        break;
                    case BREAK_ODD_PAGE:
                        w.writeControlWord("sbkodd");
                        break;
                    case BREAK_COLUMN:
                        w.writeControlWord("sbkcol");
                        break;
                    default:
                        w.writeControlWord("sbkpage");
                    }
                } else {
                    log.warn("Cannot create break-after for a paragraph inside a table.");
                }
            }
        }
    }

    /** Create an RTF container as a child of given container */
    RtfTextrun(RtfContainer parent, RtfAttributes attrs) throws IOException {
        super(parent, attrs);
    }


    /**
     * Adds instance of <code>OpenGroupMark</code> as a child with attributes.
     *
     * @param attrs  attributes to add
     * @throws IOException for I/O problems
     */
    private void addOpenGroupMark(RtfAttributes attrs) throws IOException {
        RtfOpenGroupMark r = new RtfOpenGroupMark(this, attrs);
    }

    /**
     * Adds instance of <code>CloseGroupMark</code> as a child.
     *
     * @throws IOException for I/O problems
     */
    private void addCloseGroupMark(int breakType) throws IOException {
        if (breakType == BREAK_NONE)
        {
            List children = getChildren();
            if (children.isEmpty()) return;
            if (children.get(children.size() - 1) instanceof RtfOpenGroupMark)
            {
                // Remove empty group
                children.remove(children.size() - 1);
                return;
            }
        }
        
        RtfCloseGroupMark r = new RtfCloseGroupMark(this, breakType);
    }

    /**
     * Pushes block attributes, notifies all opened blocks about pushing block
     * attributes, adds <code>OpenGroupMark</code> as a child.
     *
     * @param attrs  the block attributes to push
     * @throws IOException for I/O problems
     */
    public void pushBlockAttributes(RtfAttributes attrs) throws IOException {
        rtfSpaceManager.stopUpdatingSpaceBefore();
        RtfSpaceSplitter splitter = rtfSpaceManager.pushRtfSpaceSplitter(attrs);
        addOpenGroupMark(splitter.getCommonAttributes());
    }

    /**
     * Pops block attributes, notifies all opened blocks about pushing block
     * attributes, adds <code>CloseGroupMark</code> as a child.
     * @param breakType the break type
     * @throws IOException for I/O problems
     */
    public void popBlockAttributes(int breakType) throws IOException {
      rtfSpaceManager.popRtfSpaceSplitter();
      rtfSpaceManager.stopUpdatingSpaceBefore();
      addCloseGroupMark(breakType);
  }

    /**
     * Pushes inline attributes.
     *
     * @param attrs  the inline attributes to push
     * @throws IOException for I/O problems
     */
    public void pushInlineAttributes(RtfAttributes attrs) throws IOException {
        
        rtfSpaceManager.pushInlineAttributes(attrs);
        addOpenGroupMark(attrs);
    }

    /**
     * Inserts a page number citation.
     * @param refId the identifier being referenced
     * @throws IOException for I/O problems
     */
    public void addPageNumberCitation(String refId) throws IOException {
        RtfPageNumberCitation r = new RtfPageNumberCitation(this, refId);
    }

    /**
     * Pop inline attributes.
     *
     * @throws IOException for I/O problems
     */
    public void popInlineAttributes() throws IOException {
        rtfSpaceManager.popInlineAttributes();
        addCloseGroupMark(BREAK_NONE);
    }

    /**
     * Add string to children list.
     *
     * @param s  string to add
     * @throws IOException for I/O problems
     */
    public void addString(String s) throws IOException {
        if (s.equals("")) {
            return;
        }
        RtfAttributes attrs = rtfSpaceManager.getLastInlineAttribute();
        //add RtfSpaceSplitter to inherit accumulated space
        rtfSpaceManager.pushRtfSpaceSplitter(attrs);
        rtfSpaceManager.setCandidate(attrs);
        // create a string and add it as a child
        new RtfString(this, s);
        rtfSpaceManager.popRtfSpaceSplitter();
    }

    /**
     * Inserts a footnote.
     *
     * @return inserted footnote
     * @throws IOException for I/O problems
     */
    public RtfFootnote addFootnote() throws IOException {
        return new RtfFootnote(this);
    }

    /**
     * Inserts paragraph break before all close group marks.
     *
     * @throws IOException  for I/O problems
     * @return The paragraph break element
     */
    public RtfParagraphBreak addParagraphBreak() throws IOException {
        List children = getChildren();
        
        // Don't add paragraph break at start of textrun
        if (children.size() == 0) return null;
        
        ListIterator lit = children.listIterator(children.size());
        int markCount = 0;
        while (lit.hasPrevious()) {
            Object child = lit.previous();
            if (!(child instanceof RtfCloseGroupMark)) {
                if (child instanceof RtfParagraphBreak) {
                    // Don't add two consecutive paragraph breaks
                    return (RtfParagraphBreak) child;
                }
                break;
            }
            markCount++;
        }
        
        // Add new paragraph break before the close marks
        lit.next();
        RtfCloseGroupMark[] marks = new RtfCloseGroupMark[markCount];
        for (int i = 0; i < markCount; i++)
        {
            marks[i] = (RtfCloseGroupMark)lit.next();
            lit.remove();
        }
        RtfParagraphBreak result = new RtfParagraphBreak(this);
        for (int i = 0; i < markCount; i++)
        {
            addChild(marks[i]);
        }
        
        return result;
    }

    /**
     * Inserts a leader.
     * @param attrs Attributes for the leader
     * @throws IOException for I/O problems
     */
    public void addLeader(RtfAttributes attrs) throws IOException {
        new RtfLeader(this, attrs);
    }

    /**
     * Inserts a page number.
     * @param attr Attributes for the page number to insert.
     * @throws IOException for I/O problems
     */
    public void addPageNumber(RtfAttributes attr) throws IOException {
        RtfPageNumber r = new RtfPageNumber(this, attr);
    }

    /**
     * Inserts a hyperlink.
     * @param attr Attributes for the hyperlink to insert.
     * @return inserted hyperlink
     * @throws IOException for I/O problems
     */
    public RtfHyperLink addHyperlink(RtfAttributes attr) throws IOException {
        return new RtfHyperLink(this, attr);
    }

    /**
     * Inserts a bookmark.
     * @param id Id for the inserted bookmark
     * @throws IOException for I/O problems
     */
    public void addBookmark(String id) throws IOException {
       if (id.length() > 0) {
            // if id is not empty, add bookmark
           new RtfBookmark(this, id);
       }
    }

    /**
     * Inserts an image.
     * @return inserted image
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic newImage() throws IOException {
        return new RtfExternalGraphic(this);
    }

    /**
     * Adds a new RtfTextrun to the given container if necessary, and returns it.
     * @param container RtfContainer, which is the parent of the returned RtfTextrun
     * @param attrs RtfAttributes which are to write at the beginning of the RtfTextrun
     * @throws IOException for I/O problems
     * @return the org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun
     */
    public static RtfTextrun getTextrun(RtfContainer container, RtfAttributes attrs)
    throws IOException {
        //if the last child is a RtfTextrun, return it
        List list = container.getChildren();
        if (!list.isEmpty()) {
            Object obj = list.get(list.size() - 1);
            if (obj instanceof RtfTextrun) {
                return (RtfTextrun) obj;
            }
        }

        //add a new RtfTextrun
        return new RtfTextrun(container, attrs);
    }

    /**
     * specify, if the last paragraph control word (\par) should be suppressed.
     * @param bSuppress true, if the last \par should be suppressed
     */
    public void setSuppressLastPar(boolean bSuppress) {
        bSuppressLastPar = bSuppress;
    }

    /**
     * write RTF code of all our children
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        tryRemoveLastParagraphBreak();
        if (getChildCount() == 0) return;
        
        int groupLevel = 0;
        
        boolean bHasTableCellParent
            = this.getParentOfClass(RtfTableCell.class) != null;
        RtfAttributes attrBlockLevel = new RtfAttributes();

        //may contain for example \intbl
        w.writeAttributes(attrib, null);

        if (rtfListItem != null) {
            rtfListItem.getRtfListStyle().writeParagraphPrefix(w);
        }

        //write all children
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            final boolean bRtfParagraphBreak = (e instanceof RtfParagraphBreak);

            if (bHasTableCellParent) {
                attrBlockLevel.set(e.getRtfAttributes());
            }
            
            if (e instanceof RtfOpenGroupMark) {
                if (groupLevel++ == 0) {
                    w.newLine();
                }
            } else if (e instanceof RtfCloseGroupMark) {
                groupLevel--;
            }

            e.writeRtf(w);

            if (rtfListItem != null && bRtfParagraphBreak) {
                // May cause bug where list item symbol is written multiple times...
                rtfListItem.getRtfListStyle().writeParagraphPrefix(w);
            }
        } //for (Iterator it = ...)

        //
        if (bHasTableCellParent) {
            w.writeAttributes(attrBlockLevel, null);
        }
    }

    /**
     * Set the parent list-item of the textrun.
     *
     * @param listItem parent list-item of the textrun
     */
    public void setRtfListItem(RtfListItem listItem) {
        rtfListItem = listItem;
    }

    /**
     * Gets the parent list-item of the textrun.
     *
     * @return parent list-item of the textrun
     */
    public RtfListItem getRtfListItem() {
        return rtfListItem;
    }
    
    private void tryRemoveLastParagraphBreak()
    {
        if (!isLastSibling() || !bSuppressLastPar) return;

        // Iterate children in reverse order
        List children = getChildren();
        ListIterator lit = children.listIterator(children.size());
        
        while (lit.hasPrevious()) {
            RtfElement element = (RtfElement)lit.previous();
            if (element instanceof RtfParagraphBreak
                    && ((RtfParagraphBreak)element).canHide()) {
                lit.remove();
            }
            
            if (!isInvisibleElement(element)) break;
        }
    }

    /**
     * Indicates whether this text run is the last child of its parent.
     * @return Indication whether this text run is the last child of its parent.
     */
    private boolean isLastSibling()
    {
        //determine, if this RtfTextrun is the last child of its parent
        List siblings = parent.getChildren();
        return !siblings.isEmpty() && siblings.get(siblings.size() - 1) == this;
    }
    
    private static boolean isInvisibleElement(RtfElement element)
    {
        return element instanceof RtfOpenGroupMark
                || element instanceof RtfCloseGroupMark
                || element instanceof RtfBookmark
                || element.isEmpty();
    }
}

