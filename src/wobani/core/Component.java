package wobani.core;

import wobani.component.*;
import wobani.toolbox.annotation.*;
import wobani.toolbox.invalidatable.*;

import java.util.logging.*;

/**
 All of a GameObject's Components based on this abstract class. The GameLoop call it's update method once a frame, before
 rendering.
 */
public abstract class Component implements ComponentBase{

    /**
     The class's logger.
     */
    private static final Logger LOG = Logger.getLogger(Component.class.getName());
    /**
     Contains the Component's Invalidatables.
     */
    private final InvalidatableContainer<Component> invalidatables = new InvalidatableContainer<>(this);
    /**
     The attached GameObject.
     */
    private GameObject gameObject;
    /**
     Determines whether the Component is active. If it's not, your changes may not affect the end result, and the data
     you get from this Component may be invalid.
     */
    private boolean active = true;

    @Override
    public void addInvalidatable(@NotNull Invalidatable invalidatable){
        invalidatables.addInvalidatable(invalidatable);
    }

    @Override
    public Invalidatable getInvalidatable(int index){
        return invalidatables.getInvalidatable(index);
    }

    @Override
    public boolean containsInvalidatable(@Nullable Invalidatable invalidatable){
        return invalidatables.containsInvalidatable(invalidatable);
    }

    @Override
    public void removeInvalidatable(@Nullable Invalidatable invalidatable){
        invalidatables.removeInvalidatable(invalidatable);
    }

    @Override
    public int getInvalidatableCount(){
        return invalidatables.size();
    }

    @Override
    public boolean isActive(){
        return active;
    }

    @Override
    public void setActive(boolean active){
        this.active = active;
    }

    @Nullable
    @Override
    public GameObject getGameObject(){
        return gameObject;
    }

    @Override
    public void setGameObject(@Nullable GameObject object){
        if(object == null){
            if(gameObject != null){
                gameObject.getComponents().remove(this);
            }
        }else{
            object.getComponents().add(this);
        }
    }

    /**
     Attaches this Component to the given GameObject.

     @param object this Component's new GameObject

     @throws NullPointerException object can't be null
     */
    @Internal
    protected void attachToGameObject(@NotNull GameObject object){
        if(object == null){
            throw new NullPointerException();
        }
        gameObject = object;
        Scene.getComponentLists().addComponentToLists(this);
    }

    /**
     Detaches this Component from it's GameObject.
     */
    @Internal
    protected void detachFromGameObject(){
        Scene.getComponentLists().removeComponentFromLists(this);
        gameObject = null;
    }

    /**
     This method runs once per frame, before rendering.
     */
    @Internal
    protected void update(){
    }

    @Override
    public void invalidate(){
        invalidatables.invalidate();
        LOG.finer("Component invalidated");
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 17 * hash + (this.active ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final Component other = (Component) obj;
        return this.active == other.active;
    }

    @Override
    public String toString(){
        String go = gameObject == null ? "null" : gameObject.getName();
        StringBuilder res = new StringBuilder().append(Component.class.getSimpleName()).append("(").append(" active: ")
                .append(active).append(", gameObject: ").append(go).append(", invalidatables: ")
                .append(invalidatables.size()).append(")");
        return res.toString();
    }

}
