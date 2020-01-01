package de.ellpeck.rockbottom.net.packet.toclient;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.net.NetUtil;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketChangeWorld implements IPacket {

    private ResourceName subName;
    private DataSet worldData;

    public PacketChangeWorld(DataSet worldData, ResourceName subName) {
        this.worldData = worldData;
        this.subName = subName;
    }

    public PacketChangeWorld() {
    }

    @Override
    public void toBuffer(ByteBuf buf) {
        NetUtil.writeSetToBuffer(this.worldData, buf);
        if (this.subName != null) {
            NetUtil.writeStringToBuffer(this.subName.toString(), buf);
        }
    }

    @Override
    public void fromBuffer(ByteBuf buf) {
        this.worldData = new DataSet();
        NetUtil.readSetFromBuffer(this.worldData, buf);
        if (buf.isReadable()) {
            this.subName = new ResourceName(NetUtil.readStringFromBuffer(buf));
        }
    }

    @Override
    public void handle(IGameInstance game, ChannelHandlerContext context) {
        IWorld world = game.getWorld();
        if (world != null) {
            game.changeWorld(this.subName, this.worldData);
            RockBottomAPI.logger().info("Travelling to different world");
        }
    }
}
