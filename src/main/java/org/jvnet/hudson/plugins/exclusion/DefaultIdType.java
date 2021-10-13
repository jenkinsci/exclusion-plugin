package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Exclusion resource that can be configured per job.
 *
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public class DefaultIdType extends IdType {

    @DataBoundConstructor
    public DefaultIdType(String name) {
        super(name);
    }

    @Override
    public Id allocate(boolean launchAlloc, Run<?, ?> run, final IdAllocationManager manager, Launcher launcher, TaskListener taskListener) throws IOException, InterruptedException {
        final String n;

        if (launchAlloc) {
            n = manager.allocate(run, getFixedId(), taskListener);
        } else {
            n = getFixedId();
        }

        return new Id(this) {

            @Override
            public String get() {
                return n;
            }

            @Override
            public void cleanUp() {
                manager.free(n);
            }
        };
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.get().getDescriptorOrDie(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends IdTypeDescriptor {

        @Override
        public String getDisplayName() {
            return "New Resource";
        }
    }
}
