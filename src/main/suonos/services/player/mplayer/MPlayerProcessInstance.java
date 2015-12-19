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
package suonos.services.player.mplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import suonos.services.player.PlaybackInfo;
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
public class MPlayerProcessInstance implements PlayerInstance, Runnable {
    private Process process;
    private BufferedReader is;
    private Writer os;
    private PlayerCallback callback;
    private Thread thread;
    private String statusCode;
    private ProcessBuilder pb;
    private PlaybackInfo playbackInfo = new PlaybackInfo();

    // pausing_keep_force get_property percent_pos
    // pausing_keep set_property pause 1

    /**
     * Logger for the mplayer process. All lines are prefixed with special
     * codes: <br>
     * <b>'&lt;'</b> = Input From mplayer. <b>'!'</b> = Log line. <b>'&gt;'</b>
     * = Output to mplayer.
     */
    private static Logger log = LoggerFactory.getLogger("mplayer");

    public MPlayerProcessInstance(ProcessBuilder pb, PlayerCallback callback) {
        this.pb = pb;
        this.callback = callback;
    }

    public void start() throws IOException {

        // Start the mplayer sub process.
        // This will begin playing the file.
        //
        this.process = pb.start();
        this.statusCode = "terminated";
        this.pb = null;

        // Connect to the input of the process. Ie we write to the input stream
        // of the process.
        //
        this.os = new OutputStreamWriter(process.getOutputStream());

        // Connect to the output of the process. Ie we read from the output
        // stream of the process.
        //
        this.is = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Create a thread to monitor the process output stream. We need a
        // thread as we will block
        // on the stream read.
        //
        this.thread = new Thread(null, this, "MPlayer", 16384);

        // Now wait for the thread to start.
        //
        synchronized (this) {
            try {
                // Start the thread & wait for it to be running.
                //
                this.thread.start();
                this.wait(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        log.info("! Thread Started");

        synchronized (this) {
            this.notify();
        }

        try {
            String line;
            BufferedReader is = this.is;

            // Read the output of the mplayer process blocking until data is
            // available.
            // If the mplayer process terminates, readLine() will return null
            // for EOF.
            // If the stream is closed (is.close()), this will unblock with an
            // IOException.
            //
            while ((line = is.readLine()) != null) {
                // Process the line of output.
                //
                processOutput(line);
            }

        } catch (IOException e) {
            // What to do here?
            //
            // e.printStackTrace();

        } finally {
            log.info("! Thread Terminated");

            synchronized (this) {
                // If the process is still alive, terminate it now.
                //
                destroy();
                this.notify();
            }
        }
    }

    private void destroy() {
        synchronized (this) {
            try {
                if (process != null && process.isAlive()) {
                    process.destroy();
                    process = null;
                }

                if (is != null) {
                    is.close();
                    is = null;
                }

                if (os != null) {
                    os.close();
                    os = null;
                }

            } catch (IOException ex) {
            }
        }
    }

    private void processOutput(String line) {
        line = line.trim();

        log.info("< " + line);

        if (line.startsWith("GLOBAL: ANS_time_pos")) {
            processAnsTimePos(line);

        } else if (line.startsWith("IDENTIFY: ")) {
            process_IDENTIFY(line);

        } else if (line.startsWith("GLOBAL: EOF code:")) {
            processEofCode(line);

        } else if (line.startsWith("OPEN: Failed to open")) {
            processError(line);

        } else if (line.startsWith("CPLAYER: Starting playback")) {
            // processStartingPlayback();

        } else if (line.startsWith("Playing ")) {
            // ? is sometimes null
            callback.playbackStarted(playbackInfo);
        }
    }

    private void processStartingPlayback() {
        callback.playbackStarted(playbackInfo);
        playbackInfo.item = null;
    }

    private void process_IDENTIFY(String line) {
        line = StringUtils.substringAfter(line, "IDENTIFY: ");

        if (line.startsWith("ID_LENGTH=")) {
            double len = getNumber(line, "=");
            playbackInfo.lengthInSecs = len;
        }
        if (line.startsWith("ID_FILENAME=")) {
            playbackInfo.fileName = StringUtils.substringAfter(line, "=");
        }
        if (line.startsWith("ID_SEEKABLE=")) {
            playbackInfo.seekable = StringUtils.substringAfter(line, "=").equals("1");
        }
    }

    private void processError(String line) {
        callback.playbackError(line);
    }

    private double getNumber(String line, String after) {
        String val = StringUtils.substringAfterLast(line, after).trim();
        return Double.parseDouble(val);
    }

    private void processEofCode(String line) {
        int eofCode = (int) getNumber(line, ":");

        if (eofCode == 1) {
            statusCode = "finished";
        }
        log.debug("! EOF CODE: {}", eofCode);

        // Invoke the callback event.
        //
        callback.playerFinished(statusCode);
    }

    private void processAnsTimePos(String line) {
        String val = StringUtils.substringAfter(line, "=");
        double v = Double.parseDouble(val);

        // Round up.
        //
        int timeInSecs = (int) (v + 0.5);

        callback.playerPosUpdate(timeInSecs);
    }

    @Override
    public void stop() throws IOException {
        sendCmd("stop");
    }

    @Override
    public void quit() throws IOException {

        synchronized (this) {
            if (process.isAlive()) {
                sendCmd("quit");

                try {
                    // Wait upto 5 seconds for process to terminate.
                    //
                    this.wait(5000);

                    // If not terminated, destroy the process.
                    //
                    destroy();

                } catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public void pause(boolean pause) throws IOException {
        // Not sure why, but set_property pause is no longer implemented in
        // mplayer
        // so we have to use a bit of a hack to set the pause to true or false!
        //
        if (pause) {
            sendCmd(false, "pausing", "get_property", "pause");
        } else {
            sendCmd(false, "get_property", "pause");
        }
        // sendCmd("set_property", "pause", val ? "1" : "0");
    }

    @Override
    public void volume(int val) throws IOException {
        sendCmd("set_property", "volume", Integer.toString(val));
    }

    @Override
    public void mute(boolean val) throws IOException {
        sendCmd("set_property", "mute", val ? "1" : "0");
    }

    @Override
    public void getTimePos() throws IOException {
        sendCmd(false, "pausing_keep_force", "get_property", "time_pos");
    }

    /**
     * Send a command to the mplayer process
     * 
     * @param cmdString
     * @throws IOException
     */
    private void sendCmd(String... cmds) throws IOException {
        sendCmd(true, cmds);
    }

    private void sendCmd(boolean logCmd, String... cmds) throws IOException {
        if (process.isAlive()) {
            String cmdString = StringUtils.join(cmds, " ");
            if (logCmd) {
                log.debug("> sendCmd {}", cmdString);
            }
            this.os.write(cmdString);
            this.os.write('\n');
            this.os.flush();
        }
    }

    @Override
    public void pollPlayback() throws IOException {
        getTimePos();
    }

    @Override
    public void seek(int seekPos, boolean abs) throws IOException {
        sendCmd("seek", Integer.toString(seekPos), abs ? "2" : "1");
    }

    @Override
    public void osd(String action) throws IOException {
        sendCmd("menu", action);
    }

    @Override
    public void playFile(PlayerItem item) throws IOException {

        this.playbackInfo.item = item;

        String arg = quoteString(item.mediaPath);

        // This will abort playback of the current item.
        //
        sendCmd("loadfile", arg);
    }

    private String quoteString(String s) {
        return "\"".concat(s.replace("\"", "\\\"")).concat("\"");
    }
}
