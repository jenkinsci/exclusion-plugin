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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Anthony Roux
 * 
 * Build step -> End of critical zone
 **/
public class CriticalBlockEnd extends Builder {

    @DataBoundConstructor
    public CriticalBlockEnd() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        final Computer cur2 = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam2 = IdAllocationManager.getManager(cur2);

		//Get environemental variables
        EnvVars environment = build.getEnvironment(listener);
        List<String> listId = new ArrayList<String>();
		
		//Add to a list all "variableEnv" (which are added by IdAllocator)
		// Each variableEnv is a resource
        Set cles = environment.keySet();
        Iterator it = cles.iterator();
        while (it.hasNext()) {
            String cle = (String) it.next();
			//Only environmental variables from the current job
            String name = "variableEnv" + build.getProject().getName();
            if (cle.contains(name)) {
                String valeur = environment.get(cle);
                listId.add(valeur);
            }
        }
        if (!listId.isEmpty()) {
            listener.getLogger().println("[Exclusion] -> Releasing all the resources");
        }

		//For each resource
        for (String id : listId) {
            
            DefaultIdType p = new DefaultIdType(id);
            Id i = p.allocate(false, build, pam2, launcher, listener);
            AbstractBuild absBuild = IdAllocationManager.ids.get(i.type.name);
            if (absBuild != null) {
				//We want to release only resources from the current job
                if (absBuild.getProject().getName().equals(build.getProject().getName())) {
				    //Releasing
                    i.cleanUp();
                }
            }
        }
        return true;
    }

    public String getDisplayName() {
        return "Critical block end";
    }
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {

        DescriptorImpl() {
            super(CriticalBlockEnd.class);
            load();
        }

        public String getDisplayName() {
            return "Critical block end";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/Exclusion/helpCBE.html";
        }
    }
}
