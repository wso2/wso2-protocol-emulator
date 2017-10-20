/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.protocol.emulator.http.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.wso2.carbon.protocol.emulator.dsl.EmulatorType;
import org.wso2.carbon.protocol.emulator.http.ChannelPipelineInitializer;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientInformationContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientRequestProcessorContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.RequestResponseCorrelation;
import org.wso2.carbon.protocol.emulator.http.client.processors.HttpRequestInformationProcessor;
import org.wso2.carbon.protocol.emulator.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to initialize the Http client.
 */
public class HttpClientInitializer {
    private HttpClientInformationContext clientInformationContext;
    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private List<HttpClientRequestProcessorContext> processorContextList = new ArrayList<>();
    ;

    public HttpClientInitializer(HttpClientInformationContext clientInformationContext) {
        this.clientInformationContext = clientInformationContext;
    }

    /**
     * Initialize the HTTP client.
     * @throws Exception
     */
    public void initialize() throws Exception {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        ChannelPipelineInitializer channelPipelineInitializer = new ChannelPipelineInitializer(EmulatorType.HTTP_CLIENT,
                null);
        channelPipelineInitializer.setClientInformationContext(clientInformationContext);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(channelPipelineInitializer);

        if (clientInformationContext.getClientConfigBuilderContext().getWritingDelay() > 0) {
            bootstrap.option(ChannelOption.SO_SNDBUF, 1);
        }

        processorContextList.clear();

        for (RequestResponseCorrelation requestResponseCorrelation : clientInformationContext
                .getRequestResponseCorrelation()) {
            channelPipelineInitializer.setRequestResponseCorrelation(requestResponseCorrelation);
            HttpClientRequestProcessorContext processorContext = new HttpClientRequestProcessorContext();
            processorContext.setRequestBuilderContext(requestResponseCorrelation.getRequestBuilderContext());
            processorContext.setClientInformationContext(clientInformationContext);
            sendAndWaitForChannelClose(processorContext);
            processorContextList.add(processorContext);
        }

        shutdown();
    }

    /**
     * Initialize the HTTP client.
     * @throws Exception
     */
    public void initializeAsync() throws Exception {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        ChannelPipelineInitializer channelPipelineInitializer = new ChannelPipelineInitializer(EmulatorType.HTTP_CLIENT,
                                                                                               null);
        channelPipelineInitializer.setClientInformationContext(clientInformationContext);
        bootstrap.group(group).channel(NioSocketChannel.class).handler(channelPipelineInitializer);

        if (clientInformationContext.getClientConfigBuilderContext().getWritingDelay() > 0) {
            bootstrap.option(ChannelOption.SO_SNDBUF, 1);
        }

        processorContextList.clear();

        for (RequestResponseCorrelation requestResponseCorrelation : clientInformationContext
                .getRequestResponseCorrelation()) {
            channelPipelineInitializer.setRequestResponseCorrelation(requestResponseCorrelation);
            HttpClientRequestProcessorContext processorContext = new HttpClientRequestProcessorContext();
            processorContext.setRequestBuilderContext(requestResponseCorrelation.getRequestBuilderContext());
            processorContext.setClientInformationContext(clientInformationContext);
            sendMessage(processorContext);
            processorContextList.add(processorContext);
        }
    }

    public void shutdownClients() throws InterruptedException {
        for (HttpClientRequestProcessorContext httpClientRequestProcessorContext : processorContextList) {
            waitTillChannelClose(httpClientRequestProcessorContext);
        }

        shutdown();
    }

    private void sendMessage(HttpClientRequestProcessorContext httpClientProcessorContext)
            throws InterruptedException {
        HttpRequestInformationProcessor.process(httpClientProcessorContext);
        HttpClientConfigBuilderContext clientConfigBuilderContext = httpClientProcessorContext
                .getClientInformationContext().getClientConfigBuilderContext();
        ValidationUtil.validateMandatoryParameters(clientConfigBuilderContext);

        Channel channel = bootstrap.connect(clientConfigBuilderContext.getHost(), clientConfigBuilderContext.getPort())
                              .sync().channel();
        channel.writeAndFlush(httpClientProcessorContext.getRequest());
        httpClientProcessorContext.setChannel(channel);
    }

    /**
     * Send the request using HTTP client instance.
     *
     * @param httpClientProcessorContext
     * @throws Exception
     */
    private void sendAndWaitForChannelClose(HttpClientRequestProcessorContext httpClientProcessorContext)
            throws Exception {
        sendMessage(httpClientProcessorContext);
        waitTillChannelClose(httpClientProcessorContext);
    }

    private void waitTillChannelClose(
            HttpClientRequestProcessorContext httpClientProcessorContext) throws InterruptedException {
        httpClientProcessorContext.getChannel().closeFuture().sync();
    }

    /**
     * Shutdown the HTTP client instance.
     */
    public void shutdown() throws InterruptedException {
        group.shutdownGracefully().await();
    }
}
