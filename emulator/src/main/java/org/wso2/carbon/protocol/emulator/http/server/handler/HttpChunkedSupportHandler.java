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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * This handler checks the http version to see if it can be handled.
 */
public class HttpChunkedSupportHandler extends ChannelInboundHandlerAdapter {

    private boolean connectionClosed = false;

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest && HttpHeaders.Values.CHUNKED.equals(
                ((HttpRequest) msg).headers().get(HttpHeaders.Names.TRANSFER_ENCODING))) {
            ctx.close();
            connectionClosed = true;
        } else if (msg instanceof HttpRequest) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof HttpContent && !connectionClosed) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof LastHttpContent && !connectionClosed) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof LastHttpContent && connectionClosed) {
            connectionClosed = false;
        }
    }
}



