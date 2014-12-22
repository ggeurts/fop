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

//Java
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * <p>Class that wraps a {@link Writer} with group,control-word, and 
 * character escaping aware write methods.</p>
 *
 * <p></p>
 * <p>This work was authored by Gerke Geurts (ggeurts@geurtsrus.com)
 * and incorporates work derived from Bertrand Delacretaz (bdelacretaz@codeconsult.ch).
 * </p>
 */
public final class RtfWriter
{
    private static final char DBLQUOTE = '\"';
    private static final char QUOTE = '\'';
    private static final char SPACE = ' ';
    
    private final Writer writer;
    private boolean textMode;
    
    public RtfWriter(Writer writer)
    {
        this.writer = writer;
    }

    /**
     * must be called when done creating the document
     * @throws IOException for I/O problems
     */
    public synchronized void flush() throws IOException {
        writer.flush();
    }
    
    /**
     * Starts a new line in the RTF file being written. This is only to format
     * the RTF file itself (for easier debugging), not its content.
     * @throws IOException in case of an I/O problem
     */
    public void newLine() throws IOException {
        writer.write('\n');
        textMode = true;
    }
    
    /**
     * Write an RTF control word to our Writer
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    final void writeControlWord(String word)
    throws IOException {
        writer.write('\\');
        writer.write(word);
        textMode = false;
    }

    /**
     * Write an RTF control word and parameter to our Writer
     * @param word RTF control word to write
     * @param parameter RTF control word parameter value
     * @throws IOException for I/O problems
     */
    final void writeControlWord(String word, int parameter)
    throws IOException {
        writeControlWord(word);
        writer.write(Integer.toString(parameter));
    }
    
    /**
     * Write an RTF control word to our Writer, preceeded by a star '*'
     * meaning "ignore this if you don't know what it means"
     * @param word RTF control word to write
     * @throws IOException for I/O problems
     */
    final void writeStarControlWord(String word)
    throws IOException {
        writer.write("\\*\\");
        writer.write(word);
        textMode = false;
    }
    
    /**
     * Write a start or end group mark
     * @param isStart set to true if this is a start mark
     * @throws IOException for I/O problems
     */
    final void writeGroupMark(boolean isStart)
    throws IOException {
        writer.write(isStart ? '{' : '}');
        textMode = true;
    }

    /**
     * Writes binary data
     * @param data Byte array to be written
     * @throws IOException for I/O problems
     */
    final void write(byte[] data)
    throws IOException {
        if (data == null || data.length == 0) return;
        
        ensureTextMode('0');
        
        for (int i = 0; i < data.length; i++) {
            int iData = data[i];

            // Make positive byte
            if (iData < 0) {
                iData += 256;
            }

            if (iData < 16) {
                // Set leading zero and append
                writer.write('0');
            }

            writer.write(Integer.toHexString(iData));
        }
    }

    /**
     * Write given String, converting characters as required by RTF spec
     * @param str String to be written
     * @throws IOException for I/O problems
     */
    final void write(String str)
    throws IOException {
        if (str == null || str.length() == 0) {
            return;
        }

        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            char d;
            if (i != 0) {
                d = str.charAt(i - 1);
            } else {
                d = SPACE;
            }

            //This section modified by Chris Scott
            //add "smart" quote recognition
            switch (c) {
                case DBLQUOTE:
                    writeControlWord(d == SPACE ? "ldblquote" : "rdblquote");
                    break;
                case QUOTE:
                    writeControlWord(d == SPACE ? "lquote" : "rquote");
                    break;
                default:
                    write(c);
                    break;
            }
        }
    }

    /**
     * Write given character, converting as required by RTF spec
     * @param c Character to be written
     * @throws IOException for I/O problems
     */
    final void write(char c)
    throws IOException {
        switch (c) {
            case '\t': 
                writeControlWord("tab");
                return;
            case '\n': 
                writeControlWord("line");
                return;
            case '\\':
            case '}':
            case '{':
                writer.write("\\");
                writer.write(c);
                return;
        }
        
        if ((int)c <= 127)
        {
            // plain char that is understood by RTF natively
            ensureTextMode(c);
            writer.write(c);
        } else if ((int)c <= 255) {
            // write unicode representation - contributed by Michel Jacobson
            // <jacobson@idf.ext.jussieu.fr>
            writer.write("\'");
            writer.write(Integer.toHexString(c));
            textMode = true;
        } else {
            // write unicode representation - contributed by Michel Jacobson
            // <jacobson@idf.ext.jussieu.fr>
            writeControlWord("u", (int)c);
            writer.write("\'3f");
            textMode = true;
        } 
    }

    /**
     * Write given attribute values to our Writer
     * @param attr RtfAttributes to be written
     * @param nameList if given, only attribute names from this list are considered
     * @throws IOException for I/O problems
     */
    public void writeAttributes(RtfAttributes attr, String[] nameList)
    throws IOException {
        if (attr == null) {
            return;
        }

        if (nameList != null) {
            // process only given attribute names
            for (int i = 0; i < nameList.length; i++) {
                final String name = nameList[i];
                if (attr.isSet(name)) {
                    writeOneAttribute(name, attr.getValue(name));
                }
            }
        } else {
            // process all defined attributes
            for (Iterator it = attr.nameIterator(); it.hasNext();) {
                final String name = (String)it.next();
                if (attr.isSet(name)) {
                    writeOneAttribute(name, attr.getValue(name));
                }
            }
        }
    }
    
    /**
     * Write one attribute to our Writer
     * @param name name of attribute to write
     * @param value value of attribute to be written
     * @throws IOException for I/O problems
     */
    public void writeOneAttribute(String name, Object value)
    throws IOException {
        if (value instanceof Integer) {
            writeControlWord(name, (Integer) value);
        } else if (value instanceof String) {
            writeControlWord(name + (String)value);
        } else if (value instanceof RtfAttributes) {
            writeControlWord(name);
            writeAttributes((RtfAttributes) value, null);
        } else {
            writeControlWord(name);
        }
    }
    
    /**
     * Make a visible entry in the RTF for an exception
     * @param ie Exception to flag
     * @throws IOException for I/O problems
     */
    public void writeException(Exception ie)
    throws IOException {
        writeGroupMark(true);
        writeControlWord("par");

        // make the exception message stand out so that the problem is visible
        writeControlWord("fs48");
        write(ie.getClass().getName());

        writeControlWord("fs20");
        write(ie.toString());

        writeControlWord("par");
        writeGroupMark(false);
    }
    
    /**
     * Write given String as is.
     * @param str String to be written
     * @throws IOException for I/O problems
     */
    final void writeRaw(String str)
    throws IOException {
        writer.write(str);
    }
    
    private void ensureTextMode(char nextChar)
    throws IOException {
        if (!textMode) {
            if (requiresControlWordDelimiter(nextChar))
            {
                writer.write(SPACE);
            }
            textMode = true;
        }
    }
    
    private static boolean requiresControlWordDelimiter(char ch)
    {
        // TODO: use bitset for performance
        return (ch >= 'a' && ch < 'z') 
            || (ch >= 'A' && ch < 'Z')
            || (ch >= '0' && ch < '9')
            || ch == '-' 
            || ch == SPACE;
    }
}
