package de.ellpeck.rockbottom.net.server;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.event.EventResult;
import de.ellpeck.rockbottom.api.event.impl.ResetMovedPlayerEvent;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.api.net.packet.toclient.PacketTileEntityData;
import de.ellpeck.rockbottom.api.render.IPlayerDesign;
import de.ellpeck.rockbottom.api.tile.entity.TileEntity;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.world.IChunk;
import de.ellpeck.rockbottom.net.packet.toclient.*;
import de.ellpeck.rockbottom.world.World;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import io.netty.channel.Channel;
import org.newdawn.slick.util.Log;

import java.util.UUID;

public class ConnectedPlayer extends EntityPlayer{

    private final Channel channel;

    private int lastHealth;

    private double lastCalcX;
    private double lastCalcY;
    private int fallCalcTicks;
    private int climbingCalcTicks;

    public ConnectedPlayer(World world, UUID uniqueId, IPlayerDesign design, Channel channel){
        super(world, uniqueId, design);
        this.channel = channel;
    }

    public static void disconnectPlayer(IGameInstance game, AbstractEntityPlayer player){
        game.getWorld().savePlayer(player);
        game.getWorld().removeEntity(player);

        Log.info("Saving and removing disconnected player "+player.getName()+" with id "+player.getUniqueId()+" from world");

        RockBottomAPI.getNet().sendToAllPlayers(player.world, new PacketPlayerConnection(player.getName(), true));
        if(!game.isDedicatedServer()){
            PacketPlayerConnection.display(game, true, player.getName());
        }
    }

    @Override
    public void update(IGameInstance game){
        super.update(game);

        if(this.ticksExisted%80 == 0){
            if(!RockBottomAPI.getNet().getConnectedClients().contains(this.channel)){
                game.scheduleAction(() -> {
                    disconnectPlayer(game, this);
                    return true;
                });
            }

            double distanceSq = Util.distanceSq(this.lastCalcX, this.lastCalcY, this.x, this.y);
            double maxDist = 1.25*this.fallCalcTicks
                    +CLIMB_SPEED*this.climbingCalcTicks
                    +MOVE_SPEED*(80-this.fallCalcTicks-this.climbingCalcTicks);

            ResetMovedPlayerEvent event = new ResetMovedPlayerEvent(this, this.lastCalcX, this.lastY, this.fallCalcTicks, this.climbingCalcTicks, distanceSq, maxDist);
            boolean cancelled = RockBottomAPI.getEventHandler().fireEvent(event) == EventResult.CANCELLED;
            distanceSq = event.distanceSqMoved;
            maxDist = event.allowedDefaultDistance;

            if(!cancelled && distanceSq > maxDist*maxDist){
                this.x = this.lastCalcX;
                this.y = this.lastCalcY;

                this.motionX = 0;
                this.motionY = 0;
                this.fallAmount = 0;

                this.sendPacket(new PacketEntityUpdate(this.getUniqueId(), this.x, this.y, this.motionX, this.motionY, this.facing));
                Log.warn("Player "+this.getName()+" with id "+this.getUniqueId()+" moved a distance of "+Math.sqrt(distanceSq)+" which is more than the max "+maxDist+", moving them back");
            }
            else{
                this.lastCalcX = this.x;
                this.lastCalcY = this.y;
            }

            this.fallCalcTicks = 0;
            this.climbingCalcTicks = 0;
        }

        if(this.isClimbing){
            this.climbingCalcTicks++;
        }
        else if(this.fallAmount >= 40){
            this.fallCalcTicks++;
        }

        if(this.lastX != this.x || this.lastY != this.y){
            RockBottomAPI.getNet().sendToAllPlayersExcept(this.world, new PacketEntityUpdate(this.getUniqueId(), this.x, this.y, this.motionX, this.motionY, this.facing), this);

            this.lastX = this.x;
            this.lastY = this.y;
        }

        if(this.getHealth() != this.lastHealth && this.world.getWorldInfo().totalTimeInWorld%10 == 0){
            this.lastHealth = this.getHealth();

            if(RockBottomAPI.getNet().isServer()){
                this.sendPacket(new PacketHealth(this.getHealth()));
            }
        }
    }

    @Override
    public void setPos(double x, double y){
        super.setPos(x, y);

        this.lastCalcX = x;
        this.lastCalcY = y;
        this.fallCalcTicks = 0;
    }

    @Override
    public boolean doesSync(){
        return false;
    }

    @Override
    public void sendPacket(IPacket packet){
        if(this.channel != null){
            this.channel.writeAndFlush(packet);
        }
    }

    @Override
    public void onChunkLoaded(IChunk chunk){
        Log.debug("Sending chunk at "+chunk.getGridX()+", "+chunk.getGridY()+" to player "+this.getName()+" with id "+this.getUniqueId());

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

    @Override
    public void onChunkUnloaded(IChunk chunk){
        Log.debug("Sending chunk unloading packet for chunk at "+chunk.getGridX()+", "+chunk.getGridY()+" to player "+this.getName()+" with id "+this.getUniqueId());

        this.sendPacket(new PacketChunkUnload(chunk.getGridX(), chunk.getGridY()));
    }
}
