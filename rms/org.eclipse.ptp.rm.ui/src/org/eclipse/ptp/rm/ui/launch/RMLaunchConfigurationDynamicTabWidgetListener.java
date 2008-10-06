/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.launch;

import org.eclipse.ptp.rm.ui.utils.WidgetListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;

public abstract class RMLaunchConfigurationDynamicTabWidgetListener extends WidgetListener {
	private final AbstractRMLaunchConfigurationDynamicTab dynamicTab;

	public RMLaunchConfigurationDynamicTabWidgetListener(
			AbstractRMLaunchConfigurationDynamicTab dynamicTab) {
		super();
		this.dynamicTab = dynamicTab;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		if (isEnabled()) {
			dynamicTab.fireContentsChanged();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		super.widgetSelected(e);
		if (isEnabled()) {
			dynamicTab.fireContentsChanged();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		super.widgetDefaultSelected(e);
		if (isEnabled()) {
			dynamicTab.fireContentsChanged();
		}
	}
}
