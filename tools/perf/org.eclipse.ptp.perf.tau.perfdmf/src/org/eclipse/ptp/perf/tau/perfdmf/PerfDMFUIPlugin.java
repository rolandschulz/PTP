/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Morris - initial API and implementation
 *    Wyatt Spear - various modifications
 ****************************************************************************/
package org.eclipse.ptp.perf.tau.perfdmf;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.perf.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PerfDMFUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.ptp.perf.tau.perfdmf";

    // The shared instance
    private static PerfDMFUIPlugin plugin;

    // A handle to the view
    static PerfDMFView theView;

    /**
     * The constructor
     */
    public PerfDMFUIPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static PerfDMFUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static void registerPerfDMFView(PerfDMFView view) {
        theView = view;
    }

    /**
     * Add the profile data at location to the user's perfdmf database, organized by projectName and projectType
     * @param projectName The project that produced the data
     * @param projectType The TAU options used in the production of the data
     * @param location The location of the profile data
     * @return True on success, false on failure to upload
     */
    public static boolean displayPerformanceData(String projectName, String projectType, String trialName) {//, String location, String dbname
        try {
            PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .showView("org.eclipse.ptp.perf.tau.perfdmf.views.PerfDMFView");

            // when that class is initialized, it will call registerPerfDMFView so we can get a handle on it
            return theView.showProfile(projectName, projectType, trialName); // location,dbname
            
            //return true;

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        
    }
    
    
    /**
     * Add the profile data at location to the user's perfdmf database, organized by projectName and projectType
     * @param projectName The project that produced the data
     * @param projectType The TAU options used in the production of the data
     * @param location The location of the profile data
     * @return True on success, false on failure to upload
     */
    public static boolean addPerformanceData(String projectName, String projectType, String trialName, String location, String dbname) {
        try {
            PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .showView("org.eclipse.ptp.perf.tau.perfdmf.views.PerfDMFView");

            // when that class is initialized, it will call registerPerfDMFView so we can get a handle on it
            return theView.addProfile(projectName, projectType, trialName, location, dbname);
            
            //return true;

        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        
    }

}
