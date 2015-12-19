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
package suonos.services.player.mockplayer;

import java.io.IOException;

import javax.inject.Singleton;

import suonos.services.player.PlaybackInfo;
import suonos.services.player.PlayerBuilder;
import suonos.services.player.PlayerCallback;
import suonos.services.player.PlayerInstance;
import suonos.services.player.PlayerItem;

/**
 * Manages an instance of an mplayer process. A separate worker thread is
 * created to monitor the mplayer process and read it's output. The worker
 * thread will call callback methods in the {@link PlayerCallback} from within
 * it's thread.
 * 
 * @author anthony
 */
@Singleton
public class MockPlayer implements PlayerInstance, PlayerBuilder, Runnable {

    private PlayerItem item;
    private PlayerCallback callback;
    private Thread thread;
    private int pos;

    public MockPlayer() {
    }

    @Override
    public void quit() throws IOException {
    }

    @Override
    public void stop() throws IOException {
        this.item = null;
        this.pos = -1;
    }

    @Override
    public void getTimePos() throws IOException {
    }

    @Override
    public void volume(int val) throws IOException {
    }

    @Override
    public void pollPlayback() throws IOException {
    }

    @Override
    public void seek(int seekPos, boolean abs) throws IOException {
    }

    @Override
    public void mute(boolean val) throws IOException {
    }

    @Override
    public void pause(boolean val) throws IOException {
    }

    @Override
    public void osd(String action) throws IOException {
    }

    @Override
    public PlayerInstance createInst(PlayerCallback callback) throws IOException {
        this.callback = callback;
        return this;
    }

    @Override
    public void start() throws IOException {
        this.thread = new Thread(null, this, "MockPlayer", 16384);
        this.thread.start();
    }

    private void sleep(int len) {
        try {
            Thread.sleep(len);

        } catch (InterruptedException e) {
        }
    }

    @Override
    public void playFile(PlayerItem next) throws IOException {
        synchronized (this) {
            this.item = next;
            this.pos = -1;
        }
    }

    @Override
    public void run() {
        while (true) {
            sleep(1000);

            synchronized (this) {
                if (this.item != null) {
                    if (this.pos == -1) {
                        PlaybackInfo inf = new PlaybackInfo();
                        inf.item = item;
                        inf.lengthInSecs = 15.4;
                        callback.playbackStarted(inf);
                        pos++;
                    } else {
                        pos++;
                        callback.playerPosUpdate(pos);

                        if (this.pos >= this.item.lenInSec) {
                            this.item = null;
                            callback.playerFinished("ok");
                        }
                    }
                }
            }
        }
    }
}