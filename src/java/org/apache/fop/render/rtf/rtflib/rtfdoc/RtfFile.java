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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

/**
 * <p>Models the top-level structure of an RTF file.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch),
 * Andreas Putz (a.putz@skynamics.com), and
 * Christopher Scott (scottc@westinghouse.com).</p>
 */

public class RtfFile
extends RtfContainer 
implements IRtfMappings {
    private final RtfWriter writer;
    private final RtfHeader header;
    private RtfPageArea pageArea;
    private RtfDocumentArea docArea;

    /**
     * Create an RTF file that outputs to the given Writer
     * @param w the Writer to write to
     */
    public RtfFile(Writer w) {
        super(null);
        header = new RtfHeader(this);
        writer = w != null ? new RtfWriter(w) : null;
    }

    /**
     * Returns the {@link RtfHeader} for this RTF file.
     * @return the header.
     */
    public RtfHeader getHeader() {        
        return header;
    }

    /**
     * Gets the index of a color in the RTF color table
     *
     * @param red Color level red
     * @param green Color level green
     * @param blue Color level blue
     * @return The number of the color in the table
     */
    public int getColorNumber(int red, int green, int blue) {
        return header.getColorTable().getColorNumber(red, green, blue);
    }

    /**
     * Gets the index of a font in the RTF font table
     *
     * @param family Font family name ('Helvetica')
     * @return The number of the font in the table
     */
    public int getFontNumber(String name) {
        return header.getFontTable().getFontNumber(name);
    }
    
    /**
     * Get the list table.
     * @return the RtfListTable
     */
    public RtfListTable getListTable() {
        return header.getListTable();
    }

    /**
     * Closes the RtfHeader if not done yet, and starts the document area.
     * Like startDocumentArea, is only called once. This is not optimal,
     * must be able to have multiple page definition, and corresponding
     * Document areas
     * @return the RtfPageArea
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfPageArea startPageArea()
    throws RtfStructureException {
        if (pageArea != null) {
            throw new RtfStructureException("startPageArea called more than once");
        }
        header.close();
        return pageArea = new RtfPageArea(this);
    }

    /**
     * Call startPageArea if needed and return the page area object.
     * @return the RtfPageArea
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfPageArea getPageArea()
    throws RtfStructureException {
        if (pageArea == null) {
            return startPageArea();
        }
        return pageArea;
    }

    /**
     * Closes the RtfHeader if not done yet, and starts the document area.
     * Must be called once only.
     * @return the RtfDocumentArea
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfDocumentArea startDocumentArea()
        throws RtfStructureException {
        if (docArea != null) {
            throw new RtfStructureException("startDocumentArea called more than once");
        }
        header.close();
        return docArea = new RtfDocumentArea(this);
    }

    /**
     * Call startDocumentArea if needed and return the document area object.
     * @return the RtfDocumentArea
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfDocumentArea getDocumentArea()
    throws RtfStructureException {
        if (docArea == null) {
            return startDocumentArea();
        }
        return docArea;
    }

    /**
     * overridden to write RTF prefix code, what comes before our children
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
        w.writeGroupMark(true);
        w.writeControlWord("rtf1");
    }

    /**
     * overridden to write RTF suffix code, what comes after our children
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        w.writeGroupMark(false);
    }

    /**
     * must be called when done creating the document
     * @throws IOException for I/O problems
     */
    public synchronized void flush() throws IOException {
        if (writer != null) {
            writeRtf(writer);
            writer.flush();
        }
    }

    /**
     * minimal test and usage example
     * @param args command-line arguments
     * @throws Exception for problems
     */
    public static void main(String[] args)
    throws Exception {
        Writer w = null;
        if (args.length != 0) {
            final String outFile = args[0];
            System.err.println("Outputting RTF to file '" + outFile + "'");
            w = new BufferedWriter(new FileWriter(outFile));
        } else {
            System.err.println("Outputting RTF code to standard output");
            w = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        final RtfFile f = new RtfFile(w);
        final RtfSection sect = f.startDocumentArea().newSection();

        final RtfParagraph p = sect.newParagraph();
        p.newText("Hello, RTF world.\n", null);
        final RtfAttributes attr = new RtfAttributes();
        attr.set(RtfText.ATTR_BOLD);
        attr.set(RtfText.ATTR_ITALIC);
        attr.set(RtfText.ATTR_FONT_SIZE, 36);
        p.newText("This is bold, italic, 36 points", attr);

        f.flush();
        System.err.println("RtfFile test: all done.");
    }
}
