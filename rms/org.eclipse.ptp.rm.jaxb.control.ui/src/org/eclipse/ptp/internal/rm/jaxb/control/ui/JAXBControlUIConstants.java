/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui;

import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and the interest of uncluttered code.
 */
public class JAXBControlUIConstants extends JAXBUIConstants {

	public static final String VALIDATE = "ValidateJob";//$NON-NLS-1$
	public static final long VALIDATE_TIMER = 100;

	public static final String NAME_TAG = AT + NAME;
	public static final String VALUE_TAG = AT + VALUE;

	public static final String CHECKED_ATTRIBUTES = "checked_attributes_";//$NON-NLS-1$
	public static final String SHOW_ONLY_CHECKED = "show_only_checked";//$NON-NLS-1$
	public static final String IS_PRESET = "is_preset";//$NON-NLS-1$

	public static final String COLUMN_NAME = "Name";//$NON-NLS-1$
	public static final String COLUMN_VALUE = "Value";//$NON-NLS-1$
	public static final String COLUMN_DEFAULT = "Default";//$NON-NLS-1$
	public static final String COLUMN_DESC = "Description";//$NON-NLS-1$
	public static final String COLUMN_STATUS = "Status";//$NON-NLS-1$
	public static final String COLUMN_TOOLTIP = "Tooltip";//$NON-NLS-1$
	public static final String COLUMN_TYPE = "Type";//$NON-NLS-1$

	public static final String TABLE = "table";//$NON-NLS-1$
	public static final String TREE = "tree";//$NON-NLS-1$
	public static final String LABEL = "label";//$NON-NLS-1$
	public static final String TEXT = "text";//$NON-NLS-1$
	public static final String CHECKBOX = "checkbox";//$NON-NLS-1$
	public static final String SPINNER = "spinner";//$NON-NLS-1$
	public static final String COMBO = "combo";//$NON-NLS-1$
	public static final String RADIOBUTTON = "radiobutton";//$NON-NLS-1$
	public static final String BROWSE = "browse";//$NON-NLS-1$
	public static final String ACTION = "action";//$NON-NLS-1$
	public static final String CUSTOM = "custom";//$NON-NLS-1$

	public static final String WIDGET_EXT_PT = "widget";//$NON-NLS-1$
	public static final String WIDGETCLASS = "widgetClass";//$NON-NLS-1$
	public static final String UPDATEMODELCLASS = "updateModelClass";//$NON-NLS-1$

}
