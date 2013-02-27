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
	 * @param parent
	 *            name of the model, which will correspond to the name of an attribute if the widget value is to be saved
	 * @param style
	 *            the handler for notifying other widgets to refresh their values
	 */
	protected AbstractWidget(Composite parent, IWidgetDescriptor wd) {
		super(parent, wd.getStyle());
	}
}
