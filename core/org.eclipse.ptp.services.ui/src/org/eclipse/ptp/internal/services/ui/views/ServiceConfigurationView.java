/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.services.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ServiceConfigurationView extends ViewPart {

	private class ServiceConfigurationLabelProvider extends WorkbenchLabelProvider {

		private final Font selectedFont;
		private final Font unSelectedFont;

		public ServiceConfigurationLabelProvider(Font font) {
			unSelectedFont = font;
			FontData fd = font.getFontData()[0];
			FontData selectedFontData = new FontData(fd.getName(), fd.getHeight(), SWT.BOLD);
			selectedFont = (Font) new LocalResourceManager(JFaceResources.getResources()).get(FontDescriptor
					.createFrom(selectedFontData));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.model.WorkbenchLabelProvider#getFont(java.lang.Object)
		 */
		@Override
		public Font getFont(Object element) {
			IServiceConfiguration conf = getServiceConfiguration(element);
			if (conf != null && conf == fManager.getActiveConfiguration()) {
				return selectedFont;
			}
			return unSelectedFont;
		}

		private IServiceConfiguration getServiceConfiguration(Object parentElement) {
			IServiceConfiguration conf = null;
			if (parentElement instanceof IAdaptable) {
				conf = (IServiceConfiguration) ((IAdaptable) parentElement).getAdapter(IServiceConfiguration.class);
			}
			return conf;
		}
	}

	/**
	 * Listener for service configuration selection events.
	 */
	private class ServiceModelEventListener implements IServiceModelEventListener {
		public void handleEvent(IServiceModelEvent event) {
			refreshViewer();
		}
	}

	private TreeViewer fViewer;
	private final IServiceModelManager fManager = ServiceModelManager.getInstance();
	private final ServiceModelEventListener fEventListener = new ServiceModelEventListener();

	public ServiceConfigurationView() {
		fManager.addEventListener(fEventListener, IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED
				| IServiceModelEvent.SERVICE_CONFIGURATION_SELECTED | IServiceModelEvent.SERVICE_CONFIGURATION_ADDED
				| IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		fViewer = new TreeViewer(parent, SWT.MULTI);
		fViewer.setContentProvider(new WorkbenchContentProvider());
		fViewer.setLabelProvider(new ServiceConfigurationLabelProvider(fViewer.getTree().getFont()));

		fViewer.setInput(ServiceModelManager.getInstance());

		createContextMenu();

		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(fViewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public synchronized void dispose() {
		fManager.removeEventListener(fEventListener);
		super.dispose();
	}

	public Font getFont() {
		if (fViewer == null) {
			return null;
		}
		return fViewer.getTree().getFont();
	}

	/**
	 * Refresh the tree viewer when the model changes
	 */
	public void refreshViewer() {
		fViewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				fViewer.refresh();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	/**
	 * Create the popup menu
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager(
				"#PopupMenu", "org.eclipse.ptp.services.ui.views.serviceConfigurationView.contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
		menuManager.setRemoveAllWhenShown(true);
		Menu menu = menuManager.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, fViewer);
	}
}
