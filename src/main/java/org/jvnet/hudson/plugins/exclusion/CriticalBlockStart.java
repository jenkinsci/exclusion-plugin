package org.jvnet.hudson.plugins.exclusion;

import edu.umd.cs.findbugs.annotations.NonNull;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Executor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build step -&gt; Start of critical zone
 *
 * @author Anthony Roux
 */
public class CriticalBlockStart extends Builder implements SimpleBuildStep {

    public static IdAllocationManager pam = null;

    @DataBoundConstructor
    public CriticalBlockStart() {
    }

    public String getDisplayName() {
        return "Critical block start";
    }
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath filePath, @NonNull Launcher launcher, @NonNull TaskListener taskListener) throws InterruptedException, IOException {
        pam = IdAllocationManager.getManager(Executor.currentExecutor().getOwner());

        PrintStream logger = taskListener.getLogger();

        EnvVars environment = run.getEnvironment(taskListener);
        final List<String> listId = new ArrayList<>();
        // Add to a list all "variableEnv" (which are added by IdAllocator)
        // Each variableEnv is a resource
        for (Map.Entry<String, String> e: environment.entrySet()) {
            String cle = e.getKey();

            String name = "variableEnv" + run.getParent().getName();
            if (cle.contains(name)) {
                String value = e.getValue();
                listId.add(value);
            }
        }

        for (String id : listId) {
            DefaultIdType p = new DefaultIdType(id);

            logger.println("[Exclusion] -> Allocating resource : " + id);
            //Allocating resources
            // if one is already used, just wait for it to be released
            Id resource = p.allocate(true, run, pam, launcher, taskListener);

            logger.println("[Exclusion] -> Assigned " + resource.get());
        }
        if (!listId.isEmpty()) {
            logger.println("[Exclusion] -> Resource allocation complete");
        }
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Critical block start";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> arg0) {
            return true;
        }
    }
}
