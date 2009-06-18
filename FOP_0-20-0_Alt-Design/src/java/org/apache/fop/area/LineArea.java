/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 28/05/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.font.TextMeasurer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.Node;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fonts.FontException;

/**
 * <code>LineArea</code>s are <code>BlockArea</code>s constructed
 * automatically from a series of <code>InlineArea</code>s.  They are
 * stacked within the area generated by the block parent of the sequence
 * of inlines; e.g. <code>fo:block</code> containing <code>PCDATA</code>.
 * <p>In addition to normal dimensions, a <code>LineArea</code> has a
 * <i>leading</i>, or baseline-to-baseline spacing, an <i>ascent</i> and a
 * <i>descent</i>, both relative to its baseline.  These are available
 * through the <code>TextLayout</code> object.  The impact of a new
 * <code>LineArea</code> on the <code>block-progression-dimension</code>
 * is illustrated below.
 * <img src="doc-files/LineAreas.png">
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class LineArea extends BlockArea {
    
    private String text;

    /**
     * @param text the text of the generating Flow Object
     * @param pageSeq the ancestor page-sequence
     * @param generatedBy the generating Flow Object
     * @param parent the parent <code>Area</code> of this <code>Area</code>
     * @param sync the synchronization object of this node
     */
    public LineArea(String text, FoPageSequence pageSeq,
            FOPageSeqNode generatedBy, Node parent, Object sync) 
    throws PropertyException, FOPException {
        super(pageSeq, generatedBy, parent, sync);
        generator = generatedBy;
        this.text = text;
        preprocessText();
    }

    /** <code>generatedBy</code> as an <code>FOPageSeqNode</code> */
    protected FOPageSeqNode generator;
    /** The <code>Map</code> of <code>Font</code> attributes. */
    private Map attributes;
    /**
     * Gets the <code>Map</code> of <code>Font</code> attributes
     * @return the map
     */
    public Map getAttributes() {
        return attributes;
    }
    /** The font for this text. */
    private Font font;
    /**
     * Gets the <code>Font</code>for this text
     * @return the font
     */
    public Font getFont() {
        return font;
    }
    /** The <code>PCDATA</code> in an <code>AttributedString</code> */
    private AttributedString attText;
    /**
     * Gets the attributed text
     * @return the attributed text
     */
    public AttributedString getAttributedText() {
        return attText;
    }
    /** The <code>TextMeasurer</code> generated from the text */
    private TextMeasurer measurer;
    /**
     * Gets the <code>TextMeasurer</code> generated from the text
     * @return the measurer
     */
    public TextMeasurer getMeasurer() {
        return measurer;
    }
    /** The <code>TextLayout</code> generated from the text */
    private TextLayout layout;
    /**
     * Gets the <code>TextLayout</code> generated from the text
     * @return the layout
     */
    public TextLayout getLayout() {
        return layout;
    }
    public static final boolean IS_ANTI_ALIASED = true;
    public static final boolean USES_FRACTIONAL_METRICS = true;
    // PCDATA provides sequences of inline-areas to fill line-areas in the
    // parent block area.
    // Generate a text-layout for the PCDATA.
    /**
     * Generates a TextMeasurer from the PCDATA text.  The font and text
     * attributes of the text are applied.
     */
    private void preprocessText() throws PropertyException, FontException {
        setupMeasurement();
        // Find minima and maxima for this text
        // Text dimensions based on baseline-to-baseline leading and the
        // descent of the TextLayout
        // Maximum BPDim for text is dependent on the descent of the
        // previous line.  (See diagram in class description).  As an
        // approximation, the larger of the leading and ascent + descent is
        // used.
        // The maximum IPDim is the advance of the complete text.
        // To determine the minima, the shortest length of text is determined,
        // a TextLayout is formed from that, and the corresponding BPDim and
        // IPDim values are determined.
        pageSpaceRange.setIPDimMax(layout.getVisibleAdvance());
        pageSpaceRange.setBPDimMax(Math.max(
                    layout.getLeading(),
                    (layout.getAscent() + layout.getDescent())));
        // Find the longest fragment of the text
        BreakIterator words =
            BreakIterator.getWordInstance(generatedBy.getLocale());
        words.setText(text);
        int begin = 0;
        float maxWordWidth = 0;
        int boundary = 0;
        while ((boundary = words.next()) != BreakIterator.DONE) {
            float width = measurer.getAdvanceBetween(begin, boundary);
            maxWordWidth = Math.max(maxWordWidth, width);
            begin = boundary;
        }
        pageSpaceRange.setIPDimMin(maxWordWidth);
        // For now, set bPDimMin = bPDimMax.
        pageSpaceRange.setBPDimMin(maxWordWidth);
    }

    private void setupMeasurement() throws PropertyException, FontException {
        // Get the font, size, style and weight attributes
        attributes = generator.getFontAttributes();
        font = generator.getFopFont(attributes);
        TextDecorations decorations = generator.getDecorations();
        // Add the text decorations
        // TODO separate color support for text decorations
        if (decorations.underlined()) {
            attributes.put(TextAttribute.UNDERLINE,
                    TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        }
        if (decorations.overlined()) {
            // Not supported yet
        }
        if (decorations.struckthrough()) {
            attributes.put(TextAttribute.STRIKETHROUGH,
                    TextAttribute.STRIKETHROUGH_ON);
        }
        attText = new AttributedString(text, attributes);
        AttributedCharacterIterator iter = attText.getIterator();
        FontRenderContext identityFRC =
            new FontRenderContext(
                    null, IS_ANTI_ALIASED, USES_FRACTIONAL_METRICS);
        measurer = new TextMeasurer(iter, identityFRC);
        layout = new TextLayout(iter, identityFRC);
        
    }

}