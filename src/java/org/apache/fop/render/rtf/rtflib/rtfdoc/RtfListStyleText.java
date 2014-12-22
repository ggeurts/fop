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
 * Class to handle text list style.
 */
public class RtfListStyleText extends RtfListStyle {
    private String text;

    /**
     * Constructs a RtfListStyleText object.
     * @param s Text to be displayed
     */
    public RtfListStyleText(String s) {
        text = s;
    }

    /** {@inheritDoc} */
    public void writeListPrefix(RtfWriter w, RtfListItem item)
    throws IOException {
        // bulleted list
        w.writeControlWord("pnlvlblt");
        w.writeControlWord("ilvl0");
        w.writeOneAttribute(RtfListTable.LIST_NUMBER, item.getNumber());
        w.writeOneAttribute("pnindent",
                item.getParentList().attrib.getValue(RtfListTable.LIST_INDENT));
        w.writeControlWord("pnf1");
        w.writeGroupMark(true);
        //item.writeControlWord("pndec");
        w.writeOneAttribute(RtfListTable.LIST_FONT_TYPE, "2");
        w.writeControlWord("pntxtb");
        w.write(text);
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

        String sCount;
        if (text.length() < 10) {
            sCount = "0" + String.valueOf(text.length());
        } else {
            sCount = String.valueOf(Integer.toHexString(text.length()));
            if (sCount.length() == 1) {
                sCount = "0" + sCount;
            }
        }
        w.writeControlWord(RtfListTable.LIST_TEXT_FORM);
        w.writeControlWord("'" + sCount);
        w.write(text);
        w.writeGroupMark(false);

        w.writeGroupMark(true);
        w.writeOneAttribute(RtfListTable.LIST_NUM_POSITION, null);
        w.writeGroupMark(false);
    }
}
