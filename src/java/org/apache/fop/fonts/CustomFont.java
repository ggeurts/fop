/*
 * $Id: CustomFont.java,v 1.2 2003/03/06 17:43:05 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fonts;

import java.util.Map;


/**
 * Abstract base class for custom fonts loaded from files, for example.
 */
public abstract class CustomFont extends Typeface
            implements FontDescriptor, MutableFont {

    private String fontName = null;
    private String embedFileName = null;
    private String embedResourceName = null;

    private int capHeight = 0;
    private int xHeight = 0;
    private int ascender = 0;
    private int descender = 0;
    private int[] fontBBox = {0, 0, 0, 0};
    private int flags = 4;
    private int stemV = 0;
    private int italicAngle = 0;
    private int missingWidth = 0;
    private FontType fontType = FontType.TYPE1;
    private int firstChar = 0;
    private int lastChar = 255;

    private Map kerning = new java.util.HashMap();


    private boolean useKerning = true;


    /**
     * @see org.apache.fop.fonts.FontMetrics#getFontName()
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Returns an URI representing an embeddable font file. The URI will often
     * be a filename or an URL.
     * @return URI to an embeddable font file or null if not available.
     */
    public String getEmbedFileName() {
        return embedFileName;
    }

    /**
     * Returns the lookup name to an embeddable font file available as a
     * resource.
     * (todo) Remove this method, this should be done using a resource: URI.
     * @return the lookup name
     */
    public String getEmbedResourceName() {
        return embedResourceName;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getAscender()
     */
    public int getAscender() {
        return ascender;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getDescender()
     */
    public int getDescender() {
        return descender;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getCapHeight()
     */
    public int getCapHeight() {
        return capHeight;
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getAscender(int)
     */
    public int getAscender(int size) {
        return size * ascender;
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getDescender(int)
     */
    public int getDescender(int size) {
        return size * descender;
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getCapHeight(int)
     */
    public int getCapHeight(int size) {
        return size * capHeight;
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getXHeight(int)
     */
    public int getXHeight(int size) {
        return size * xHeight;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFontBBox()
     */
    public int[] getFontBBox() {
        return fontBBox;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFlags()
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getStemV()
     */
    public int getStemV() {
        return stemV;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getItalicAngle()
     */
    public int getItalicAngle() {
        return italicAngle;
    }

    /**
     * Returns the width to be used when no width is available.
     * @return a character width
     */
    public int getMissingWidth() {
        return missingWidth;
    }

    /**
     * @see org.apache.fop.fonts.FontDescriptor#getFontType()
     */
    public FontType getFontType() {
        return fontType;
    }

    /**
     * Returns the index of the first character defined in this font.
     * @return the index of the first character
     */
    public int getFirstChar() {
        return 0;
        // return firstChar;
        /**(todo) Why is this hardcoded??? This code was in SingleByteFont.java */
    }

    /**
     * Returns the index of the last character defined in this font.
     * @return the index of the last character
     */
    public int getLastChar() {
        return lastChar;
    }

    /**
     * Used to determine if kerning is enabled.
     * @return True if kerning is enabled.
     */
    public boolean isKerningEnabled() {
        return useKerning;
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#hasKerningInfo()
     */
    public final boolean hasKerningInfo() {
        return (isKerningEnabled() & kerning.isEmpty());
    }

    /**
     * @see org.apache.fop.fonts.FontMetrics#getKerningInfo()
     */
    public final Map getKerningInfo() {
        if (isKerningEnabled()) {
            return kerning;
        } else {
            return java.util.Collections.EMPTY_MAP;
        }
    }


    /* ---- MutableFont interface ---- */

    /**
     * @see org.apache.fop.fonts.MutableFont#setFontName(String)
     */
    public void setFontName(String name) {
        this.fontName = name;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setEmbedFileName(String)
     */
    public void setEmbedFileName(String path) {
        this.embedFileName = path;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setEmbedResourceName(String)
     */
    public void setEmbedResourceName(String name) {
        this.embedResourceName = name;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setCapHeight(int)
     */
    public void setCapHeight(int capHeight) {
        this.capHeight = capHeight;
    }

    /**
     * Returns the XHeight value of the font.
     * @param xHeight the XHeight value
     */
    public void setXHeight(int xHeight) {
        this.xHeight = xHeight;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setAscender(int)
     */
    public void setAscender(int ascender) {
        this.ascender = ascender;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setDescender(int)
     */
    public void setDescender(int descender) {
        this.descender = descender;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setFontBBox(int[])
     */
    public void setFontBBox(int[] bbox) {
        this.fontBBox = bbox;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setFlags(int)
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setStemV(int)
     */
    public void setStemV(int stemV) {
        this.stemV = stemV;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setItalicAngle(int)
     */
    public void setItalicAngle(int italicAngle) {
        this.italicAngle = italicAngle;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setMissingWidth(int)
     */
    public void setMissingWidth(int width) {
        this.missingWidth = width;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setFontType(FontType)
     */
    public void setFontType(FontType fontType) {
        this.fontType = fontType;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setFirstChar(int)
     */
    public void setFirstChar(int index) {
        this.firstChar = index;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setLastChar(int)
     */
    public void setLastChar(int index) {
        this.lastChar = index;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#setKerningEnabled(boolean)
     */
    public void setKerningEnabled(boolean enabled) {
        this.useKerning = enabled;
    }

    /**
     * @see org.apache.fop.fonts.MutableFont#putKerningEntry(Integer, Map)
     */
    public void putKerningEntry(Integer key, Map value) {
        this.kerning.put(key, value);
    }

}
