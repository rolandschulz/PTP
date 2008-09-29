/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.sputiming.ui.debug.Debug;
import org.eclipse.ptp.cell.sputiming.ui.parse.ParsedTimingFile;
import org.eclipse.ptp.cell.sputiming.ui.views.SpuTimingDiagramView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

	//The shared instance.
	private static Activator plugin;
	
	/**
	 * SPU timing information thats is currently being displayed.
	 * If null, then nothing is displayed
	 */
	private ParsedTimingFile parsedTiming = null;
	
	/**
	 * The view that is currently displaying sputiming diagram,
	 * or null if this view was not yet created.
	 */
	private SpuTimingDiagramView view = null;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.cell.plugin.sputiming", path); //$NON-NLS-1$
	}
	
	public void setTiming(ParsedTimingFile parsedTiming) {
		this.parsedTiming = parsedTiming;
		
		// If view exists, force to update
		if (view != null) {
			view.invalidateTiming();
		} 
		
		// Force to show view
		final IWorkbench workbench = PlatformUI.getWorkbench();
		Display display = workbench.getDisplay();
		display.syncExec(
			new Runnable() {
				public void run() {
					try {
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
								page.showView("org.eclipse.ptp.cell.sputiming.views.SpuTimingDiagramView"); //$NON-NLS-1$
							}
						}
					} catch (Exception e) {
						Debug.POLICY.error(Debug.DEBUG_UI, e);
						Debug.POLICY.logError(e);
					}
				}
			}
			);

	}
	
	public ParsedTimingFile getTiming() {
		return parsedTiming;
	}
	
	public void setView(SpuTimingDiagramView view) {
		this.view = view;
	}
}
