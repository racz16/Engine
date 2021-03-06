package components.camera;

import core.*;
import java.nio.*;
import java.util.*;
import org.joml.*;
import org.lwjgl.*;
import resources.*;
import toolbox.*;
import toolbox.annotations.*;

/**
 * Implements basic camera functions. It has two projection modes: perspective
 * and orthographic and you can switch between them anytime you want. Note that
 * the field of view is only used in perspective mode and the scale is only used
 * in orthographic mode. By the way you cen also get or set these values
 * anytime. This Component offers you frustum culling methods in both modes. You
 * can get the camera's projection and view matrices.
 *
 * @see GameObject
 */
public class CameraComponent extends Component implements Camera {

    /**
     * Near plane's distance from the eye position.
     */
    private float nearPlaneDistance = 0.1f;
    /**
     * Far plane's distance from the eye position.
     */
    private float farPlaneDistance = 200;
    /**
     * Vertical field of view value (in degrees).
     */
    private float fov = 45;
    /**
     * The orthographic projection's scale.
     */
    private float scale = 10;
    /**
     * Determines that the camera's projection mode is perspective or
     * orthographic.
     */
    private ProjectionMode projectionMode;
    /**
     * Performs the frustum intersection tests.
     */
    private final FrustumIntersection frustum = new FrustumIntersection();
    /**
     * Determines whether this Component's data is valid.
     */
    protected boolean valid;
    /**
     * View matrix.
     */
    private final Matrix4f viewMatrix = new Matrix4f();
    /**
     * Projection matrix.
     */
    private final Matrix4f projectionMatrix = new Matrix4f();
    /**
     * The corner points of the camera's frustum.
     */
    private final HashMap<CornerPoint, Vector3f> cornerPoints = new HashMap<>();
    /**
     * Frustum's center.
     */
    private final Vector3f center = new Vector3f();
    /**
     * The Matrices UBO.
     */
    private static Ubo ubo;
    /**
     * FloatBuffer for frequent UBO updates.
     */
    private static FloatBuffer temp;

    static {
        createUbo();
        temp = BufferUtils.createFloatBuffer(32);
    }

    /**
     * Initializes a new CameraComponent in perspective mode.
     *
     * @see ProjectionMode
     */
    public CameraComponent() {
        setProjectionMode(ProjectionMode.PERSPECTIVE);
        for (CornerPoint cp : CornerPoint.values()) {
            cornerPoints.put(cp, new Vector3f());
        }
    }

    /**
     * Initializes a new CameraComponent to the given values. Note that the near
     * plane's distance must be higher than 0 and lower than the far plane's
     * distance. Fov must be higher than 0 and lower than 180.
     *
     * @param fov vertical field of view (in degrees)
     * @param nearPlane near plane's distance
     * @param farPlane far plane's distance
     */
    public CameraComponent(float fov, float nearPlane, float farPlane) {
        this();
        setFov(fov);
        setNearPlaneDistance(nearPlane);
        setFarPlaneDistance(farPlane);
    }

    /**
     * Returns the field of view. Note that this value is only used in
     * perspective mode.
     *
     * @return vertical field of view (in degrees)
     * @see #getProjectionMode()
     */
    public float getFov() {
        return fov;
    }

    /**
     * Sets the field of view to the given value. Note the field of view value
     * must be higher than 0, lower than 180 and this value is only used in
     * perspective mode.
     *
     * @param fov vertical field of view (in degrees)
     *
     * @throws IllegalArgumentException field of view must be higher than 0 and
     * lower than 180
     * @see #getProjectionMode()
     */
    public void setFov(float fov) {
        if (fov <= 0 || fov >= 180) {
            throw new IllegalArgumentException("Field of view must be higher than 0 and lower than 180");
        }
        this.fov = fov;
        if (projectionMode == ProjectionMode.PERSPECTIVE) {
            invalidate();
        }
    }

