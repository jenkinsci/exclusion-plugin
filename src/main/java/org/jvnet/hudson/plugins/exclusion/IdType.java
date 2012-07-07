package org.jvnet.hudson.plugins.exclusion;

import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public abstract class IdType implements ExtensionPoint, Describable<IdType>, Serializable {

    public final String name;

    protected IdType(String name) {
        this.name = name.toUpperCase();
    }

    public final String getFixedId() {
        if (name != null) {
            return name;
        } else {
            return "random" + (int) (Math.random() * (99999 - 1)) + 1;
        }
    }

    public abstract Id allocate(boolean launchAlloc, AbstractBuild<?, ?> build, IdAllocationManager manager, Launcher launcher, BuildListener buildListener) throws IOException, InterruptedException;

    public abstract IdTypeDescriptor getDescriptor();
    private static final long serialVersionUID = 1L;
}
