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

package org.apache.fop.render.rtf;

// Java
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.expr.NumericOp;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.EnumLength;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.layoutmgr.inline.ImageLayout;
import org.apache.fop.layoutmgr.table.ColumnSetup;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.rtf.rtflib.exceptions.RtfException;
import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfListContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfTableContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfTextrunContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAfter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfBefore;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfExternalGraphic;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFootnote;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfHyperLink;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfList;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem.RtfListItemLabel;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfPage;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraphBreak;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;
import org.apache.fop.render.rtf.rtflib.tools.BuilderContext;
import org.apache.fop.render.rtf.rtflib.tools.PercentContext;
import org.apache.fop.render.rtf.rtflib.tools.TableContext;

/**
 * RTF Handler: generates RTF output using the structure events from
 * the FO Tree sent to this structure handler.
 */
public class RTFHandler extends FOEventHandler {
    private final static Log log = LogFactory.getLog(RTFHandler.class);

    private final OutputStream os;
    private final BuilderContext builderContext = new BuilderContext();
    private final PercentContext percentManager = new PercentContext();

    private RtfFile rtfFile;
    private RtfDocumentArea docArea;
    private final Map rtfRegions = new HashMap();   // Maps region name to RtfContainer
    
    private boolean bDefer;              //true, if each called handler shall be
                                         //processed at later time.
    private int nestedTableDepth = 1;


    /**
     * Creates a new RTF structure handler.
     * @param userAgent the FOUserAgent for this process
     * @param os OutputStream to write to
     */
    public RTFHandler(FOUserAgent userAgent, OutputStream os) {
        super(userAgent);
        this.os = os;
        bDefer = true;

        boolean base14Kerning = false;
        FontSetup.setup(fontInfo, null, userAgent.getResourceResolver(), base14Kerning);
    }

    public RtfFile getRtfFile()
    {
        return rtfFile;
    }
    
    /** {@inheritDoc} */
    public void startDocument() throws SAXException {
        rtfFile = new RtfFile(new OutputStreamWriter(os));
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        try {
            rtfFile.flush();
        } catch (IOException ioe) {
            RendererEventProducer eventProducer = RendererEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.ioError(this, ioe);
        }
        docArea = null;
    }

    /** {@inheritDoc} */
    public void startPageSequence(PageSequence pageSeq)  {
        if (bDefer) {
            return;
        }
         
        SimplePageMaster pageMaster = pageSeq.getNextSimplePageMaster(1, false, false, false);
        if (pageMaster == null) {
            RTFEventProducer eventProducer = RTFEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.noSPMFound(this, pageSeq.getLocator());
            return;
        }

        if (docArea == null)
        {
            try {
                docArea = rtfFile.startDocumentArea();
                docArea.getRtfAttributes().set(
                    PageAttributesConverter.convertPageAttributes(pageMaster));
            } catch (RtfStructureException ex) {
                throw new RuntimeException(ex);
            }
        } 

        rtfRegions.clear();

        RtfSection rtfSection = docArea.newSection();
        rtfSection.getRtfAttributes().set(
            PageAttributesConverter.convertSectionAttributes(pageMaster));
        builderContext.pushContainer(rtfSection);

        Region bodyRegion = pageMaster.getRegion(Constants.FO_REGION_BODY);
        if (bodyRegion != null) {
            rtfRegions.put(bodyRegion.getRegionName(), rtfSection);
        }
        createRtfSectionHeadersAndFooters(pageSeq, rtfSection);

        int usablePageWidth = getUsablePageWidthInMpt(pageMaster, rtfSection);
        percentManager.setDimension(pageSeq, usablePageWidth);
    }

    private void createRtfSectionHeadersAndFooters(PageSequence pageSeq, 
            RtfSection rtfSection) {
        SimplePageMaster firstPageMaster = 
                pageSeq.getNextSimplePageMaster(1, true, false, false);
        SimplePageMaster rightPageMaster = 
                pageSeq.getNextSimplePageMaster(1, false, false, false);
        SimplePageMaster leftPageMaster = 
                pageSeq.getNextSimplePageMaster(2, false, false, false);

        if (firstPageMaster != null 
                && firstPageMaster != rightPageMaster
                && firstPageMaster != leftPageMaster) {
            // Treat first page differently from subsequent pages
            rtfSection.getRtfAttributes().set(RtfPage.TITLE_PAGE);
            createRtfHeaderAndFooter(firstPageMaster, rtfSection,
                    RtfBefore.HEADER_FIRST, RtfAfter.FOOTER_FIRST);
        }

        if (rightPageMaster != null && rightPageMaster != leftPageMaster) {
            // Treat left and right pages differently.
            RtfAttributes docAreaAtts = docArea.getRtfAttributes();
            docAreaAtts.set(RtfDocumentArea.FACING_PAGES);
            
            // RTF spec does not support specification of different margins for 
            // odd and even pages. However, mirroring of margins is supported. 
            // We apply the margins for odd pages to the section. If the margins
            // of even pages are different, we will enable margin mirroring.
            if (!haveSameMargins(rightPageMaster, leftPageMaster)) {
                docAreaAtts.set(RtfDocumentArea.MARGIN_MIRROR);
                log.warn("Ignoring margins for page master " + leftPageMaster.getMasterName());
            }
            
            createRtfHeaderAndFooter(rightPageMaster, rtfSection,
                    RtfBefore.HEADER_RIGHT, RtfAfter.FOOTER_RIGHT);
            createRtfHeaderAndFooter(leftPageMaster, rtfSection,
                    RtfBefore.HEADER_LEFT, RtfAfter.FOOTER_LEFT);
        } else {
            // Treat left and right pages the same
            SimplePageMaster pageMaster = rightPageMaster != null
                    ? rightPageMaster
                    : leftPageMaster;
            createRtfHeaderAndFooter(pageMaster, rtfSection,
                    RtfBefore.HEADER, RtfAfter.FOOTER);
        }
    }
    
