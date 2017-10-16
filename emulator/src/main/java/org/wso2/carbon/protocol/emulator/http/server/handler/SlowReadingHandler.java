/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.protocol.emulator.http.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;


/**
 * This handler imposes a reading delay on the message.
 */
public class SlowReadingHandler extends ChannelInboundHandlerAdapter {
    private int delay;

    public SlowReadingHandler(int delay) {
        this.delay = delay;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TimeUnit.MILLISECONDS.sleep(delay);
        ctx.fireChannelRead(msg);
    }

}
