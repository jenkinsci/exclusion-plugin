package org.jvnet.hudson.plugins.exclusion;

import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Describable;
import hudson.model.BuildListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * Base class for different types of TCP port.
 *
 * <p>
 * This class implements {@link Serializable} so that the clean up task to be executed
 * remotely can drag this class into the serialization graph.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class IdType implements ExtensionPoint, Describable<IdType>, Serializable {
    /**
     * Name that identifies {@link PortType} among other {@link PortType}s in the
     * same {@link PortAllocator}, or the numeric port number value if that port
     * number is fixed.
     */
    public final String name;

    protected IdType(String name) {
        // to avoid platform difference issue in case sensitivity of environment variables,
        // always use uppser case.
        this.name = name.toUpperCase();
    }

    /**
     * If this port type has a fixed port number, return that value.
     * Otherwise 0.
     */
    public final String getFixedId() {
        if(name!=null){
            return name;
        }
        else{
            return "random" + (int)(Math.random() * (99999-1)) + 1;
        }
    }


    public abstract Id allocate(boolean launchAlloc, AbstractBuild<?, ?> build, IdAllocationManager manager, Launcher launcher, BuildListener buildListener) throws IOException, InterruptedException;

    public abstract IdTypeDescriptor getDescriptor();

    private static final long serialVersionUID = 1L;
}
