package org.jvnet.hudson.plugins.exclusion;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.listeners.ItemListener;

/**
 *
 * first author Kohsuke Kawaguchi
 * fork by Anthony Roux
 */
public class PluginImpl extends Plugin {

    @Override
    public void start() throws Exception {
        IdTypeDescriptor.LIST.add(DefaultIdType.DescriptorImpl.INSTANCE);
    }

    @Extension
    public static final class RenameListener extends ItemListener {

        @Override
        public void onRenamed(Item item, String oldName, String newName) {
            IdAllocator.updateList(oldName, newName);
        }

        @Override
        public void onDeleted(Item item) {
            IdAllocator.deleteList(item.getName());
        }

        @Override
        public void onCreated(Item item) {
            super.onCreated(item);
        }
    }
}
