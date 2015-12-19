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

/**
 * Callback events. These are called directly from the player and will typically
 * be called within the context of a thread dedicated to the player.
 * 
 * @author anthony
 */
public interface PlayerCallback {
    /**
     * Playback has successfully started.
     */
    void playbackStarted(PlaybackInfo item);

    /**
     * Player has finished playing the track.
     * 
     * @param statusCode
     */
    void playerFinished(String statusCode);

    /**
     * Player has updated it's position in the file. Can be used to update a UI
     * indicator.
     * 
     * @param timeInSecs
     */
    void playerPosUpdate(int timeInSecs);

    /**
     * Called when an error occurs during playback. The error can be displayed
     * on the UI.
     * 
     * @param msg
     */
    void playbackError(String msg);
}
