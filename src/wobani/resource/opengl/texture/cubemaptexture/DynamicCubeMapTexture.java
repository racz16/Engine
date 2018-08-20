package wobani.resource.opengl.texture.cubemaptexture;

import org.joml.*;
import org.lwjgl.opengl.*;
import wobani.resource.*;
import wobani.resource.opengl.texture.texture2d.*;
import wobani.toolbox.annotation.*;

public class DynamicCubeMapTexture extends CubeMapTexture{

    public DynamicCubeMapTexture(@NotNull Vector2i size){
        super(new ResourceId());
        if(size.x <= 0 || size.y <= 0){
            throw new IllegalArgumentException("Width and height must be positive");
        }
        createTexture(getTarget(), getSampleCount());
        setSize(size);
        setDataSize(size.x * size.y * 4 * 4 * 6);

        bind();

        setFilter(TextureFilterType.MINIFICATION, getFilter(TextureFilterType.MINIFICATION));
        setFilter(TextureFilterType.MAGNIFICATION, getFilter(TextureFilterType.MAGNIFICATION));
        setWrap(TextureWrapDirection.WRAP_U, TextureWrap.CLAMP_TO_EDGE);
        setWrap(TextureWrapDirection.WRAP_V, TextureWrap.CLAMP_TO_EDGE);
        setBorderColor(getBorderColor());

        for(int i = 0; i < 6; i++){
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (float[]) null);
        }
    }

    @Override
    protected int createTextureId(){
        //TODO instead of this, create pool
        return GL45.glCreateTextures(getTarget());
    }

    public void setSide(@NotNull CubeMapSide side, @NotNull DynamicTexture2D texture){
        //        GL11.glTexImage2D(side.getCode(), 0, GL11.GL_RGB, size.x, size.y, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
    }

    private int getTarget(){
        return GL13.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    protected String getTypeName(){
        return "Dynamic CubeMap Texture";
    }

    @Override
    public int getCachedDataSize(){
        return 0;
    }

    @Override
    public void release(){
        super.release();
    }

    @Override
    public boolean isUsable(){
        return getId() != -1;
    }
}
