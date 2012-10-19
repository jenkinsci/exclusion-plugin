package org.jvnet.hudson.plugins.exclusion;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Anthony Roux
 * Build step -> Start of critical zone
 *
 **/
public class CriticalBlockStart extends Builder {

    public static IdAllocationManager pam = null;

    @DataBoundConstructor
    public CriticalBlockStart() {
    }

    //Called when step "Critical Block Start" started
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        final Computer cur = Executor.currentExecutor().getOwner();
        pam = IdAllocationManager.getManager(cur);

        //Init Builder
        PrintStream logger = listener.getLogger();

        //Get environemental variables
        EnvVars environment = build.getEnvironment(listener);
        List<String> listId = new ArrayList<String>();
        Set cles = environment.keySet();
        Iterator it = cles.iterator();
        //Add to a list all "variableEnv" (which are added by IdAllocator)
        // Each variableEnv is a resource
        while (it.hasNext()) {
            String cle = (String) it.next();
			//Only environmental variables from the current job
            String name = "variableEnv" + build.getProject().getName();
            if (cle.contains(name)) {
                String valeur = environment.get(cle);
                listId.add(valeur);
            }
        }

	// if resources are allocated to this Job
        if (listId != null) {

            for (String id : listId) {
                DefaultIdType p = new DefaultIdType(id);

               logger.println("[Exclusion] -> Allocating resource : " + id);
                //Allocating resources
				// if one is already used, just wait for it to be released
                Id resource = p.allocate(true, build, pam, launcher, listener);

                logger.println("[Exclusion] -> Assigned " + resource.get());
            }
            if (!listId.isEmpty()) {
                logger.println("[Exclusion] -> Resource allocation complete");
            }
        }
        return true;
    }

    public String getDisplayName() {
        return "Critical block start";
    }
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {

        DescriptorImpl() {
            super(CriticalBlockStart.class);
            load();
        }

        public String getDisplayName() {
            return "Critical block start";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/Exclusion/helpCBS.html";
        }
    }
}
