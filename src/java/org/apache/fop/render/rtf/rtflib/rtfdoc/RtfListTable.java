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
import java.util.LinkedList;

/**
 * <p>RtfListTable: used to make the list table in the header section of the RtfFile.
 * This is the method that Word uses to make lists in RTF and the way most RTF readers,
 * esp. Adobe FrameMaker read lists from RTF.</p>
 *
 * <p>This work was authored by Christopher Scott (scottc@westinghouse.com).</p>
 */
public class RtfListTable extends RtfContainer {
    private final LinkedList lists = new LinkedList();
    private final LinkedList styles = new LinkedList();

//static data members
    /** constant for a list table */
    public static final String LIST_TABLE = "listtable";
    /** constant for a list */
    public static final String LIST = "list";
    /** constant for a list template id */
    public static final String LIST_TEMPLATE_ID = "listtemplateid";
    /** constant for a list level */
    public static final String LIST_LEVEL = "listlevel";
    /** constant for a list number type */
    public static final String LIST_NUMBER_TYPE = "levelnfc";
    /** constant for a list justification */
    public static final String LIST_JUSTIFICATION = "leveljc";
    /** constant for list following character */
    public static final String LIST_FOLLOWING_CHAR = "levelfollow";
    /** constant for list start at */
    public static final String LIST_START_AT = "levelstartat";
    /** constant for list space */
    public static final String LIST_SPACE = "levelspace";
    /** constant for list indentation */
    public static final String LIST_INDENT = "levelindent";
    /** constant for list text format */
    public static final String LIST_TEXT_FORM = "leveltext";
    /** constant for list number positioning */
    public static final String LIST_NUM_POSITION = "levelnumbers";
    /** constant for list name */
    public static final String LIST_NAME = "listname ;";
    /** constant for list ID */
    public static final String LIST_ID = "listid";
    /** constant for list font type */
    public static final String LIST_FONT_TYPE = "f";
    /** constant for list override table */
    public static final String LIST_OVR_TABLE = "listoverridetable";
    /** constant for list override */
    public static final String LIST_OVR = "listoverride";
    /** constant for list override count */
    public static final String LIST_OVR_COUNT = "listoverridecount";
    /** constant for list number */
    public static final String LIST_NUMBER = "ls";

    /** String array of list table attributes */
    public static final String [] LIST_TABLE_ATTR = {
        LIST_TABLE,             LIST,                   LIST_TEMPLATE_ID,
        LIST_NUMBER_TYPE,       LIST_JUSTIFICATION,     LIST_FOLLOWING_CHAR,
        LIST_START_AT,          LIST_SPACE,             LIST_INDENT,
        LIST_TEXT_FORM,         LIST_NUM_POSITION,      LIST_ID,
        LIST_OVR_TABLE,         LIST_OVR,               LIST_OVR_COUNT,
        LIST_NUMBER,            LIST_LEVEL
    };

    /**
     * RtfListTable Constructor: sets the number of the list, and allocates
     * for the RtfAttributes
     * @param parent RtfContainer holding this RtfListTable
     */
    public RtfListTable(RtfContainer parent) {
        super(parent);
    }

    /**
     * Add List
     * @param list RtfList to add
     * @return number of lists in the table after adding
     */
    public int addList(RtfList list) {
        lists.add(list);
        return lists.size();
    }

    /**
     * Write the content
     * @param w the value of w
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        w.newLine();
        w.writeGroupMark(true);
        w.writeStarControlWord(LIST_TABLE);
        w.newLine();
        for (Iterator it = lists.iterator(); it.hasNext();) {
            final RtfList list = (RtfList)it.next();
            writeListTableEntry(w, list);
            w.newLine();
        }
        w.writeGroupMark(false);

        w.newLine();
        w.writeGroupMark(true);
        w.writeStarControlWord(LIST_OVR_TABLE);
        int z = 1;
        w.newLine();
        for (Iterator it = styles.iterator(); it.hasNext();) {
            final RtfListStyle style = (RtfListStyle)it.next();

            w.writeGroupMark(true);
            w.writeStarControlWord(LIST_OVR);
            w.writeGroupMark(true);

            w.writeOneAttribute(LIST_ID, style.getRtfList().getListId().toString());
            w.writeOneAttribute(LIST_OVR_COUNT, new Integer(0));
            w.writeOneAttribute(LIST_NUMBER, new Integer(z++));

            w.writeGroupMark(false);
            w.writeGroupMark(false);
            w.newLine();
        }

        w.writeGroupMark(false);
        w.newLine();
    }

    /**
     * Since this has no text content we have to overwrite isEmpty to print
     * the table
     */
    public boolean isEmpty() {
        return lists.isEmpty();
    }

    private void writeListTableEntry(RtfWriter w, RtfList list)
    throws IOException {
        //write list-specific attributes
        w.writeGroupMark(true);
        w.writeControlWord(LIST);
        w.writeOneAttribute(LIST_TEMPLATE_ID, list.getListTemplateId().toString());
        w.writeOneAttribute(LIST, attrib.getValue(LIST));

        // write level-specific attributes
        w.writeGroupMark(true);
        w.writeControlWord(LIST_LEVEL);

        w.writeOneAttribute(LIST_JUSTIFICATION, attrib.getValue(LIST_JUSTIFICATION));
        w.writeOneAttribute(LIST_FOLLOWING_CHAR, attrib.getValue(LIST_FOLLOWING_CHAR));
        w.writeOneAttribute(LIST_SPACE, new Integer(0));
        w.writeOneAttribute(LIST_INDENT, attrib.getValue(LIST_INDENT));

        RtfListItem item = (RtfListItem)list.getChildren().get(0);
        if (item != null) {
            item.getRtfListStyle().writeLevelGroup(w, this);
        }

        w.writeGroupMark(false);

        w.writeGroupMark(true);
        w.writeControlWord(LIST_NAME);
        w.writeGroupMark(false);

        w.writeOneAttribute(LIST_ID, list.getListId().toString());

        w.writeGroupMark(false);
    }

    /**
     * Add list style
     * @param ls ListStyle to set
     * @return number of styles after adding
     */
    public int addRtfListStyle(RtfListStyle ls) {
        styles.add(ls);
        return styles.size();
    }
}
