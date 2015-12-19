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

import java.util.List;

public final class AddPlayableCmd implements Cmd {

    private List<PlayerItem> list;
    private boolean replacedAndPlay;

    public AddPlayableCmd(List<PlayerItem> list, boolean replaceAndPlay) {
        // A track / movie.
        //
        this.list = list;
        this.replacedAndPlay = replaceAndPlay;
    }

    @Override
    public void execute(MediaPlayer mp) throws Exception {
        if (replacedAndPlay) {
            // Clear down the playlist.
            //
            mp.clearPlaylist();

            // Add the new tracks to the playlist.
            //
            mp.addItemsToPlaylist(list);

            mp.stopPlayer();

            mp.startPlayer();

            mp.nextTrack();

        } else {
            mp.addItemsToPlaylist(list);
        }
    }

    @Override
    public void queued(MediaPlayer mp) {
    }
}
