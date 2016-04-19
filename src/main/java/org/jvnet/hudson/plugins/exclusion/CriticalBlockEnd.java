package org.jvnet.hudson.plugins.exclusion;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Build step -> End of critical zone
 *
 * @author Anthony Roux
 */
public class CriticalBlockEnd extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public CriticalBlockEnd() {
    }


    public String getDisplayName() {
        return "Critical block end";
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        final IdAllocationManager pam = IdAllocationManager.getManager(Executor.currentExecutor().getOwner());

        //Get environmental variables
        EnvVars environment = run.getEnvironment(taskListener);
        List<String> listId = new ArrayList<String>();

        //Add to a list all "variableEnv" (which are added by IdAllocator)
        // Each variableEnv is a resource
        for (Entry<String, String> e: environment.entrySet()) {
            String cle = e.getKey();
            //Only environmental variables from the current job
            String name = "variableEnv" + run.getParent().getName();
            if (cle.contains(name)) {
                String value = e.getValue();
                listId.add(value);
            }
        }
        if (!listId.isEmpty()) {
            taskListener.getLogger().println("[Exclusion] -> Releasing all the resources");
        }

        for (String id : listId) {

            DefaultIdType p = new DefaultIdType(id);
            Id i = p.allocate(false, run, pam, launcher, taskListener);
            Run<?, ?> absBuild = IdAllocationManager.getOwnerBuild(i.type.name);
            if (absBuild != null) {
                //We want to release only resources from the current job
                if (absBuild.getParent().getName().equals(run.getParent().getName())) {
                    //Releasing
                    i.cleanUp();
                }
            }
        }
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Critical block end";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> arg0) {
            return true;
        }
    }
}
