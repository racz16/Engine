package resources.environmentProbes;

import org.joml.*;
import resources.textures.cubeMapTexture.*;
import toolbox.annotations.*;

public class StaticEnvironmentProbe implements EnvironmentProbe {

    private StaticCubeMapTexture cubeMap;

    public StaticEnvironmentProbe(@NotNull StaticCubeMapTexture cubeMapTexture) {
        setCubeMap(cubeMapTexture);
    }

    @NotNull
    public StaticCubeMapTexture getCubeMap() {
        return cubeMap;
    }

    public void setCubeMap(@NotNull StaticCubeMapTexture cubeMap) {
        if (cubeMap == null) {
            throw new NullPointerException();
        }
        this.cubeMap = cubeMap;
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        cubeMap.bindToTextureUnit(textureUnit);
    }

    @NotNull @ReadOnly
    @Override
    public Vector2i getSize() {
        return cubeMap.getSize();
    }

}
