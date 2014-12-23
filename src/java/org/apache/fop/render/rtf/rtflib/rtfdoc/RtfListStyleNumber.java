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
 * Class to handle number list style.
 */
public class RtfListStyleNumber extends RtfListStyle {

    /** {@inheritDoc} */
    public void writeListPrefix(RtfWriter w, RtfListItem item)
    throws IOException {
        w.writeControlWord("pnlvlbody");
        w.writeControlWord("ilvl0");
        w.writeOneAttribute(RtfListTable.LIST_NUMBER, "0");
        w.writeControlWord("pndec");
        w.writeOneAttribute("pnstart", new Integer(1));
        w.writeOneAttribute("pnindent",
                item.attrib.getValue(RtfListTable.LIST_INDENT));
        w.writeControlWord("pntxta.");
    }

    /** {@inheritDoc} */
    public void writeParagraphPrefix(RtfWriter w)
    throws IOException {
        w.writeGroupMark(true);
        w.writeControlWord("pntext");
        w.writeControlWord("f" + getFontNumber("Symbol"));
        w.writeControlWord("'b7");
        w.writeControlWord("tab");
        w.writeGroupMark(false);
    }

    /** {@inheritDoc} */
    public void writeLevelGroup(RtfWriter w, RtfListTable table) 
    throws IOException {
        table.attrib.set(RtfListTable.LIST_NUMBER_TYPE, 0);

        w.writeOneAttribute(RtfListTable.LIST_START_AT, new Integer(1));

        w.writeGroupMark(true);
        w.writeOneAttribute(RtfListTable.LIST_TEXT_FORM, "\\'03\\\'00. ;");
        w.writeGroupMark(false);

        w.writeGroupMark(true);
        w.writeOneAttribute(RtfListTable.LIST_NUM_POSITION, "\\'01;");
        w.writeGroupMark(false);

        w.writeOneAttribute(RtfListTable.LIST_FONT_TYPE, new Integer(0));
    }
}
