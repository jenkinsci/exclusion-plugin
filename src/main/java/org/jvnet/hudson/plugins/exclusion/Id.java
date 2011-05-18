package org.jvnet.hudson.plugins.exclusion;

import java.io.IOException;

/**
 * Represents an assigned Id and encapsulates how it should be cleaned up.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class Id {
    /**
     * {@link IdType} that created this Id.
     */
    public final IdType type;

    protected Id(IdType type) {
        this.type = type;
    }

    /**
     * Gets the Id.
     */
    public abstract String get();

    /**
     * Frees the Id.
     */
    public abstract void cleanUp() throws IOException, InterruptedException;
}
