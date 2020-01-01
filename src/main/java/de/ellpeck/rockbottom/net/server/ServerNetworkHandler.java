package de.ellpeck.rockbottom.net.server;

import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.net.chat.component.ChatComponentTranslation;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.log.Logging;
import de.ellpeck.rockbottom.net.packet.toclient.PacketReject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.Level;

public class ServerNetworkHandler extends SimpleChannelInboundHandler<IPacket> {

    private final Server server;

    public ServerNetworkHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.server.connectedChannels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.server.connectedChannels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket packet) {
        RockBottomAPI.getGame().enqueueAction((game, context) -> {
            try {
                packet.handle(game, context);
            } catch (Exception e) {
                RockBottomAPI.logger().log(Level.ERROR, "There was an error handling a packet on the server, closing the connection to the client", e);
                this.notifyAndClose(context, e);
            }
        }, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logging.nettyLogger.log(Level.ERROR, "The server network handler caught an exception, closing the connection to the client", cause);
        this.notifyAndClose(ctx, cause);
    }

    private void notifyAndClose(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        if (message == null) {
            message = "Unspecified reason";
        }
        ctx.writeAndFlush(new PacketReject(new ChatComponentTranslation(ResourceName.intern("info.reject.exception"), message)));
        ctx.disconnect();
    }
}
