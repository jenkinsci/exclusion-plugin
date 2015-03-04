package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 * @author Anthony Roux
 */
public class DefaultIdType extends IdType {

    @DataBoundConstructor
    public DefaultIdType(String name) {
        super(name);
    }

    @Override
    public Id allocate(boolean launchAlloc, AbstractBuild<?, ?> build, final IdAllocationManager manager, Launcher launcher, BuildListener buildListener) throws IOException, InterruptedException {
        final String n;

        if (launchAlloc) {
            n = manager.allocate(build, getFixedId(), buildListener);
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
        return DescriptorImpl.INSTANCE;
    }

    @Extension
    public static final class DescriptorImpl extends IdTypeDescriptor {

        @Override
        public DefaultIdType newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // TODO: we need form binding from JSON
            return new DefaultIdType(formData.getString("name"));
        }

        @Override
        public String getDisplayName() {
            return "New Resource";
        }
        public static final DescriptorImpl INSTANCE = new DescriptorImpl();
    }
}
