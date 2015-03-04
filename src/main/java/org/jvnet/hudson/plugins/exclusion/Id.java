package org.jvnet.hudson.plugins.exclusion;

import java.io.IOException;

/**
 * Exclusion resource.
 *
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public abstract class Id {

    public final IdType type;

    protected Id(IdType type) {
        this.type = type;
    }

    public abstract String get();

    public abstract void cleanUp() throws IOException, InterruptedException;
}
