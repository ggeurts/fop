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
import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

/**
 * <p>Common code for RtfAfter and RtfBefore.</p>
 *
 * <p>This work was authored by Andreas Lambert (andreas.lambert@cronidesoft.com),
 * Christopher Scott (scottc@westinghouse.com), and
 * Christoph Zahm (zahm@jnet.ch) [support for tables in headers/footers].</p>
 */

abstract class RtfAfterBeforeBase
extends RtfContainer
implements IRtfParagraphContainer, IRtfExternalGraphicContainer, IRtfTableContainer,
        IRtfTextrunContainer {

    RtfAfterBeforeBase(RtfSection parent, RtfAttributes attrs) {
        super((RtfContainer)parent, attrs);
    }

    public RtfParagraph newParagraph() {
        return new RtfParagraph(this);
    }

    public RtfParagraph newParagraph(RtfAttributes attrs) {
        return new RtfParagraph(this, attrs);
    }

    public RtfExternalGraphic newImage() {
        return new RtfExternalGraphic(this);
    }

    /** {@inheritDoc} */
    protected void writeRtfPrefix(RtfWriter w) throws IOException {
        w.writeGroupMark(true);
        writeMyAttributes(w);
    }

    /** must be implemented to write the header or footer attributes */
    protected abstract void writeMyAttributes(RtfWriter w) throws IOException;

    /** {@inheritDoc} */
    protected void writeRtfSuffix(RtfWriter w) throws IOException {
        w.writeGroupMark(false);
    }

    /** close current table if any and start a new one
     * @param tc added by Boris Poudérous on july 2002 in order to process
     *  number-columns-spanned attribute
     */
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) {
        return new RtfTable(this, attrs, tc);
    }

    /** close current table if any and start a new one  */
    public RtfTable newTable(ITableColumnsInfo tc) {
        return new RtfTable(this, tc);
    }

    public RtfTextrun getTextrun() {
        return RtfTextrun.getTextrun(this, null);
    }

    public boolean isEmpty() {
        // Write empty headers and footers to prevent inheritance from previous 
        // section.
        return attrib.isEmpty();
    }
    
    /** 
     * {@inheritDoc}
     * Closes any previous child.
     */
    protected void addChild(RtfElement e) {
        RtfElement previousChild = getLastChild();
        if (previousChild != null) {
            previousChild.close();
        }
        
        super.addChild(e);
    }
}
