/*
 * Copyright 2014 ggeurts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fop.render.rtf;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAfter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfBefore;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfPage;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for handling of simple and complex page layouts.
 */
public class RtfPageLayoutTestCase extends BaseRTFTest {
    private static final String baseUrl = new File(".").toURI().toString();

    private static final String FO_SIMPLE_BODY_ONLY = 
        "<?xml version='1.0' encoding='UTF-8'?>" +
        "<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'>" +
        "  <fo:layout-master-set>" +
        "    <fo:simple-page-master master-name='simplePM' page-height='25cm' page-width='20cm' " + 
                    "margin-top='1cm' margin-bottom='1cm' margin-left='1.5cm' margin-right='1.5cm'>" +
        "      <fo:region-body margin-top='1cm' margin-bottom='2cm' margin-left='3cm' margin-right='4cm' />" +
        "    </fo:simple-page-master>" +
        "  </fo:layout-master-set>" +
        "  <fo:page-sequence master-reference='simplePM'>" +
        "    <fo:flow flow-name='xsl-region-body'>" +
        "      <fo:block>Some block.</fo:block>" +
        "    </fo:flow>" +
        "  </fo:page-sequence>" +
        "</fo:root>";

    private static final String FO_COMPLEX_BODY_ONLY =
        "<?xml version='1.0' encoding='UTF-8'?>" +
        "<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'>" +
        "  <fo:layout-master-set>" +
        "    <fo:simple-page-master master-name='first-page' page-height='25cm' page-width='20cm' margin='2.5cm'>" +
        "      <fo:region-body />" +
        "    </fo:simple-page-master>" +
        "    <fo:simple-page-master master-name='odd-pages' page-height='25cm' page-width='20cm' " + 
                    "margin-left='3cm' margin-right='2cm'>" +
        "      <fo:region-body margin-top='3cm' margin-bottom='3cm' />" +
        "    </fo:simple-page-master>" +
        "    <fo:simple-page-master master-name='even-pages' page-height='25cm' page-width='20cm' " + 
                    "margin-left='2cm' margin-right='3cm'>" +
        "      <fo:region-body margin-top='3cm' margin-bottom='3cm' />" +
        "    </fo:simple-page-master>" +
        "    <fo:page-sequence-master master-name='my-sequence'>" +
        "      <fo:repeatable-page-master-alternatives>" + 
        "        <fo:conditional-page-master-reference page-position='first' master-reference='first-page'/>" +
        "        <fo:conditional-page-master-reference page-position='any' odd-or-even='odd' master-reference='odd-pages'/>" +
        "        <fo:conditional-page-master-reference page-position='any' odd-or-even='even' master-reference='even-pages'/>" +
        "      </fo:repeatable-page-master-alternatives>" + 
        "    </fo:page-sequence-master>" +
        "  </fo:layout-master-set>" +
        "  <fo:page-sequence master-reference='my-sequence'>" +
        "    <fo:flow flow-name='xsl-region-body'>" +
        "      <fo:block>Some block.</fo:block>" +
        "    </fo:flow>" +
        "  </fo:page-sequence>" +
        "</fo:root>";
    
    public RtfPageLayoutTestCase() 
    throws Exception {
        super(getDefaultConfFile());
    }
    
    /** create an FOUserAgent for our tests
     *  @return an initialized FOUserAgent
     * */
    protected FOUserAgent getUserAgent() {
        final FOUserAgent userAgent = fopFactory.newFOUserAgent();
        return userAgent;
    }
    
    @Test
    public void simplePageMaster_withBodyOnly()
    throws Exception {
        StreamSource foSource = new StreamSource(new StringReader(FO_SIMPLE_BODY_ONLY), baseUrl); 
        RtfFile rtfFile = convertFO(foSource, getUserAgent());
        assertThat("RtfFile", rtfFile, not(nullValue()));

        RtfDocumentArea rtfDocArea = rtfFile.getDocumentArea();
        assertThat("RtfDocumentArea", rtfDocArea, not(nullValue()));
        verifyPageSizeAndMarginForDocument(rtfDocArea, 25, 20, 2, 3, 4.5f, 5.5f);
        verifyAttributeUnset(rtfDocArea, RtfDocumentArea.FACING_PAGES);
        
        RtfSection rtfSection = getFirstChild(rtfDocArea, RtfSection.class);
        assertThat("RtfSection", rtfSection, is(not(nullValue())));
        verifyPageSizeAndMarginForSection(rtfSection, 25, 20, 2, 3, 4.5f, 5.5f);
        verifyAttributeUnset(rtfSection, RtfPage.TITLE_PAGE);
        
        List<RtfBefore> rtfHeaders = getChildren(rtfSection, RtfBefore.class);
        assertThat("One header", rtfHeaders.size(), equalTo(1));
        verifyHeaderOrFooter(rtfHeaders.get(0), RtfBefore.HEADER, false, "Header");
        
        List<RtfAfter> rtfFooters = getChildren(rtfSection, RtfAfter.class);
        assertThat("One footer", rtfFooters.size(), equalTo(1));
        verifyHeaderOrFooter(rtfFooters.get(0), RtfAfter.FOOTER, false, "Footer");
    }

