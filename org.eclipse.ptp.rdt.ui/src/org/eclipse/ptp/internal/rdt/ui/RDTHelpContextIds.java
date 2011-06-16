/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui;

import org.eclipse.ptp.rdt.ui.UIPlugin;

/**
 * This interface holds constants used in creating context sensitive help.
 * 
 * The string is the id of the help context in context_RDT.xml in
 * org.eclipse.ptp.rdt.doc.user.
 * 
 * @author bergerm
 */
public final class RDTHelpContextIds {

	private RDTHelpContextIds() {
	}

	public static final String PREFIX = UIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String CONVERTING_TO_REMOTE_PROJECT = PREFIX + "converting_to_remote_project_context"; //$NON-NLS-1$
	public static final String CREATING_A_REMOTE_PROJECT = PREFIX + "creating_a_remote_project_context"; //$NON-NLS-1$
	public static final String SERVICE_MODEL_WIZARD = PREFIX + "service_model_wizard_context"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String SERVICE_MODEL_PROPERTIES = PREFIX + "service_model_properties_context"; //$NON-NLS-1$
	public static final String REMOTE_CALL_HIERARCHY = PREFIX + "remote_call_hierarchy_view_context"; //$NON-NLS-1$
	public static final String REMOTE_TYPE_HIERARCHY = PREFIX + "remote_type_hierarchy_view_context"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String REMOTE_INCLUDE_BROWSER = PREFIX + "remote_include_browser_view_context"; //$NON-NLS-1$
	public static final String REMOTE_C_CPP_EDITOR = PREFIX + "remote_C_CPP_editor_context"; //$NON-NLS-1$
	public static final String REMOTE_C_CPP_SEARCH = PREFIX + "remote_C_CPP_search_context"; //$NON-NLS-1$
	public static final String SERVICE_MODEL_PROPERTY_PAGE = PREFIX + "service_model_properties_context"; //$NON-NLS-1$
	public static final String REMOTE_INCLUDE_TAB = PREFIX + "remote_include_tab_context"; //$NON-NLS-1$
	public static final String REMOTE_SYMBOL_TAB = PREFIX + "remote_symbol_tab_context"; //$NON-NLS-1$

	public static final String REMOTE_C_CPP_OUTLINE = PREFIX + "remote_C_CPP_outline_view_context"; //$NON-NLS-1$
	
}
