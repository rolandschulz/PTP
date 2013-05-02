/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.feedback.sample.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackAction;
import org.eclipse.ptp.etfw.feedback.IFeedbackItem;
import org.eclipse.ptp.internal.etfw.feedback.sample.Activator;

/**
 * A sample action that can be added to the ETFw Feedback view
 * 
 * @author beth
 * 
 */
public class SampleFeedbackAction extends AbstractFeedbackAction {

	public void run(IMarker marker) {
		String name = "(unknown)";
		try {
			name = (String) marker.getAttribute("name");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDialog.openInformation(null, "Sample action", "Do something interesting here with marker: " + name);

	}

	public void run(IFeedbackItem item) {
		MessageDialog.openInformation(null, "Sample action", "Do something interesting here with IFeedbackItem");

	}

	public String getToolTip() {
		return "SampleFeedbackAction tooltip";
	}

	@Override
	public String getText() {
		return "SampleFeedbackAction text";
	}

	@Override
	public String getPluginId() {
		return Activator.PLUGIN_ID;
	}

}
