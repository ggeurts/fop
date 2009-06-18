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

package org.apache.fop.image;

/**
 * Enhanced metafile image.
 * This supports loading a EMF image.
 *
 * @see AbstractFopImage
 * @see FopImage
 */
public class EmfImage extends AbstractFopImage {
    
    /**
     * Create a bitmap image with the image data.
     *
     * @param imgInfo the image information
     */
    public EmfImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
    }

    /**
     * Load the original EMF data.
     * This loads the original EMF data and reads the color space,
     * and icc profile if any.
     *
     * @return true if loaded false for any error
     */
    protected boolean loadOriginalData() {
        return loadDefaultOriginalData();
    }
}
