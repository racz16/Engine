package wobani.window;

import wobani.toolbox.annotation.Nullable;
import wobani.toolbox.annotation.NotNull;
import wobani.window.eventhandler.WindowEventHandler;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.joml.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import wobani.core.*;
import wobani.toolbox.*;

/**
 * Object oriented wrapper class above the native GLFW window.
 *
 * @see GLFW
 */
public class Window {

    //
    //callbacks-----------------------------------------------------------------
    //
    /**
     * Error callback.
     */
    private static GLFWErrorCallback errorCallback;
    /**
     * Window close callback.
     */
    private static GLFWWindowCloseCallback windowCloseCallback;
    /**
     * Window size callback.
     */
    private static GLFWWindowSizeCallback windowSizeCallback;
    /**
     * Framebuffer size callback.
     */
    private static GLFWFramebufferSizeCallback frameBufferSizeCallback;
    /**
     * Window position callback.
     */
    private static GLFWWindowPosCallback windowPosCallback;
    /**
     * Window minization callback.
     */
    private static GLFWWindowIconifyCallback windowIconifyCallback;
    /**
     * Window focus callback.
     */
    private static GLFWWindowFocusCallback windowFocusCallback;
    //
    //temp variables------------------------------------------------------------
    //
    /**
     * Temporary variable.
     */
    private static final int[] temp1 = new int[1];
    /**
     * Temporary variable.
     */
    private static final int[] temp2 = new int[1];
    /**
     * Temporary variable.
     */
    private static final int[] temp3 = new int[1];
    /**
     * Temporary variable.
     */
    private static final int[] temp4 = new int[1];
    //misc----------------------------------------------------------------------
    /**
     * The id of the window.
     */
    private static long id;
    /**
     * Cursor object for unique cursor shape.
     */
    private static long cursor = -1;
    /**
     * The window's title.
     */
    private static String title;
    /**
     * List of the registered window event handlers.
     */
    private final static List<WindowEventHandler> eventHandlers = new ArrayList<>();
    /**
     * Determines whether the window is in fullscreen mode.
     */
    private static boolean fullscreen;
    /**
     * The vSync level.
     */
    private static int vSync;
    /**
     * The nem of the window's configuration file.
     */
    private static final String WINDOW_CONFIGURATION_FILE = "windowSettings.ini";
    /**
     * The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(Window.class.getName());

    /**
     * Mouse shape.
     */
    public enum MouseShape {
	/**
	 * Arrow (normal) mouse shape.
	 */
	ARROW(GLFW_ARROW_CURSOR),
	/**
	 * I beam shape (for text editing).
	 */
	IBEAM(GLFW_IBEAM_CURSOR),
	/**
	 * Crosshair mouse shape.
	 */
	CROSSHAIR(GLFW_CROSSHAIR_CURSOR),
	/**
	 * Hand mouse shape.
	 */
	HAND(GLFW_HAND_CURSOR),
	/**
	 * Horizontal resize mouse shape.
	 */
	HORIZONTAL_RESIZE(GLFW_HRESIZE_CURSOR),
	/**
	 * Vertical resize mouse shape.
	 */
	VERTICAL_RESIZE(GLFW_VRESIZE_CURSOR);

	/**
	 * Mouse shape's GLFW code.
	 */
	private final int code;

	/**
	 * Initializes a new MouseShape to the given value.
	 *
	 * @param code mouse shape's GLFW code
	 */
	private MouseShape(int code) {
	    this.code = code;
	}

	/**
	 * Returns the mouse shape's GLFW code.
	 *
	 * @return the mouse shape's GLFW code
	 */
	public int getCode() {
	    return code;
	}
    }

    /**
     * To can't create Window instance.
     */
    private Window() {
    }

