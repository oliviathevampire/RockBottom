package de.ellpeck.rockbottom.world.gen.biome;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;
import de.ellpeck.rockbottom.api.world.IChunk;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.gen.INoiseGen;
import de.ellpeck.rockbottom.api.world.gen.biome.BiomeBasic;
import de.ellpeck.rockbottom.api.world.gen.biome.level.BiomeLevel;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

public class ShadowPlainsGrasslands extends BiomeBasic {

    public ShadowPlainsGrasslands(ResourceName name, int weight, BiomeLevel... levels) {
        super(name, weight, levels);
    }

    public static TileState getState(TileLayer layer, int y, int height, int stoneHeight) {
        if (layer == TileLayer.MAIN || layer == TileLayer.BACKGROUND) {
            if (y == height && layer == TileLayer.MAIN) {
                return GameContent.SHADOW_GRASS_TILE.getDefState();
            } else if (y <= height) {
                if (y >= stoneHeight) {
                    return GameContent.SHADOW_DIRT_TILE.getDefState();
                } else {
                    return GameContent.SHADOW_STONE_TILE.getDefState();
                }
            }
        }
        return GameContent.TILE_AIR.getDefState();
    }

    @Override
    public TileState getState(IWorld world, IChunk chunk, int x, int y, TileLayer layer, INoiseGen noise, int surfaceHeight) {
        int stoneHeight = surfaceHeight - Util.ceil(noise.make2dNoise((chunk.getX() + x) / 5D, 0D) * 3D) - 2;
        return getState(layer, chunk.getY() + y, surfaceHeight, stoneHeight);
    }

    @Override
    public boolean hasUndergroundFeatures(IWorld world, IChunk chunk) {
        return true;
    }

    @Override
    public TileState getFillerTile(IWorld world, IChunk chunk, int x, int y) {
        return GameContent.SHADOW_STONE_TILE.getDefState();
    }
}
