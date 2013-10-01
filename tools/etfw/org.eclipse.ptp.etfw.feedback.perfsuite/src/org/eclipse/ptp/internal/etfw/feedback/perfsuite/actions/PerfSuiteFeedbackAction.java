/*******************************************************************************
 * Copyright (c) 2013 The Board of Trustees of the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.etfw.feedback.perfsuite.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackAction;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.Activator;

/**
 * PerfSuite action that's added to the ETFw Feedback view.
 * 
 * @author Rui Liu
 * 
 */
public class PerfSuiteFeedbackAction extends AbstractFeedbackAction {

	@Override
	public void run(IMarker marker) {
		String name = "(unknown)";
		try {
			name = (String) marker.getAttribute("name");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.openInformation(null, "PerfSuite action", "The current location of the executable is: " + name);

	}

	@Override
	public void run(IFeedbackItem item) {
		MessageDialog.openInformation(null, "PerfSuite action", "The action for an IFeedbackItem, currently no-op.");

	}

	@Override
	public String getToolTip() {
		return "PerfSuiteFeedbackAction tooltip";
	}

	@Override
	public String getText() {
		return "PerfSuiteFeedbackAction text";
	}

	@Override
	public String getPluginId() {
		return Activator.PLUGIN_ID;
	}

}
