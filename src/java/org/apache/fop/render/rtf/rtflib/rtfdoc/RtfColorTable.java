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
 * <p>The RTF color table.
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */

public final class RtfColorTable extends RtfElement {
    //////////////////////////////////////////////////
    // @@ Symbolic constants
    //////////////////////////////////////////////////

    // Defines the bit moving for the colors
    private static final int RED = 16;
    private static final int GREEN = 8;
    private static final int BLUE = 0;

    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Map of color names to color numbers */
    private static final Hashtable namedColors = new Hashtable();
    
    /** Index table for the colors */
    private final Hashtable colorIndex = new Hashtable();
    /** Used colors to this vector */
    private final Vector colorTable = new Vector();


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    static {
        addNamedColor("black", 0, 0, 0);
        addNamedColor("white", 255, 255, 255);
        addNamedColor("red", 255, 0, 0);
        addNamedColor("green", 0, 255, 0);
        addNamedColor("blue", 0, 0, 255);
        addNamedColor("cyan", 0, 255, 255);
        addNamedColor("magenta", 255, 0, 255);
        addNamedColor("yellow", 255, 255, 0);
        addNamedColor("gray", 128, 128, 128);
    }
    
    /**
     * Constructor.
     */
    RtfColorTable(RtfHeader header) 
    throws IOException {
        super(header);
    }

    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////

    /** 
     * Define a named color for getColorNumber(String) 
     * 
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     */
    public static void addNamedColor(String name, int red, int green, int blue) {
        namedColors.put(name.toLowerCase(), determineIdentifier(red, green, blue));
    }

    /**
     * @param name a named color
     * @return the RTF number of a named color, or null if name not found
     */
    public static Integer getColorNumber(String name) {
        return ((Integer)namedColors.get(name.toLowerCase()));
    }

    /**
     * Gets the number of color in the color table
     *
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     *
     * @return The number of the color in the table
     */
    public int getColorNumber(int red, int green, int blue) {
        Integer identifier = determineIdentifier(red, green, blue);
        Object o = colorIndex.get(identifier);

        if (o == null) {
            //The color currently does not exist, so add it to the table.
            //First add it, then read the size as index (to return it).
            //So the first added color gets index 1. That is OK, because
            //index 0 is reserved for auto-colored.
            addColor(identifier);
            return colorTable.size();
        } else {
            //The color was found. Before returning the index, increment
            //it by one. Because index 0 is reserved for auto-colored, but
            //is not contained in colorTable.
            return ((Integer) o) + 1;
        }
    }

    /**
     * Writes the color table in the header.
     *
     * @param w The {@link RtfWriter} to write to
     *
     * @throws IOException On error
     */
    public void writeRtfContent(RtfWriter w)
    throws IOException {
        w.newLine();
        w.writeGroupMark(true);

        //implicitly writes the first color (=index 0), which
        //is reserved for auto-colored.
        w.writeControlWord("colortbl;");

        int len = colorTable.size();
        for (int i = 0; i < len; i++) {
            int identifier = ((Integer) colorTable.get(i));

            w.newLine();
            w.writeControlWord("red", determineColorLevel(identifier, RED));
            w.writeControlWord("green", determineColorLevel(identifier, GREEN));
            w.writeControlWord("blue", determineColorLevel(identifier, BLUE));
            w.write(";");
        }

        w.newLine();
        w.writeGroupMark(false);
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return false;
    }
    
    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

    /**
     * Adds a color to the table.
     *
     * @param i Identifier of color
     */
    private void addColor(Integer i) {
        colorIndex.put(i, new Integer(colorTable.size()));
        colorTable.addElement(i);
    }

    /**
     * Determines a identifier for the color.
     *
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     *
     * @return Unique identifier of color
     */
    private static int determineIdentifier(int red, int green, int blue) {
        int c = red << RED;

        c += green << GREEN;
        c += blue << BLUE;

        return c;
    }

    /**
     * Determines the color level from the identifier.
     *
     * @param identifier Unique color identifier
     * @param color One of the bit moving constants
     *
     * @return Color level in byte size
     */
    private static int determineColorLevel(int identifier, int color) {
        int retVal = (byte) (identifier >> color);
        return retVal < 0 ? retVal + 256 : retVal;
    }
}
