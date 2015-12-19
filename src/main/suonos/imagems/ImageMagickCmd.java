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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.github.am0e.jdi.annotn.Setting;

public final class ImageMagickCmd {

    private ProcessBuilder pb = new ProcessBuilder();
    private List<String> command = new ArrayList<>(10);

    @Inject
    @Setting(path = "settings.imagemagick.path")
    private String imagemagickPath = "convert";

    public ImageMagickCmd convert() {
        return setCmd("convert");
    }

    public ImageMagickCmd composite() {
        return setCmd("composite");
    }

    private ImageMagickCmd setCmd(String cmd) {
        command.clear();
        command.add(imagemagickPath);
        command.add(cmd);
        return this;
    }

    public ImageMagickCmd add(String param) {
        command.add(param);
        return this;
    }

    public ImageMagickCmd file(Path f) {
        command.add(f.toAbsolutePath().toString());
        return this;
    }

    public ImageMagickCmd strip() {
        return add("-strip");
    }

    public ImageMagickCmd flatten() {
        return add("-flatten");
    }

    public ImageMagickCmd fuzz(String arg) {
        return add("-fuzz").add(arg);
    }

    public ImageMagickCmd quality(int q) {
        if (q < 0 || q > 100)
            q = 85;
        return add("-quality").add(Integer.toString(q));
    }

    public ImageMagickCmd compress(String s) {
        return add("-compress").add(s);
    }

    public ImageMagickCmd background(String color) {
        return add("-background").add(color);
    }

    public ImageMagickCmd resize(String geometry) {
        return add("-resize").add(geometry);
    }

    public ImageMagickCmd extent(String geometry) {
        return add("-extent").add(geometry);
    }

    public ImageMagickCmd gravity(String arg) {
        return add("-gravity").add(arg);
    }

    public ImageMagickCmd filter(String filter) {
        return add("-filter").add(filter);
    }

    public ImageMagickCmd watermark(Path watermarkFile) {
        // Use stack to process the watermark:
        // mainfile.jpg ( watermarkfile.jpg -resize 300x300> ) -composite
        //
        // The resize is applied to the watermark.
        //
        // The composite action is performed on the result:
        // mainfile.jpg watermarkresizeresult.jpg
        //
        return add("(").file(watermarkFile).resize("100%x100%").add(")").add("-composite").gravity("center")
                .add("-compose").add("Over");
    }

    /**
     * Exec the command, waits for it to finish, logs errors if exit status is
     * nonzero, and returns true if exit status is 0 (success).
     *
     * @param command
     *            Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException
     * @throws InterruptedException
     */
    public void exec() throws IOException, InterruptedException {
        pb.command(command);

        Process proc = pb.start();
        int exitStatus = proc.waitFor();

        // Ensure file handles are closed.
        // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4784692
        //
        proc.destroy();

        if (exitStatus > 1) {
            throw new IOException("ImageMagick failed to convert image");
        }
    }

    /**
     * @return the imagemagickPath
     */
    public String getImagemagickPath() {
        return imagemagickPath;
    }

    /**
     * @param imagemagickPath
     *            the imagemagickPath to set
     */
    public void setImagemagickPath(String imagemagickPath) {
        this.imagemagickPath = imagemagickPath;
    }
}
