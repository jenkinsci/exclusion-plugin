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
 * Fin de la delimitation de la zone critique
 **/
public class CriticalBlockEnd extends Builder {

    @DataBoundConstructor
    public CriticalBlockEnd() {
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

       // List<RessourcesMonitor> listRessources = IdAllocator.getListRessources();

        // On remet la variable pour savoir si la ressource est en cours d'utilisation à "false"
       /* for (RessourcesMonitor rm : listRessources) {
        if (build.getProject().getName().equals(rm.getJobName())) {
        rm.setBuild(false);
        }
        }
        IdAllocator.setListRessources(listRessources);*/



        final Computer cur2 = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam2 = IdAllocationManager.getManager(cur2);

        EnvVars environment = build.getEnvironment(listener);
        List<String> listId = new ArrayList<String>();

        Set cles = environment.keySet();
        Iterator it = cles.iterator();
        while (it.hasNext()) {
            String cle = (String) it.next();
            String name = "variableEnv" + build.getProject().getName();
            if (cle.contains(name)) {
                String valeur = environment.get(cle);
                listId.add(valeur);
            }
        }
        if (listId.size() != 0) {
            listener.getLogger().println("[Exclusion] -> Releasing all the resources");
        }

        for (String id : listId) {
            // On les liberes
            DefaultIdType p = new DefaultIdType(id);
            Id i = p.allocate(false, build, pam2, launcher, listener);
            AbstractBuild absBuild = IdAllocationManager.ids.get(i.type.name);
            if (absBuild != null) {
                if (absBuild.getProject().getName().equals(build.getProject().getName())) {
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
    }
}
