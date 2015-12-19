/*******************************************************************************
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
 *******************************************************************************/
package suonos.imagems;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;

import com.github.am0e.commons.msgs.Msgs;
import com.github.am0e.commons.utils.Collections8;

import suonos.models.media.MetaDataFolder;
import suonos.models.music.MusicAlbum;

public class Imagems {

    static Map<String, Integer> sizes = Collections8.asMap(MusicAlbum.COVER_LG, 500, MusicAlbum.COVER_XS, 150,
            MusicAlbum.COVER_SM, 250);

    static int JPEG_QUALITY = 85;

    /**
     * Sizes? large 500 x 500 thumbnail 100 x 100
     * 
     * @param resId
     * @param imageFile
     * @throws InterruptedException
     */
    public void convertImage(MetaDataFolder folder, InputStream imagePath) throws IOException, InterruptedException {

        // Create a temp file to copy the image to.
        //
        Path tmpFile = Files.createTempFile("tmp", null);
        Files.copy(imagePath, tmpFile);

        convertImage(folder, tmpFile);
    }

    public void convertImage(MetaDataFolder folder, Path sourceImageFile) throws IOException, InterruptedException {
        // Create ".suonos/" folder
        //
        Files.createDirectories(folder.getPath());

        for (String key : sizes.keySet()) {
            // ".suonos/thumbnail.jpg"
            //
            int size = sizes.get(key);
            Path destPath = folder.getFilePath(key + ".jpg");
            convertImage(sourceImageFile, destPath, size);
        }
    }

    /**
     * Uses a Runtime.exec()to use imagemagick to perform the given conversion
     * operation. Returns true on success, false on failure. Does not check if
     * either file exists.
     */
    private void convertImage(Path sourceImagePath, Path destImagePath, int size)
            throws IOException, InterruptedException {

        String geometry = null;

        if (Files.exists(destImagePath)) {
            FileTime modtime1 = Files.getLastModifiedTime(sourceImagePath);
            FileTime modtime2 = Files.getLastModifiedTime(destImagePath);

            if (modtime1.compareTo(modtime2) <= 0) {
                return;
            }
        }

        ImageMagickCmd IM = new ImageMagickCmd();
        IM.convert();

        // geometry = "{}x{}>": Shrink only if img dimensions are larger than
        // the corresponding width and/or height
        // arguments.
        //
        geometry = Msgs.format("{0}x{0}>", size);

        // -sampling-factor 1x1 -quality 80 -fill "#000001" -opaque "#000000"
        // -fill "#000001" -opaque "#000000" is for IE6/7 to prevent the jpeg
        // bug which renders black pixels
        // as white when the css opacity filter is applied to the image.
        //
        IM.gravity("center");
        IM.add("-sampling-factor");
        IM.add("1x1");
        IM.quality(JPEG_QUALITY);
        IM.resize(geometry);
        IM.file(sourceImagePath);
        IM.file(destImagePath);
        IM.exec();
    }

}
