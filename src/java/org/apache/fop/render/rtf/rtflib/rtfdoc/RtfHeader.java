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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>RTF file header, contains style, font and other document-level information.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch),
 * Andreas Putz (a.putz@skynamics.com), Marc Wilhelm Kuester and Gerke Geurts.</p>
 */

public class RtfHeader extends RtfContainer {
    private static final String CHARSET = "ansi";
    
    private final Map userProperties = new HashMap();
    private final RtfColorTable colorTable;
    private final RtfFontTable fontTable;
    private final RtfListTable listTable;

    /** Create an RTF header
     * @param f the parent RTF file
     */
    RtfHeader(RtfFile f) throws IOException {
        super(f);
        colorTable = new RtfColorTable(this);
        fontTable = new RtfFontTable(this);
        new RtfGenerator(this);
        listTable = new RtfListTable(this);
    }

    /** Gets {@link RtfColorTable} for RTF file. */
    public RtfColorTable getColorTable()
    {
        return colorTable;
    }

    /** Gets {@link RtfFontTable} for RTF file. */
    public RtfFontTable getFontTable()
    {
        return fontTable;
    }
    
    /** Gets {@link RtfListTable} for RTF file. */
    public RtfListTable getListTable()
    {
        return listTable;
    }
    
    /** {@inheritDoc} 
     * Overridden to write our own data before our children's data.
     */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        w.writeControlWord(CHARSET);
        writeUserProperties(w);
        super.writeRtfContent(w);
        RtfTemplate.getInstance().writeTemplate(w);
        RtfStyleSheetTable.getInstance().writeStyleSheet(w);
        writeFootnoteProperties(w);
    }

    /** write user properties if any */
    private void writeUserProperties(RtfWriter w) throws IOException {
        if (userProperties.size() > 0) {
            w.writeGroupMark(true);
            w.writeStarControlWord("userprops");
            for (Iterator it = userProperties.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry)it.next();
                w.writeGroupMark(true);
                w.writeControlWord("propname");
                w.write(entry.getKey().toString());
                w.writeGroupMark(false);
                w.writeControlWord("proptype30");
                w.writeGroupMark(true);
                w.writeControlWord("staticval");
                w.write(entry.getValue().toString());
                w.writeGroupMark(false);
            }
            w.writeGroupMark(false);
        }
    }

    /**
     *write properties for footnote handling
     */
    private void writeFootnoteProperties(RtfWriter w) throws IOException {
        w.newLine();
        w.writeControlWord("fet0");  //footnotes, not endnotes
        w.writeControlWord("ftnbj"); //place footnotes at the end of the
                                     //page (should be the default, but
                                     //Word 2000 thinks otherwise)
    }
}
