package components.audio;

import core.*;
import java.util.*;
import org.joml.*;
import resources.audio.*;
import toolbox.annotations.*;

/**
 * Audio sources can emit sounds. The audio sources' position, orientation and
 * velocity affect how we hear these sounds. If you want to play or pause the
 * sound, change the attenuation or substitute the sound effects, you should use
 * the model behind it.
 *
 * @see #getSource()
 */
public class AudioSourceComponent extends Component {

    /**
     * The OpenAL audio source.
     */
    private AudioSource source;
    /**
     * The audio source's position in the last frame.
     */
    private final Vector3f lastPosition = new Vector3f();
    /**
     * Determines whether the audio source is directional.
     */
    private boolean directionalSource = true;

    /**
     * Initializes a new AudioSourceComponent to the given value.
     *
     * @param source audio source
     */
    public AudioSourceComponent(@NotNull AudioSource source) {
        setSource(source);
    }

    /**
     * Returns the OpenAL audio source. The Component is mainly just an adapter
     * and this return value is the model. This means that if you want to play
     * or pause the sound, change the attenuation or substitute the sound
     * effects, you should use this return value.
     *
     * @return OpenAL audio source
     */
    @NotNull
    public AudioSource getSource() {
        return source;
    }

    /**
     * Sets the OpenAL source to the given value.
     *
     * @param source OpenAL audio source
     *
     * @throws NullPointerException source can't be null
     */
    public void setSource(@NotNull AudioSource source) {
        if (source == null) {
            throw new NullPointerException();
        }
        this.source = source;
    }

    /**
     * Determines whether the audio source is directional. If it returns true it
     * emits sound along the GameObject's forward vector. If it returns true it
     * emits sound towards the audio listener.
     *
     * @return true if it's a directional audio source, false otherwise
     */
    public boolean isDirectionalSource() {
        return directionalSource;
    }

    /**
     * Sets whether or not the audio source should be a directional audio
     * source. If it's a directional source, it emits sound along the
     * GameObject's forward vector. If it's not, it emits sound towards the
     * audio listener.
     *
     * @param directional true if it should be a directional audio source, false
     * otherwise
     */
    public void setDirectionalSource(boolean directional) {
        directionalSource = directional;
    }

    @Override
    public void update() {
        if (getGameObject() != null && source.isUsable()) {
            Vector3f currentPosition = new Vector3f(getGameObject().getTransform().getAbsolutePosition());
            Vector3f velocity = new Vector3f();
            currentPosition.sub(lastPosition, velocity);
            source.setPosition(currentPosition);
            source.setVelocity(velocity);
            Vector3f forward = getGameObject().getTransform().getForwardVector();
            if (isDirectionalSource()) {
                source.setDirection(forward);
            } else {
                AudioListenerComponent alc = Scene.getAudioListener();
                if (alc == null) {
                    source.setDirection(forward);
                } else {
                    Vector3f direction = new Vector3f();
                    alc.getGameObject().getTransform().getAbsolutePosition().sub(currentPosition, direction);
                    source.setDirection(direction.normalize());
                }
            }
            lastPosition.set(currentPosition);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.source);
        hash = 17 * hash + Objects.hashCode(this.lastPosition);
        hash = 17 * hash + (this.directionalSource ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final AudioSourceComponent other = (AudioSourceComponent) obj;
        if (this.directionalSource != other.directionalSource) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.lastPosition, other.lastPosition)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "\nAudioSourceComponent{" + "source=" + source
                + ", lastPosition=" + lastPosition
                + ", directionalSource=" + directionalSource + '}';
    }

}
