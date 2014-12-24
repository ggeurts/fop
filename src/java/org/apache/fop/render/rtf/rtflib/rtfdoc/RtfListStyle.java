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

//Java
import java.io.IOException;

/**
 * Class to handle list styles.
 */
public class RtfListStyle {
    private RtfListItem rtfListItem;

    /**
     * Sets the RtfListItem this style belongs to.
     * @param item RtfListItem this style belongs to
     */
    public void setRtfListItem(RtfListItem item) {
        rtfListItem = item;
    }

    /**
     * Gets the RtfListItem this style belongs to.
     * @return RtfListItem this style belongs to
     */
    public RtfListItem getRtfListItem() {
        return rtfListItem;
    }

    /**
     * Gets the RtfList this style belongs to.
     * @return RtfList this style belongs to
     */
    public RtfList getRtfList() {
        return rtfListItem.getParentList();
    }
    
    /**
     * Gets the index of a font in the RTF font table
     *
     * @param family Font family name ('Helvetica')
     * @return The number of the font in the table
     */
    protected int getFontNumber(String name)
    {
        return rtfListItem.getRtfFile().getFontNumber(name);
    }

    /**
     * Gets called before a RtfListItem has to be written.
     * @param w The {@link RtfWriter} to write to.
     * @param item The {@link RtfListItem} this style belongs to.
     * @throws IOException Thrown when an IO-problem occurs.
     */
    public void writeListPrefix(RtfWriter w, RtfListItem item)
    throws IOException {
    }
    /**
     * Gets called before a paragraph has to be written, which is contained by a RtfListItem.
     * @param w The {@link RtfWriter} to write to.
     * @throws IOException Thrown when an IO-problem occurs.
     */
    public void writeParagraphPrefix(RtfWriter w)
    throws IOException {
    }

    /**
     * Gets called when the list table has to be written.
     * @param w The {@link RtfWriter} to write to.
     * @param table The @{link RtfListTable} that is to be written.
     * @throws IOException Thrown when an IO-problem occurs.
     */
    public void writeLevelGroup(RtfWriter w, RtfListTable table)
    throws IOException {
    }
}
