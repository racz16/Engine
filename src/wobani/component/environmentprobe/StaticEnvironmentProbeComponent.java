package wobani.component.environmentprobe;

import wobani.toolbox.annotation.NotNull;
import wobani.resources.environmentprobe.StaticEnvironmentProbe;

/**
 * Stores a StaticEnvironmentProbe.
 */
public class StaticEnvironmentProbeComponent extends EnvironmentProbeComponent<StaticEnvironmentProbe> {

    /**
     * Initializes a new StaticEnvironmentProbeComponent to the given value.
     *
     * @param probe StaticEnvironmentProbe
     */
    public StaticEnvironmentProbeComponent(@NotNull StaticEnvironmentProbe probe) {
        super(probe);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append(super.toString()).append("\n")
                .append("StaticEnvironmentProbeComponent(")
                .append(")");
        return res.toString();
    }

}