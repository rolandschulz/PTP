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
package org.eclipse.ptp.etfw.tau.perfdmf;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 3.0
 */
public class PerfDMFUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.etfw.tau.perfdmf"; //$NON-NLS-1$

	// The shared instance
	private static PerfDMFUIPlugin plugin;

	// A handle to the view
	static PerfDMFView theView;

	static final String VIEW_ID = "org.eclipse.ptp.etfw.tau.perfdmf.views.PerfDMFView"; //$NON-NLS-1$

	/**
	 * Add the profile data at location to the user's perfdmf database, organized by projectName and projectType
	 * 
	 * @param projectName
	 *            The project that produced the data
	 * @param projectType
	 *            The TAU options used in the production of the data
	 * @param location
	 *            The location of the profile data
	 * @return True on success, false on failure to upload
	 * @since 3.0
	 */
	public static boolean addPerformanceData(String projectName, String projectType, String trialName, IFileStore location,
			String dbname) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID);

			// when that class is initialized, it will call registerPerfDMFView
			// so we can get a handle on it
			return theView.addProfile(projectName, projectType, trialName, location, dbname);

			// return true;

		} catch (final Throwable t) {
			t.printStackTrace();
			return false;
		}

	}

	/**
	 * Add the profile data at location to the user's perfdmf database, organized by projectName and projectType
	 * 
	 * @param projectName
	 *            The project that produced the data
	 * @param projectType
	 *            The TAU options used in the production of the data
	 * @param location
	 *            The location of the profile data
	 * @return True on success, false on failure to upload
	 */
	public static boolean displayPerformanceData(String projectName, String projectType, String trialName) {// ,
																											// String
																											// location,
																											// String
																											// dbname
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID);

			// when that class is initialized, it will call registerPerfDMFView
			// so we can get a handle on it
			return theView.showProfile(projectName, projectType, trialName); // location,dbname

			// return true;

		} catch (final Throwable t) {
			t.printStackTrace();
			return false;
		}

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
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @since 2.0
	 */
	public static PerfDMFView getPerfDMFView() {

		if (theView == null) {

			final IViewReference viewReferences[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getViewReferences();
			for (final IViewReference viewReference : viewReferences) {
				if (VIEW_ID.equals(viewReference.getId())) {
					theView = (PerfDMFView) viewReference.getView(false);
					if (theView != null) {
						return theView;
					}
				}
			}

			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID);
			} catch (final PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return theView;
	}

	public static void registerPerfDMFView(PerfDMFView view) {
		theView = view;
	}

	/**
	 * The constructor
	 */
	public PerfDMFUIPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