    private void createRtfHeaderAndFooter(SimplePageMaster pageMaster, 
            RtfSection rtfSection, String rtfHeaderAttr, String rtfFooterAttr)
    {
        Region regionBefore = pageMaster != null
                ? pageMaster.getRegion(Constants.FO_REGION_BEFORE)
                : null;
        createRtfHeader(regionBefore, rtfSection, rtfHeaderAttr);
        
        Region regionAfter = pageMaster != null
                ? pageMaster.getRegion(Constants.FO_REGION_AFTER)
                : null;
        createRtfFooter(regionAfter, rtfSection, rtfFooterAttr);
    }
    
    private void createRtfHeader(Region region, 
            RtfSection rtfSection, String rtfHeaderAttr) {
        RtfAttributes headerAttrs = new RtfAttributes();
        headerAttrs.set(rtfHeaderAttr);
        RtfBefore header = rtfSection.newBefore(headerAttrs);
        if (region != null) {
            rtfRegions.put(region.getRegionName(), header);
        }
    }

    private void createRtfFooter(Region region, 
            RtfSection rtfSection, String rtfFooterAttr) {
        RtfAttributes footerAttrs = new RtfAttributes();
        footerAttrs.set(rtfFooterAttr);
        RtfAfter footer = rtfSection.newAfter(footerAttrs);
        if (region != null) {
            rtfRegions.put(region.getRegionName(), footer);
        }
    }
    
    private static boolean haveSameMargins(
            SimplePageMaster left, SimplePageMaster right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        
        if (!haveSameMargins(
                left.getCommonMarginBlock(), 
                right.getCommonMarginBlock())) {
            return false;
        }
        
        RegionBody leftBody = (RegionBody)left.getRegion(Constants.FO_REGION_BODY);
        RegionBody rightBody = (RegionBody)right.getRegion(Constants.FO_REGION_BODY);
        return haveSameMargins(
                leftBody.getCommonMarginBlock(), 
                rightBody.getCommonMarginBlock());
    }

    private static boolean haveSameMargins(
            CommonMarginBlock left, CommonMarginBlock right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.marginLeft.getValue() == right.marginLeft.getValue()
                && left.marginTop.getValue() == right.marginTop.getValue()                
                && left.marginRight.getValue() == right.marginRight.getValue()                
                && left.marginBottom.getValue() == right.marginBottom.getValue();
    }
    
    private int getUsablePageWidthInMpt(
            SimplePageMaster pageMaster, RtfSection rtfSection) {
        CommonMarginBlock pageMargin = pageMaster.getCommonMarginBlock();
        RtfAttributes rtfSectionAttr = rtfSection.getRtfAttributes();
        
        return pageMaster.getPageWidth().getValue()
                - pageMargin.marginLeft.getValue()
                - pageMargin.marginRight.getValue()
                - FoUnitsConverter.convertTwipsToMpt(
                        rtfSectionAttr.getValueAsInteger(RtfSection.MARGIN_LEFT))
                - FoUnitsConverter.convertTwipsToMpt(
                        rtfSectionAttr.getValueAsInteger(RtfSection.MARGIN_RIGHT));    
    }
            
    
    /** {@inheritDoc} */
    public void endPageSequence(PageSequence pageSeq) {
        if (bDefer) {
            //If endBlock was called while SAX parsing, and the passed FO is Block
            //nested within another Block, stop deferring.
            //Now process all deferred FOs.
            bDefer = false;
            recurseFONode(pageSeq);
            bDefer = true;

            return;
        } else {
            builderContext.popContainer(RtfSection.class, this);
        }
    }

    /** {@inheritDoc} */
    public void startFlow(Flow fl) {
        if (bDefer) {
            return;
        }

        log.debug("starting flow: " + fl.getFlowName());
        RtfContainer rtfContainer = (RtfContainer) rtfRegions.get(fl.getFlowName());
        if (rtfContainer != null) {
            builderContext.pushContainer(rtfContainer);
        } else {
            log.warn("A " + fl.getLocalName() + " has been skipped: " + fl.getFlowName());
        }
    }

    /** {@inheritDoc} */
    public void endFlow(Flow fl) {
        if (bDefer) {
            return;
        }

        RtfContainer rtfContainer = (RtfContainer) rtfRegions.get(fl.getFlowName());
        if (rtfContainer != null) 
        {
            builderContext.popContainer(rtfContainer.getClass(), this);
        }
    }

