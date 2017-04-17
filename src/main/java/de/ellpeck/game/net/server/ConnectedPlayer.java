package de.ellpeck.game.net.server;

import de.ellpeck.game.Game;
import de.ellpeck.game.net.NetHandler;
import de.ellpeck.game.net.packet.IPacket;
import de.ellpeck.game.net.packet.toclient.PacketChunk;
import de.ellpeck.game.net.packet.toclient.PacketEntityChange;
import de.ellpeck.game.net.packet.toclient.PacketTileEntityData;
import de.ellpeck.game.world.Chunk;
import de.ellpeck.game.world.World;
import de.ellpeck.game.world.entity.Entity;
import de.ellpeck.game.world.entity.player.EntityPlayer;
import de.ellpeck.game.world.tile.entity.TileEntity;
import io.netty.channel.Channel;
import org.newdawn.slick.util.Log;

import java.util.UUID;

public class ConnectedPlayer extends EntityPlayer{

    private final Channel channel;

    public ConnectedPlayer(World world, UUID uniqueId, Channel channel){
        super(world, uniqueId);
        this.channel = channel;
    }

    @Override
    public void update(Game game){
        super.update(game);

        if(this.ticksExisted%80 == 0){
            if(!NetHandler.getConnectedClients().contains(this.channel)){
                game.scheduleAction(() -> {
                    game.world.removeEntity(this);
                    Log.info("Removing disconnected player with id "+this.getUniqueId()+" from world");

                    return true;
                });
            }
        }
    }

    @Override
    public int getUpdateFrequency(){
        return 80;
    }

    @Override
    public void sendPacket(IPacket packet){
        if(this.channel != null){
            this.channel.writeAndFlush(packet);
        }
    }

    @Override
    public void onChunkNewlyLoaded(Chunk chunk){
        Log.info("Sending chunk at "+chunk.gridX+", "+chunk.gridY+" to player with id "+this.getUniqueId());

        this.sendPacket(new PacketChunk(chunk));

        for(Entity entity : chunk.getAllEntities()){
            if(entity != this){
                this.sendPacket(new PacketEntityChange(entity, false));
            }
        }

        for(TileEntity tile : chunk.getAllTileEntities()){
            this.sendPacket(new PacketTileEntityData(tile.x, tile.y, tile));
        }
    }
}