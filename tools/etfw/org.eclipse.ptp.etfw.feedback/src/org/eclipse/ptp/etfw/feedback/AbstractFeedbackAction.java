/**********************************************************************
 * Copyright (c) 2010,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.feedback;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.internal.etfw.feedback.Activator;

/**
 * Extend this class to add an action to the toolbar, an action that can be
 * performed on the selected item
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * @author beth tibbitts
 * @since 2.0
 * 
 */
public abstract class AbstractFeedbackAction {
	private String iconName;

	/**
	 * What is a better arg to run(), the IFeedbackItem, or the IMarker? We'll
	 * ask for implementation of both for now. If it's the IFeedbackItem, we
	 * will have to cache that in the marker object.
	 * 
	 * @param item
	 */
	public abstract void run(IMarker marker);

	/**
	 * @since 6.0
	 */
	public abstract void run(IFeedbackItem item);

	public abstract String getPluginId();

	public void addIcon(String iconName) {
		this.iconName = iconName;
	}

	public String getIcon() {
		return this.iconName;
	}

	public ImageDescriptor getIconImageDescriptor() {
		ImageDescriptor imgDesc = Activator.imageDescriptorFromPlugin(getPluginId(), iconName);
		return imgDesc;
	}

	abstract public String getToolTip();

	/** get text e.g. could be used for a menu item for this action */
	abstract public String getText();

}
