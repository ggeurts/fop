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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

/** RtfContainer that encloses footers */
public class RtfAfter extends RtfAfterBeforeBase {
    /** Constant for footer on all pages */
    public static final String FOOTER = "footer";
    /** Constant for footer on first page */
    public static final String FOOTER_FIRST = "footerf";
    /** Constant for footer on left pages */
    public static final String FOOTER_LEFT = "footerl";
    /** Constant for footer on right pages */
    public static final String FOOTER_RIGHT = "footerr";
    
    /** String array of footer attributes */
    public static final String[] FOOTER_ATTR = new String[]{
        FOOTER, FOOTER_FIRST, FOOTER_LEFT, FOOTER_RIGHT 
    };

    RtfAfter(RtfSection parent, RtfAttributes attrs) {
        super(parent, attrs);
    }

    /** {@inheritDoc} */
    protected void writeMyAttributes(RtfWriter w) throws IOException {
        w.writeAttributes(attrib, FOOTER_ATTR);
    }
}
