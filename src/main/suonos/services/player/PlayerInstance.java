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

import java.io.IOException;

public interface PlayerInstance {
    void quit() throws IOException;

    void start() throws IOException;

    void stop() throws IOException;

    void getTimePos() throws IOException;

    void volume(int val) throws IOException;

    void pollPlayback() throws IOException;

    void seek(int seekPos, boolean abs) throws IOException;

    void mute(boolean val) throws IOException;

    void pause(boolean val) throws IOException;

    /**
     * Execute an OSD menu command:
     * <dl>
     * <dt>up</dt>
     * <dd>Move cursor up.</dd>
     * <dt>down</dt>
     * <dd>Move cursor down.</dd>
     * <dt>cancel</dt>
     * <dd>Cancel selection.</dd>
     * <dt>hide</dt>
     * <dd>Hide OSD menu.</dd>
     * <dt>ok</dt>
     * <dd>Accept selection.</dd>
     * </dl>
     * 
     * @param action
     * @throws IOException
     */
    void osd(String action) throws IOException;

    /**
     * Play the requested media file aborting the currently playing file.
     * 
     * @param next
     * @throws IOException
     */
    void playFile(PlayerItem next) throws IOException;
}
