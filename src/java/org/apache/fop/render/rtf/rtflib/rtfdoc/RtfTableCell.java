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
import java.util.Iterator;

/**
 * <p>A cell in an RTF table, container for paragraphs, lists, etc.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch).</p>
 */

public class RtfTableCell
        extends RtfContainer
        implements IRtfParagraphContainer, IRtfListContainer, IRtfTableContainer,
            IRtfExternalGraphicContainer, IRtfTextrunContainer {

    /** cell merging: this cell is not merged */
    public static final int NO_MERGE = 0;

    /** cell merging: this cell is the start of a range of merged cells */
    public static final int MERGE_START = 1;

    /** cell merging: this cell is part of (but not the start of) a range of merged cells */
    public static final int MERGE_WITH_PREVIOUS = 2;
    
    private boolean setCenter;
    private boolean setRight;
    private final int id;

    /** cell width in twips */
    private final int cellWidth;

    /** cell merging has three states */
    private int vMerge = NO_MERGE;
    private int hMerge = NO_MERGE;

    /** Create an RTF element as a child of given container */
    RtfTableCell(RtfTableRow parent, int cellWidth, int idNum) {
        super(parent);
        id = idNum;
        this.cellWidth = cellWidth;
        setCenter = false;
        setRight = false;
    }

    /** Create an RTF element as a child of given container */
    RtfTableCell(RtfTableRow parent, int cellWidth, RtfAttributes attrs, int idNum)  {
        super(parent, attrs);
        id = idNum;
        this.cellWidth = cellWidth;
    }

    /**
     * Start a new paragraph after closing current current paragraph, list and table
     * @param attrs attributes of new RtfParagraph
     * @return new RtfParagraph object
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) {
        RtfParagraph paragraph = new RtfParagraph(this, attrs);

        if (paragraph.attrib.isSet("qc")) {
            setCenter = true;
        } else if (paragraph.attrib.isSet("qr")) {
            setRight = true;
        }

        //lines modified by Chris Scott, Westinghouse
        return paragraph;
    }

    /**
     * Start a new external graphic after closing current paragraph, list and table
     * @return new RtfExternalGraphic object
     */
    public RtfExternalGraphic newImage() {
        return new RtfExternalGraphic(this);
    }

    /**
     * Start a new paragraph with default attributes after closing current
     * paragraph, list and table
     * @return new RtfParagraph object
     */
    public RtfParagraph newParagraph() {
        return newParagraph(null);
    }

    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrib attributes for new RtfList
     * @return new RtfList object
     */
    public RtfList newList(RtfAttributes attrib) {
        return new RtfList(this, attrib);
    }

    /**
     * Start a new nested table after closing current paragraph, list and table
     * @param tc table column info for new RtfTable
     * @return new RtfTable object
     */
    public RtfTable newTable(ITableColumnsInfo tc) {
        return new RtfTable(this, tc);
    }

    /**
     * Start a new nested table after closing current paragraph, list and table
     * @param attrs attributes of new RtfTable
     * @param tc table column info for new RtfTable
     * @return new RtfTable object
     */
    // Modified by Boris Poudérous on 07/22/2002
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) {
        return new RtfTable(this, attrs, tc); // Added tc Boris Poudérous 07/22/2002
    }

    /** used by RtfTableRow to write the <celldef> cell definition control words
     *  @param w The {@link RtfWriter} to write to.
     *  @param offset sum of the widths of preceeding cells in same row
     *  @return offset + width of this cell
     */
    int writeCellDef(RtfWriter w, int offset) throws IOException {
        /*
         * Don't write \clmgf or \clmrg. Instead add the widths
         * of all spanned columns and create a single wider cell,
         * because \clmgf and \clmrg won't work in last row of a
         * table (Word2000 seems to do the same).
         * Cause of this, dont't write horizontally merged cells.
         * They just exist as placeholders in TableContext class,
         * and are never written to RTF file.
         */
        // horizontal cell merge codes
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return offset;
        }

        w.newLine();

        // vertical cell merge codes
        if (vMerge == MERGE_START) {
            w.writeControlWord("clvmgf");
        } else if (vMerge == MERGE_WITH_PREVIOUS) {
            w.writeControlWord("clvmrg");
        }

        /**
         * Added by Boris POUDEROUS on 2002/06/26
         */
        // Cell background color processing :
        w.writeAttributes(attrib, ITableAttributes.CELL_COLOR);
        /** - end - */

        w.writeAttributes(attrib, ITableAttributes.ATTRIB_CELL_PADDING);
        w.writeAttributes(attrib, ITableAttributes.CELL_BORDER);
        w.writeAttributes(attrib, IBorderAttributes.BORDERS);

        // determine cell width
        int iCurrentWidth = this.cellWidth;
        if (attrib.getValue("number-columns-spanned") != null) {
            // Get the number of columns spanned
            int nbMergedCells = ((Integer)attrib.getValue("number-columns-spanned")).intValue();

            RtfTable tab = getRow().getTable();

            // Get the context of the current table in order to get the width of each column
            ITableColumnsInfo tableColumnsInfo
                = tab.getITableColumnsInfo();

            tableColumnsInfo.selectFirstColumn();

            // Reach the column index in table context corresponding to the current column cell
            // id is the index of the current cell (it begins at 1)
            // getColumnIndex() is the index of the current column in table context (it begins at 0)
            //  => so we must withdraw 1 when comparing these two variables.
            while ((this.id - 1) != tableColumnsInfo.getColumnIndex()) {
               tableColumnsInfo.selectNextColumn();
            }

            // We withdraw one cell because the first cell is already created
            // (it's the current cell) !
            int i = nbMergedCells - 1;
            while (i > 0) {
                tableColumnsInfo.selectNextColumn();
                iCurrentWidth += (int)tableColumnsInfo.getColumnWidth();

                i--;
            }
        }
        final int xPos = offset + iCurrentWidth;

        //these lines added by Chris Scott, Westinghouse
        //some attributes need to be written before opening block
        if (setCenter) {
            w.writeControlWord("trqc");
        } else if (setRight) {
            w.writeControlWord("trqr");
        }
        
        w.writeAttributes(attrib, ITableAttributes.CELL_VERT_ALIGN);
        w.writeControlWord("cellx", xPos);

        return xPos;
    }

    /**
     * Overriden to avoid writing any it's a merged cell.
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent(RtfWriter w) throws IOException {
       // Never write horizontally merged cells.
       if (hMerge == MERGE_WITH_PREVIOUS) {
           return;
       }

       super.writeRtfContent(w);
    }

    /**
     * Called before writeRtfContent; overriden to avoid writing
     * any it's a merged cell.
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
        // Never write horizontally merged cells.
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return;
        }

        w.writeControlWord("intbl");
        
        if (getTable().isNestedTable()) {
            //itap is the depth (level) of the current nested table
            w.writeControlWord("itap", getTable().getNestedTableDepth() + 1);
        }
        
        super.writeRtfPrefix(w);
    }

    /**
     * The "cell" control word marks the end of a cell
     * {@inheritDoc} 
     */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        // Never write horizontally merged cells.
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return;
        }

        if (getTable().isNestedTable()) {
            //nested table
            w.writeControlWord("nestcell");
        } else {
            // word97 hangs if cell does not contain at least one "par" control word
            // TODO this is what causes the extra spaces in nested table of test
            //      004-spacing-in-tables.fo,
            // but if is not here we generate invalid RTF for word97

            if (setCenter) {
                w.writeControlWord("qc");
            } else if (setRight) {
                w.writeControlWord("qr");
            }

            w.writeControlWord("cell");
            w.newLine();
        }
    }

    /**
     * @param mergeStatus vertical cell merging status to set
     */
    public void setVMerge(int mergeStatus) { this.vMerge = mergeStatus; }

    /**
     * @return vertical cell merging status
     */
    public int getVMerge() { return this.vMerge; }

    /**
     * Set horizontal cell merging status
     * @param mergeStatus mergeStatus to set
     */
    public void setHMerge(int mergeStatus) {
        this.hMerge = mergeStatus;
    }

    /**
     * @return horizontal cell merging status
     */
    public int getHMerge() {
        return this.hMerge;
    }

    /** get cell width */
    int getCellWidth() { return this.cellWidth; }

    /**
     * A table cell always contains "useful" content, as it is here to take some
     * space in a row.
     * 
     * @return false (always)
     */
    public boolean isEmpty() {
        return false;
    }

    /** true if the "par" control word must be written for given RtfParagraph
     *  (which is not the case for the last non-empty paragraph of the cell)
     */
    boolean paragraphNeedsPar(RtfParagraph p) {
        // true if there is at least one non-empty paragraph after p in our children
        boolean pFound = false;
        boolean result = false;
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final Object o = it.next();
            if (!pFound) {
                // set pFound when p is found in the list
                pFound =  (o == p);
            } else {
                if (o instanceof RtfParagraph) {
                    final RtfParagraph p2 = (RtfParagraph)o;
                    if (!p2.isEmpty()) {
                        // found a non-empty paragraph after p
                        result = true;
                        break;
                    }
                } else if (o instanceof RtfTable) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the current RtfTextrun object.
     * Opens a new one if necessary.
     * @return The RtfTextrun object
     */
    public RtfTextrun getTextrun() {
        RtfAttributes attrs = new RtfAttributes();
        RtfTextrun textrun = RtfTextrun.getTextrun(this, attrs);

        //Suppress the very last \par, because the closing \cell applies the
        //paragraph attributes.
        textrun.setSuppressLastPar(true);
        return textrun;
    }

    /**
     * Get the parent row.
     * @return The parent row.
     */
    public RtfTableRow getRow() {
        return (RtfTableRow)parent;
    }

    /**
     * Get the parent tab;e.
     * @return The parent row.
     */
    public RtfTable getTable() {
        return getRow().getTable();
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
