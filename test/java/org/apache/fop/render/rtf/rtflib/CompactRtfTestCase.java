/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.fop.render.rtf.rtflib;

import java.io.IOException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for RTF rendering API
 * 
 * @author ggeurts
 */
public class CompactRtfTestCase extends BaseRtflibTest
{
    /* Avoid spaces between control words */
    
    @Test
    public void writeTextAttributesWithoutSpaces() 
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
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
    
    
    /* Avoid writing of redundant control words in textruns */
    
    @Test
    public void textrunIgnoresBlockAttributesThatDuplicateParentBlockAttributes()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                RtfAttributes blockAtts1 = new RtfAttributes()
                        .set(RtfText.ATTR_BOLD)
                        .set(RtfText.ATTR_ITALIC);
                RtfAttributes blockAtts2 = new RtfAttributes()
                        .set(RtfText.ATTR_UNDERLINE)    // new attribute
                        .set(RtfText.ATTR_BOLD, 0)      // changed attribute 
                        .set(RtfText.ATTR_ITALIC);      // unchanged attribute
                RtfTextrun textrun = section.getTextrun();
                textrun.pushBlockAttributes(blockAtts1);
                textrun.pushBlockAttributes(blockAtts2);
                textrun.addString("Some text");
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                return textrun;
            }
        });
        assertThat(rtf, equalTo("\n{\\b\\i{\\b0\\ul Some text}}"));
    }

    @Test
    public void textrunIgnoresInlineAttributesThatDuplicateBlockAttributes()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                RtfAttributes blockAtts1 = new RtfAttributes()
                        .set(RtfText.ATTR_BOLD)
                        .set(RtfText.ATTR_ITALIC);
                RtfAttributes blockAtts2 = new RtfAttributes()
                        .set(RtfText.ATTR_BOLD, 0)      // changed attribute 
                        .set(RtfText.ATTR_ITALIC);      // unchanged attribute
                RtfAttributes inlineAtts = new RtfAttributes()
                        .set(RtfText.ATTR_UNDERLINE)    // new attribute
                        .set(RtfText.ATTR_BOLD)         // changed attribute 
                        .set(RtfText.ATTR_ITALIC);      // unchanged attribute
                RtfTextrun textrun = section.getTextrun();
                textrun.pushBlockAttributes(blockAtts1);
                textrun.pushBlockAttributes(blockAtts2);
                textrun.pushInlineAttributes(inlineAtts);
                textrun.addString("Some text");
                textrun.popInlineAttributes();
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                return textrun;
            }
        });
        assertThat(rtf, equalTo("\n{\\b\\i{\\b0{\\b\\ul Some text}}}"));
    }
    
    @Test
    public void textRunIgnoresZeroBeforeAndAfterSpace()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                RtfAttributes blockAtts = new RtfAttributes()
                        .set(RtfText.SPACE_BEFORE, 0)
                        .set(RtfText.SPACE_AFTER, 0);
                RtfTextrun textrun = section.getTextrun();
                textrun.pushBlockAttributes(blockAtts);
                textrun.addString("Some text");
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                return textrun;
            }
        });
        assertThat(rtf, equalTo("\n{Some text}"));
    }

    @Test
    public void textRunAccumulatesDuplicateBeforeAndAfterSpace()
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                RtfTextrun textrun = section.getTextrun();
                textrun.pushBlockAttributes(new RtfAttributes()
                        .set(RtfText.SPACE_BEFORE, 5)
                        .set(RtfText.SPACE_AFTER, 5));
                textrun.pushInlineAttributes(new RtfAttributes()
                        .set(RtfText.SPACE_BEFORE, 5)
                        .set(RtfText.SPACE_AFTER, 5));
                textrun.addString("Some text");
                textrun.popInlineAttributes();
                textrun.popBlockAttributes(RtfTextrun.BREAK_NONE);
                return textrun;
            }
        });
        assertThat(rtf, equalTo("\n{{\\sa10\\sb10 Some text}}"));
    }
    
    /* Avoid writing unnecessary color table entries */
    
    @Test
    public void colorTableWritesUsedColorsOnly() 
    throws Exception {
        String rtf = toRtf(new RtfBuilder() {
            @Override
            public RtfElement build(RtfSection section) throws IOException {
                return ((RtfFile)section.getParentOfClass(RtfFile.class))
                        .getHeader()
                        .getColorTable();
            }
        });
        assertThat(rtf, equalTo("\n{#colortbl;\n}"));
    }
}
