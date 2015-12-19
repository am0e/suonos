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
package suonos.services.player;

import suonos.models.StoreObject;
import suonos.models.movies.Movie;
import suonos.models.music.MusicTrack;

public final class PlayerItem {
    /**
     * Title of the item to play.
     */
    public final String title;

    /**
     * File path. This is the physical location of the file to play.
     */
    public final String mediaPath;

    /**
     * Related objid of the {@link StoreObject}. Contains null if there is no
     * related object. ie the item is a URL not backed by a {@link StoreObject}.
     * Otherwise it will contain a reference to a {@link MusicTrack} or a
     * {@link Movie} object.
     */
    public final String relId;

    /**
     * Length of the track in seconds.
     */
    public int lenInSec;

    public PlayerItem(String title, String filePath, String relId) {
        this.title = title;
        this.mediaPath = filePath;
        this.relId = relId;
    }

    public String toString() {
        return title == null ? mediaPath : title;
    }

    public int percentagePos(int posInSecs) {
        if (lenInSec == 0)
            return -1;
        else
            return (int) (((double) posInSecs / (double) lenInSec) * 100.0);
    }
}
