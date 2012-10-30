package org.jvnet.hudson.plugins.exclusion;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public final class IdAllocationManager {

    private final Computer node;
    public final static Map<String, AbstractBuild> ids = new HashMap<String, AbstractBuild>();
    private static final Map<Computer, WeakReference<IdAllocationManager>> INSTANCES = new WeakHashMap<Computer, WeakReference<IdAllocationManager>>();
    private IdAllocationManager(Computer node) {
        this.node = node;
    }

    public synchronized String allocate(AbstractBuild owner, String id, BuildListener buildListener) throws InterruptedException, IOException {

        PrintStream logger = buildListener.getLogger();

	//if resource already used just wait
        while (ids.get(id) != null) {
            logger.println("Waiting ressource : " + id + " currently used by : " + ids.get(id).toString());
            wait(10000);
        }

	// When allocate a resource, add it to the hashmap
        ids.put(id, owner);
        return id;
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

   //Release a resource
    public synchronized void free(String n) {
        ids.remove(n);
        notifyAll();
    }
}
