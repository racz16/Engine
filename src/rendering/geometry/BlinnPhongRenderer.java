package rendering.geometry;

import components.renderables.*;
import core.*;
import materials.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import rendering.*;
import resources.*;
import resources.meshes.*;
import resources.shaders.*;
import resources.splines.*;
import resources.textures.texture2D.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * This GeometryRenderer can draw meshes and splines by using the Blinn-Phong
 shading. You can fill the materials with diffuse color or diffuse map,
 specular color or specular map and normal map. If you set the appropirate
 parameters, the specular map's alpha channel used as the glossiness value and
 the normal map's alpha channel as a parallax map. If you don't fill the
 diffuse or specular slots, the shader uses default values (basically you can
 even use this GeometryRenderer with an empty material).
 *
 * @see Material#PARAM_POM_MAX_LAYERS_F
 * @see Material#PARAM_POM_MIN_LAYERS_F
 * @see Material#PARAM_POM_SCALE_F
 * @see Material#PARAM_USE_POM_F
 * @see Material#PARAM_REFRACTION_INDEX_F
 * @see Material#PARAM_USE_GLOSSINESS_F
 */
public class BlinnPhongRenderer extends GeometryRenderer {

    /**
     * Blinn-Phong shader.
     */
    private BlinnPhongShader shader;
    /**
     * The only BlinnPhongRenderer instance.
     */
    private static BlinnPhongRenderer instance;

    /**
     * Initializes a new BlinnPhongRenderer.
     */
    private BlinnPhongRenderer() {
        shader = BlinnPhongShader.getInstance();
    }

    /**
     * Returns the BlinnPhongRenderer instance.
     *
     * @return the BlinnPhongRenderer instance
     */
    @NotNull
    public static BlinnPhongRenderer getInstance() {
        if (instance == null) {
            instance = new BlinnPhongRenderer();
        }
        return instance;
    }

    /**
     * Renders the scene.
     */
    @Override
    public void render() {
        beforeDrawShader();
        Class<BlinnPhongRenderer> renderer = BlinnPhongRenderer.class;
        //meshes
        for (Mesh mesh : Scene.getMeshes(renderer)) {
            beforeDrawRenderable(mesh);
            MeshComponent meshComponent;
            for (int i = 0; i < Scene.getNumberOfMeshComponents(renderer, mesh); i++) {
                meshComponent = Scene.getMeshComponent(renderer, mesh, i);
                if (meshComponent.isActive() && meshComponent.isMeshActive() && Utility.isInsideFrustum(meshComponent)) {
                    beforeDrawInstance(meshComponent);
                    mesh.draw();
                }
            }
            afterDrawRenderable(mesh);
        }
        //splines
        for (Spline spline : Scene.getSplines(renderer)) {
            beforeDrawRenderable(spline);
            SplineComponent splineComponent;
            for (int i = 0; i < Scene.getNumberOfSplineComponents(renderer, spline); i++) {
                splineComponent = Scene.getSplineComponent(renderer, spline, i);
                if (splineComponent.isActive() && splineComponent.isSplineActive() && Utility.isInsideFrustum(splineComponent)) {
                    beforeDrawInstance(splineComponent);
                    spline.draw();
                }
            }
            afterDrawRenderable(spline);
        }
        shader.stop();
        OpenGl.setFaceCulling(true);
    }

    /**
     * Prepares the shader to the rendering.
     */
    private void beforeDrawShader() {
        if (shader == null || !shader.isUsable()) {
            shader = BlinnPhongShader.getInstance();
        }
        shader.start();
        shader.loadGlobalUniforms();
        RenderingPipeline.bindFbo();
        OpenGl.setViewport(RenderingPipeline.getRenderingSize(), new Vector2i());
        OpenGl.setWireframe(Settings.isWireframeMode());
        numberOfRenderedElements = 0;
        numberOfRenderedFaces = 0;
        //shadow map
        Texture2D shadowMap = RenderingPipeline.getTextureParameter(RenderingPipeline.TEXTURE_SHADOWMAP);
        if (shadowMap != null) {
            shadowMap.bindToTextureUnit(0);
        }
    }

    /**
     * Prepares the given model to the rendering.
     *
     * @param renderable Renderable
     */
    private void beforeDrawRenderable(@NotNull Renderable renderable) {
        renderable.beforeDraw();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);
    }

    /**
     * Unbinds the model's VAO and the vertex attrib arrays after rendering.
     *
     * @param renderable Renderable
     */
    private void afterDrawRenderable(@NotNull Renderable renderable) {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        renderable.afterDraw();
    }

    /**
     * Prepares the MeshComponent to the rendering.
     *
     * @param rc MeshComponent
     */
    private void beforeDrawInstance(@NotNull MeshComponent rc) {
        numberOfRenderedElements++;
        numberOfRenderedFaces += rc.getMesh().getFaceCount();
        Transform transform = rc.getGameObject().getTransform();
        shader.loadObjectUniforms(transform.getModelMatrix(), new Matrix3f(transform.getInverseModelMatrix()), rc.isReceiveShadows());
        Material material = rc.getMaterial();
        shader.loadMaterial(material);
        if (!rc.isTwoSided()) {
            OpenGl.setFaceCulling(true);
        } else {
            OpenGl.setFaceCulling(false);
        }
    }

    /**
     * Prepares the SplineComponent to the rendering.
     *
     * @param rc SplineComponent
     */
    private void beforeDrawInstance(@NotNull SplineComponent rc) {
        numberOfRenderedElements++;
        Transform transform = rc.getGameObject().getTransform();
        shader.loadObjectUniforms(transform.getModelMatrix(), new Matrix3f(transform.getInverseModelMatrix()), rc.isReceiveShadows());
        Material material = rc.getMaterial();
        shader.loadMaterial(material);
    }

    /**
     * Removes the shader program from the GPU's memory. After this method call
     * you can't use this shader.
     */
    @Override
    public void release() {
        shader.release();
    }

    @Override
    public void removeFromRenderingPipeline() {

    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nBlinnPhongRenderer{" + "shader=" + shader + '}';
    }

}