    /** {@inheritDoc} */
    public void startBlock(Block bl) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr
                = TextAttributesConverter.convertAttributes(bl, rtfFile);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.pushBlockAttributes(rtfAttr);
            textrun.addBookmark(bl.getId());
        } catch (Exception e) {
            log.error("startBlock: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /** {@inheritDoc} */
    public void endBlock(Block bl) {

        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();
            RtfParagraphBreak par = textrun.addParagraphBreak();

            RtfTableCell cellParent = (RtfTableCell)textrun.getParentOfClass(RtfTableCell.class);
            if (cellParent != null && par != null) {
                int iDepth = cellParent.findChildren(textrun);
                cellParent.setLastParagraph(par, iDepth);
            }

            int breakValue = toRtfBreakValue(bl.getBreakAfter());
            textrun.popBlockAttributes(breakValue);
        } catch (Exception e) {
            log.error("startBlock:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void startBlockContainer(BlockContainer blc) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr = TextAttributesConverter
                    .convertBlockContainerAttributes(blc, rtfFile);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            textrun.pushBlockAttributes(rtfAttr);
        } catch (Exception e) {
            log.error("startBlock: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /** {@inheritDoc} */
    public void endBlockContainer(BlockContainer bl) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                        IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();

            textrun.addParagraphBreak();
            int breakValue = toRtfBreakValue(bl.getBreakAfter());
            textrun.popBlockAttributes(breakValue);
        } catch (Exception e) {
            log.error("startBlock:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private int toRtfBreakValue(int foBreakValue) {
        switch (foBreakValue) {
        case Constants.EN_PAGE:
            return RtfTextrun.BREAK_PAGE;
        case Constants.EN_EVEN_PAGE:
            return RtfTextrun.BREAK_EVEN_PAGE;
        case Constants.EN_ODD_PAGE:
            return RtfTextrun.BREAK_ODD_PAGE;
        case Constants.EN_COLUMN:
            return RtfTextrun.BREAK_COLUMN;
        default:
            return RtfTextrun.BREAK_NONE;
        }
    }

    /** {@inheritDoc} */
    public void startTable(Table tbl) {
        if (bDefer) {
            return;
        }

        // create an RtfTable in the current table container
        TableContext tableContext = new TableContext(builderContext);

        try {
            final IRtfTableContainer tc
                = (IRtfTableContainer)builderContext.getContainer(
                        IRtfTableContainer.class, true, null);

            RtfAttributes atts
                = TableAttributesConverter.convertTableAttributes(tbl);

            RtfTable table = tc.newTable(atts, tableContext);
            table.setNestedTableDepth(nestedTableDepth);
            nestedTableDepth++;

            CommonBorderPaddingBackground border = tbl.getCommonBorderPaddingBackground();
            RtfAttributes borderAttributes = new RtfAttributes();

            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.BEFORE,
                    borderAttributes, ITableAttributes.CELL_BORDER_TOP, rtfFile);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.AFTER,
                    borderAttributes, ITableAttributes.CELL_BORDER_BOTTOM, rtfFile);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.START,
                    borderAttributes, ITableAttributes.CELL_BORDER_LEFT, rtfFile);
            BorderAttributesConverter.makeBorder(border, CommonBorderPaddingBackground.END,
                    borderAttributes,  ITableAttributes.CELL_BORDER_RIGHT, rtfFile);

            table.setBorderAttributes(borderAttributes);

            builderContext.pushContainer(table);
        } catch (Exception e) {
            log.error("startTable:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        builderContext.pushTableContext(tableContext);
    }

    /** {@inheritDoc} */
    public void endTable(Table tbl) {
        if (bDefer) {
            return;
        }

        nestedTableDepth--;
        builderContext.popTableContext();
        builderContext.popContainer(RtfTable.class, this);
    }

    /** {@inheritDoc} */
    public void startColumn(TableColumn tc) {
        if (bDefer) {
            return;
        }

        try {
            int iWidth = tc.getColumnWidth().getValue(percentManager);
            percentManager.setDimension(tc, iWidth);

            //convert to twips
            Float width = new Float(FoUnitsConverter.getInstance().convertMptToTwips(iWidth));
            builderContext.getTableContext().setNextColumnWidth(width);
            builderContext.getTableContext().setNextColumnRowSpanning(
                  new Integer(0), null);
            builderContext.getTableContext().setNextFirstSpanningCol(false);
        } catch (Exception e) {
            log.error("startColumn: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endColumn(TableColumn tc) {
    }

    /** {@inheritDoc} */
    public void startHeader(TableHeader header) {
        builderContext.pushPart(header);
        startPart(header);
    }

    /** {@inheritDoc} */
    public void endHeader(TableHeader header) {
        builderContext.popPart(header.getClass(), this);
        endPart(header);
    }

    /** {@inheritDoc} */
    public void startFooter(TableFooter footer) {
        builderContext.pushPart(footer);
        startPart(footer);
    }

    /** {@inheritDoc} */
    public void endFooter(TableFooter footer) {
        builderContext.popPart(footer.getClass(), this);
        endPart(footer);
    }

    /** {@inheritDoc} */
    public void startInline(Inline inl) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr = TextAttributesConverter
                    .convertCharacterAttributes(inl, rtfFile);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                        IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.pushInlineAttributes(rtfAttr);
            textrun.addBookmark(inl.getId());
        } catch (FOPException fe) {
            log.error("startInline:" + fe.getMessage());
            throw new RuntimeException(fe.getMessage());
        } catch (Exception e) {
            log.error("startInline:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endInline(Inline inl) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                        IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.popInlineAttributes();
        } catch (Exception e) {
            log.error("startInline:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void startPart(TablePart part) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes atts = TableAttributesConverter.convertTablePartAttributes(part);
            RtfTable tbl = (RtfTable)builderContext.getContainer(
                    RtfTable.class, true, this);
            tbl.setHeaderAttribs(atts);
        } catch (Exception e) {
            log.error("startPart: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void endPart(TablePart tb) {
        if (bDefer) {
            return;
        }

        try {
            RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class, true, this);
            tbl.setHeaderAttribs(null);
        } catch (Exception e) {
            log.error("endPart: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


     /**
     * {@inheritDoc}
     */
    public void startBody(TableBody body) {
        builderContext.pushPart(body);
        startPart(body);
    }

    /** {@inheritDoc} */
    public void endBody(TableBody body) {
        builderContext.popPart(TableBody.class, this);
        endPart(body);
    }

    /**
     * {@inheritDoc}
     */
    public void startRow(TableRow tr) {
        if (bDefer) {
            return;
        }

        try {
            // create an RtfTableRow in the current RtfTable
            final RtfTable tbl = (RtfTable)builderContext.getContainer(RtfTable.class,
                    true, null);

            RtfAttributes atts = TableAttributesConverter.convertRowAttributes(tr,
                    tbl.getHeaderAttribs(), rtfFile);

            if (tr.getParent() instanceof TableHeader) {
                atts.set(ITableAttributes.ATTR_HEADER);
            }

            builderContext.pushContainer(tbl.newTableRow(atts));

            // reset column iteration index to correctly access column widths
            builderContext.getTableContext().selectFirstColumn();
        } catch (Exception e) {
            log.error("startRow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endRow(TableRow tr) {
        if (bDefer) {
            return;
        }

        try {
            TableContext tctx = builderContext.getTableContext();
            final RtfTableRow row = (RtfTableRow)builderContext.getContainer(RtfTableRow.class,
                    true, null);

            //while the current column is in row-spanning, act as if
            //a vertical merged cell would have been specified.
            while (tctx.getNumberOfColumns() > tctx.getColumnIndex()
                  && tctx.getColumnRowSpanningNumber().intValue() > 0) {
                RtfTableCell vCell = row.newTableCellMergedVertically(
                        (int)tctx.getColumnWidth(),
                        tctx.getColumnRowSpanningAttrs());

                if (!tctx.getFirstSpanningCol()) {
                    vCell.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
                }

                tctx.selectNextColumn();
            }
        } catch (Exception e) {
            log.error("endRow: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        builderContext.popContainer(RtfTableRow.class, this);
        builderContext.getTableContext().decreaseRowSpannings();
    }

    /** {@inheritDoc} */
    public void startCell(TableCell tc) {
        if (bDefer) {
            return;
        }

        try {
            TableContext tctx = builderContext.getTableContext();
            final RtfTableRow row = (RtfTableRow)builderContext.getContainer(RtfTableRow.class,
                    true, null);

            int numberRowsSpanned = tc.getNumberRowsSpanned();
            int numberColumnsSpanned = tc.getNumberColumnsSpanned();

            //while the current column is in row-spanning, act as if
            //a vertical merged cell would have been specified.
            while (tctx.getNumberOfColumns() > tctx.getColumnIndex()
                  && tctx.getColumnRowSpanningNumber().intValue() > 0) {
                RtfTableCell vCell = row.newTableCellMergedVertically(
                        (int)tctx.getColumnWidth(),
                        tctx.getColumnRowSpanningAttrs());

                if (!tctx.getFirstSpanningCol()) {
                    vCell.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
                }

                tctx.selectNextColumn();
            }

            //get the width of the currently started cell
            float width = tctx.getColumnWidth();

            // create an RtfTableCell in the current RtfTableRow
            RtfAttributes atts = TableAttributesConverter
                    .convertCellAttributes(tc, rtfFile);
            RtfTableCell cell = row.newTableCell((int)width, atts);

            //process number-rows-spanned attribute
            if (numberRowsSpanned > 1) {
                // Start vertical merge
                cell.setVMerge(RtfTableCell.MERGE_START);

                // set the number of rows spanned
                tctx.setCurrentColumnRowSpanning(new Integer(numberRowsSpanned),
                        cell.getRtfAttributes());
            } else {
                tctx.setCurrentColumnRowSpanning(
                        new Integer(numberRowsSpanned), null);
            }

            //process number-columns-spanned attribute
            if (numberColumnsSpanned > 0) {
                // Get the number of columns spanned
                tctx.setCurrentFirstSpanningCol(true);

                // We widthdraw one cell because the first cell is already created
                // (it's the current cell) !
                 for (int i = 0; i < numberColumnsSpanned - 1; ++i) {
                    tctx.selectNextColumn();

                    //aggregate width for further elements
                    width += tctx.getColumnWidth();
                    tctx.setCurrentFirstSpanningCol(false);
                    RtfTableCell hCell = row.newTableCellMergedHorizontally(
                            0, null);

                    if (numberRowsSpanned > 1) {
                        // Start vertical merge
                        hCell.setVMerge(RtfTableCell.MERGE_START);

                        // set the number of rows spanned
                        tctx.setCurrentColumnRowSpanning(
                                new Integer(numberRowsSpanned),
                                cell.getRtfAttributes());
                    } else {
                        tctx.setCurrentColumnRowSpanning(
                                new Integer(numberRowsSpanned), cell.getRtfAttributes());
                    }
                }
            }
            //save width of the cell, convert from twips to mpt
            percentManager.setDimension(tc, (int)width * 50);

            builderContext.pushContainer(cell);
        } catch (Exception e) {
            log.error("startCell: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endCell(TableCell tc) {
        if (bDefer) {
            return;
        }
        try {
            RtfTableCell cell = (RtfTableCell)builderContext.getContainer(RtfTableCell.class, false, this);
            cell.finish();
        } catch (Exception e) {
            log.error("endCell: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        builderContext.popContainer(RtfTableCell.class, this);
        builderContext.getTableContext().selectNextColumn();
    }

    // Lists
    /** {@inheritDoc} */
    public void startList(ListBlock lb) {
        if (bDefer) {
            return;
        }

        try  {
            // create an RtfList in the current list container
            final IRtfListContainer c
                = (IRtfListContainer)builderContext.getContainer(
                    IRtfListContainer.class, true, this);
            final RtfList newList = c.newList(
                ListAttributesConverter.convertAttributes(lb));
            builderContext.pushContainer(newList);
        } catch (FOPException fe) {
            log.error("startList: " + fe.getMessage());
            throw new RuntimeException(fe.getMessage());
        } catch (Exception e) {
            log.error("startList: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endList(ListBlock lb) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer(RtfList.class, this);
    }

    /** {@inheritDoc} */
    public void startListItem(ListItem li) {
        if (bDefer) {
            return;
        }

        // create an RtfListItem in the current RtfList
        try {
            RtfList list = (RtfList)builderContext.getContainer(
                    RtfList.class, true, this);

            /**
             * If the current list already contains a list item, then close the
             * list and open a new one, so every single list item gets its own
             * list. This allows every item to have a different list label.
             * If all the items would be in the same list, they had all the
             * same label.
             */
            //TODO: do this only, if the labels content <> previous labels content
            if (list.getChildCount() > 0) {
                this.endListBody(null);
                this.endList((ListBlock) li.getParent());
                this.startList((ListBlock) li.getParent());
                this.startListBody(null);

                list = (RtfList)builderContext.getContainer(
                        RtfList.class, true, this);
            }

            builderContext.pushContainer(list.newListItem());
        } catch (Exception e) {
            log.error("startList: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endListItem(ListItem li) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer(RtfListItem.class, this);
    }

    /** {@inheritDoc} */
    public void startListLabel(ListItemLabel listItemLabel) {
        if (bDefer) {
            return;
        }

        try {
            RtfListItem item
                = (RtfListItem)builderContext.getContainer(RtfListItem.class, true, this);

            RtfListItemLabel label = item.new RtfListItemLabel(item);
            builderContext.pushContainer(label);
        } catch (Exception e) {
            log.error("startPageNumber: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endListLabel(ListItemLabel listItemLabel) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer(RtfListItemLabel.class, this);
    }

    /** {@inheritDoc} */
    public void startListBody(ListItemBody listItemBody) {
    }

    /** {@inheritDoc} */
    public void endListBody(ListItemBody listItemBody) {
    }

    // Static Regions
    /** {@inheritDoc} */
    public void startStatic(StaticContent staticContent) {
    }

    /** {@inheritDoc} */
    public void endStatic(StaticContent statisContent) {
    }

    /** {@inheritDoc} */
    public void startMarkup() {
    }

    /** {@inheritDoc} */
    public void endMarkup() {
    }

    /** {@inheritDoc} */
    public void startLink(BasicLink basicLink) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();

            RtfHyperLink link = textrun.addHyperlink(new RtfAttributes());

            if (basicLink.hasExternalDestination()) {
                link.setExternalURL(basicLink.getExternalDestination());
            } else {
                link.setInternalURL(basicLink.getInternalDestination());
            }

            builderContext.pushContainer(link);
        } catch (Exception e) {
            log.error("startLink: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endLink(BasicLink basicLink) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer(RtfHyperLink.class, this);
    }

    /** {@inheritDoc} */
    public void image(ExternalGraphic eg) {
        if (bDefer) {
            return;
        }

        String uri = eg.getURL();
        ImageInfo info = null;
        try {

            //set image data
            FOUserAgent userAgent = eg.getUserAgent();
            ImageManager manager = userAgent.getImageManager();
            info = manager.getImageInfo(uri, userAgent.getImageSessionContext());

            putGraphic(eg, info);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, uri, ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    /** {@inheritDoc} */
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
        if (bDefer) {
            return;
        }

        try {
            XMLObj child = ifo.getChildXMLObj();
            Document doc = child.getDOMDocument();
            String ns = child.getNamespaceURI();

            ImageInfo info = new ImageInfo(null, null);
            // Set the resolution to that of the FOUserAgent
            FOUserAgent ua = ifo.getUserAgent();
            ImageSize size = new ImageSize();
            size.setResolution(ua.getSourceResolution());

            // Set the image size to the size of the svg.
            Point2D csize = new Point2D.Float(-1, -1);
            Point2D intrinsicDimensions = child.getDimension(csize);
            if (intrinsicDimensions == null) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.ifoNoIntrinsicSize(this, child.getLocator());
                return;
            }
            size.setSizeInMillipoints(
                    (int)Math.round(intrinsicDimensions.getX() * 1000),
                    (int)Math.round(intrinsicDimensions.getY() * 1000));
            size.calcPixelsFromSize();
            info.setSize(size);

            ImageXMLDOM image = new ImageXMLDOM(info, doc, ns);

            FOUserAgent userAgent = ifo.getUserAgent();
            ImageManager manager = userAgent.getImageManager();
            Map hints = ImageUtil.getDefaultHints(ua.getImageSessionContext());
            Image converted = manager.convertImage(image, FLAVORS, hints);
            putGraphic(ifo, converted);

        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, null, ie, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, null, ioe, null);
        }
    }

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_EMF, ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
    };

    /**
     * Puts a graphic/image into the generated RTF file.
     * @param abstractGraphic the graphic (external-graphic or instream-foreign-object)
     * @param info the image info object
     * @throws IOException In case of an I/O error
     */
    private void putGraphic(AbstractGraphics abstractGraphic, ImageInfo info)
            throws IOException {
        try {
            FOUserAgent userAgent = abstractGraphic.getUserAgent();
            ImageManager manager = userAgent.getImageManager();
            ImageSessionContext sessionContext = userAgent.getImageSessionContext();
            Map hints = ImageUtil.getDefaultHints(sessionContext);
            Image image = manager.getImage(info, FLAVORS, hints, sessionContext);

            putGraphic(abstractGraphic, image);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, null, ie, null);
        }
    }

    /**
     * Puts a graphic/image into the generated RTF file.
     * @param abstractGraphic the graphic (external-graphic or instream-foreign-object)
     * @param image the image
     * @throws IOException In case of an I/O error
     */
    private void putGraphic(AbstractGraphics abstractGraphic, Image image)
            throws IOException {
        byte[] rawData = null;

        final ImageInfo info = image.getInfo();

        if (image instanceof ImageRawStream) {
            ImageRawStream rawImage = (ImageRawStream)image;
            InputStream in = rawImage.createInputStream();
            try {
                rawData = IOUtils.toByteArray(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        if (rawData == null) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, null);
            return;
        }

        //Set up percentage calculations
        this.percentManager.setDimension(abstractGraphic);
        PercentBaseContext pContext = new PercentBaseContext() {

            public int getBaseLength(int lengthBase, FObj fobj) {
                switch (lengthBase) {
                case LengthBase.IMAGE_INTRINSIC_WIDTH:
                    return info.getSize().getWidthMpt();
                case LengthBase.IMAGE_INTRINSIC_HEIGHT:
                    return info.getSize().getHeightMpt();
                default:
                    return percentManager.getBaseLength(lengthBase, fobj);
                }
            }

        };
        ImageLayout layout = new ImageLayout(abstractGraphic, pContext,
                image.getInfo().getSize().getDimensionMpt());

        final IRtfTextrunContainer c
            = (IRtfTextrunContainer)builderContext.getContainer(
                IRtfTextrunContainer.class, true, this);

        final RtfExternalGraphic rtfGraphic = c.getTextrun().newImage();

        //set URL
        if (info.getOriginalURI() != null) {
            rtfGraphic.setURL(info.getOriginalURI());
        }
        rtfGraphic.setImageData(rawData);

        FoUnitsConverter converter = FoUnitsConverter.getInstance();
        Dimension viewport = layout.getViewportSize();
        Rectangle placement = layout.getPlacement();
        int cropLeft = Math.round(converter.convertMptToTwips(-placement.x));
        int cropTop = Math.round(converter.convertMptToTwips(-placement.y));
        int cropRight = Math.round(converter.convertMptToTwips(
                -1 * (viewport.width - placement.x - placement.width)));
        int cropBottom = Math.round(converter.convertMptToTwips(
                -1 * (viewport.height - placement.y - placement.height)));
        rtfGraphic.setCropping(cropLeft, cropTop, cropRight, cropBottom);

        int width = Math.round(converter.convertMptToTwips(viewport.width));
        int height = Math.round(converter.convertMptToTwips(viewport.height));
        width += cropLeft + cropRight;
        height += cropTop + cropBottom;
        rtfGraphic.setWidthTwips(width);
        rtfGraphic.setHeightTwips(height);

        //TODO: make this configurable:
        //      int compression = m_context.m_options.getRtfExternalGraphicCompressionRate ();
        int compression = 0;
        if (compression != 0) {
            if (!rtfGraphic.setCompressionRate(compression)) {
                log.warn("The compression rate " + compression
                    + " is invalid. The value has to be between 1 and 100 %.");
            }
        }
    }

    /** {@inheritDoc} */
    public void pageRef() {
    }

    /** {@inheritDoc} */
    public void startFootnote(Footnote footnote) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class,
                    true, this);

            RtfTextrun textrun = container.getTextrun();
            RtfFootnote rtfFootnote = textrun.addFootnote();

            builderContext.pushContainer(rtfFootnote);
        } catch (Exception e) {
            log.error("startFootnote: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /** {@inheritDoc} */
    public void endFootnote(Footnote footnote) {
        if (bDefer) {
            return;
        }

        builderContext.popContainer(RtfFootnote.class, this);
    }

    /** {@inheritDoc} */
    public void startFootnoteBody(FootnoteBody body) {
        if (bDefer) {
            return;
        }

        try {
            RtfFootnote rtfFootnote
                = (RtfFootnote)builderContext.getContainer(
                    RtfFootnote.class,
                    true, this);

            rtfFootnote.startBody();
        } catch (Exception e) {
            log.error("startFootnoteBody: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /** {@inheritDoc} */
    public void endFootnoteBody(FootnoteBody body) {
        if (bDefer) {
            return;
        }

        try {
            RtfFootnote rtfFootnote
                = (RtfFootnote)builderContext.getContainer(
                    RtfFootnote.class,
                    true, this);

            rtfFootnote.endBody();
        } catch (Exception e) {
            log.error("endFootnoteBody: " + e.getMessage());
            throw new RuntimeException("Exception: " + e);
        }
    }

    /** {@inheritDoc} */
    public void startLeader(Leader l) {
        if (bDefer) {
            return;
        }

        try {
            percentManager.setDimension(l);
            RtfAttributes rtfAttr = TextAttributesConverter
                    .convertLeaderAttributes(l, percentManager, rtfFile);

            IRtfTextrunContainer container = 
                    (IRtfTextrunContainer)builderContext.getContainer(
                            IRtfTextrunContainer.class, true, this);
            RtfTextrun textrun = container.getTextrun();

            textrun.addLeader(rtfAttr);
        } catch (RtfException e) {
            log.error("startLeader: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (FOPException e) {
            log.error("startLeader: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @param text FOText object
     * @param characters CharSequence of the characters to process.
     */
    public void text(FOText text, CharSequence characters) {
        if (bDefer) {
            return;
        }

        try {
            IRtfTextrunContainer container
                    = (IRtfTextrunContainer)builderContext.getContainer(
                            IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            RtfAttributes rtfAttr = TextAttributesConverter
                    .convertCharacterAttributes(text, rtfFile);

            textrun.pushInlineAttributes(rtfAttr);
            textrun.addString(characters.toString());
            textrun.popInlineAttributes();
        } catch (Exception e) {
            log.error("characters:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void startPageNumber(PageNumber pagenum) {
        if (bDefer) {
            return;
        }

        try {
            RtfAttributes rtfAttr = TextAttributesConverter
                    .convertCharacterAttributes(pagenum, rtfFile);

            IRtfTextrunContainer container
                = (IRtfTextrunContainer)builderContext.getContainer(
                    IRtfTextrunContainer.class, true, this);

            RtfTextrun textrun = container.getTextrun();
            textrun.addPageNumber(rtfAttr);
        } catch (Exception e) {
            log.error("startPageNumber: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void endPageNumber(PageNumber pagenum) {
        if (bDefer) {
            return;
        }
    }

    /** {@inheritDoc} */
    public void startPageNumberCitation(PageNumberCitation l) {
        if (bDefer) {
            return;
        }
        try {

            IRtfTextrunContainer container
                    = (IRtfTextrunContainer)builderContext.getContainer(
                            IRtfTextrunContainer.class, true, this);
            RtfTextrun textrun = container.getTextrun();

            textrun.addPageNumberCitation(l.getRefId());
        } catch (Exception e) {
            log.error("startPageNumberCitation: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void startPageNumberCitationLast(PageNumberCitationLast l) {
        if (bDefer) {
            return;
        }
        try {

            IRtfTextrunContainer container
                    = (IRtfTextrunContainer)builderContext.getContainer(
                            IRtfTextrunContainer.class, true, this);
            RtfTextrun textrun = container.getTextrun();

            textrun.addPageNumberCitation(l.getRefId());
        } catch (RtfException e) {
            log.error("startPageNumberCitationLast: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void prepareTable(Table tab) {
        // Allows to receive the available width of the table
        percentManager.setDimension(tab);

        // Table gets expanded by half of the border on each side inside Word
        // When using wide borders the table gets cut off
        int tabDiff = tab.getCommonBorderPaddingBackground().getBorderStartWidth(false) / 2
                + tab.getCommonBorderPaddingBackground().getBorderEndWidth(false);

        // check for "auto" value
        if (!(tab.getInlineProgressionDimension().getMaximum(null).getLength()
                    instanceof EnumLength)) {
            // value specified
            percentManager.setDimension(tab,
                    tab.getInlineProgressionDimension().getMaximum(null)
                        .getLength().getValue(percentManager)
                    - tabDiff);
        } else {
            // set table width again without border width
            percentManager.setDimension(tab, percentManager.getBaseLength(
                    LengthBase.CONTAINING_BLOCK_WIDTH, tab) - tabDiff);
        }

        ColumnSetup columnSetup = new ColumnSetup(tab);
        //int sumOfColumns = columnSetup.getSumOfColumnWidths(percentManager);
        float tableWidth = percentManager.getBaseLength(LengthBase.CONTAINING_BLOCK_WIDTH, tab);
        float tableUnit = columnSetup.computeTableUnit(percentManager, Math.round(tableWidth));
        percentManager.setTableUnit(tab, Math.round(tableUnit));
    }

    /**
     * Calls the appropriate event handler for the passed FObj.
     *
     * @param foNode FO node whose event is to be called
     * @param bStart TRUE calls the start handler, FALSE the end handler
     */
    private void invokeDeferredEvent(FONode foNode, boolean bStart) {
        if (foNode instanceof PageSequence) {
            if (bStart) {
                startPageSequence((PageSequence) foNode);
            } else {
                endPageSequence((PageSequence) foNode);
            }
        } else if (foNode instanceof Flow) {
            if (bStart) {
                startFlow((Flow) foNode);
            } else {
                endFlow((Flow) foNode);
            }
        } else if (foNode instanceof StaticContent) {
            if (bStart) {
                startStatic(null);
            } else {
                endStatic(null);
            }
        } else if (foNode instanceof ExternalGraphic) {
            if (bStart) {
                image((ExternalGraphic) foNode);
            }
        } else if (foNode instanceof InstreamForeignObject) {
            if (bStart) {
                endInstreamForeignObject((InstreamForeignObject) foNode);
            }
        } else if (foNode instanceof Block) {
            if (bStart) {
                startBlock((Block) foNode);
            } else {
                endBlock((Block) foNode);
            }
        } else if (foNode instanceof BlockContainer) {
            if (bStart) {
                startBlockContainer((BlockContainer) foNode);
            } else {
                endBlockContainer((BlockContainer) foNode);
            }
        } else if (foNode instanceof BasicLink) {
            //BasicLink must be placed before Inline
            if (bStart) {
                startLink((BasicLink) foNode);
            } else {
                endLink(null);
            }
        } else if (foNode instanceof Inline) {
            if (bStart) {
                startInline((Inline) foNode);
            } else {
                endInline((Inline) foNode);
            }
        } else if (foNode instanceof FOText) {
            if (bStart) {
                FOText text = (FOText) foNode;
                text(text, text.getCharSequence());
            }
        } else if (foNode instanceof Character) {
            if (bStart) {
                Character c = (Character) foNode;
                character(c);
            }
        } else if (foNode instanceof PageNumber) {
            if (bStart) {
                startPageNumber((PageNumber) foNode);
            } else {
                endPageNumber((PageNumber) foNode);
            }
        } else if (foNode instanceof Footnote) {
            if (bStart) {
                startFootnote((Footnote) foNode);
            } else {
                endFootnote((Footnote) foNode);
            }
        } else if (foNode instanceof FootnoteBody) {
            if (bStart) {
                startFootnoteBody((FootnoteBody) foNode);
            } else {
                endFootnoteBody((FootnoteBody) foNode);
            }
        } else if (foNode instanceof ListBlock) {
            if (bStart) {
                startList((ListBlock) foNode);
            } else {
                endList((ListBlock) foNode);
            }
        } else if (foNode instanceof ListItemBody) {
            if (bStart) {
                startListBody(null);
            } else {
                endListBody(null);
            }
        } else if (foNode instanceof ListItem) {
            if (bStart) {
                startListItem((ListItem) foNode);
            } else {
                endListItem((ListItem) foNode);
            }
        } else if (foNode instanceof ListItemLabel) {
            if (bStart) {
                startListLabel(null);
            } else {
                endListLabel(null);
            }
        } else if (foNode instanceof Table) {
            if (bStart) {
                startTable((Table) foNode);
            } else {
                endTable((Table) foNode);
            }
        } else if (foNode instanceof TableHeader) {
            if (bStart) {
                startHeader((TableHeader) foNode);
            } else {
                endHeader((TableHeader) foNode);
            }
        } else if (foNode instanceof TableFooter) {
            if (bStart) {
                startFooter((TableFooter) foNode);
            } else {
                endFooter((TableFooter) foNode);
            }
        } else if (foNode instanceof TableBody) {
            if (bStart) {
                startBody((TableBody) foNode);
            } else {
                endBody((TableBody) foNode);
            }
        } else if (foNode instanceof TableColumn) {
            if (bStart) {
                startColumn((TableColumn) foNode);
            } else {
                endColumn((TableColumn) foNode);
            }
        } else if (foNode instanceof TableRow) {
            if (bStart) {
                startRow((TableRow) foNode);
            } else {
                endRow((TableRow) foNode);
            }
        } else if (foNode instanceof TableCell) {
            if (bStart) {
                startCell((TableCell) foNode);
            } else {
                endCell((TableCell) foNode);
            }
        } else if (foNode instanceof Leader) {
            if (bStart) {
                startLeader((Leader) foNode);
            }
        } else if (foNode instanceof PageNumberCitation) {
            if (bStart) {
                startPageNumberCitation((PageNumberCitation) foNode);
            } else {
                endPageNumberCitation((PageNumberCitation) foNode);
            }
        } else if (foNode instanceof PageNumberCitationLast) {
            if (bStart) {
                startPageNumberCitationLast((PageNumberCitationLast) foNode);
            } else {
                endPageNumberCitationLast((PageNumberCitationLast) foNode);
            }
        } else {
            RTFEventProducer eventProducer = RTFEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.ignoredDeferredEvent(this, foNode, bStart, foNode.getLocator());
        }
    }

    /**
     * Closes any mismatched tags that are detected in the RTF structure.
     * @param containerClass The class representing the tag to close.
     * @return Determines whether the tag mismatch has been handled.
     */
    public boolean endContainer(Class containerClass) {
        if (containerClass == RtfTableRow.class) {
            endRow(null);
            return true;
        }
        return false;
    }

    /**
     * Calls the event handlers for the passed FONode and all its elements.
     *
     * @param foNode FONode object which shall be recursed
     */
    private void recurseFONode(FONode foNode) {
        invokeDeferredEvent(foNode, true);

        if (foNode instanceof PageSequence) {
            PageSequence pageSequence = (PageSequence) foNode;

            Flow mainFlow = pageSequence.getMainFlow();
            for (Iterator it = rtfRegions.keySet().iterator(); it.hasNext();)
            {
                String regionName = (String)it.next();
                if (!regionName.equals(mainFlow.getFlowName()))
                {
                    FONode staticNode = pageSequence.getFlowMap().get(regionName);
                    recurseFONode(staticNode);
                }
            }

            recurseFONode(mainFlow);
        } else if (foNode instanceof Table) {
            Table table = (Table) foNode;

            //recurse all table-columns
            if (table.getColumns() != null) {
                //Calculation for column-widths which are not set
                prepareTable(table);

                for (Iterator it = table.getColumns().iterator(); it.hasNext();) {
                    recurseFONode((FONode) it.next());
                }
            } else {
                //TODO Implement implicit column setup handling!
                RTFEventProducer eventProducer = RTFEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.explicitTableColumnsRequired(this, table.getLocator());
            }

            //recurse table-header
            if (table.getTableHeader() != null) {
                recurseFONode(table.getTableHeader());
            }

            //recurse table-footer
            if (table.getTableFooter() != null) {
                recurseFONode(table.getTableFooter());
            }

            if (foNode.getChildNodes() != null) {
                for (Iterator it = foNode.getChildNodes(); it.hasNext();) {
                    recurseFONode((FONode) it.next());
                }
            }
        } else if (foNode instanceof ListItem) {
            ListItem item = (ListItem) foNode;

            recurseFONode(item.getLabel());
            recurseFONode(item.getBody());
        } else if (foNode instanceof Footnote) {
            Footnote fn = (Footnote)foNode;

            recurseFONode(fn.getFootnoteCitation());
            recurseFONode(fn.getFootnoteBody());
        } else {
            //Any other FO-Object: Simply recurse through all childNodes.
            if (foNode.getChildNodes() != null) {
                for (Iterator it = foNode.getChildNodes(); it.hasNext();) {
                    FONode fn = (FONode)it.next();
                    if (log.isTraceEnabled()) {
                        log.trace("  ChildNode for " + fn + " (" + fn.getName() + ")");
                    }
                    recurseFONode(fn);
                }
            }
        }

        invokeDeferredEvent(foNode, false);
    }
}
