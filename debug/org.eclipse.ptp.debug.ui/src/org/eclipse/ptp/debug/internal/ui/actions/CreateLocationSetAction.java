/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.model.IPLocationSet;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.internal.ui.views.locations.PLocationView;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.debug.ui.views.ParallelDebugView;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class CreateLocationSetAction extends Action {
	public static final String ID = "org.eclipse.ptp.debug.ui.CreateLocationSetAction"; //$NON-NLS-1$
	public static final String name = Messages.CreateLocationSetAction_0;

	protected PLocationView view;

	public CreateLocationSetAction(PLocationView view) {
		this.view = view;
		setText(name);
		setImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_VAR_ADD_NORMAL));
		// setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		if (view == null || view.getViewer() == null || !(view.getViewer().getInput() instanceof IPSession)) {
			setEnabled(false);
			return;
		}

		IPSession session = (IPSession) view.getViewer().getInput();
		if (session == null || !session.isReady()) {
			setEnabled(false);
			return;
		}

		if (!(view.getViewer().getSelection() instanceof IStructuredSelection)) {
			setEnabled(false);
			return;
		}

		ParallelDebugView parallelDebugView = null;
		try {
			IWorkbenchPage page = view.getViewSite().getPage();
			IViewPart part = page.showView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
			if (part instanceof ParallelDebugView)
				parallelDebugView = (ParallelDebugView) part;
		} catch (NullPointerException e) {
		} catch (PartInitException e) {
		}

		if (parallelDebugView == null) {
			setEnabled(false);
			return;
		}

		String setID = null;
		IStructuredSelection selection = (IStructuredSelection) view.getViewer().getSelection();
		@SuppressWarnings("unchecked")
		IPLocationSet[] locations = (IPLocationSet[]) selection.toList().toArray(new IPLocationSet[0]);
		IElementHandler setManager = parallelDebugView.getCurrentElementHandler();
		for (IPLocationSet locationSet : locations) {
			IElementSet rootSet = setManager.getSetRoot();
			List<IElement> elementsMatchingTasks = new ArrayList<IElement>();
			int[] tasks = locationSet.getTasks().toArray();
			for (int taskID : tasks) {
				IElement element = rootSet.getElementByName(Integer.toString(taskID));
				elementsMatchingTasks.add(element);
			}

			String name = locationSet.getFunction() + ":" + locationSet.getFile() + ":" + locationSet.getLineNumber(); //$NON-NLS-1$ //$NON-NLS-2$
			/*
			 * The set already exists, destroy and recreate it
			 */
			if (setManager.getElementByID(name) instanceof IElementSet) {
				parallelDebugView.getUIManager().removeSet(name, setManager);
			}
			setID = parallelDebugView.getUIManager().createSet(elementsMatchingTasks.toArray(new IElement[0]), name, name,
					setManager);
		}

		if (setID != null)
			/*
			 * Select the last set created, if any
			 */
			parallelDebugView.selectSet((IElementSet) setManager.getElementByID(setID));
		parallelDebugView.refresh(false);
	}
}
