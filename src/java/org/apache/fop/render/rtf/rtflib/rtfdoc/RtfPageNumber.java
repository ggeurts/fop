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

/**
 * <p>Page number container.</p>
 *
 * <p>This work was authored by Christopher Scott (scottc@westinghouse.com).</p>
 */
public class RtfPageNumber extends RtfContainer {
    /* RtfText attributes: fields
       must be carefull of group markings and star control
       ie page field:
           "{\field {\*\fldinst {PAGE}} {\fldrslt}}"
    */

    /** constant for field */
    public static final String RTF_FIELD = "field";
    /** constant for field on page */
    public static final String RTF_FIELD_PAGE = "fldinst { PAGE }";
    /** constant for field result */
    public static final String RTF_FIELD_RESULT = "fldrslt";

    /** Create an RTF paragraph as a child of given container with default attributes */
    RtfPageNumber(IRtfPageNumberContainer parent) {
        super((RtfContainer)parent);
    }

    /** Create an RTF page number as a child of given container with given attributes */
     RtfPageNumber(RtfContainer parent, RtfAttributes attrs) {
         // Adds the attributes of the parent paragraph
         super(parent, attrs);
     }

    /** Create an RTF page number as a child of given paragraph,
     *  copying the paragraph attributes
     */
     RtfPageNumber(RtfParagraph parent) {
         // Adds the attributes of the parent paragraph
         super((RtfContainer)parent, parent.attrib);

         // copy parent's text attributes
         RtfAttributes textAttr = parent.getTextAttributes();
         if (textAttr != null) {
             attrib.set(textAttr);
         }
     }

    /** {@inheritDoc} */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        w.writeGroupMark(true);
        w.writeAttributes(attrib, RtfText.ATTR_NAMES);
        w.writeControlWord("chpgn");
        w.writeGroupMark(false);
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return false;
    }
}
