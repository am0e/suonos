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
package suonos.models.music;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.am0e.jdi.annotn.Global;
import com.github.am0e.jdi.annotn.Setting;

import suonos.models.MediaLib;

@Singleton
@Global
public final class MusicLib extends MediaLib {

    public static final String CODE = "MU";

    @Inject
    @Setting(path = "settings.musiclib.path")
    private Path root = Paths.get("~/Music");

    public MusicLib() {
        super("MU");
    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public Path setRoot(Path path) {
        return root;
    }

    public Path getTrackFolderPath(MusicTrack track) {
        return resolvePath(Paths.get(track.getPath()));
    }
}
