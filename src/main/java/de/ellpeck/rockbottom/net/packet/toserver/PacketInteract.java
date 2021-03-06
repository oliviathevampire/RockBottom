package de.ellpeck.rockbottom.net.packet.toserver;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.world.entity.player.InteractionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

public class PacketInteract implements IPacket {

    private UUID playerId;
    private TileLayer layer;
    private double x;
    private double y;
    private boolean isDestroyKey;

    public PacketInteract(UUID playerId, TileLayer layer, double x, double y, boolean isDestroyKey) {
        this.playerId = playerId;
        this.layer = layer;
        this.x = x;
        this.y = y;
        this.isDestroyKey = isDestroyKey;
    }

    public PacketInteract() {

    }

    @Override
    public void toBuffer(ByteBuf buf) {
        buf.writeLong(this.playerId.getMostSignificantBits());
        buf.writeLong(this.playerId.getLeastSignificantBits());
        buf.writeInt(this.layer.index());
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeBoolean(this.isDestroyKey);
    }

    @Override
    public void fromBuffer(ByteBuf buf) {
        this.playerId = new UUID(buf.readLong(), buf.readLong());
        this.layer = TileLayer.getAllLayers().get(buf.readInt());
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.isDestroyKey = buf.readBoolean();
    }

    @Override
    public void handle(IGameInstance game, ChannelHandlerContext context) {
        if (game.getWorld() != null) {
            AbstractEntityPlayer player = game.getWorld().getPlayer(this.playerId);
            if (player != null) {
                InteractionManager.interact(player, this.layer, this.x, this.y, this.isDestroyKey);
            }
        }
    }
}