    /**
     * Initializes the Window based on the data of Settings and the given
     * parameters.
     *
     * @param parameters for initialization
     *
     * @throws IllegalStateException unable to initialize GLFW
     * @throws RuntimeException      failed to create the GLFW window
     */
    public static void initialize(@Nullable WindowParameters parameters) {
	initializeGlfw();
	initializeDefaultHints();
	initializeUserHints(parameters);
	createWindow();
	createContextAndCapabilities();
	addCallbacks();
	LOG.info("Window initialized");
    }

    /**
     * Initializes the Glfw and sets the error callback.
     */
    public static void initializeGlfw() {
	glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
	if (!glfwInit()) {
	    throw new IllegalStateException("Unable to initialize GLFW");
	}
    }

    /**
     * Intializes the Glfw' hints with OpenGL settings.
     */
    public static void initializeDefaultHints() {
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
    }

    /**
     * Initializes the Glfw's hints based on the given user defined parameters.
     *
     * @param parameters user defined Glfw hints
     */
    public static void initializeUserHints(@Nullable WindowParameters parameters) {
	title = "Wobani Engine";
	if (parameters != null) {
	    glfwWindowHint(GLFW_RESIZABLE, parameters.isResizable() ? GLFW_TRUE : GLFW_FALSE);
	    glfwWindowHint(GLFW_VISIBLE, parameters.isVisibleAtStart() ? GLFW_TRUE : GLFW_FALSE);
	    glfwWindowHint(GLFW_DECORATED, parameters.isDecorated() ? GLFW_TRUE : GLFW_FALSE);
	    glfwWindowHint(GLFW_FOCUSED, parameters.isFocusedAtStart() ? GLFW_TRUE : GLFW_FALSE);
	    glfwWindowHint(GLFW_FLOATING, parameters.isAlwaysOnTop() ? GLFW_TRUE : GLFW_FALSE);
	    glfwWindowHint(GLFW_MAXIMIZED, parameters.isMaximizedAtStart() ? GLFW_TRUE : GLFW_FALSE);
	    title = parameters.getTitleAtStart();
	}
    }

    /**
     * Creates the window based on the user defined settings loaded from file.
     */
    public static void createWindow() {
	Map<String, Integer> params = loadSettings();
	fullscreen = params.get("FULLSCREEN") == 1;
	createWindowId(params.get("WIDTH"), params.get("HEIGHT"));
	if (id == NULL) {
	    throw new RuntimeException("Failed to create the GLFW window");
	}
    }

    /**
     * Creates the actual Glfw window.
     *
     * @param width  window's width
     * @param height window's height
     */
    public static void createWindowId(int width, int height) {
	if (isFullscreen()) {
	    GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
	    id = glfwCreateWindow(videoMode.width(), videoMode.height(), title, glfwGetPrimaryMonitor(), NULL);
	} else {
	    id = glfwCreateWindow(width, height, title, NULL, NULL);
	}
    }

    /**
     * Creates the context and sets the capabilities.
     */
    public static void createContextAndCapabilities() {
	glfwMakeContextCurrent(id);
	GL.createCapabilities();
    }

    /**
     * Loads the window settings from file.
     *
     * @return map of the settings
     */
    private static Map<String, Integer> loadSettings() {
	Map<String, Integer> params = new HashMap<>();

	try (BufferedReader br = new BufferedReader(new FileReader(new File(WINDOW_CONFIGURATION_FILE)))) {
	    String[] line;
	    while (br.ready()) {
		line = br.readLine().split(" ");
		switch (line[0]) {
		    case "[width]":
			params.put("WIDTH", Integer.valueOf(line[1]));
			break;
		    case "[height]":
			params.put("HEIGHT", Integer.valueOf(line[1]));
			break;
		    case "[fullscreen]":
			params.put("FULLSCREEN", Boolean.valueOf(line[1]) ? 1 : 0);
			break;
		}
	    }
	} catch (FileNotFoundException ex) {
	    Utility.logException(ex);
	} catch (IOException | IllegalArgumentException ex) {
	    Utility.logException(ex);
	}
	return params;
    }