    /**
     * Returns the near plane's distance.
     *
     * @return near plane's distance
     */
    public float getNearPlaneDistance() {
        return nearPlaneDistance;
    }

    /**
     * Sets the near plane's distance to the given value. Note that the near
     * plane's distance must be higher than 0 and lower than the far plane's
     * distance.
     *
     * @param nearPlaneDistance near plane's distance
     *
     * @throws IllegalArgumentException near plane's distance must be higher
     * than 0 and lower than the far plane distance
     */
    public void setNearPlaneDistance(float nearPlaneDistance) {
        if (nearPlaneDistance <= 0 || nearPlaneDistance >= farPlaneDistance) {
            throw new IllegalArgumentException("Near plane's distance must be higher than 0 and lower than the far plane distance");
        }
        this.nearPlaneDistance = nearPlaneDistance;
        invalidate();
    }

    /**
     * Returns the far plane's distance.
     *
     * @return far plane's distance
     */
    public float getFarPlaneDistance() {
        return farPlaneDistance;
    }

    /**
     * Sets the far plane's distance to the given value. Note that the far
     * plane's distance must be higher than the near plane's distance.
     *
     * @param farPlaneDistance far plane's distance
     *
     * @throws IllegalArgumentException far plane's distance must be higher than
     * near plane's distance
     */
    public void setFarPlaneDistance(float farPlaneDistance) {
        if (nearPlaneDistance >= farPlaneDistance) {
            throw new IllegalArgumentException("Near plane's distance must be lower than the far plane's distance");
        }
        this.farPlaneDistance = farPlaneDistance;
        invalidate();
    }

