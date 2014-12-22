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
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public final class RtfFontManager {
    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Singleton instance */
    private static RtfFontManager instance = new RtfFontManager();

    /** Index table for the fonts */
    private Hashtable fontIndex;
    /** Used fonts to this vector */
    private Vector fontTable;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private RtfFontManager() {
        fontTable = new Vector();
        fontIndex = new Hashtable();

        init();
    }

    /**
     * Singleton.
     *
     * @return The instance of RtfFontManager
     */
    public static RtfFontManager getInstance() {
        return instance;
    }


    //////////////////////////////////////////////////
    // @@ Initializing
    //////////////////////////////////////////////////

    /**
     * Initialize the font table.
     */
    private void init() {

//        getFontNumber ("Helvetica");
        //Chanded by R.Marra default font Arial
        getFontNumber("Arial");
        getFontNumber("Symbol"); // used by RtfListItem.java
        getFontNumber("Times New Roman");

/*
        {\\f0\\fswiss Helv;}

        // f1 is used by RtfList and RtfListItem for bullets

        {\\f1\\froman\\fcharset2 Symbol;}
        {\\f2\\froman\\fprq2 Times New Roman;}
        {\\f3\\froman Times New Roman;}
*/
    }


    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////


    /**
     * Gets the number of font in the font table
     *
     * @param family Font family name ('Helvetica')
     *
     * @return The number of the font in the table
     */
    public int getFontNumber(String family) {

        Object o = fontIndex.get(getFontKey(family));
        int retVal;

        if (o == null) {
            addFont(family);

            retVal = fontTable.size() - 1;
        } else {
            retVal = ((Integer)o).intValue();
        }

        return retVal;
    }

    /**
     * Writes the font table in the header.
     *
     * @param The {@link RtfWriter} to write to
     *
     * @throws IOException On error
     */
    public void writeFonts(RtfWriter w) throws IOException {
        if (fontTable == null || fontTable.size() == 0) {
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
            w.write(";");
            w.writeGroupMark(false);
        }

        w.newLine();
        w.writeGroupMark(false);
    }


    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

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
