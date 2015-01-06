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

package org.apache.fop.render.rtf.rtflib;

import java.io.File;
import java.io.IOException;
import static org.apache.fop.render.rtf.rtflib.BaseRtflibTest.toRtf;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;
import org.apache.fop.render.rtf.rtflib.tools.TableContext;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ggeurts
 */
public class RtfTableTestCase  extends BaseRtflibTest {

    @Test
    public void emptyTable()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                return table;
            }
        });
        assertThat(rtf, equalTo(""));
    }

    @Test
    public void tableWithSingleNonEmptyCell()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                RtfTableRow row = table.newTableRow();
                RtfTableCell cell = row.newTableCell(1000);
                cell.getTextrun().addString("one cell");
                return table;
            }
        });
        assertThat(rtf, equalTo("\n\\trowd\n\\cellx1000\n\\intbl one cell\\cell\n\\row"));
    }

    @Test
    public void tableWithSingleEmptyCell()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                RtfTableRow row = table.newTableRow();
                RtfTableCell cell = row.newTableCell(1000);
                return table;
            }
        });
        assertThat(rtf, equalTo("\n\\trowd\n\\cellx1000\n\\intbl\\cell\n\\row"));
    }
    
    @Test
    public void tableWithOneRowOfTwoCells()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                RtfTableRow row = table.newTableRow();
                RtfTableCell cell1 = row.newTableCell(1100);
                cell1.getTextrun().addString("Cell 1");
                RtfTableCell cell2 = row.newTableCell(1200);
                cell2.getTextrun().addString("Cell 2");
                return table;
            }
        });
        assertThat(rtf, equalTo("\n\\trowd\n" + 
                "\\cellx1100\n" +
                "\\cellx2300\n" + 
                "\\intbl Cell 1\\cell\n" + 
                "\\intbl Cell 2\\cell\n" + 
                "\\row"));
    }
    
    @Test
    public void tableCellWithParagraphBreak()
    throws Exception {
        File dumpFile = null; //File.createTempFile("tableCellWithParagraphBreak", ".rtf");
        
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                RtfTableRow row = table.newTableRow();
                RtfTableCell cell = row.newTableCell(1000);
                RtfTextrun textrun = cell.getTextrun();
                textrun.addString("Paragraph one");
                textrun.addParagraphBreak();
                textrun.addString("Paragraph two");
                textrun.addParagraphBreak();
                return table;
            }
        }, dumpFile);
        assertThat(rtf, equalTo("\n\\trowd\n" + 
                "\\cellx1000\n" +
                "\\intbl Paragraph one\\par Paragraph two\\cell\n" + 
                "\\row"));
    }
    
    @Test
    public void nestedTable()
    throws Exception {
        File dumpFile = null; //File.createTempFile("tableCellWithParagraphBreak", ".rtf");
        
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                TableContext tableContext = new TableContext();
                RtfTable table = section.newTable(tableContext);
                RtfTableRow row = table.newTableRow();
                RtfTableCell cell = row.newTableCell(1000);
                
                TableContext nestedTableContext = new TableContext();
                RtfTable nestedTable = cell.newTable(tableContext);
                RtfTableRow nestedRow = nestedTable.newTableRow();
                RtfTableCell nestedCell = nestedRow.newTableCell(500);
                
                RtfTextrun textrun = nestedCell.getTextrun();
                textrun.addString("Nested content");
                return table;
            }
        }, dumpFile);
        assertThat(rtf, equalTo("\n\\trowd\n" + 
                "\\cellx1000\n" +
                "\\intbl\n" + 
                "{\\intbl\\itap2 Nested content\\nestcell" +
                "{\\*\\nesttableprops\\trowd\n\\cellx500\n\\nestrow}" + 
                "{\\nonesttables\\par}}" + 
                "\\cell\n" + 
                "\\row"));
    }
}

