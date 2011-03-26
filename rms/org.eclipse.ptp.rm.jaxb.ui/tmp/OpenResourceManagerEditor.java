/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.actions;

import java.net.URI;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ConfigurationChoiceDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.ui.views.ResourceManagerView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;

public class OpenResourceManagerEditor implements IViewActionDelegate, IJAXBUINonNLSConstants {

	private ResourceManagerView view;

	public void init(IViewPart view) {
		this.view = (ResourceManagerView) view;
	}

	public void run(IAction action) {
		ConfigurationChoiceDialog dialog = new ConfigurationChoiceDialog(view, view.getState());
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		String selected = dialog.getChoice();
		if (ZEROSTR.equals(selected)) {
			return;
		}

		try {
			URI uri = new URI(selected);
			URI fileToOpen = null;
			if (dialog.isPreset()) {
				fileToOpen = RemoteUIServicesUtils.exportResource(uri, view.getSite().getShell());
				if (fileToOpen == null) {
					return;
				}
			} else {
				fileToOpen = uri;
			}

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditor(page, uri, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
			// }
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(view.getSite().getShell(), t, Messages.OpenResourceManagerEditorAction_error,
					Messages.OpenResourceManagerEditorAction_title, false);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
