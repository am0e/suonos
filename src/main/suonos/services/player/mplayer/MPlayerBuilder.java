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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import suonos.services.player.PlayerBuilder;
import suonos.services.player.PlayerCallback;
import suonos.services.player.PlayerInstance;

public class MPlayerBuilder implements PlayerBuilder {

    @Override
    public PlayerInstance createInst(PlayerCallback callback) throws IOException {
        List<String> args = new ArrayList<>();

        args.add("mplayer");
        args.add("-noconfig");
        args.add("user:system");
        args.add("-slave");
        args.add("-quiet");
        args.add("-identify");
        args.add("-noterm-osd");
        args.add("-msgmodule");
        args.add("-msglevel");
        args.add("all=5");
        args.add("-msglevel");
        args.add("global=6");
        args.add("-input");
        args.add("nodefault-bindings");
        args.add("-input");
        args.add("conf=./conf/mplayer.conf");
        args.add("-idle");

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.redirectOutput();
        pb.environment().put("TERM", "xterm-256color");
        pb.environment().put("MPLAYER_CHARSET", "noconv");

        MPlayerProcessInstance inst = new MPlayerProcessInstance(pb, callback);
        return inst;
    }
}
