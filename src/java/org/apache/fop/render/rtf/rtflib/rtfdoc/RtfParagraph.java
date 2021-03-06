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
import java.util.List;


/**
 * <p>Model of an RTF paragraph, which can contain RTF text elements.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch),
 * Andreas Putz (a.putz@skynamics.com), and
 * Boris Poudérous (boris.pouderous@free.fr).</p>
 */

public class RtfParagraph extends RtfBookmarkContainerImpl
implements IRtfTextContainer, IRtfPageBreakContainer, IRtfHyperLinkContainer,
        IRtfExternalGraphicContainer, IRtfPageNumberContainer,
        IRtfPageNumberCitationContainer {
    private RtfText text;
    private boolean keepn;
    private boolean resetProperties;

    /* needed for importing Rtf into FrameMaker
       FrameMaker is not as forgiving as word in rtf
           thus /pard/par must be written in a page break directly
           after a table.  /pard is probably needed in other places
           also, this is just a hack to make FrameMaker import Jfor rtf
           correctly */
    private boolean writeForBreak;

    /** Create an RTF paragraph as a child of given container with default attributes */
    RtfParagraph(IRtfParagraphContainer parent) {
        super((RtfContainer)parent);
    }

    /** Create an RTF paragraph as a child of given container with given attributes */
    RtfParagraph(IRtfParagraphContainer parent, RtfAttributes attr) {
        super((RtfContainer)parent, attr);
    }

    /**
     * Accessor for the paragraph text
     * @return the paragraph text
     */
    public String getText() {
        return (text.getText());
    }

    /** Set the keepn attribute for this paragraph */
    public void setKeepn() {
        this.keepn = true;
    }

    /** Force reset properties */
    public void setResetProperties() {
        this.resetProperties = true;
    }

    /**
     * Overridden to write our attributes before our content
    /* {@inheritDoc}
     */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {

        //Reset paragraph properties if needed
        if (resetProperties) {
            w.writeControlWord("pard");
        }

        /*
         * Original comment said "do not write text attributes here, they are
         * handled by RtfText." However, the text attributes appear to be
         * relevant to paragraphs as well.
         */
        w.writeAttributes(attrib, RtfText.ATTR_NAMES);

        //Set keepn if needed (Keep paragraph with the next paragraph)
        if (keepn) {
            w.writeControlWord("keepn");
        }

        // start a group for this paragraph and write our own attributes if needed
        if (mustWriteGroupMark()) {
            w.writeGroupMark(true);
        }

        if (mustWriteAttributes()) {
            w.writeAttributes(attrib, RtfText.ALIGNMENT);

            //this line added by Chris Scott, Westinghouse
            w.writeAttributes(attrib, RtfText.BORDER);
            w.writeAttributes(attrib, RtfText.INDENT);
            w.writeAttributes(attrib, RtfText.TABS);
            if (writeForBreak) {
                w.writeControlWord("pard\\par");
            }
        }
    }

    /**
     * Overridden to close paragraph
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        // sometimes the end of paragraph mark must be suppressed in table cells
        boolean writeMark = true;
        if (parent instanceof RtfTableCell) {
            writeMark = ((RtfTableCell)parent).paragraphNeedsPar(this);
        }
        if (writeMark) {
            w.writeControlWord("par");
        }

        if (mustWriteGroupMark()) {
            w.writeGroupMark(false);
        }


    }

    /**
     * Close current text run if any and start a new one with default attributes
     * @param str if not null, added to the RtfText created
     * @return the new RtfText object
     * @throws IOException for I/O problems
     */
    public RtfText newText(String str) {
        return newText(str, null);
    }

    /**
     * Close current text run if any and start a new one
     * @param str if not null, added to the RtfText created
     * @param attr attributes of the text
     * @return the new RtfText object
     * @throws IOException for I/O problems
     */
    public RtfText newText(String str, RtfAttributes attr) {
        return text = new RtfText(this, str, attr);
    }

    /**
     * add a page break
     */
    public void newPageBreak() {
        writeForBreak = true;
        new RtfPageBreak(this);
    }

    /**
     * add a line break
     * @throws IOException for I/O problems
     */
    public void newLineBreak() {
        new RtfLineBreak(this);
    }

    /**
     * Add a page number
     * @return new RtfPageNumber object
     */
    public RtfPageNumber newPageNumber() {
        return new RtfPageNumber(this);
    }

    /**
     * Added by Boris POUDEROUS on 2002/07/09
     * @param id string containing the citation text
     * @return the new RtfPageNumberCitation object
     */
    public RtfPageNumberCitation newPageNumberCitation(String id) {
       return new RtfPageNumberCitation(this, id);
    }

    /**
     * Creates a new hyperlink.
     * @param str string containing the hyperlink text
     * @param attr attributes of new hyperlink
     * @return the new RtfHyperLink object
     */
    public RtfHyperLink newHyperLink(String str, RtfAttributes attr) {
        return new RtfHyperLink(this, str, attr);
    }

    /**
     * Start a new external graphic after closing all other elements
     * @return the new RtfExternalGraphic
     */
    public RtfExternalGraphic newImage() {
        return new RtfExternalGraphic(this);
    }

    /** true if we must write our own (non-text) attributes in the RTF */
    private boolean mustWriteAttributes() {
        boolean writeAttributes = false;
        final int children = getChildCount();
        if (children > 0) {
            final List childList = getChildren();
            for (int i = 0; i < children; i++) {
                final RtfElement el = (RtfElement) childList.get(i);
                if (!el.isEmpty()) {
                    if (el.getClass() == RtfText.class) {
                        boolean tmp = ((RtfText) el).isNbsp();
                        if (!tmp) {
                            writeAttributes = true;
                            break;
                        }
                    } else {
                        writeAttributes = true;
                        break;
                    }
                }
            }
        }
        return writeAttributes;
    }

    /** true if we must write a group mark around this paragraph
     *  TODO is this correct, study interaction with mustWriteAttributes()
     *       <-- On implementation i have noticed if the groupmark set, the
     *       format attributes are only for this content, i think this
     *       implementation is ok
     */
    private boolean mustWriteGroupMark() {
        return getChildCount() > 0;
    }

    /**
     * accessor for text attributes
     * @return attributes of the text
     */
    public RtfAttributes getTextAttributes() {
        if (text == null) {
            return null;
        }
        return text.getTextAttributes();
    }
}
