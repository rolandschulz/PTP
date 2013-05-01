/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.signals;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement Chu
 */
public class SignalsView extends AbstractDebugEventHandlerView implements ISelectionListener, INullSelectionListener,
		IPreferenceChangeListener, IDebugExceptionHandler {
	public class SignalsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return getModelPresentation().getImage(element);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IPSignal) {
				try {
					switch (columnIndex) {
					case 0:
						return ((IPSignal) element).getName();
					case 1:
						return (((IPSignal) element).isPassEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
					case 2:
						return (((IPSignal) element).isStopEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
					case 3:
						return ((IPSignal) element).getDescription();
					}
				} catch (DebugException e) {
				}
			}
			return null;
		}

		private IDebugModelPresentation getModelPresentation() {
			return PTPDebugUIPlugin.getDebugModelPresentation();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Viewer createViewer(Composite parent) {
		Preferences.addPreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), this);

		// add tree viewer
		final SignalsViewer vv = new SignalsViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		vv.setContentProvider(createContentProvider());
		vv.setLabelProvider(new SignalsViewLabelProvider());
		vv.setUseHashlookup(true);

		// listen to selection in debug view
		getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		setEventHandler(new SignalsViewEventHandler(this));

		return vv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	@Override
	protected void createActions() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	@Override
	protected String getHelpContextId() {
		// return IPDebugHelpContextIds.SIGNALS_VIEW;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		updateObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface
	 * .action.IToolBarManager)
	 */
	@Override
	protected void configureToolBar(IToolBarManager tbm) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!isAvailable() || !isVisible()) {
			return;
		}
		if (selection == null) {
			setViewerInput(new StructuredSelection());
		} else if (selection instanceof IStructuredSelection) {
			setViewerInput((IStructuredSelection) selection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	public void preferenceChange(PreferenceChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.ui.views.IDebugExceptionHandler#
	 * handleException(org.eclipse.debug.core.DebugException)
	 */
	public void handleException(DebugException e) {
		showMessage(e.getMessage());
	}

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	private IContentProvider createContentProvider() {
		SignalsViewContentProvider cp = new SignalsViewContentProvider();
		cp.setExceptionHandler(this);
		return cp;
	}

	protected void setViewerInput(IStructuredSelection ssel) {
		IPDebugTarget target = null;
		if (ssel != null && ssel.size() == 1) {
			Object input = ssel.getFirstElement();
			if (input instanceof IDebugElement && ((IDebugElement) input).getDebugTarget() instanceof IPDebugTarget) {
				target = (IPDebugTarget) ((IDebugElement) input).getDebugTarget();
			}
		}

		if (getViewer() == null) {
			return;
		}

		Object current = getViewer().getInput();
		if (current != null && current.equals(target)) {
			updateObjects();
			return;
		}

		showViewer();
		getViewer().setInput(target);
		updateObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	@Override
	protected void becomesHidden() {
		setViewerInput(new StructuredSelection());
		super.becomesHidden();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	@Override
	protected void becomesVisible() {
		super.becomesVisible();
		IViewPart part = getSite().getPage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (part != null) {
			ISelection selection = getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
			selectionChanged(part, selection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		Preferences.removePreferenceChangeListener(PTPDebugCorePlugin.getUniqueIdentifier(), this);
		super.dispose();
	}
}
