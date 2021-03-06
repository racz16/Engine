package toolbox.annotations;

import java.lang.annotation.*;

/**
 * Signs that the parameter or the return value can't be null. If you give null
 * to a NotNull parameter method, it can cause NullPointerException or other
 * errors.
 */
@Documented
public @interface NotNull {

}
