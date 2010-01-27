/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.managedbuilder.xlc.ui.properties;


import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.cdt.managedbuilder.xlc.ui.properties.XLCompilerPropertyPage;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.*;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ptp.rdt.managedbuilder.xlc.ui.messages.*;

/**
 * a new version of XL Compiler property page which handles both local and remote project
 *
 */
public class RemoteXLCompilerPropertyPage extends XLCompilerPropertyPage {

	/**
	 * override parent function, which is originally for a local directory browsing.
	 */
	public void createPathEditor() {
		IProject thisProject = ((IResource) (getElement().getAdapter(IResource.class))).getProject();
		if (RemoteNature.hasRemoteNature(thisProject)) {
			createPathEditor4RemoteProject();

		} else {
			super.createPathEditor();
		}
	}

	/**
	 * create a path editor for remote directory browsing.
	 */
	public void createPathEditor4RemoteProject() {
		
		Composite parent = getFieldEditorParent();
		final IProject thisProject = ((IResource) (getElement().getAdapter(IResource.class))).getProject();

		IHost projectHost = RSEUtils.getAnyConnection(thisProject.getLocationURI());
		
		if (projectHost != null) {
			
			final String projectHostName = projectHost.getHostName();

			fPathEditor = new RemoteDirectoryFieldEditor(PreferenceConstants.P_XL_COMPILER_ROOT, Messages.getString(
					"REMOTEXLCompilerPropertyPage_0", projectHost.getHostName()), parent, projectHost) { //$NON-NLS-1$
				protected boolean doCheckState() {
					//try to get a connected connection, if the host connection is disconnected, projectConnectedHost will be null
					//so we can validate the connection below. but we will just set it as a warning message, since once user click on browse
					//button,  a connection dialog will be pop up if the host connection is disconnected.
					IHost projectConnectedHost = RSEUtils.getConnection(thisProject.getLocationURI());
					if (projectConnectedHost == null) {

						setMessage(
								Messages.getString("XLCompilerPropertyPage_DisconnectedErrorMsg", projectHostName), IMessageProvider.WARNING); //$NON-NLS-1$

					}
					// always return true, as we don't want to fail cases when
					// compiler path is not existed
					else if (!super.doCheckState()) {
						setMessage(
								Messages.getString("XLCompilerPropertyPage_ErrorMsg", projectHostName), IMessageProvider.WARNING); //$NON-NLS-1$
					} else {
						setMessage(originalMessage);
					}

					return true;
				}

				protected boolean checkState() {
					return doCheckState();
				}

			};

			addField(fPathEditor);

			String currentPath = null;

			try {
				currentPath = thisProject.getPersistentProperty(new QualifiedName("", //$NON-NLS-1$
						PreferenceConstants.P_XL_COMPILER_ROOT));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (currentPath == null) {
				// if the property isn't set, then use the workbench preference
				IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
				currentPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
			}

			fPathEditor.setStringValue(currentPath);
		}else{
			//should never reach here for a well configured project
			setMessage(
					Messages.getString("XLCompilerPropertyPage_NoHostErrorMsg"), IMessageProvider.ERROR); //$NON-NLS-1$
		}

	}

	

}
