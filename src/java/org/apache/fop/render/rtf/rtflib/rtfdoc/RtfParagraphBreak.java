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

import java.io.IOException;

/**  Class which represents a paragraph break.*/
public class RtfParagraphBreak extends RtfElement {
    RtfParagraphBreak(RtfContainer parent) {
        super(parent);
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        w.writeControlWord("par");
    }
}
