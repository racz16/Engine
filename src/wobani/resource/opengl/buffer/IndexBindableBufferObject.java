package wobani.resource.opengl.buffer;

import org.lwjgl.opengl.*;
import wobani.toolbox.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Buffer Objects which you can bind to a binding point.
 */
public abstract class IndexBindableBufferObject extends BufferObject{

    /**
     The Buffer Object's binding points.
     */
    private final Set<Integer> bindingPoints = new HashSet<>();

    /**
     Initializes a new IndexBindableBufferObject to the given target.

     @param target type
     */
    public IndexBindableBufferObject(int target){
        super(target);
    }

    /**
     Binds the Buffer Object to the given binding point. You don't have to bind the Buffer Object itself before calling
     this method (but it's not a problem if you do). Note that this method doesn't bind the Buffer Object, it binds to a
     binding point. If you want to bind the Buffer, you should call the bind method.

     @param bindingPoint binding point

     @throws IllegalArgumentException if binding point is lower than 0 or higher than the highest valid binding point
     @see #bind()
     */
    public void bindToBindingPoint(int bindingPoint){
        checkRelease();
        if(bindingPoint < 0 || bindingPoint > getHighestValidBindingPoint()){
            throw new IllegalArgumentException("Binding point can't be lower than 0 or higher than the highest valid binding point");
        }
        this.bindingPoints.add(bindingPoint);
        GL30.glBindBufferBase(getTarget(), bindingPoint, getId());
    }

    /**
     Unbinds the Buffer Object from the given binding point. You don't have to bind the Buffer Object before calling this
     method (but it's not a problem if you do). Note that this method doesn't unbind the Buffer Object, it unbinds from a
     binding point. If you want to unbind the Buffer, you should call the unbind method.

     @param bindingPoint binding point

     @throws IllegalArgumentException if binding point is lower than 0 or higher than the highest valid binding point or
     if the Buffer Object isn't bound to the given binding point
     @see #unbind()
     */
    public void unbindFromBindingPoint(int bindingPoint){
        checkRelease();
        if(bindingPoint < 0 || bindingPoint > getHighestValidBindingPoint()){
            throw new IllegalArgumentException("Binding point can't be lower than 0 or higher than the highest valid binding point");
        }
        if(!bindingPoints.contains(bindingPoint)){
            throw new IllegalArgumentException("The Buffer Object not bound to the given binding point");
        }
        GL30.glBindBufferBase(getTarget(), bindingPoint, 0);
        bindingPoints.remove(bindingPoint);

    }

    /**
     Returns the Buffer Object's binding points.

     @return the Buffer Object's binding points
     */
    @NotNull
    @ReadOnly
    public Collection<Integer> getBindingPoint(){
        return Collections.unmodifiableCollection(bindingPoints);
    }

    /**
     Returns the highest valid binding point.

     @return the highest valid binding point
     */
    protected abstract int getHighestValidBindingPoint();

    @Override
    public String toString(){
        return super.toString() + "\n" + IndexBindableBufferObject.class
                .getSimpleName() + "(" + "bindingPoints: " + Utility.toString(bindingPoints) + ")";
    }
}
