/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui;

import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.ui.serviceproviders.IndexLocationChangeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.osgi.framework.BundleContext;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class UIPlugin extends Plugin {

	private static UIPlugin fInstance = null;

	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.ui"; //$NON-NLS-1$
	public static final String TYPE_HIERARCHY_VIEW_ID = "org.eclipse.ptp.rdt.ui.typeHierarchy"; //$NON-NLS-1$
	public static final String CALL_HIERARCHY_VIEW_ID = "org.eclipse.ptp.rdt.ui.callHierarchy"; //$NON-NLS-1$
	public static final String INCLUDE_BROWSER_VIEW_ID = "org.eclipse.ptp.rdt.ui.includeBrowser"; //$NON-NLS-1$

	private static final String PREFERENCE_HAS_ALREADY_RUN = "org.eclipse.ptp.rdt.ui.hasRun"; //$NON-NLS-1$
	public static final String REMOTE_SEARCH_ACTION_SET_ID = "org.eclipse.ptp.rdt.ui.SearchActionSet"; //$NON-NLS-1$

	public UIPlugin() {
		fInstance = this;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		IndexLocationChangeListener.startListening();
		turnOffIndexerAnnotations();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			IndexLocationChangeListener.stopListening();
		} finally {
			super.stop(context);
		}
	}

	public static UIPlugin getDefault() {
		return fInstance;
	}

	/**
	 * Turns off the C/C++ Indexer Marker annotations.
	 * 
	 * This is done because there are currently bugs with false annotations
	 * being presented by the remote editor, so its better to just not show them
	 * at all.
	 * 
	 * Only runs once, the first time RDT is used.
	 * 
	 * TODO Find a better way to fix this!
	 */
	private static void turnOffIndexerAnnotations() {
		Preferences preferences = EditorsPlugin.getDefault().getPluginPreferences();
		boolean hasAlreadyRun = preferences.getBoolean(PREFERENCE_HAS_ALREADY_RUN); // default
																					// is
																					// false

		if (!hasAlreadyRun) {
			// Set the preferences to false, this is the same as going to the
			// Annotations
			// preference page for C/C++ Indexer Markers and unchecking the
			// checkboxes.
			preferences.setValue("indexResultIndicationInOverviewRuler", false); //$NON-NLS-1$
			preferences.setValue("indexResultIndicationInVerticalRuler", false); //$NON-NLS-1$
			preferences.setValue("indexResultIndication", false); //$NON-NLS-1$

			// only do this once ever
			preferences.setValue(PREFERENCE_HAS_ALREADY_RUN, true);

			EditorsPlugin.getDefault().savePluginPreferences();
		}
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

	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), ICStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}
}
