package de.ellpeck.rockbottom.net.server;

import de.ellpeck.rockbottom.RockBottom;
import de.ellpeck.rockbottom.data.DataManager;
import de.ellpeck.rockbottom.net.NetHandler;
import de.ellpeck.rockbottom.net.decode.PacketDecoder;
import de.ellpeck.rockbottom.net.encode.PacketEncoder;
import de.ellpeck.rockbottom.data.settings.CommandPermissions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.compression.FastLzFrameDecoder;
import io.netty.handler.codec.compression.FastLzFrameEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;

public class Server{

    private final EventLoopGroup group;
    public final Channel channel;

    public final ChannelGroup connectedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public CommandPermissions commandPermissions = new CommandPermissions();

    public Server(String ip, int port) throws Exception{
        DataManager manager = RockBottom.get().dataManager;
        manager.loadPropSettings(this.commandPermissions, manager.commandPermissionFile);

        this.group = NetHandler.HAS_EPOLL ?
                new EpollEventLoopGroup(0, new DefaultThreadFactory("EpollServer", true)) :
                new NioEventLoopGroup(0, new DefaultThreadFactory("NioServer", true));

        this.channel = new ServerBootstrap()
                .group(this.group)
                .channel(NetHandler.HAS_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer(){
                    @Override
                    protected void initChannel(Channel channel) throws Exception{
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);

                        channel.pipeline()
                                .addLast(new FastLzFrameDecoder())
                                .addLast(new PacketDecoder())
                                .addLast(new FastLzFrameEncoder())
                                .addLast(new PacketEncoder())
                                .addLast(new ServerNetworkHandler(Server.this));
                    }
                }).bind(ip != null ? InetAddress.getByName(ip) : null, port).syncUninterruptibly().channel();
    }

    public void shutdown(){
        this.group.shutdownGracefully();

        DataManager manager = RockBottom.get().dataManager;
        manager.savePropSettings(this.commandPermissions, manager.commandPermissionFile);
    }
}