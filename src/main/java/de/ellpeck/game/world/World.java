package de.ellpeck.game.world;

import de.ellpeck.game.Constants;
import de.ellpeck.game.Game;
import de.ellpeck.game.data.set.DataSet;
import de.ellpeck.game.data.set.part.PartDataSet;
import de.ellpeck.game.util.BoundBox;
import de.ellpeck.game.util.Direction;
import de.ellpeck.game.util.MathUtil;
import de.ellpeck.game.util.Vec2;
import de.ellpeck.game.world.Chunk.TileLayer;
import de.ellpeck.game.world.entity.Entity;
import de.ellpeck.game.world.entity.player.EntityPlayer;
import de.ellpeck.game.world.tile.Tile;
import de.ellpeck.game.world.tile.entity.TileEntity;

import java.util.*;

public class World implements IWorld{

    public final Random rand = new Random();
    public final Random generatorRandom;

    public final List<Chunk> chunks = new ArrayList<>();
    private final Map<Vec2, Chunk> chunkLookup = new HashMap<>();

    public List<EntityPlayer> players = new ArrayList<>();

    public final DataSet saveData;

    public World(long seed, DataSet saveData){
        this.generatorRandom = new Random(seed);
        this.saveData = saveData;
    }

    public void update(Game game){
        for(EntityPlayer player : this.players){
            for(int x = -Constants.CHUNK_LOAD_DISTANCE; x <= Constants.CHUNK_LOAD_DISTANCE; x++){
                for(int y = -Constants.CHUNK_LOAD_DISTANCE; y <= Constants.CHUNK_LOAD_DISTANCE; y++){
                    Chunk chunk = this.getChunkFromGridCoords(player.chunkX+x, player.chunkY+y);
                    chunk.loadTimer = 250;
                }
            }
        }

        for(int i = 0; i < this.chunks.size(); i++){
            Chunk chunk = this.chunks.get(i);
            chunk.update(game);

            chunk.loadTimer--;
            if(chunk.loadTimer <= 0 || chunk.shouldUnload()){
                this.saveChunk(chunk);

                this.chunks.remove(i);
                this.chunkLookup.remove(new Vec2(chunk.getGridX(), chunk.getGridY()));
                i--;
            }
        }
    }

    @Override
    public void addEntity(Entity entity){
        Chunk chunk = this.getChunk(entity.x, entity.y);
        chunk.addEntity(entity);

        if(entity instanceof EntityPlayer){
            this.players.add((EntityPlayer)entity);
        }
    }

    @Override
    public void addTileEntity(TileEntity tile){
        Chunk chunk = this.getChunk(tile.x, tile.y);
        chunk.addTileEntity(tile);
    }

    @Override
    public void removeEntity(Entity entity){
        Chunk chunk = this.getChunk(entity.x, entity.y);
        chunk.removeEntity(entity);

        if(entity instanceof EntityPlayer){
            this.players.remove(entity);
        }
    }

    @Override
    public void removeTileEntity(int x, int y){
        Chunk chunk = this.getChunk(x, y);
        chunk.removeTileEntity(x, y);
    }

    @Override
    public TileEntity getTileEntity(int x, int y){
        Chunk chunk = this.getChunk(x, y);
        return chunk.getTileEntity(x, y);
    }

    @Override
    public List<Entity> getAllEntities(){
        List<Entity> entities = new ArrayList<>();
        for(Chunk chunk : this.chunks){
            entities.addAll(chunk.getAllEntities());
        }
        return entities;
    }

    @Override
    public List<TileEntity> getAllTileEntities(){
        List<TileEntity> tiles = new ArrayList<>();
        for(Chunk chunk : this.chunks){
            tiles.addAll(chunk.getAllTileEntities());
        }
        return tiles;
    }

    @Override
    public List<Entity> getEntities(BoundBox area){
        int minChunkX = MathUtil.toGridPos(area.getMinX())-1;
        int minChunkY = MathUtil.toGridPos(area.getMinY())-1;
        int maxChunkX = MathUtil.toGridPos(area.getMaxX())+1;
        int maxChunkY = MathUtil.toGridPos(area.getMaxY())+1;

        List<Entity> entities = new ArrayList<>();
        for(int x = minChunkX; x <= maxChunkX; x++){
            for(int y = minChunkY; y <= maxChunkY; y++){
                Chunk chunk = this.getChunkFromGridCoords(x, y);
                entities.addAll(chunk.getEntities(area));
            }
        }
        return entities;
    }

    @Override
    public List<BoundBox> getCollisions(BoundBox area){
        List<BoundBox> collisions = new ArrayList<>();

        for(int x = MathUtil.floor(area.getMinX()); x <= MathUtil.ceil(area.getMaxX()); x++){
            for(int y = MathUtil.floor(area.getMinY()); y <= MathUtil.ceil(area.getMaxY()); y++){
                Tile tile = this.getTile(x, y);

                BoundBox box = tile.getBoundBox(this, x, y);
                if(box != null && !box.isEmpty()){
                    collisions.add(box.copy().add(x, y));
                }
            }
        }

        return collisions;
    }

    public Chunk getChunk(double x, double y){
        return this.getChunkFromGridCoords(MathUtil.toGridPos(x), MathUtil.toGridPos(y));
    }

    public Chunk getChunkFromGridCoords(int gridX, int gridY){
        Chunk chunk = this.chunkLookup.get(new Vec2(gridX, gridY));

        if(chunk == null){
            DataSet set = this.saveData.getDataInPart("c_"+gridX+"_"+gridY);
            chunk = new Chunk(this, gridX, gridY, set);

            this.chunks.add(chunk);
            this.chunkLookup.put(new Vec2(gridX, gridY), chunk);
        }

        return chunk;
    }

    @Override
    public Tile getTile(int x, int y){
        return this.getTile(TileLayer.MAIN, x, y);
    }

    @Override
    public Tile getTile(TileLayer layer, int x, int y){
        Chunk chunk = this.getChunk(x, y);
        return chunk.getTile(layer, x, y);
    }

    @Override
    public byte getMeta(int x, int y){
        Chunk chunk = this.getChunk(x, y);
        return chunk.getMeta(x, y);
    }

    @Override
    public void setTile(int x, int y, Tile tile){
        this.setTile(TileLayer.MAIN, x, y, tile);
    }

    @Override
    public void setTile(TileLayer layer, int x, int y, Tile tile){
        Chunk chunk = this.getChunk(x, y);
        chunk.setTile(layer, x, y, tile);
    }

    @Override
    public void setMeta(int x, int y, int meta){
        this.setMeta(x, y, (byte)meta);
    }

    @Override
    public void setMeta(int x, int y, byte meta){
        Chunk chunk = this.getChunk(x, y);
        chunk.setMeta(x, y, meta);
    }

    public void notifyNeighborsOfChange(int x, int y){
        for(Direction direction : Direction.DIRECTIONS){
            int offX = x+direction.offsetX;
            int offY = y+direction.offsetY;

            Tile tile = this.getTile(offX, offY);
            tile.onChangeAround(this, offX, offY, x, y);
        }
    }

    public void save(){
        for(Chunk chunk : this.chunks){
            this.saveChunk(chunk);
        }
    }

    private void saveChunk(Chunk chunk){
        if(chunk.needsSave()){
            int gridX = chunk.getGridX();
            int gridY = chunk.getGridY();

            DataSet set = this.saveData.getDataInPart("c_"+gridX+"_"+gridY);
            if(set == null){
                set = new DataSet();
                this.saveData.put(new PartDataSet("c_"+gridX+"_"+gridY, set));
            }
            chunk.save(set);
        }
    }
}
