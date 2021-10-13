package org.jvnet.hudson.plugins.exclusion;

import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public final class IdAllocationManager {

    private final Computer node;
    private static final Map<String, Run<?, ?>> CURRENT_OWNERS = new HashMap<>();
    private static final Map<Computer, WeakReference<IdAllocationManager>> INSTANCES = new WeakHashMap<>();

    private IdAllocationManager(Computer node) {
        this.node = node;
    }

    public synchronized String allocate(Run<?, ?> owner, String id, TaskListener taskListener) throws InterruptedException, IOException {
        PrintStream logger = taskListener.getLogger();
        boolean printed = false;

        while (CURRENT_OWNERS.get(id) != null) {
            if (!printed) {
                logger.printf("[Exclusion] -> Waiting for resource '%s' currently used by '%s'%n", id, CURRENT_OWNERS.get(id).toString());
                printed = true;
            }
            wait(1000);

            // periodically detect if any locked resource belongs to the completed build
            releaseDeadlockedResource(id, taskListener);
        }

        // When allocate a resource, add it to the hashmap
        CURRENT_OWNERS.put(id, owner);
        return id;
    }

    private void releaseDeadlockedResource(String id, TaskListener taskListener) {
        PrintStream logger = taskListener.getLogger();
        // check if 'lockable resource' exists
        Run<?, ?> resourceOwner = CURRENT_OWNERS.get(id);
        if (resourceOwner != null && !resourceOwner.isBuilding()) { // build was completed
            boolean canRelease = true;
            Job<?, ?> parentJob = resourceOwner.getParent();
            if (parentJob instanceof AbstractProject) { // It might not be an AbstractProject (Workflow plugin)
                List<AbstractProject> downstreamProjects = ((AbstractProject<?, ?>) parentJob).getDownstreamProjects();
                for (AbstractProject<?, ?> abstractProject : downstreamProjects) {
                    if (abstractProject.isBuilding()) {
                        canRelease = false;
                        break;
                    }
                }
            }
            if (canRelease) {
                logger.println("[Exclusion] -> Release resource from completed build: " + resourceOwner);
                CURRENT_OWNERS.remove(id);
            }
        }
     }
    
    public static synchronized IdAllocationManager getManager(Computer node) {
        WeakReference<IdAllocationManager> idAllocationManagerRef = INSTANCES.get(node);
        if (idAllocationManagerRef != null) {
            IdAllocationManager idAllocationManager = idAllocationManagerRef.get();
            if (idAllocationManager != null) {
                return idAllocationManager;
            }
        }
        IdAllocationManager idAllocationManager = new IdAllocationManager(node);
        INSTANCES.put(node, new WeakReference<>(idAllocationManager));
        return idAllocationManager;
    }

    /**
     * Release a resource
     */
    public synchronized void free(String n) {
        CURRENT_OWNERS.remove(n);
        notifyAll();
    }

    /*package*/ static Run<?, ?> getOwnerBuild(String resource) {
        return CURRENT_OWNERS.get(resource);
    }

    /*package*/ static HashMap<String, Run<?, ?>> getAllocations() {
        return new HashMap<>(CURRENT_OWNERS);
    }
}
