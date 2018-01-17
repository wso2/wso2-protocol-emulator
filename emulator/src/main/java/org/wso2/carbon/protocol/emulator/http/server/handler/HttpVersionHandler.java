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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED;

/**
 * This handler checks the http version to see if it can be handled.
 */
public class HttpVersionHandler extends ChannelInboundHandlerAdapter {
    private HttpVersion httpVersion;
    private boolean responded = false;

    public HttpVersionHandler(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        versionCheck(ctx, msg);
    }


    private void versionCheck(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest && !httpVersion.equals(((HttpRequest) msg).getProtocolVersion())) {
            FullHttpResponse response = populate505VersionError(httpVersion, ((HttpRequest) msg).getProtocolVersion());
            ctx.writeAndFlush(response);
            responded = true;
        } else if (msg instanceof HttpRequest) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof HttpContent && !responded) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof LastHttpContent && !responded) {
            ctx.fireChannelRead(msg);
        } else if (msg instanceof LastHttpContent && responded) {
            responded = false;
        }
    }

    private FullHttpResponse populate505VersionError(HttpVersion httpVersion, HttpVersion protocolVersion) {
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HTTP_VERSION_NOT_SUPPORTED,
                                                                Unpooled.copiedBuffer("The " + protocolVersion +
                                                                                              " is not supported " +
                                                                                              "because of the " +
                                                                                              "configurations\n",
                                                                                      CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

}
