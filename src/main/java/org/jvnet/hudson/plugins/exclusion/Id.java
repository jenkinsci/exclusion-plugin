package org.jvnet.hudson.plugins.exclusion;

import java.io.IOException;

/**
 *
 * first @author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public abstract class Id {

    public final IdType type;

    protected Id(IdType type) {
        this.type = type;
    }

    public abstract String get();

    public abstract void cleanUp() throws IOException, InterruptedException;
}
