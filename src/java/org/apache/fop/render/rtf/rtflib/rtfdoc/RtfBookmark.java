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
import java.io.Writer;

/**
 * <p>RTF Bookmark.
 * Create an RTF bookmark as a child of given container with default attributes.
 * This class belongs to the "id" attribute processing.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public class RtfBookmark extends RtfElement {
    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Name of the bokkmark */
    private String bookmark;
    /** Word 2000 supports a length of 40 characters only */
    public static final int MAX_BOOKMARK_LENGTH = 40;
    /** Word 2000 converts '.' in bookmarks to "_", thats why we control this replacement. */
    public static final char REPLACE_CHARACTER = '_';


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     *
     * @param parent a <code>RtfBookmarkContainer</code> value
     * @param bookmark Name of the bookmark
     */
    RtfBookmark(RtfContainer parent, String bookmark) throws IOException {
        super(parent);

        int now = bookmark.length();

        this.bookmark = bookmark.substring(0,
                now < MAX_BOOKMARK_LENGTH ? now : MAX_BOOKMARK_LENGTH);
        this.bookmark = this.bookmark.replace('.', REPLACE_CHARACTER);
        this.bookmark = this.bookmark.replace(' ', REPLACE_CHARACTER);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

    /** {@inheritDoc} */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
        // {\*\bkmkstart test}
        writeRtfBookmark(w, "bkmkstart");
    }

    /** {@inheritDoc} */
    protected void writeRtfContent(RtfWriter w) throws IOException {
    }

    /** {@inheritDoc} */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        // {\*\bkmkend test}
        writeRtfBookmark(w, "bkmkend");
    }

    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

    /**
     * Writes the rtf bookmark.
     *
     * @param tag Begin or close tag
     *
     * @throws IOException On error
     */
    private void writeRtfBookmark(RtfWriter w, String tag) throws IOException {
        if (bookmark == null) {
            return;
        }

        w.writeGroupMark(true);
        w.writeStarControlWord(tag);
        w.write(bookmark);
        w.writeGroupMark(false);
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return bookmark == null || bookmark.trim().length() == 0;
    }
}
