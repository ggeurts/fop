/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.fop.render.rtf;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfTextContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.internal.matchers.StringContains.*;

/**
 *
 * @author ggeurts
 */
public class CompactRtfTestCase
{
    
    public CompactRtfTestCase()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void writeTextAttributesWithoutSpaces() throws Exception
    {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException
            {
                RtfAttributes atts = new RtfAttributes();
                atts.set(RtfText.ATTR_BOLD);
                atts.set(RtfText.ATTR_ITALIC);
                atts.set(RtfText.ATTR_UNDERLINE);
                RtfParagraph p = section.newParagraph();
                p.newText("hello", atts);
                return p;
            }
        });
        assertThat(rtf, equalTo("{{\\b\\i\\ul hello}\\par}"));
    }
    
    private static String toRtf(RtfBuilder operation) throws IOException
    {
        StringWriter writer = new StringWriter();

        RtfFile rtfFile = new RtfFile(writer);
        RtfDocumentArea rtfDocumentArea = rtfFile.startDocumentArea();
        RtfSection section = rtfDocumentArea.newSection();

        RtfElement result = operation.build(section);
        result.writeRtf();
        
        return writer.getBuffer().toString();
    }
    
    private interface RtfBuilder
    {
        public RtfElement build(RtfSection section) throws IOException;
    }
}
