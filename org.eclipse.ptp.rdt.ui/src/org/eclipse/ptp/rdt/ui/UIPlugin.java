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
}
