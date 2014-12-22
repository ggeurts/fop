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
 * Class to handle bullet list style.
 */
public class RtfListStyleBullet extends RtfListStyle {

    /** {@inheritDoc} */
    public void writeListPrefix(RtfListItem item, RtfWriter w)
    throws IOException {
        // bulleted list
        w.writeControlWord("pnlvlblt");
        w.writeControlWord("ilvl0");
        w.writeOneAttribute(RtfListTable.LIST_NUMBER, new Integer(item.getNumber()));
        w.writeOneAttribute("pnindent",
                item.getParentList().attrib.getValue(RtfListTable.LIST_INDENT));
        w.writeControlWord("pnf1");
        w.writeGroupMark(true);
        w.writeControlWord("pndec");
        w.writeOneAttribute(RtfListTable.LIST_FONT_TYPE, "2");
        w.writeControlWord("pntxtb");
        w.writeControlWord("'b7");
        w.writeGroupMark(false);
    }

    /** {@inheritDoc} */
    public void writeParagraphPrefix(RtfWriter w)
    throws IOException {
        w.writeGroupMark(true);
        w.writeControlWord("pntext");
        w.writeGroupMark(false);
    }

    /** {@inheritDoc} */
    public void writeLevelGroup(RtfWriter w, RtfListTable table) 
    throws IOException {
        table.attrib.set(RtfListTable.LIST_NUMBER_TYPE, 23);
        table.attrib.set(RtfListTable.LIST_FONT_TYPE, 2);

        w.writeGroupMark(true);
        w.writeOneAttribute(RtfListTable.LIST_TEXT_FORM, "\\'01\\'b7");
        w.writeGroupMark(false);

        w.writeGroupMark(true);
        w.writeOneAttribute(RtfListTable.LIST_NUM_POSITION, null);
        w.writeGroupMark(false);
    }
}
