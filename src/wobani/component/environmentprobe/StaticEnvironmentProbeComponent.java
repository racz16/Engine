package wobani.component.environmentprobe;

import wobani.resources.environmentprobe.*;
import wobani.toolbox.annotation.*;

/**
 Stores a StaticEnvironmentProbe.
 */
public class StaticEnvironmentProbeComponent extends EnvironmentProbeComponent<StaticEnvironmentProbe>{

    /**
     Initializes a new StaticEnvironmentProbeComponent to the given value.

     @param probe StaticEnvironmentProbe
     */
    public StaticEnvironmentProbeComponent(@NotNull StaticEnvironmentProbe probe){
        super(probe);
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(super.toString()).append("\n")
                .append(StaticEnvironmentProbeComponent.class.getSimpleName()).append("(").append(")");
        return res.toString();
    }

}
