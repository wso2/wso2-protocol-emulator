package org.wso2.carbon.protocol.emulator.http.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.wso2.carbon.protocol.emulator.http.server.contexts.HttpServerInformationContext;

import java.util.concurrent.TimeUnit;


/**
 * This handler imposes a reading delay on the message.
 */
public class SlowReadingServerHandler extends ChannelInboundHandlerAdapter {
    private HttpServerInformationContext serverInformationContext;
    private static final Logger log = Logger.getLogger(SlowReadingServerHandler.class);

    public SlowReadingServerHandler(HttpServerInformationContext serverInformationContext) {
        this.serverInformationContext = serverInformationContext;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int delay = serverInformationContext.getServerConfigBuilderContext().getReadingDelay();
        if (delay > 0) {
            TimeUnit.MILLISECONDS.sleep(delay);
        }

        ctx.fireChannelRead(msg);
    }

}
