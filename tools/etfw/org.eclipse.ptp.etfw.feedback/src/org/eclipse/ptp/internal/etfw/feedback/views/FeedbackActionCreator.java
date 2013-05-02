/**********************************************************************
 * Copyright (c) 2009,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackAction;
import org.eclipse.ptp.internal.etfw.feedback.Activator;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There is no guarantee that
 * this API will work or that it will remain the same. We do not recommending using this API without consulting with the
 * etfw.feedback team.
 * 
 * @since 2.0
 */
public class FeedbackActionCreator {
	private final boolean traceOn = false;
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_CLASSNAME = "class"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$

	/**
	 * Find the eclipse extension (if any) that specifies an optional feedback
	 * action
	 * 
	 * @param viewID
	 *            id of the view that action can be added to. Some action
	 *            extensions may specify a viewID, in which case they are ONLY
	 *            added to that view.
	 * 
	 */
	public AbstractFeedbackAction findFeedbackAction(String viewID) {
		final String pid = Activator.PLUGIN_ID;
		final String extid = Activator.FEEDBACK_ACTION_EXTENSION_ID;
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(pid, extid).getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extn = extensions[i];
			String extLabel = extn.getLabel();
			if (traceOn)
				System.out.println("Found extension for " + extLabel + "  id=" + extn.getUniqueIdentifier()); //$NON-NLS-1$ //$NON-NLS-2$
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement ice = configElements[j];
				// from this thing we should be able to make the specific parts
				// that we need.
				// specifically: an action. We assume one or none found for now.
				// Could have multiple later??
				if (traceOn)
					System.out.println(ice.getAttributeNames());
				String className = ice.getAttribute(ATTR_CLASSNAME);
				String name = ice.getAttribute(ATTR_NAME);
				String icon = ice.getAttribute(ATTR_ICON);
				String actionViewID = ice.getAttribute("viewID"); //$NON-NLS-1$
				if (actionViewID != null) {
					// if available, viewID specifies that the action extension
					// is only to be added
					// to the view whose view ID is this.
					if (!actionViewID.equals(viewID))
						continue;
				}
				if (traceOn)
					System.out.println("class=" + className + "   name=" + name); //$NON-NLS-1$ //$NON-NLS-2$

				Object obj = null;
				try {
					obj = ice.createExecutableExtension(ATTR_CLASSNAME);// err
					if (obj instanceof AbstractFeedbackAction) {
						AbstractFeedbackAction fa = (AbstractFeedbackAction) obj;
						fa.addIcon(icon);
						return fa;
					}
				} catch (CoreException e) {
					System.out.println("Failed to create class " + className); //$NON-NLS-1$
					e.printStackTrace();
				}

			}
		}
		return null;
	}

}
