/**********************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.etfw.feedback.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.etfw.feedback.AbstractFeedbackAction;
import org.eclipse.ptp.etfw.feedback.Activator;

public class FeedbackActionCreator {
	private boolean traceOn=true;
	private static final String ATTR_NAME = "name";
	private static final String ATTR_CLASSNAME = "class";
	private static final String ATTR_ICON = "icon";

	/**
	 * Find the eclipse extension (if any) that specifies an optional feedback action
	 * @param viewID id of the view that action can be added to.  Some action extensions may specify a viewID, in which case they are ONLY added to that view.
	 * 
	 */
	public AbstractFeedbackAction findFeedbackAction(String viewID) {
		final String pid = Activator.PLUGIN_ID;
		final String extid = Activator.FEEDBACK_ACTION_EXTENSION_ID;
		IExtensionRegistry ier = Platform.getExtensionRegistry();
		IExtensionPoint ixp = ier.getExtensionPoint(pid, extid);
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(pid, extid).getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extn = extensions[i];
			String extLabel = extn.getLabel();
			if (traceOn)System.out.println("Found extension for " + extLabel + "  id=" + extn.getUniqueIdentifier());
			IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
			for (int j = 0; j < configElements.length; j++) {
				IConfigurationElement ice = configElements[j];
				// from this thing we should be able to make the specific parts that we need.
				// specifically: an action. We assume one or none found for now.
				// Could have multiple later??
				if (traceOn)System.out.println(ice.getAttributeNames());
				String id = ice.getAttribute("id");// is this the plugin id? no
				String className = ice.getAttribute(ATTR_CLASSNAME);
				String name = ice.getAttribute(ATTR_NAME);
				String icon = ice.getAttribute(ATTR_ICON);
				String actionViewID = ice.getAttribute("viewID");
				if(actionViewID!=null) {
					// if available, viewID specifies that the action extension is only to be added
					// to the view whose view ID is this.
					if(!actionViewID.equals(viewID))
						continue;				
				}
				String [] ans=ice.getAttributeNames();
				if (traceOn)System.out.println("class=" + className + "   name=" + name);
				// Determine if this action extension is for the view in the current plugin
				String nsi = ice.getNamespaceIdentifier();// identifies which plugin this extension comes from
				Object parent = ice.getParent();
				Object obj = null;
				try {
					obj = ice.createExecutableExtension(ATTR_CLASSNAME);//err
					if (obj instanceof AbstractFeedbackAction) {
						AbstractFeedbackAction fa = (AbstractFeedbackAction) obj;
						fa.addIcon(icon);
						return fa;
					}
				} catch (CoreException e) {
					System.out.println("Failed to create class " + className);
					e.printStackTrace();
				}

			}
		}
		return null;
	}

}
