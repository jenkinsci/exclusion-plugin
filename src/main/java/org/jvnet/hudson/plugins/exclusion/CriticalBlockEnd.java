package org.jvnet.hudson.plugins.exclusion;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Environment;
import hudson.model.Executor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Anthony Roux
 **/
public class CriticalBlockEnd extends Builder {

    @DataBoundConstructor
    public CriticalBlockEnd() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        List<RessourcesMonitor> listRessources = IdAllocator.getListRessources();

        for (RessourcesMonitor rm : listRessources) {
            if (build.getProject().getName().equals(rm.getJobName())) {
                rm.setBuild(false);
            }
        }
        IdAllocator.setListRessources(listRessources);

    
        final Computer cur2 = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam2 = IdAllocationManager.getManager(cur2);
         System.out.println("celui dans end " + pam2.toString());
        EnvVars environment = build.getEnvironment(listener);
        List<String> listId = new ArrayList<String>();

        Set cles = environment.keySet();
        Iterator it = cles.iterator();
        while (it.hasNext()) {
            String cle = (String) it.next();
            if (cle.contains("variableEnv")) {
                String valeur = environment.get(cle);
                listId.add(valeur);
            }
        }

        for (String id : listId) {
            System.out.println("listid --- > : " + id);
            //System.out.println("Dans la boucle de liberation : " + id);
            // on les liberes
            DefaultIdType p = new DefaultIdType(id);
            Id i = p.allocate(false, build, pam2, launcher, listener);
            //System.out.println("----------->  " + i.type.name);
            i.cleanUp();
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
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            //save();
            return true;
        }
    }
}
