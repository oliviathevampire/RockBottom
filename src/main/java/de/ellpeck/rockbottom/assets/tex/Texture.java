package de.ellpeck.rockbottom.assets.tex;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import de.ellpeck.rockbottom.api.IRenderer;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.ITexture;
import de.ellpeck.rockbottom.api.util.Colors;
import de.ellpeck.rockbottom.api.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Texture implements ITexture{

    public static int binds;
    private static Texture boundTexture;

    private Random rand;
    private Map<String, JsonElement> additionalData;
    private List<ITexture> variations;

    private final int id;
    private int textureWidth;
    private int textureHeight;
    private int renderWidth;
    private int renderHeight;
    private int renderOffsetX;
    private int renderOffsetY;
    private ByteBuffer pixelData;

    public Texture(int textureWidth, int textureHeight, ByteBuffer data){
        this(GL11.GL_NEAREST);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.pixelData = data;

        this.init();
    }

    public Texture(InputStream stream) throws Exception{
        this(GL11.GL_NEAREST);
        this.load(stream);
    }

    public Texture(int filter){
        this.id = GL11.glGenTextures();

        this.param(GL11.GL_TEXTURE_MIN_FILTER, filter);
        this.param(GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    private Texture(int id, int textureWidth, int textureHeight, ByteBuffer pixelData){
        this.id = id;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.pixelData = pixelData;
    }

    @Override
    public Texture getSubTexture(int x, int y, int width, int height){
        return this.getSubTexture(x, y, width, height, true, true);
    }

    @Override
    public Texture getSubTexture(int x, int y, int width, int height, boolean inheritVariations, boolean inheritData){
        Texture sub = new Texture(this.getId(), this.textureWidth, this.textureHeight, this.pixelData);

        sub.renderOffsetX = x;
        sub.renderOffsetY = y;
        sub.renderWidth = width;
        sub.renderHeight = height;

        if(inheritData){
            sub.setAdditionalData(this.additionalData);
        }
        if(inheritVariations){
            if(this.variations != null){
                List<ITexture> newVariations = new ArrayList<>();
                for(ITexture variation : this.variations){
                    newVariations.add(variation.getSubTexture(x, y, width, height, inheritVariations, inheritData));
                }
                sub.setVariations(newVariations);
            }
        }

        return sub;
    }

    public void setAdditionalData(Map<String, JsonElement> data){
        this.additionalData = data;
    }

    public void setVariations(List<ITexture> variations){
        this.variations = variations;
    }

    @Override
    public JsonElement getAdditionalData(String name){
        return this.additionalData != null ? this.additionalData.get(name) : null;
    }

    @Override
    public void draw(float x, float y){
        this.draw(x, y, 1F);
    }

    @Override
    public void draw(float x, float y, float scale){
        this.draw(x, y, this.textureWidth*1F, this.textureHeight*1F);
    }

    @Override
    public void draw(float x, float y, float width, float height){
        this.draw(x, y, width, height, Colors.WHITE);
    }

    @Override
    public void draw(float x, float y, float width, float height, int[] light){
        this.draw(x, y, width, height, light, Colors.WHITE);
    }

    @Override
    public void draw(float x, float y, float width, float height, int filter){
        this.draw(x, y, x+width, y+height, 0, 0, this.renderWidth, this.renderHeight, null, filter);
    }

    @Override
    public void draw(float x, float y, float width, float height, int[] light, int filter){
        this.draw(x, y, x+width, y+height, 0, 0, this.renderWidth, this.renderHeight, light, filter);
    }

    @Override
    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2){
        this.draw(x, y, x2, y2, srcX, srcY, srcX2, srcY2, Colors.WHITE);
    }

    @Override
    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2, int[] light){
        this.draw(x, y, x2, y2, srcX, srcY, srcX2, srcY2, light, Colors.WHITE);
    }

    @Override
    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2, int filter){
        this.draw(x, y, x2, y2, srcX, srcY, srcX2, srcY2, null, filter);
    }

    @Override
    public void draw(float x, float y, float x2, float y2, float srcX, float srcY, float srcX2, float srcY2, int[] light, int filter){
        this.draw(x, y, x, y2, x2, y2, x2, y, srcX, srcY, srcX2, srcY2, light, filter);
    }

    @Override
    public void draw(float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, float srcX, float srcY, float srcX2, float srcY2, int[] light, int filter){

        IRenderer renderer = RockBottomAPI.getGame().getRenderer();
        renderer.addTexturedRegion(this, x, y, x2, y2, x3, y3, x4, y4, srcX, srcY, srcX2, srcY2, light, filter);
    }

    @Override
    public int getTextureColor(int x, int y){
        int offset = (x+(y*this.textureWidth))*4;
        return Colors.rgb(this.pixelData.get(offset), this.pixelData.get(offset+1), this.pixelData.get(offset+2), this.pixelData.get(offset+3));
    }

    @Override
    public ITexture getVariation(Random random){
        if(this.variations == null){
            return this;
        }
        else{
            int index = random.nextInt(this.variations.size()+1);

            if(index == 0){
                return this;
            }
            else{
                return this.variations.get(index-1);
            }
        }
    }

    @Override
    public ITexture getPositionalVariation(int x, int y){
        if(this.variations == null){
            return this;
        }
        else{
            if(this.rand == null){
                this.rand = new Random();
            }

            this.rand.setSeed(Util.scrambleSeed(x, y));
            return this.getVariation(this.rand);
        }
    }

    private void load(InputStream stream) throws Exception{
        byte[] input = ByteStreams.toByteArray(stream);
        ByteBuffer data = BufferUtils.createByteBuffer(input.length);
        data.put(input);
        data.flip();

        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer width = stack.mallocInt(1);
        IntBuffer height = stack.mallocInt(1);

        this.pixelData = STBImage.stbi_load_from_memory(data, width, height, stack.mallocInt(1), 4);
        if(this.pixelData == null){
            throw new IOException("Failed to load texture:\n"+STBImage.stbi_failure_reason());
        }

        this.textureWidth = width.get();
        this.textureHeight = height.get();

        this.renderWidth = this.textureWidth;
        this.renderHeight = this.textureHeight;

        stack.pop();

        this.init();
    }

    @Override
    public void bind(){
        if(boundTexture != this){
            this.forceBind();
        }
    }

    @Override
    public void forceBind(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.id);
        boundTexture = this;

        binds++;
    }

    private void init(){
        this.bind();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.textureWidth, this.textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.pixelData);
    }

    @Override
    public void param(int param, int value){
        this.bind();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, param, value);
    }

    @Override
    public int getId(){
        return this.id;
    }

    @Override
    public int getTextureWidth(){
        return this.textureWidth;
    }

    @Override
    public int getTextureHeight(){
        return this.textureHeight;
    }

    @Override
    public ByteBuffer getPixelData(){
        return this.pixelData;
    }

    @Override
    public void unbind(){
        if(boundTexture == this){
            this.forceUnbind();
        }
    }

    @Override
    public void forceUnbind(){
        unbindAll();
    }

    public static void unbindAll(){
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        boundTexture = null;
    }

    @Override
    public void dispose(){
        this.unbind();
        GL11.glDeleteTextures(this.id);
    }

    @Override
    public int getRenderWidth(){
        return this.renderWidth;
    }

    @Override
    public int getRenderHeight(){
        return this.renderHeight;
    }

    @Override
    public int getRenderOffsetX(){
        return this.renderOffsetX;
    }

    @Override
    public int getRenderOffsetY(){
        return this.renderOffsetY;
    }
}
