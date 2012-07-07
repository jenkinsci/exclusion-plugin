package org.jvnet.hudson.plugins.exclusion;

import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public abstract class IdTypeDescriptor extends Descriptor<IdType> {

    protected IdTypeDescriptor(Class<? extends IdType> clazz) {
        super(clazz);
    }

    @Override
    public final IdType newInstance(StaplerRequest req) throws FormException {
        throw new UnsupportedOperationException();
    }
    public static final List<IdTypeDescriptor> LIST = new ArrayList<IdTypeDescriptor>();
}
