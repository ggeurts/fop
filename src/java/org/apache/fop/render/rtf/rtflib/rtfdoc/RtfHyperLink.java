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

import org.apache.fop.apps.FOPException;

/**
 * <p>Creates an hyperlink.
 * This class belongs to the <fo:basic-link> tag processing.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public class RtfHyperLink
extends RtfContainer
implements IRtfTextContainer,
           IRtfTextrunContainer {

    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** The url of the image */
    protected String url;

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////


    /**
     * A constructor.
     *
     * @param parent a <code>RtfContainer</code> value
     * @param str text of the link
     * @param attr a <code>RtfAttributes</code> value
     */
    public RtfHyperLink(IRtfTextContainer parent, String str, RtfAttributes attr) {
        super((RtfContainer) parent, attr);
        new RtfText(this, str, attr);
    }

    /**
     * A constructor.
     *
     * @param parent a <code>RtfContainer</code> value
     * @param attr a <code>RtfAttributes</code> value
     */
    public RtfHyperLink(RtfTextrun parent, RtfAttributes attr) {
        super(parent, attr);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

    /** {@inheritDoc} */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
        w.writeGroupMark(true);
        w.writeControlWord("field");

        w.writeGroupMark(true);
        w.writeStarControlWord("fldinst");

        w.writeRaw("HYPERLINK \"");
        w.write(url);
        w.writeRaw("\" ");
        w.writeGroupMark(false);

        w.writeGroupMark(true);
        w.writeControlWord("fldrslt");

        // start a group for this paragraph and write our own attributes if needed
        if (attrib != null && attrib.isSet("cs")) {
            w.writeGroupMark(true);
            w.writeAttributes(attrib, new String [] {"cs"});
        }
    }

    /** {@inheritDoc} */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        if (attrib != null && attrib.isSet("cs")) {
            w.writeGroupMark(false);
        }
        w.writeGroupMark(false);
        w.writeGroupMark(false);
    }


    //////////////////////////////////////////////////
    // @@ IRtfContainer implementation
    //////////////////////////////////////////////////

    /**
     * close current text run if any and start a new one with default attributes
     * @param str if not null, added to the RtfText created
     * @return new RtfText object
     */
    public RtfText newText(String str) {
        return newText(str, null);
    }

    /**
     * close current text run if any and start a new one
     * @param str if not null, added to the RtfText created
     * @param attr attributes of text to add
     * @return the new RtfText object
     */
    public RtfText newText(String str, RtfAttributes attr) {
        return new RtfText(this, str, attr);
    }

    /**
     * IRtfTextContainer requirement:
     * @return a copy of our attributes
     * @throws FOPException if attributes cannot be cloned
     */
    public RtfAttributes getTextContainerAttributes() throws FOPException {
        if (attrib == null) {
            return null;
        }
        try {
            return (RtfAttributes) this.attrib.clone();
        } catch (CloneNotSupportedException e) {
            throw new FOPException(e);
        }
    }


    /**
     * add a line break
     * @throws IOException for I/O problems
     */
    public void newLineBreak() {
        new RtfLineBreak(this);
    }

    //////////////////////////////////////////////////
    // @@ Member access
    //////////////////////////////////////////////////

    /**
     * Sets the url of the external link.
     *
     * @param url Link url like "http://..."
     */
    public void setExternalURL(String url) {
        this.url = url;
    }

    /**
     * Sets the url of the external link.
     *
     * @param jumpTo Name of the text mark
     */
    public void setInternalURL(String jumpTo) {
        int now = jumpTo.length();
        int max = RtfBookmark.MAX_BOOKMARK_LENGTH;
        this.url = "#" + jumpTo.substring(0, now > max ? max : now);
        this.url = this.url.replace('.', RtfBookmark.REPLACE_CHARACTER);
        this.url = this.url.replace(' ', RtfBookmark.REPLACE_CHARACTER);
    }

    /**
     *
     * @return false (always)
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return a text run
     * @throws IOException if not caught
     */
    public RtfTextrun getTextrun() {
        return RtfTextrun.getTextrun(this, null);
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
