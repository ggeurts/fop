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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.CharUtilities;

import java.util.List;
import java.util.LinkedList;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.inline.AlignmentContext;

import org.axsl.fontR.Font;
import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontUse;

/**
 * LayoutManager for the fo:character formatting object
 */
public class CharacterLayoutManager extends LeafNodeLayoutManager {
    private Character fobj;
    private MinOptMax letterSpaceIPD;
    private int hyphIPD;
    private FontUse fontUse;
    private FontConsumer fontConsumer;
    /** Font size in millipoints */
    private int fontSize;
    private CommonBorderPaddingBackground borderProps = null;

    /**
     * Constructor
     *
     * @param node the fo:character formatting object
     */
    public CharacterLayoutManager(Character node) {
        // @todo better null checking of node
        super(node);
        fobj = node;
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize */
    public void initialize() {
        fontConsumer = fobj.getFOEventHandler().getFontConsumer();
        CommonFont commonFont = fobj.getCommonFont();
        fontUse = commonFont.getFontState(fontConsumer, this);
        fontSize = commonFont.getFontSize(this);
        SpaceVal ls = SpaceVal.makeLetterSpacing(fobj.getLetterSpacing());
        letterSpaceIPD = ls.getSpace();
        hyphIPD = fontUse.getFont().width(fobj.getCommonHyphenation().hyphenationCharacter,
                           fontSize);
        borderProps = fobj.getCommonBorderPaddingBackground();
        setCommonBorderPaddingBackground(borderProps);
        org.apache.fop.area.inline.TextArea chArea = getCharacterInlineArea(fobj);
        chArea.setBaselineOffset(fontUse.getFont().getAscender(fontSize));
        setCurrentArea(chArea);
    }

    private org.apache.fop.area.inline.TextArea getCharacterInlineArea(Character node) {
        org.apache.fop.area.inline.TextArea text 
            = new org.apache.fop.area.inline.TextArea();
        char ch = node.getCharacter();
        if (CharUtilities.isAnySpace(ch)) {
            text.addSpace(ch, 0, CharUtilities.isAdjustableSpace(ch));
        } else {
            text.addWord(String.valueOf(ch), 0);
        }
        TraitSetter.setProducerID(text, node.getId());
        TraitSetter.addTextDecoration(text, fobj.getTextDecoration());
        return text;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(LayoutContext, int) */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        MinOptMax ipd;
        curArea = get(context);
        KnuthSequence seq = new InlineKnuthSequence();

        if (curArea == null) {
            setFinished(true);
            return null;
        }

        Font font = fontUse.getFont();
        char curChar = fobj.getCharacter();
        ipd = new MinOptMax(font.width(curChar, fontSize));
        fontUse.registerCharUsed(curChar);

        curArea.setIPD(ipd.opt);
        curArea.setBPD(font.getAscender(fontSize) - font.getDescender(fontSize));

        TraitSetter.addFontTraits(curArea, fontUse, fontSize);
        curArea.addTrait(Trait.COLOR, fobj.getColor());

        // TODO: may need some special handling for fo:character
        alignmentContext = new AlignmentContext(font
                                    , fontSize
                                    , fontSize
                                    , fobj.getAlignmentAdjust()
                                    , fobj.getAlignmentBaseline()
                                    , fobj.getBaselineShift()
                                    , fobj.getDominantBaseline()
                                    , context.getAlignmentContext());

        addKnuthElementsForBorderPaddingStart(seq);
        
        // create the AreaInfo object to store the computed values
        areaInfo = new AreaInfo((short) 0, ipd, false, alignmentContext);

        // node is a fo:Character
        if (letterSpaceIPD.min == letterSpaceIPD.max) {
            // constant letter space, only return a box
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
        } else {
            // adjustable letter space, return a sequence of elements;
            // at the moment the character is supposed to have no letter spaces,
            // but returning this sequence allows us to change only one element
            // if addALetterSpaceTo() is called
            seq.add(new KnuthInlineBox(areaInfo.ipdArea.opt, areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            seq.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            seq.add(new KnuthGlue(0, 0, 0,
                                         new LeafPosition(this, -1), true));
            seq.add(new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), true));
        }

        addKnuthElementsForBorderPaddingEnd(seq);

        LinkedList returnList = new LinkedList();
        returnList.add(seq);
        setFinished(true);
        return returnList;
    }

    /** @see InlineLevelLayoutManager#getWordChars(StringBuffer, Position) */
    public void getWordChars(StringBuffer sbChars, Position bp) {
        sbChars.append
            (((org.apache.fop.area.inline.TextArea) curArea).getText());
    }

    /** @see InlineLevelLayoutManager#hyphenate(Position, HyphContext) */
    public void hyphenate(Position pos, HyphContext hc) {
        if (hc.getNextHyphPoint() == 1) {
            // the character ends a syllable
            areaInfo.bHyphenated = true;
            isSomethingChanged = true;
        } else {
            // hc.getNextHyphPoint() returned -1 (no more hyphenation points)
            // or a number > 1;
            // the character does not end a syllable
        }
        hc.updateOffset(1);
    }

    /** @see InlineLevelLayoutManager#applyChanges(List) */
    public boolean applyChanges(List oldList) {
        setFinished(false);
        if (isSomethingChanged) {
            // there is nothing to do,
            // possible changes have already been applied
            // in the hyphenate() method
            return true;
        } else {
            return false;
        }
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(List, int) */
    public LinkedList getChangedKnuthElements(List oldList, int alignment) {
        if (isFinished()) {
            return null;
        }

        LinkedList returnList = new LinkedList();

        addKnuthElementsForBorderPaddingStart(returnList);

        if (letterSpaceIPD.min == letterSpaceIPD.max
            || areaInfo.iLScount == 0) {
            // constant letter space, or no letter space
            returnList.add(new KnuthInlineBox(areaInfo.ipdArea.opt,
                                        areaInfo.alignmentContext,
                                        notifyPos(new LeafPosition(this, 0)), false));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                                      new LeafPosition(this, -1), false));
            }
        } else {
            // adjustable letter space
            returnList.add
                (new KnuthInlineBox(areaInfo.ipdArea.opt
                              - areaInfo.iLScount * letterSpaceIPD.opt,
                              areaInfo.alignmentContext,
                              notifyPos(new LeafPosition(this, 0)), false));
            returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE, false,
                                            new LeafPosition(this, -1), true));
            returnList.add
                (new KnuthGlue(areaInfo.iLScount * letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.max - letterSpaceIPD.opt,
                               areaInfo.iLScount * letterSpaceIPD.opt - letterSpaceIPD.min,
                               new LeafPosition(this, -1), true));
            returnList.add(new KnuthInlineBox(0, null,
                                        notifyPos(new LeafPosition(this, -1)), true));
            if (areaInfo.bHyphenated) {
                returnList.add
                    (new KnuthPenalty(hyphIPD, KnuthPenalty.FLAGGED_PENALTY, true,
                                      new LeafPosition(this, -1), false));
            }
        }

        addKnuthElementsForBorderPaddingEnd(returnList);

        setFinished(true);
        return returnList;
    }

    /** @see LeafNodeLayoutManager#addId */
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }

}
