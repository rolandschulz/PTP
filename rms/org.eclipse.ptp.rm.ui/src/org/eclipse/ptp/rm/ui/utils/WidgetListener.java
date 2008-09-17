/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ui.utils;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;

/**
 * Common features useful for widget event listeners on PreferencePages.
 * <p>
 * At the moment, only enable/disable feature is provided. The listener must be
 * disabled before updating contents of input widgets. When content of SWT
 * widgets is changed by code, some of the widgets do dispatch events as if they
 * were user events.
 *
 * @author Daniel Felix Ferber
 */
public abstract class WidgetListener implements SelectionListener,
		ModifyListener {
	/** State of the listener (enabled/disabled). */
	private boolean listenerEnabled = true;

	/** Enable the listener to handle events. */
	public synchronized void enable() {
		listenerEnabled = true;
	}

	/** Disable listener, received events shall be ignored. */
	public synchronized void disable() {
		listenerEnabled = false;
	}

	/** Test if the listener is enabled. */
	public synchronized boolean isEnabled() {
		return listenerEnabled;
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		if (isEnabled()) {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: widgetDefaultSelected on {0}", calculareSource(e));
			doWidgetDefaultSelected(e);
		} else {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: ignored widgetDefaultSelected on {0}", calculareSource(e));
		}
	}

	protected void doWidgetDefaultSelected(SelectionEvent e) {
		// Default empty implementation.
	}

	public void widgetSelected(SelectionEvent e) {
		if (isEnabled()) {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: widgetSelected on {0}", calculareSource(e));
			doWidgetSelected(e);
		} else {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: ignored widgetSelected on {0}", calculareSource(e));
		}
	}

	protected void doWidgetSelected(SelectionEvent e) {
		// Default empty implementation.
	}

	public void modifyText(ModifyEvent e) {
		if (isEnabled()) {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: modifyText on {0}", calculareSource(e));
			doModifyText(e);
		} else {
			DebugUtil.trace(DebugUtil.DATASOURCE_TRACING, "Widget listener: ignored modifyText on {0}", calculareSource(e));
		}
	}

	protected void doModifyText(ModifyEvent e) {
		// Default empty implementation.
	}

	static private String calculareSource(TypedEvent e) {
		Object source = e.getSource();
		return source.getClass().getName();
	}
}