    //
    //event handlers, callbacks-------------------------------------------------
    //
    /**
     * Adds the given window event handler to the list of window event handlers.
     *
     * @param eh window event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void addEventHandler(@NotNull WindowEventHandler eh) {
	if (eh == null) {
	    throw new NullPointerException();
	}
	if (!Utility.containsReference(eventHandlers, eh)) {
	    eventHandlers.add(eh);
	}
    }

    /**
     * Removes the given window event handler from the list of window event
     * handlers.
     *
     * @param eh window event handler
     *
     * @throws NullPointerException parameter can't be null
     */
    public static void removeEventHandler(@NotNull WindowEventHandler eh) {
	if (eh == null) {
	    throw new NullPointerException();
	}
	Utility.removeReference(eventHandlers, eh);
    }

    /**
     * Removes the specified window event handler from the list of window event
     * handlers.
     *
     * @param index window event handler's index
     */
    public static void removeEventHandler(int index) {
	eventHandlers.remove(index);
    }

    /**
     * Removes all the window event handlers.
     */
    public static void removeAllEventHandlers() {
	eventHandlers.clear();
    }

    /**
     * Returns the number of registered window event handlers.
     *
     * @return number of registered window event handlers
     */
    public static int getEventHandlerCount() {
	return eventHandlers.size();
    }

    /**
     * Creates the window callbacks.
     */
    private static void addCallbacks() {
	addWindowCloseCallback();
	addWindowSizeCallback();
	addFrameBufferSizeCallback();
	addWindowPosCallback();
	addWindowIconifyCallback();
	addWindowFocusCallback();
    }

