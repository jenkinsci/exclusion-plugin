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
public class CriticalBlockStart extends Builder {

    public static IdAllocator pa;

    @DataBoundConstructor
    public CriticalBlockStart() {
    }

    //Methode appellé lors du step Critical Block Start
    //
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
       List<RessourcesMonitor> listRessources = IdAllocator.getListRessources();

        final Computer cur = Executor.currentExecutor().getOwner();
        final IdAllocationManager pam = IdAllocationManager.getManager(cur);
        
             System.out.println("celui dans start " + pam.toString());
        for (RessourcesMonitor rm : listRessources) {
            if(build.getProject().getName().equals(rm.getJobName())){
                rm.setBuild(true);
                rm.setAbsBuild(build);
                rm.setLauncher(launcher);
                rm.setListener(listener);
                rm.setPam(pam);
                rm.setCur(cur);
            }
        }
        IdAllocator.setListRessources(listRessources);

  
        //Init Builder
        PrintStream logger = listener.getLogger();
       

        //Liste des IDs utilisées
        //
        final List<Id> allocated = new ArrayList<Id>();

        //On verifie qu'on a bien coché le plugin
        // & qu'on a des IDs  à allouer
        // A TESTER : Enlever le pa != null
        if (pa != null && pa.ids != null && pa.isActivated) {
            //Pour chaque ID du projet
            //
            for (IdType pt : pa.ids) {
                logger.println("Allocating Id : " + pt.name);

                //On alloue l'id :
                // isActivated à faux car on lancer le job
                // Attendre tant que l'id est utilisé
                //Quand fini on sajoute nous meme dans le dico pour dire les IDs qu'on utilise
                //c'est la méthode synchronized
               
                Id p = pt.allocate(true, build, pam, launcher, listener);

                //On ajoute dans allocate les IDs utilise
                allocated.add(p);

                logger.println("  -> Assigned " + p.get());

            }
               // TODO: only log messages when we are blocking.
            logger.println("Id allocation complete");
        }

      /*  env = new Environment() {

            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                System.out.println("Entrer dans tearDown");
                // Pour chaque id allouées
                //
                for (Id p : allocated) {
                    System.out.println("Dans la boucle allocated : " + p.get());
                    // on les liberes
                    p.cleanUp();
                }

                return true;
            }
        };


        CriticalBlockEnd.cbs = this;*/
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
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            //  formData.put(req, pa)

            // save();
            return true;
        }
    }
}
