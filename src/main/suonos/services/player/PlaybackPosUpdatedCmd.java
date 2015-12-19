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

import suonos.services.player.events.PositionUpdated;

/**
 * Track playback position updated.
 * 
 * @author anthony
 */
public class PlaybackPosUpdatedCmd implements Cmd {

    private int position;

    public PlaybackPosUpdatedCmd(int timeInSecs) {
        this.position = timeInSecs;
    }

    @Override
    public void execute(MediaPlayer mp) throws Exception {

        /**
         * If an item is playing, raise an event.
         */
        if (mp.itemPlaying != null) {
            // Calc percentage position.
            //
            mp.playerPos = mp.itemPlaying.percentagePos(position);

            MediaPlayer.log.debug("playerPosUpdate {} {} {}%", mp.itemPlaying.mediaPath, position, mp.playerPos);

            mp.events.post(new PositionUpdated(mp.itemPlaying, position));
        }
    }

    @Override
    public void queued(MediaPlayer mp) {
    }
}