    /**
     * Returns the scale value. Note that this value is only used in orhographic
     * mode.
     *
     * @return scale value
     * @see #getProjectionMode()
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the scale to the given value. Note that the scale must be higher
     * than 0 and this value is only used in orhographic mode.
     *
     * @param scale scale
     *
     * @throws IllegalArgumentException scale must be higher than 0
     * @see #getProjectionMode()
     */
    public void setScale(float scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be higher than 0");
        }
        this.scale = scale;
        if (projectionMode == ProjectionMode.ORTHOGRAPHIC) {
            invalidate();
        }
    }

    /**
     * Returs the camera's projection mode.
     *
     * @return projection mode
     */
    @NotNull
    public ProjectionMode getProjectionMode() {
        return projectionMode;
    }

    /**
     * Sets the camera's projection mode to the given value.
     *
     * @param projectionMode projection mode
     *
     * @throws NullPointerException projection mode can't be null
     */
    public void setProjectionMode(@NotNull ProjectionMode projectionMode) {
        if (projectionMode == null) {
            throw new NullPointerException();
        }
        if (this.projectionMode != projectionMode) {
            this.projectionMode = projectionMode;
            invalidate();
        }
    }

    @Override
    public void invalidate() {
        valid = false;
        super.invalidate();
        updateUbo();
    }

    /**
     * Refreshes this Component's data if it's invalid.
     */
    protected void refresh() {
        if (!valid) {
            //projection matrix
            if (projectionMode == ProjectionMode.PERSPECTIVE) {
                projectionMatrix.set(Utility.computePerspectiveProjectionMatrix(fov, nearPlaneDistance, farPlaneDistance));
            } else {
                projectionMatrix.set(Utility.computeOrthographicProjectionMatrix(scale, nearPlaneDistance, farPlaneDistance));
            }
            if (getGameObject() != null) {
                //view matrix and frustumintersection
                viewMatrix.set(Utility.computeViewMatrix(getGameObject().getTransform().getAbsolutePosition(), getGameObject().getTransform().getAbsoluteRotation()));
                frustum.set(new Matrix4f(projectionMatrix).mul(viewMatrix));
                //frustum corner points and center
                setPlaneVertices();
                center.set(0, 0, 0);
                for (CornerPoint cp : CornerPoint.values()) {
                    center.add(cornerPoints.get(cp));
                }
                center.div(8);
            }
            valid = true;
        }
    }

    /**
     * Computes the frustum's corner points.
     */
    private void setPlaneVertices() {
        Matrix4f inverseViewPorjectionMatrix = new Matrix4f();
        if (projectionMode == ProjectionMode.PERSPECTIVE) {
            projectionMatrix.invertPerspectiveView(viewMatrix, inverseViewPorjectionMatrix);
        } else {
            projectionMatrix.mulAffine(viewMatrix, inverseViewPorjectionMatrix);
            inverseViewPorjectionMatrix.invertAffine();
        }

        Vector4f vec = new Vector4f();
        for (CornerPoint cp : CornerPoint.values()) {
            vec.set(cp.getClipSpacePosition().mul(inverseViewPorjectionMatrix));
            vec.div(vec.w);
            cornerPoints.get(cp).set(vec.x, vec.y, vec.z);
        }
    }

    /**
     * Returns true if the sphere (determined by the given parameters) is
     * inside, or intersects the frustum and returns false if it is fully
     * outside. Note that if frustum culling is disabled, or this Component
     * isn't connected to a GameObject this method always returns true.
     *
     * @param position position
     * @param radius radius
     * @return false if the sphere is fully outside the frustum, true otherwise
     *
     * @throws NullPointerException position can't be null
     * @throws IllegalArgumentException radius can't be negative
     * @see Settings#isFrustumCulling()
     */
    @Override
    public boolean isInsideFrustum(@NotNull Vector3f position, float radius) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (radius < 0) {
            throw new IllegalArgumentException("Radius can't be negative");
        }
        if (Settings.isFrustumCulling() && getGameObject() != null) {
            refresh();
            return frustum.testSphere(position, radius);
        } else {
            return true;
        }
    }

    /**
     * Returns true if the axis alligned bounding box (determined by the given
     * parameters) is inside, or intersects the frustum and returns false if it
     * is fully outside. Note that if frustum culling is disabled, or this
     * Component isn't connected to a GameObject this method always returns
     * true.
     *
     * @param aabbMin the axis alligned bounding box's minimum x, y and z values
     * @param aabbMax the axis alligned bounding box's maximum x, y and z values
     * @return false if the bounding box is fully outside the frustum, true
     * otherwise
     *
     * @throws NullPointerException the parameters can't be null
     * @see Settings#isFrustumCulling()
     */
    @Override
    public boolean isInsideFrustum(@NotNull Vector3f aabbMin, @NotNull Vector3f aabbMax) {
        if (aabbMin == null || aabbMax == null) {
            throw new NullPointerException();
        }
        if (Settings.isFrustumCulling() && getGameObject() != null) {
            refresh();
            return frustum.testAab(aabbMin, aabbMax);
        } else {
            return true;
        }
    }

    /**
     * Returns the frustum's corner points in the following order: far-top-left,
     * far-top-right, far-bottom-left, far-bottom-right, near-top-left,
     * near-top-right, near-bottom-left, near-bottom-right. If this Component
     * isn't connected to a GameObject, this method returns null.
     *
     * @return frustum's corner points
     */
    @Nullable @ReadOnly
    @Override
    public List<Vector3f> getFrustumCornerPoints() {
        if (getGameObject() != null) {
            ArrayList<Vector3f> ret = new ArrayList<>(8);
            for (CornerPoint cp : CornerPoint.values()) {
                ret.add(getFrustumCornerPoint(cp));
            }
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Returns the frustum's specified corner point. If this Component isn't
     * connected to a GameObject, this method returns null.
     *
     * @param cornerPoint corner point
     * @return frustum's specified corner point
     *
     * @throws NullPointerException parameter can't be null
     */
    @Nullable @ReadOnly
    @Override
    public Vector3f getFrustumCornerPoint(@NotNull CornerPoint cornerPoint) {
        if (cornerPoint == null) {
            throw new NullPointerException();
        }
        if (getGameObject() != null) {
            refresh();
            return new Vector3f(cornerPoints.get(cornerPoint));
        } else {
            return null;
        }
    }

    /**
     * Returns the frustum's center point. If this Component isn't connected to
     * a GameObject, this method returns null.
     *
     * @return frustum's center
     */
    @Nullable @ReadOnly
    @Override
    public Vector3f getFrustumCenter() {
        if (getGameObject() != null) {
            refresh();
            return new Vector3f(center);
        } else {
            return null;
        }
    }

    /**
     * Returns the view matrix. If this Component isn't connected to a
     * GameObject, this method returns null.
     *
     * @return view matrix
     */
    @Nullable @ReadOnly
    @Override
    public Matrix4f getViewMatrix() {
        if (getGameObject() != null) {
            refresh();
            return new Matrix4f(viewMatrix);
        } else {
            return null;
        }
    }

    /**
     * Returns the projection matrix.
     *
     * @return projection matrix
     */
    @NotNull @ReadOnly
    @Override
    public Matrix4f getProjectionMatrix() {
        refresh();
        return new Matrix4f(projectionMatrix);
    }

    /**
     * Updates the matrices in the UBO.
     */
    protected void updateUbo() {
        if (Scene.getCamera() != this || ubo == null || !ubo.isUsable()) {
            return;
        }
        temp.position(0);
        getViewMatrix().get(temp);
        getProjectionMatrix().get(16, temp);

        ubo.bind();
        ubo.storeData(temp, 0);
        ubo.unbind();
    }

    /**
     * Creates the UBO.
     */
    private static void createUbo() {
        if (ubo == null) {
            ubo = new Ubo();
            ubo.bind();
            ubo.allocateMemory(128, false);
            ubo.unbind();
            ubo.bindToBindingPoint(2);
        }
    }

    /**
     * Releases the UBO. After calling this mathod, you can't use the Matrices
     * UBO and can't recreate it. Note that some renderers (like the
     * BlinnPhongRenderer) may expect to access to the Matrices UBO (which isn't
     * possible after calling this method).
     */
    public static void releaseUbo() {
        ubo.release();
        ubo = null;
    }

    @Override
    protected void removeFromGameObject() {
        getGameObject().getTransform().removeInvalidatable(this);
        super.removeFromGameObject();
        invalidate();
    }

    @Override
    protected void addToGameObject(@NotNull GameObject g) {
        super.addToGameObject(g);
        invalidate();
        getGameObject().getTransform().addInvalidatable(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Float.floatToIntBits(this.nearPlaneDistance);
        hash = 61 * hash + Float.floatToIntBits(this.farPlaneDistance);
        hash = 61 * hash + Float.floatToIntBits(this.fov);
        hash = 61 * hash + Float.floatToIntBits(this.scale);
        hash = 61 * hash + Objects.hashCode(this.projectionMode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final CameraComponent other = (CameraComponent) obj;
        if (Float.floatToIntBits(this.nearPlaneDistance) != Float.floatToIntBits(other.nearPlaneDistance)) {
            return false;
        }
        if (Float.floatToIntBits(this.farPlaneDistance) != Float.floatToIntBits(other.farPlaneDistance)) {
            return false;
        }
        if (Float.floatToIntBits(this.fov) != Float.floatToIntBits(other.fov)) {
            return false;
        }
        if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
            return false;
        }
        if (this.projectionMode != other.projectionMode) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nCameraComponent{"
                + "nearPlaneDistance=" + nearPlaneDistance
                + ", farPlaneDistance=" + farPlaneDistance + ", fov=" + fov
                + ", scale=" + scale + ", projectionMode=" + projectionMode
                + ", frustum=" + frustum + ", valid=" + valid
                + ", viewMatrix=" + viewMatrix + ", projectionMatrix=" + projectionMatrix
                + ", cornerPoints=" + cornerPoints + ", center=" + center + '}';
    }

}
