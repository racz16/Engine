package resources.environmentProbes;

import core.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import resources.*;
import resources.textures.cubeMapTexture.*;
import toolbox.*;
import toolbox.annotations.*;

public class DynamicEnvironmentProbe implements EnvironmentProbe {

    private CubeMapTexture cubeMap;
    private float maxDistance = 1000;
    private float minSize = 0;
    private int resolution = 128;
    private int renderingFrequency = 1;
    private final Vector3f position;
    private static Matrix4f projectionMatrix;
    private final Matrix4f[] viewMatrices;
    private Fbo fbo;

    //render now and than static
    public DynamicEnvironmentProbe() {
        refresh();
        position = new Vector3f();
        projectionMatrix = new Matrix4f().setPerspective(Utility.toRadians(90), 1, 0.001f, 1000);
        viewMatrices = new Matrix4f[6];
        refresshViewMatrices();
        Scene.addProbe(this);
    }

    public void refresh() {
        refreshCubeMap();
        refreshFbo();
    }

    public boolean shouldRenderNow() {
        if (renderingFrequency == 0) {
            return true;
        } else if (GameLoop.getFrameCount() % renderingFrequency == 0) {
            return true;
        }
        return false;
    }

    private void refreshCubeMap() {
        if (cubeMap == null || !cubeMap.isUsable()) {
            cubeMap = new DynamicCubeMapTexture(new Vector2i(resolution));
        } else {
            if (resolution != cubeMap.getSize().x) {
                releaseCubeMap();
                cubeMap = new DynamicCubeMapTexture(new Vector2i(resolution));
            }
        }
    }

    private void refreshFbo() {
        if (fbo == null || !fbo.isUsable()) {
            createFbo();
        } else {
            if (resolution != fbo.getSize().x) {
                releaseFbo();
                createFbo();
            }
        }
    }

    private void createFbo() {
        fbo = new Fbo(new Vector2i(resolution), false, 1, false);
        fbo.bind();
//                fbo.addAttachment(Fbo.FboAttachmentSlot.COLOR, Fbo.FboAttachmentType.TEXTURE, 0);
        fbo.addAttachment(Fbo.FboAttachmentSlot.DEPTH, Fbo.FboAttachmentType.RBO, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, cubeMap.getId(), 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        if (!fbo.isComplete()) {
            System.out.println("ERROR");
        }
        fbo.unbind();
    }

    public void bindFbo() {
        fbo.bind();
    }

    public void bindCubeMap() {
        cubeMap.bind();
    }

    public void unbindFbo() {
        fbo.unbind();
    }

    public void unbindCubeMap() {
        cubeMap.unbind();
    }

    private void refresshViewMatrices() {
        viewMatrices[0] = new Matrix4f().lookAt(position, new Vector3f(position.x + 100, position.y, position.z), new Vector3f(0, -1, 0));
        viewMatrices[1] = new Matrix4f().lookAt(position, new Vector3f(position.x - 100, position.y, position.z), new Vector3f(0, -1, 0));
        viewMatrices[2] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y + 100, position.z), new Vector3f(0, 0, 1));
        viewMatrices[3] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y - 100, position.z), new Vector3f(0, 0, 1));
        viewMatrices[4] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y, position.z + 100), new Vector3f(0, -1, 0));
        viewMatrices[5] = new Matrix4f().lookAt(position, new Vector3f(position.x, position.y, position.z - 100), new Vector3f(0, -1, 0));
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(float maxDistance) {
        if (maxDistance < 0) {
            throw new IllegalArgumentException("Max distance can't be lower than 0");
        }
        this.maxDistance = maxDistance;
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        if (minSize < 0) {
            throw new IllegalArgumentException("Size can't be lower than 0");
        }
        this.minSize = minSize;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        if (resolution <= 0) {
            throw new IllegalArgumentException("Resolution must be higher than 0");
        }
        this.resolution = resolution;
        refresh();
    }

    public int getRenderingFrequency() {
        return renderingFrequency;
    }

    public void setRenderingFrequency(int renderingFrequency) {
        if (renderingFrequency < 0) {
            throw new IllegalArgumentException("Rendering frequency can't be lower than 0");
        }
        this.renderingFrequency = renderingFrequency;
    }

    @NotNull @ReadOnly
    public Matrix4f getViewMatrix(int index) {
        return new Matrix4f(viewMatrices[index]);
    }

    @NotNull @ReadOnly
    public static Matrix4f getProjectionMatrix() {
        return new Matrix4f(projectionMatrix);
    }

    @NotNull
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public void setPosition(@NotNull Vector3f position) {
        this.position.set(position);
        refresshViewMatrices();
    }

    public void releaseCubeMap() {
        cubeMap.release();
        cubeMap = null;
    }

    public void releaseFbo() {
        fbo.release();
        fbo = null;
    }

    public void release() {
        releaseCubeMap();
        releaseFbo();
    }

    @Override
    public void bindToTextureUnit(int textureUnit) {
        cubeMap.bindToTextureUnit(textureUnit);
    }

    public void FboTexture(int index) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + index, cubeMap.getId(), 0);
    }

}
