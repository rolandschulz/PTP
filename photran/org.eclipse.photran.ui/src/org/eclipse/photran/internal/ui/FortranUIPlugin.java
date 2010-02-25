/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class for the Photran UI plug-in
 * 
 * @author (generated)
 */
public class FortranUIPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.photran.ui";
    
    // The shared instance.
    private static FortranUIPlugin plugin;

    /**
     * The constructor.
     */
    public FortranUIPlugin()
    {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static FortranUIPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.photran.ui", path);
    }

    /**
     * Set the default preferences plugin values here.
     */
    protected void initializeDefaultPluginPreferences()
    {
    }
    
    public static void log(Throwable e) {
        log("Error", e); //$NON-NLS-1$
    }

    public static void log(String message, Throwable e) {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
}