    /**
     * Adds the window close callback.
     */
    private static void addWindowCloseCallback() {
	if (windowCloseCallback == null) {
	    glfwSetWindowCloseCallback(id, windowCloseCallback = new GLFWWindowCloseCallback() {
		@Override
		public void invoke(long window) {
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.closeCallback();
		    }
		}
	    });
	}
    }

    /**
     * Adds the window size callback.
     */
    private static void addWindowSizeCallback() {
	if (windowSizeCallback == null) {
	    glfwSetWindowSizeCallback(id, windowSizeCallback = new GLFWWindowSizeCallback() {
		@Override
		public void invoke(long window, int width, int height) {
		    Scene.getParameters().getValue(Scene.MAIN_CAMERA).invalidate();
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.sizeCallback(new Vector2i(width, height));
		    }
		}
	    });
	}
    }

    /**
     * Adds the frame buffer size callback.
     */
    private static void addFrameBufferSizeCallback() {
	if (frameBufferSizeCallback == null) {
	    glfwSetFramebufferSizeCallback(id, frameBufferSizeCallback = new GLFWFramebufferSizeCallback() {
		@Override
		public void invoke(long window, int width, int height) {
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.frameBufferSizeCallback(new Vector2i(width, height));
		    }
		}
	    });
	}
    }

    /**
     * Adds the window pos callback.
     */
    private static void addWindowPosCallback() {
	if (windowPosCallback == null) {
	    glfwSetWindowPosCallback(id, windowPosCallback = new GLFWWindowPosCallback() {
		@Override
		public void invoke(long window, int xpos, int ypos) {
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.positionCallback(new Vector2i(xpos, ypos));
		    }
		}
	    });
	}
    }

    /**
     * Adds the window iconify callback.
     */
    private static void addWindowIconifyCallback() {
	if (windowIconifyCallback == null) {
	    glfwSetWindowIconifyCallback(id, windowIconifyCallback = new GLFWWindowIconifyCallback() {
		@Override
		public void invoke(long window, boolean iconified) {
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.minimizationCallback(iconified);
		    }
		}
	    });
	}
    }

    /**
     * Adds the window focus callback.
     */
    private static void addWindowFocusCallback() {
	if (windowFocusCallback == null) {
	    glfwSetWindowFocusCallback(id, windowFocusCallback = new GLFWWindowFocusCallback() {
		@Override
		public void invoke(long window, boolean focused) {
		    for (WindowEventHandler eventHandler : eventHandlers) {
			eventHandler.focusCallback(focused);
		    }
		}
	    });
	}
    }

    /**
     * Releases all the window callbacks.
     */
    private static void removeCallbacks() {
	releaseWindowCloseCallback();
	releaseWindowSizeCallback();
	releaseFrameBufferSizeCallback();
	releaseWindowPosCallback();
	releaseWindowIconifyCallback();
	releaseWindowFocusCallback();
    }

    /**
     * Releases the window close callback.
     */
    private static void releaseWindowCloseCallback() {
	if (windowCloseCallback != null) {
	    windowCloseCallback.free();
	    windowCloseCallback = null;
	}
    }

    /**
     * Releases the window size callback.
     */
    private static void releaseWindowSizeCallback() {
	if (windowSizeCallback != null) {
	    windowSizeCallback.free();
	    windowSizeCallback = null;
	}
    }

    /**
     * Releases the frame buffer size callback.
     */
    private static void releaseFrameBufferSizeCallback() {
	if (frameBufferSizeCallback != null) {
	    frameBufferSizeCallback.free();
	    frameBufferSizeCallback = null;
	}
    }

    /**
     * Releases the window pos callback.
     */
    private static void releaseWindowPosCallback() {
	if (windowPosCallback != null) {
	    windowPosCallback.free();
	    windowPosCallback = null;
	}
    }

    /**
     * Releases the window iconify callback.
     */
    private static void releaseWindowIconifyCallback() {
	if (windowIconifyCallback != null) {
	    windowIconifyCallback.free();
	    windowIconifyCallback = null;
	}
    }

    /**
     * Releases the window focus callback.
     */
    private static void releaseWindowFocusCallback() {
	if (windowFocusCallback != null) {
	    windowFocusCallback.free();
	    windowFocusCallback = null;
	}
    }

    //
    //cursor--------------------------------------------------------------------
    //
    /**
     * Sets the cursor shape to the given value.
     *
     * @param shape cursor shape
     */
    public static void setMouseShape(@NotNull MouseShape shape) {
	removeCursor();
	cursor = glfwCreateStandardCursor(shape.getCode());
	glfwSetCursor(id, cursor);
    }

    /**
     * Sets the cursor shape to the specified image.
     *
     * @param path  image's relative path (with extension like
     *              "res/textures/myTexture.png")
     * @param click the cursor hotspot is specified in pixels, relative to the
     *              upper-left corner of the cursor image
     */
    public static void setMouseShape(@NotNull File path, @NotNull Vector2i click) {
	removeCursor();
	Image image = new Image(path, false);
	GLFWImage cursorImage = createCursor(image);
	cursor = glfwCreateCursor(cursorImage, click.x, click.y);
	glfwSetCursor(id, cursor);
	image.release();
    }

    /**
     * Creates the new cursor image used the Glfw's native type based on the
     * given image.
     *
     * @param image cursor image
     *
     * @return cursor image in Glfw's native type
     */
    private static GLFWImage createCursor(Image image) {
	GLFWImage cursorImage = GLFWImage.create();
	cursorImage.pixels(image.getData());
	cursorImage.width(image.getSize().x);
	cursorImage.height(image.getSize().y);
	return cursorImage;
    }

    /**
     * Sets the cursor to the default shape and removes unique cursor shape.
     */
    private static void removeCursor() {
	glfwSetCursor(id, 0);
	if (cursor != -1) {
	    glfwDestroyCursor(cursor);
	    cursor = -1;
	}
    }

    //
    //positions, sizes----------------------------------------------------------
    //
    /**
     * Returns the size of the window's fremebuffer.
     *
     * @return the size of the window's fremebuffer
     */
    @NotNull
    public static Vector2i getFrameBufferSize() {
	glfwGetFramebufferSize(id, temp1, temp2);
	return new Vector2i(temp1[0], temp2[0]);
    }

    /**
     * Returns the size of the window's frame. First value is the left, second
     * is the top, third is the right and the fourth is the bottom.
     *
     * @return size of the window's frame
     */
    @NotNull
    public static Vector4i getFrameSize() {
	glfwGetWindowFrameSize(id, temp1, temp2, temp3, temp4);
	return new Vector4i(temp1[0], temp2[0], temp3[0], temp4[0]);
    }

    /**
     * Returns the size of the window.
     *
     * @return the size of the window
     */
    @NotNull
    public static Vector2i getSize() {
	Vector4i frameSize = getFrameSize();
	Vector2i clientAreaSize = getClientAreaSize();
	return new Vector2i(frameSize.x + clientAreaSize.x + frameSize.z, frameSize.y + clientAreaSize.y + frameSize.w);
    }

    /**
     * Returns the size of the window's client area.
     *
     * @return the size of the window's client area
     */
    @NotNull
    public static Vector2i getClientAreaSize() {
	glfwGetWindowSize(id, temp1, temp2);
	return new Vector2i(temp1[0], temp2[0]);
    }

    /**
     * Sets the size of the window's client area to the given value.
     *
     * @param size client area's size
     *
     * @throws IllegalArgumentException width and height must be positive
     */
    public static void setClientAreaSize(@NotNull Vector2i size) {
	if (size.x <= 0 || size.y <= 0) {
	    throw new IllegalArgumentException("Width and height must be positive");
	}
	glfwSetWindowSize(id, size.x, size.y);
    }

    /**
     * Sets the size limits of the window's client area to the given values. Max
     * width and max height can't be 0. Max width can't be lower than min width
     * and max height can't be lower than min height. Negative valeues mean
     * don't care.
     *
     * @param minWidth  minimum width
     * @param minHeight minimum height
     * @param maxWidth  maximum width
     * @param maxHeight maximum height
     *
     * @throws IllegalArgumentException Max width and max height can't be 0
     * @throws IllegalArgumentException Min width can't be lower than max width
     *                                  and min height can't be lower than max
     *                                  height
     */
    public static void setClientAreaSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
	if (maxWidth == 0 || maxHeight == 0) {
	    throw new IllegalArgumentException("Max width and max height can't be 0");
	}
	if (minWidth >= 0 && maxWidth >= 0 && minWidth > maxWidth || minHeight >= 0 && maxHeight >= 0 && minHeight > maxHeight) {
	    throw new IllegalArgumentException("Min width can't be lower than max width and min height can't be lower than max height");
	}
	setClientAreaSizeLimitsWithoutInspection(minWidth, minHeight, maxWidth, maxHeight);
    }

    /**
     * Sets the size limits of the window's client area to the given values.
     * Negative valeues mean don't care.
     *
     * @param minWidth  minimum width
     * @param minHeight minimum height
     * @param maxWidth  maximum width
     * @param maxHeight maximum height
     */
    private static void setClientAreaSizeLimitsWithoutInspection(int minWidth, int minHeight, int maxWidth, int maxHeight) {
	minWidth = minWidth < 0 ? GLFW_DONT_CARE : minWidth;
	minHeight = minHeight < 0 ? GLFW_DONT_CARE : minHeight;
	maxWidth = maxWidth < 0 ? GLFW_DONT_CARE : maxWidth;
	maxHeight = maxHeight < 0 ? GLFW_DONT_CARE : maxHeight;
	glfwSetWindowSizeLimits(id, minWidth, minHeight, maxWidth, maxHeight);
    }

    /**
     * Return the aspect ratio of the window's client area.
     *
     * @return aspect ratio
     */
    public static float getAspectRatio() {
	Vector2i size = getClientAreaSize();
	return size.x * 1f / size.y;
    }

    /**
     * Sets the required aspect ratio of the client area of the window. If the
     * window is full screen, the aspect ratio only takes effect once it is made
     * windowed. If the window is not resizable, this function does nothing. The
     * aspect ratio is specified as a numerator and a denominator and both
     * values must be greater than zero. For example, the common 16:9 aspect
     * ratio is specified as 16 and 9, respectively. The aspect ratio is applied
     * immediately to a windowed mode window and may cause it to be resized.
     * Negative numbers mean don't care.
     *
     * @param denominator denominator
     * @param numerator   numerator
     *
     * @throws IllegalArgumentException width and heigh can't be 0
     */
    public static void setAspectRatio(int numerator, int denominator) {
	numerator = numerator < 0 ? GLFW_DONT_CARE : numerator;
	denominator = denominator < 0 ? GLFW_DONT_CARE : denominator;
	if (numerator == 0 || denominator == 0) {
	    throw new IllegalArgumentException("Numerator and denominator can't be 0");
	}
	glfwSetWindowAspectRatio(id, numerator, denominator);
    }

    /**
     * Returns the position, in screen coordinates, of the upper-left corner of
     * the client area of the window.
     *
     * @return position
     */
    @NotNull
    public static Vector2i getPosition() {
	glfwGetWindowPos(id, temp1, temp2);
	return new Vector2i(temp1[0], temp2[0]);
    }

    /**
     * Sets the window's position to the given value.
     *
     * @param position position
     */
    public static void setPosition(Vector2i position) {
	glfwSetWindowPos(id, position.x, position.y);
    }

    //
    //misc----------------------------------------------------------------------
    //
    /**
     * Returns the glfw window's id. You should use it only if you really don't
     * have other choise.
     *
     * @return window's id
     */
    public static long getId() {
	return id;
    }

    /**
     * Swaps the back and the front buffers. You should call it once every
     * frame.
     */
    public static void swapBuffers() {
	glfwSwapBuffers(id);
    }

    /**
     * Listens for events. You should call it once every frame.
     */
    public static void pollEvents() {
	glfwPollEvents();
    }

    /**
     * Returns the window's vSync level.
     *
     * @return the window's vSync level
     */
    public static int getVSync() {
	return vSync;
    }

    /**
     * Sets the window's vSync based to the given value.
     *
     * @param value vSync value
     *
     * @throws IllegalArgumentException value can't be lower than 0
     */
    public static void setVSync(int value) {
	if (value < 0) {
	    throw new IllegalArgumentException("Value can't be lower than 0");
	}
	glfwSwapInterval(value);
	vSync = value;
    }

    /**
     * Returns the window's title.
     *
     * @return the window's title
     */
    @NotNull
    public static String getTitle() {
	return title;
    }

    /**
     * Sets the window's title to the given value.
     *
     * @param title title
     *
     * @throws NullPointerException title can't be null
     */
    public static void setTitle(@NotNull String title) {
	if (title == null) {
	    throw new NullPointerException();
	}
	glfwSetWindowTitle(id, title);
    }

    /**
     * Sets the window's icon to the specified image.
     *
     * @param path image's relative path (with extension like
     *             "res/textures/myTexture.png")
     */
    public static void setIcon(@NotNull File path) {
	Image image = new Image(path, false);
	GLFWImage cursorImage = GLFWImage.malloc();
	GLFWImage.Buffer imagebf = GLFWImage.malloc(1);
	cursorImage.set(image.getSize().x, image.getSize().y, image.getData());
	imagebf.put(0, cursorImage);
	glfwSetWindowIcon(id, imagebf);
	image.release();
    }

    /**
     * Returns the value of the close flag of the window. If it returns true the
     * window and the application will close unless a WindowEventHandler
     * counteracts.
     *
     * @return the close flag's value
     */
    public static boolean isWindowShouldClose() {
	return glfwWindowShouldClose(id);
    }

    /**
     * Sets the window's close flag to the given value. If you set it to true
     * the window and the application will close unless a WindowEventHandler
     * counteracts.
     *
     * @param close window's close flag
     */
    public static void setWindowShouldClose(boolean close) {
	glfwSetWindowShouldClose(id, close);
    }

    /**
     * Determines whether the window is minimized.
     *
     * @return true if the window is minimized, false otherwise
     */
    public static boolean isMinimized() {
	return glfwGetWindowAttrib(id, GLFW_ICONIFIED) == 1;
    }

    /**
     * Minimizes the window.
     */
    public static void minimize() {
	glfwIconifyWindow(id);
    }

    /**
     * Determines whether the window is maximized.
     *
     * @return true if the window is maximized, false otherwise
     */
    public static boolean isMaximized() {
	return glfwGetWindowAttrib(id, GLFW_MAXIMIZED) == 1;
    }

    /**
     * Maximizes the window.
     */
    public static void maximize() {
	glfwMaximizeWindow(id);
    }

    /**
     * Restores the window from minimization.
     */
    public static void restore() {
	glfwRestoreWindow(id);
    }

    /**
     * Hides the window.
     */
    public static void hide() {
	glfwHideWindow(id);
    }

    /**
     * Shows the window.
     */
    public static void show() {
	glfwShowWindow(id);
    }

    /**
     * Determines whether the window is visible.
     *
     * @return true if the window is visible, false otherwise
     */
    public static boolean isVisible() {
	return glfwGetWindowAttrib(id, GLFW_VISIBLE) == 1;
    }

    /**
     * Gives focus to the window.
     */
    public static void focus() {
	glfwFocusWindow(id);
    }

    /**
     * Determines whether the window gains focus.
     *
     * @return true if the window gains focus, false otherwise
     */
    public static boolean isFocused() {
	return glfwGetWindowAttrib(id, GLFW_FOCUSED) == 1;
    }

    /**
     * Determines whether the window is in fullscreen mode.
     *
     * @return true if the window is in fullscreen mode, false otherwise
     */
    public static boolean isFullscreen() {
	return fullscreen;
    }

    /**
     * Sets whether or not the window should work in fullscreen mode.
     *
     * @param fullscreen fullscreen
     */
    public static void setFullscreen(boolean fullscreen) {
	if (fullscreen) {
	    activateFullscreen();
	} else {
	    deactivateFullscreen();
	}
	Window.fullscreen = fullscreen;
    }

    /**
     * Activates the fullscreen mode.
     */
    public static void activateFullscreen() {
	GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
	glfwSetWindowMonitor(id, glfwGetPrimaryMonitor(), 0, 0, mode.width(), mode.height(), GLFW_DONT_CARE);
    }

    /**
     * Deactivates the fullscreen mode.
     */
    public static void deactivateFullscreen() {
	Vector2i position = getPosition();
	Vector2i size = getClientAreaSize();
	glfwSetWindowMonitor(id, NULL, position.x, position.y, size.x, size.y, GLFW_DONT_CARE);
    }

    /**
     * Determines whether the window is resizable.
     *
     * @return true if the window is resizable, false otherwise
     */
    public static boolean isResizable() {
	return glfwGetWindowAttrib(id, GLFW_RESIZABLE) == 1;
    }

    /**
     * Determines whether the window is decorated.
     *
     * @return true if the window is decorated, false otherwise
     */
    public static boolean isDecorated() {
	return glfwGetWindowAttrib(id, GLFW_DECORATED) == 1;
    }

    /**
     * Determines whether the window is always on top.
     *
     * @return true if the window is always on top, false otherwise
     */
    public static boolean isAlwaysOnTop() {
	return glfwGetWindowAttrib(id, GLFW_FLOATING) == 1;
    }

    /**
     * Releases the resources owned by the window. After calling this method,
     * you can't use the window for anything. You should only call this method
     * when the program terminates.
     */
    public static void release() {
	removeCursor();
	removeCallbacks();
	glfwDestroyWindow(id);
	glfwTerminate();
	errorCallback.free();
	LOG.info("Window released");
    }

}