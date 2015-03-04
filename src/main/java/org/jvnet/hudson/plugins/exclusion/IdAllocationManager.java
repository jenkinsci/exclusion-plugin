package org.jvnet.hudson.plugins.exclusion;

import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.AbstractProject;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;


/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public final class IdAllocationManager {

    private final Computer node; // TODO unused
    private final static Map<String, AbstractBuild<?, ?>> ids = new HashMap<String, AbstractBuild<?, ?>>();
    private static final Map<Computer, WeakReference<IdAllocationManager>> INSTANCES = new WeakHashMap<Computer, WeakReference<IdAllocationManager>>();

    private IdAllocationManager(Computer node) {
        this.node = node;
    }

    public synchronized String allocate(AbstractBuild<?, ?> owner, String id, BuildListener buildListener) throws InterruptedException, IOException {
        PrintStream logger = buildListener.getLogger();
        boolean printed = false;

        while (ids.get(id) != null) {

            if (printed == false) {
                logger.println("Waiting ressource : " + id + " currently use by : " + ids.get(id).toString());
                printed = true;
            }
            wait(1000);

            // periodically detect if any locked resource belongs to the completed build
            releaseDeadlockedResource(id, buildListener);
        }

        // When allocate a resource, add it to the hashmap
        ids.put(id, owner);
        return id;
    }

    private void releaseDeadlockedResource(String id, BuildListener buildListener) {
        PrintStream logger = buildListener.getLogger();
        // check if 'lockable resource' exists
        AbstractBuild<?, ?> resourceOwner = ids.get(id);
        if (resourceOwner != null && !resourceOwner.isBuilding()) { // build was completed
            List<AbstractProject> downstreamProjects = resourceOwner.getProject().getDownstreamProjects();
            boolean canRelease = true;
            for (AbstractProject<?, ?> proj: downstreamProjects) {
                if (proj.isBuilding()) {
                    canRelease = false;
                    break;
                }
            }
            if (canRelease) { 
                logger.println("Release resource from completed build: " + resourceOwner.toString()); ids.remove(id); 
            }
        }
     }
    
    public static IdAllocationManager getManager(Computer node) {
        IdAllocationManager pam;
        WeakReference<IdAllocationManager> ref = INSTANCES.get(node);
        if (ref != null) {
            pam = ref.get();
            if (pam != null) {
                return pam;
            }
        }
        pam = new IdAllocationManager(node);
        INSTANCES.put(node, new WeakReference<IdAllocationManager>(pam));
        return pam;
    }

    /**
     * Release a resource
     */
    public synchronized void free(String n) {
        ids.remove(n);
        notifyAll();
    }

    /*package*/ static AbstractBuild<?, ?> getOwnerBuild(String resource) {
        return ids.get(resource);
    }

    /*package*/ static HashMap<String, AbstractBuild<?, ?>> getAllocations() {
        return new HashMap<String, AbstractBuild<?, ?>>(ids);
    }
}
