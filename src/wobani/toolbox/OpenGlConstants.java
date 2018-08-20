package wobani.toolbox;

import org.lwjgl.opengl.*;

/**
 Collects useful OpenGL constants.
 */
public class OpenGlConstants{

    /**
     OpenGL's major version.
     */
    public static final int MAJOR_VERSION;
    /**
     OpenGL's minor version.
     */
    public static final int MINOR_VERSION;
    /**
     The used vendor's name.
     */
    public static final String VENDOR;
    /**
     The used renderer's name.
     */
    public static final String RENDERER;
    /**
     The maximum length of a label.
     */
    public static final int MAX_LABEL_LENGTH;
    /**
     The maximum number of UBO binding points.
     */
    public static final int MAX_UNIFORM_BUFFER_BINDINGS;
    /**
     The maximum number of SSBO binding points.
     */
    public static final int MAX_SHADER_STORAGE_BUFFER_BINDINGS;
    /**
     The maximum number of Vertex Attrib Arrays.
     */
    public static final int MAX_VERTEX_ATTRIBS;
    /**
     The maximum number of UBO size.
     */
    public static final int MAX_UNIFORM_BLOCK_SIZE;
    /**
     The maximum number of SSBO size.
     */
    public static final int MAX_SHADER_STORAGE_BLOCK_SIZE;
    /**
     The maximum number of samples in a texel.
     */
    public static final int MAX_SAMPLES;
    /**
     The maximum width and height for a texture.
     */
    public static final int MAX_TEXTURE_SIZE;
    /**
     Determines whether the GPU be able to use anisotropic filtering.
     */
    public static final boolean ANISOTROPIC_FILTER_ENABLED;
    /**
     The anisotropic filter's max level.
     */
    public static final float ANISOTROPIC_FILTER_MAX_LEVEL;

    static{
        MAJOR_VERSION = GL11.glGetInteger(GL30.GL_MAJOR_VERSION);
        MINOR_VERSION = GL11.glGetInteger(GL30.GL_MINOR_VERSION);
        VENDOR = GL11.glGetString(GL11.GL_VENDOR);
        RENDERER = GL11.glGetString(GL11.GL_RENDERER);
        MAX_LABEL_LENGTH = GL11.glGetInteger(GL43.GL_MAX_LABEL_LENGTH);
        MAX_UNIFORM_BUFFER_BINDINGS = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BUFFER_BINDINGS);
        MAX_SHADER_STORAGE_BUFFER_BINDINGS = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
        MAX_VERTEX_ATTRIBS = GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS);
        MAX_UNIFORM_BLOCK_SIZE = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BLOCK_SIZE);
        MAX_SHADER_STORAGE_BLOCK_SIZE = GL11.glGetInteger(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
        MAX_SAMPLES = GL11.glGetInteger(GL30.GL_MAX_SAMPLES);
        MAX_TEXTURE_SIZE = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        ANISOTROPIC_FILTER_ENABLED = GL.getCapabilities().GL_EXT_texture_filter_anisotropic;
        ANISOTROPIC_FILTER_MAX_LEVEL = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
    }
}
