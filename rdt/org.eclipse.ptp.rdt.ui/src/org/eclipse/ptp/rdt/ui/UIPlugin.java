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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.osgi.framework.BundleContext;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 *
 */
public class UIPlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.ui"; //$NON-NLS-1$
	public static final String TYPE_HIERARCHY_VIEW_ID = "org.eclipse.ptp.rdt.ui.typeHierarchy"; //$NON-NLS-1$
	public static final String CALL_HIERARCHY_VIEW_ID = "org.eclipse.ptp.rdt.ui.callHierarchy"; //$NON-NLS-1$
	public static final String INCLUDE_BROWSER_VIEW_ID = "org.eclipse.ptp.rdt.ui.includeBrowser"; //$NON-NLS-1$
	
	private static final String PREFERENCE_HAS_ALREADY_RUN = "org.eclipse.ptp.rdt.ui.hasRun"; //$NON-NLS-1$
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		turnOffIndexerAnnotations();
	}
	
	
	
	/**
	 * Turns off the C/C++ Indexer Marker annotations.
	 * 
	 * This is done because there are currently bugs with false annotations being
	 * presented by the remote editor, so its better to just not show them at all.
	 * 
	 * Only runs once, the first time RDT is used.
	 * 
	 * TODO Find a better way to fix this!
	 */
	private static void turnOffIndexerAnnotations() {
		Preferences preferences = EditorsPlugin.getDefault().getPluginPreferences();
		boolean hasAlreadyRun = preferences.getBoolean(PREFERENCE_HAS_ALREADY_RUN); // default is false
		
		if(!hasAlreadyRun) {
			// Set the preferences to false, this is the same as going to the Annotations 
			// preference page for C/C++ Indexer Markers and unchecking the checkboxes. 
			preferences.setValue("indexResultIndicationInOverviewRuler", false); //$NON-NLS-1$
			preferences.setValue("indexResultIndicationInVerticalRuler", false); //$NON-NLS-1$
			preferences.setValue("indexResultIndication", false); //$NON-NLS-1$
			
			// only do this once ever
			preferences.setValue(PREFERENCE_HAS_ALREADY_RUN, true);
			
			EditorsPlugin.getDefault().savePluginPreferences();
		}
	}
	
	
}
