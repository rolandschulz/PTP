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
package org.eclipse.photran.internal.cdtinterface;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * Plug-in activator class.
 * 
 * @author (generated)
 */
public class CDTInterfacePlugin extends AbstractUIPlugin
{
    private static final String PLUGIN_ID = "org.eclipse.photran.cdtinterface"; //$NON-NLS-1$

    // The shared instance.
    private static CDTInterfacePlugin plugin;

    /**
     * The constructor.
     */
    public CDTInterfacePlugin()
    {
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
    public static CDTInterfacePlugin getDefault()
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
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static void log(Throwable e)
    {
        log("Error", e); //$NON-NLS-1$
    }

    public static void log(String message, Throwable e)
    {
        log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
    }

    public static void log(IStatus status)
    {
        getDefault().getLog().log(status);
    }
}
