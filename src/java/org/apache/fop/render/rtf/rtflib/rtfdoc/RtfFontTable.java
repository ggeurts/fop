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
import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>RTF font table.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Andreas Putz (a.putz@skynamics.com).</p>
 */

public final class RtfFontTable extends RtfElement {
    /** Index table for the fonts */
    private final Hashtable fontIndex = new Hashtable();
    /** Used fonts to this vector */
    private final Vector fontTable = new Vector();

    
    /** Create a RTF header */
    RtfFontTable(RtfHeader h) {
        super(h);
    }

    /** write our contents to m_writer.
     * @param w the value of w */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        if (fontTable.isEmpty()) {
            return;
        }

        w.newLine();
        w.writeGroupMark(true);
        w.writeControlWord("fonttbl");

        int len = fontTable.size();
        for (int i = 0; i < len; i++) {
            w.writeGroupMark(true);
            w.newLine();
            w.writeControlWord("f", i);
            w.write((String)fontTable.elementAt(i));
            w.write(';');
            w.writeGroupMark(false);
        }

        w.newLine();
        w.writeGroupMark(false);
    }

    /**
     * Gets the number of font in the font table
     *
     * @param family Font family name ('Helvetica')
     *
     * @return The number of the font in the table
     */
    public int getFontNumber(String family) {

        Object o = fontIndex.get(getFontKey(family));
        if (o == null) {
            addFont(family);
            return fontTable.size() - 1;
        } else {
            return (Integer)o;
        }
    }

    /** true if this element would generate no "useful" RTF content */
    public boolean isEmpty() {
        return false;
    }
    
    private String getFontKey(String family) {
        return family.toLowerCase();
    }

    /**
     * Adds a font to the table.
     *
     * @param family Identifier of font
     */
    private void addFont(String family) {
        fontIndex.put(getFontKey(family), new Integer(fontTable.size()));
        fontTable.addElement(family);
    }
}