    @Test
    public void complexPageMaster_withBodyOnly()
    throws Exception {
        StreamSource foSource = new StreamSource(new StringReader(FO_COMPLEX_BODY_ONLY), baseUrl); 
        RtfFile rtfFile = convertFO(foSource, getUserAgent());
        assertThat("RtfFile", rtfFile, not(nullValue()));

        RtfDocumentArea rtfDocArea = rtfFile.getDocumentArea();
        assertThat("RtfDocumentArea", rtfDocArea, not(nullValue()));
        verifyPageSizeAndMarginForDocument(rtfDocArea, 25, 20, 3, 3, 3, 2);
        verifyAttributeSet(rtfDocArea, RtfDocumentArea.FACING_PAGES, null);
        verifyAttributeSet(rtfDocArea, RtfDocumentArea.MARGIN_MIRROR, null);
        
        RtfSection rtfSection = getFirstChild(rtfDocArea, RtfSection.class);
        assertThat("RtfSection", rtfSection, is(not(nullValue())));
        verifyPageSizeAndMarginForSection(rtfSection, 25, 20, 3, 3, 3, 2);
        verifyAttributeSet(rtfSection, RtfPage.TITLE_PAGE, null);
        
        List<RtfBefore> rtfHeaders = getChildren(rtfSection, RtfBefore.class);
        assertThat("Three headers", rtfHeaders.size(), equalTo(3));
        verifyHeaderOrFooter(rtfHeaders.get(0), RtfBefore.HEADER_FIRST, false, 
                "Title page header");
        verifyHeaderOrFooter(rtfHeaders.get(1), RtfBefore.HEADER_RIGHT, false, 
                "Right page header");
        verifyHeaderOrFooter(rtfHeaders.get(2), RtfBefore.HEADER_LEFT, false, 
                "Left page header");
        
        List<RtfAfter> rtfFooters = getChildren(rtfSection, RtfAfter.class);
        assertThat("Three footers", rtfFooters.size(), equalTo(3));
        verifyHeaderOrFooter(rtfFooters.get(0), RtfAfter.FOOTER_FIRST, false, 
                "Title page footer");
        verifyHeaderOrFooter(rtfFooters.get(1), RtfAfter.FOOTER_RIGHT, false, 
                "Right page footer");
        verifyHeaderOrFooter(rtfFooters.get(2), RtfAfter.FOOTER_LEFT, false, 
                "Left page footer");
    }

    private void verifyPageSizeAndMarginForDocument(RtfDocumentArea rtfDocArea, float pageHeightInCm, int pageWidthInCm, float marginTopInCm, float marginBottomInCm, float marginLeftInCm, float marginRightInCm) {
        verifyAttributeSet(rtfDocArea, RtfPage.PAGE_HEIGHT, 
                (int)(pageHeightInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfDocArea, RtfPage.PAGE_WIDTH, 
                (int)(pageWidthInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfDocArea, RtfPage.MARGIN_LEFT, 
                (int)(marginLeftInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfDocArea, RtfPage.MARGIN_TOP, 
                (int)(marginTopInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfDocArea, RtfPage.MARGIN_RIGHT, 
                (int)(marginRightInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfDocArea, RtfPage.MARGIN_BOTTOM, 
                (int)(marginBottomInCm * FoUnitsConverter.CM_TO_TWIPS));
    }
    
    private void verifyPageSizeAndMarginForSection(RtfSection rtfSection, float pageHeightInCm, int pageWidthInCm, float marginTopInCm, float marginBottomInCm, float marginLeftInCm, float marginRightInCm) {
        verifyAttributeSet(rtfSection, RtfSection.PAGE_HEIGHT, 
                (int)(pageHeightInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfSection, RtfSection.PAGE_WIDTH, 
                (int)(pageWidthInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfSection, RtfSection.MARGIN_LEFT, 
                (int)(marginLeftInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfSection, RtfSection.MARGIN_TOP, 
                (int)(marginTopInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfSection, RtfSection.MARGIN_RIGHT, 
                (int)(marginRightInCm * FoUnitsConverter.CM_TO_TWIPS));
        verifyAttributeSet(rtfSection, RtfSection.MARGIN_BOTTOM, 
                (int)(marginBottomInCm * FoUnitsConverter.CM_TO_TWIPS));
    }
    
    private static void verifyHeaderOrFooter(RtfContainer container, 
            String expectedAttr, boolean hasContent, String description) {
        assertThat(description + ": has content", container.getChildCount() > 0, 
                equalTo(hasContent));
        assertTrue(description + ": has attribute " + expectedAttr, 
                container.getRtfAttributes().isSet(expectedAttr));
    }
    
    private static void verifyAttributeSet(RtfElement e, String attrName, 
            Integer expectedValue) {
        assertTrue(e.toString() + ": has attribute " + attrName, 
                e.getRtfAttributes().isSet(attrName));
        assertThat(e.toString() + ": value of " + attrName, 
                e.getRtfAttributes().getValueAsInteger(attrName),
                equalTo(expectedValue));
    }

    private static void verifyAttributeUnset(RtfElement e, String attrName) {
        assertFalse(e.toString() + ": has attribute " + attrName, 
                e.getRtfAttributes().isSet(attrName));
    }
}
