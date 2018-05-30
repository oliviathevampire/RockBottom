package de.ellpeck.rockbottom.world.entity;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.render.entity.IEntityRenderer;
import de.ellpeck.rockbottom.api.util.Colors;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.particle.ParticleFirework;
import de.ellpeck.rockbottom.render.entity.FireworkEntityRenderer;

public class EntityFirework extends Entity{

    private final IEntityRenderer renderer = new FireworkEntityRenderer();
    private int lifetime;

    public EntityFirework(IWorld world){
        super(world);
        this.lifetime = Util.RANDOM.nextInt(30)+20;
    }

    @Override
    public IEntityRenderer getRenderer(){
        return this.renderer;
    }

    @Override
    public void applyMotion(){
        this.motionY = 0.2;
        this.motionX *= 0.99;
    }

    @Override
    public void update(IGameInstance game){
        super.update(game);

        if(!this.world.isClient()){
            this.lifetime--;
            if(this.lifetime <= 0){
                this.kill();
            }
        }

        if(!game.isDedicatedServer()){
            if(Util.RANDOM.nextBoolean()){
                game.getParticleManager().addSmokeParticle(this.world, this.getX(), this.getY(), Util.RANDOM.nextGaussian()*0.05F, -0.1F, 0.15F);
            }
        }
    }

    @Override
    public void setDead(boolean dead){
        super.setDead(dead);

        if(dead){
            IGameInstance game = RockBottomAPI.getGame();
            if(!game.isDedicatedServer()){
                int particleAmount = Util.RANDOM.nextInt(100)+20;
                int color = Colors.rainbow(Util.RANDOM.nextFloat()*256F);

                for(int i = 0; i < particleAmount; i++){
                    double motionX = Util.RANDOM.nextGaussian()*0.05D;
                    double motionY = Util.RANDOM.nextGaussian()*0.05D;
                    int maxLife = Util.RANDOM.nextInt(50)+10;

                    game.getParticleManager().addParticle(new ParticleFirework(this.world, this.getX(), this.getY(), motionX, motionY, maxLife, color));
                }
            }
        }
    }

    @Override
    public void save(DataSet set){
        super.save(set);
        set.addInt("lifetime", this.lifetime);
    }

    @Override
    public void load(DataSet set){
        super.load(set);
        this.lifetime = set.getInt("lifetime");
    }
}
