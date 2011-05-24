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
 **/
public class CriticalBlockStart extends Builder {

    public static IdAllocator pa;
    public static IdAllocationManager pam = null;

    @DataBoundConstructor
    public CriticalBlockStart() {
    }

    //Methode appellé lors du step Critical Block Start
    //
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        //    List<RessourcesMonitor> listRessources = IdAllocator.getListRessources();

        final Computer cur = Executor.currentExecutor().getOwner();

        pam = IdAllocationManager.getManager(cur);

        //Init Builder
        PrintStream logger = listener.getLogger();

        // On recupere les ressources dans les variables d'environnement qui correspondent à ce job
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
        //Liste des IDs utilisées
        //
        final List<Id> allocated = new ArrayList<Id>();
       
        //On verifie que le plugin est bien coché (nombre de ressource de la list = nb qui est en variable d'env
        //Pour le cas ou on met un start & un end et qu'on coche pas le plugin
        if (listId != null && pa != null && pa.ids !=null) {
            if (listId.size() == pa.ids.length) {
                //Pour chaque ID du projet
                //
                for (IdType pt : pa.ids) {
                    logger.println("[Exclusion] -> Allocating resource : " + pt.name);

                    //On alloue l'id :
                    // isActivated à faux car on lancer le job
                    // Attendre tant que l'id est utilisé
                    //Quand fini on sajoute nous meme dans le dico pour dire les IDs qu'on utilise
                    //c'est la méthode synchronized

                    Id p = pt.allocate(true, build, pam, launcher, listener);

                    List<RessourcesMonitor> listR = IdAllocator.getListRessources();

                    for (RessourcesMonitor rm : listR) {
                        if (build.getProject().getName().equals(rm.getJobName()) && p.type.name.equals(rm.getRessource())) {
                            rm.setBuild(true);
                            rm.setAbsBuild(build);
                            rm.setLauncher(launcher);
                            rm.setListener(listener);
                        }
                    }

                    IdAllocator.setListRessources(listR);
                    //On ajoute dans allocate les IDs utilise
                    allocated.add(p);

                    logger.println("[Exclusion] -> Assigned " + p.get());
                }
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
    }
}
