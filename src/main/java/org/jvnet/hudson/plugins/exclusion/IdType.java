package org.jvnet.hudson.plugins.exclusion;

import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * Exclusion resource factory.
 *
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public abstract class IdType implements ExtensionPoint, Describable<IdType>, Serializable {

    public final String name;

    protected IdType(String name) {
        this.name = name.toUpperCase();
    }

    public final String getFixedId() {
        return name;
    }

    public abstract Id allocate(boolean launchAlloc, Run<?, ?> run, IdAllocationManager manager, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException;

    public abstract IdTypeDescriptor getDescriptor();

    private static final long serialVersionUID = 1L;
}
