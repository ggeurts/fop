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

/**
 * <p>Model of an RTF footnote.</p>
 *
 * <p>This work was authored by Peter Herweg (pherweg@web.de) and
 *  Marc Wilhelm Kuester.</p>
 */
public class RtfFootnote extends RtfContainer
        implements IRtfTextrunContainer, IRtfListContainer {
    RtfTextrun textrunInline;
    RtfContainer body;
    RtfList list;
    boolean bBody;

    /**
     * Create an RTF list item as a child of given container with default attributes.
     * @param parent a container
     * @param w a writer
     * @throws IOException if not caught
     */
    RtfFootnote(RtfContainer parent) throws IOException {
        super(parent);
        textrunInline = new RtfTextrun(this, null);
        body = new RtfContainer(this);
    }

    /**
     * @return a text run
     * @throws IOException if not caught
     */
    public RtfTextrun getTextrun() throws IOException {
        if (bBody) {
            RtfTextrun textrun = RtfTextrun.getTextrun(body, null);
            textrun.setSuppressLastPar(true);

            return textrun;
        } else {
            return textrunInline;
        }
    }

    /** {@inheritDoc} */
    protected void writeRtfContent(RtfWriter w) throws IOException {
        textrunInline.writeRtfContent(w);

        w.writeGroupMark(true);
        w.writeControlWord("footnote");
        w.writeControlWord("ftnalt");

        body.writeRtfContent(w);

        w.writeGroupMark(false);
    }

    /**
     * @param attrs some attributes
     * @return a RTF list
     * @throws IOException if not caught
     */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        if (list != null) {
            list.close();
        }

        list = new RtfList(body, attrs);
        return list;
    }

    /** start body */
    public void startBody() {
        bBody = true;
    }

    /** end body */
    public void endBody() {
        bBody = false;
    }
}
