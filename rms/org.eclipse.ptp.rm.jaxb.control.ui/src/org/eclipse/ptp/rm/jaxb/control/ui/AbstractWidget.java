/**********************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * Base class for implementations of custom widgets.
 * 
 * @since 1.1
 * 
 */
public abstract class AbstractWidget extends Composite {

	/**
	 * Constructor that supplies the parent composite and information that may be useful to the widget
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param wd
	 *            a widget descriptor containing information useful to the control
	 * @since 1.2
	 */
	protected AbstractWidget(Composite parent, IWidgetDescriptor wd) {
		super(parent, wd.getStyle());
	}

	/**
	 * Constructor that supplies the parent composite and additional information that may be useful to the widget
	 * 
	 * @param parent
	 *            a widget which will be the parent of the new instance (cannot be null)
	 * @param wd
	 *            a widget descriptor containing additional information useful to the control
	 * @since 2.0
	 */
	protected AbstractWidget(Composite parent, IWidgetDescriptor2 wd) {
		this(parent, (IWidgetDescriptor) wd);
	}
}
