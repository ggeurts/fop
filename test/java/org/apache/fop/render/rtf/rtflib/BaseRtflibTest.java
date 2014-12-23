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
package org.apache.fop.render.rtf.rtflib;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfFile;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfWriter;

/**
 *
 * @author ggeurts
 */
public abstract class BaseRtflibTest {
    
    protected static String toRtf(RtfBuilder operation) 
    throws IOException {
        StringWriter writer = new StringWriter();
        RtfWriter rtfWriter = new RtfWriter(writer);

        RtfFile rtfFile = new RtfFile(null);
        RtfDocumentArea rtfDocumentArea = rtfFile.startDocumentArea();
        RtfSection section = rtfDocumentArea.newSection();

        RtfElement result = operation.build(section);
        result.writeRtf(rtfWriter);
        return writer.getBuffer().toString();
    }
    
    protected interface RtfBuilder {
        public RtfElement build(RtfSection section) throws IOException;
    }
}
