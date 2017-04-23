package de.ellpeck.rockbottom.render;

import de.ellpeck.rockbottom.Constants;
import de.ellpeck.rockbottom.RockBottom;
import de.ellpeck.rockbottom.assets.AssetManager;
import de.ellpeck.rockbottom.particle.ParticleManager;
import de.ellpeck.rockbottom.render.entity.IEntityRenderer;
import de.ellpeck.rockbottom.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.util.Util;
import de.ellpeck.rockbottom.world.Chunk;
import de.ellpeck.rockbottom.world.TileLayer;
import de.ellpeck.rockbottom.world.World;
import de.ellpeck.rockbottom.world.entity.Entity;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.world.entity.player.InteractionManager;
import de.ellpeck.rockbottom.world.tile.Tile;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorldRenderer{

    public static final Color[] SKY_COLORS = new Color[50];
    public static final Color[] BACKGROUND_COLORS = new Color[Constants.MAX_LIGHT+1];
    public static final Color[] MAIN_COLORS = new Color[Constants.MAX_LIGHT+1];

    public static void init(){
        float step = 1F/(Constants.MAX_LIGHT+1);
        for(int i = 0; i <= Constants.MAX_LIGHT; i++){
            float modifier = step+i*step;

            MAIN_COLORS[i] = new Color(modifier, modifier, modifier, 1F);
            BACKGROUND_COLORS[i] = new Color(modifier*0.5F, modifier*0.5F, modifier*0.5F, 1F);
        }

        Color sky = new Color(0x4C8DFF);
        for(int i = 0; i < SKY_COLORS.length; i++){
            float percent = (float)i/(float)SKY_COLORS.length;
            SKY_COLORS[i] = sky.darker(1F-percent);
        }
    }

    public void render(RockBottom game, AssetManager manager, ParticleManager particles, Graphics g, World world, EntityPlayer player, InteractionManager input){
        g.scale(game.settings.renderScale, game.settings.renderScale);

        int skyLight = (int)(world.getSkylightModifier()*(SKY_COLORS.length-1));
        g.setBackground(SKY_COLORS[game.isLightDebug ? SKY_COLORS.length-1 : skyLight]);

        double width = game.getWidthInWorld();
        double height = game.getHeightInWorld();
        double worldAtScreenX = player.x-width/2;
        double worldAtScreenY = -player.y-height/2;

        g.translate((float)-worldAtScreenX, (float)-worldAtScreenY);

        List<Entity> entities = new ArrayList<>();

        int topLeftX = Util.toGridPos(worldAtScreenX);
        int topLeftY = Util.toGridPos(-worldAtScreenY+1);
        int bottomRightX = Util.toGridPos(worldAtScreenX+width);
        int bottomRightY = Util.toGridPos(-worldAtScreenY-height);

        int minX = Math.min(topLeftX, bottomRightX);
        int minY = Math.min(topLeftY, bottomRightY);
        int maxX = Math.max(topLeftX, bottomRightX);
        int maxY = Math.max(topLeftY, bottomRightY);

        for(int gridX = minX; gridX <= maxX; gridX++){
            for(int gridY = minY; gridY <= maxY; gridY++){
                if(world.isChunkLoaded(gridX, gridY)){
                    Chunk chunk = world.getChunkFromGridCoords(gridX, gridY);

                    for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
                            int tileX = chunk.x+x;
                            int tileY = chunk.y+y;

                            if(tileX >= worldAtScreenX-1 && -tileY >= worldAtScreenY-1 && tileX < worldAtScreenX+width && -tileY < worldAtScreenY+height){
                                Tile tile = chunk.getTileInner(x, y);
                                byte light = chunk.getCombinedLightInner(x, y);

                                if(!game.isBackgroundDebug){
                                    if(!tile.isFullTile() || game.isForegroundDebug){
                                        Tile tileBack = chunk.getTileInner(TileLayer.BACKGROUND, x, y);
                                        ITileRenderer rendererBack = tileBack.getRenderer();
                                        if(rendererBack != null){
                                            rendererBack.render(game, manager, g, world, tileBack, tileX, tileY, tileX, -tileY, BACKGROUND_COLORS[game.isLightDebug ? Constants.MAX_LIGHT : light]);

                                            if(input.breakingLayer == TileLayer.BACKGROUND){
                                                this.doBreakAnimation(input, manager, tileX, tileY);
                                            }
                                        }
                                    }
                                }

                                if(!game.isForegroundDebug){
                                    ITileRenderer renderer = tile.getRenderer();
                                    if(renderer != null){
                                        renderer.render(game, manager, g, world, tile, tileX, tileY, tileX, -tileY, MAIN_COLORS[game.isLightDebug ? Constants.MAX_LIGHT : light]);

                                        if(input.breakingLayer == TileLayer.MAIN){
                                            this.doBreakAnimation(input, manager, tileX, tileY);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    entities.addAll(chunk.getAllEntities());
                }
            }
        }

        entities.stream().sorted(Comparator.comparingInt(Entity:: getRenderPriority)).forEach(entity -> {
            if(entity.shouldRender()){
                IEntityRenderer renderer = entity.getRenderer();
                if(renderer != null){
                    int light = world.getCombinedLight(Util.floor(entity.x), Util.floor(entity.y));
                    renderer.render(game, manager, g, world, entity, (float)entity.x, (float)-entity.y+1F, MAIN_COLORS[game.isLightDebug ? Constants.MAX_LIGHT : light]);
                }
            }
        });

        particles.render(game, manager, g, world);

        g.resetTransform();
    }

    private void doBreakAnimation(InteractionManager input, AssetManager manager, int tileX, int tileY){
        if(input.breakProgress > 0){
            if(tileX == input.breakTileX && tileY == input.breakTileY){
                Image brk = manager.getImage("break."+Util.ceil(input.breakProgress*8F));
                brk.draw(tileX, -tileY, 1F, 1F);
            }
        }
    }
}