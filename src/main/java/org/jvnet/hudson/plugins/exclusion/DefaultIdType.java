package org.jvnet.hudson.plugins.exclusion;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.Launcher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
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

            public String get() {
                return n;
            }

            public void cleanUp() {
                manager.free(n);
            }
        };
    }

    public DescriptorImpl getDescriptor() {
        return DescriptorImpl.INSTANCE;
    }

    public static final class DescriptorImpl extends IdTypeDescriptor {

        private DescriptorImpl() {
            super(DefaultIdType.class);
        }

        public DefaultIdType newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // TODO: we need form binding from JSON
            return new DefaultIdType(formData.getString("name"));
        }

        public String getDisplayName() {
            return "New Resource";
        }
        public static final DescriptorImpl INSTANCE = new DescriptorImpl();
    }
}
