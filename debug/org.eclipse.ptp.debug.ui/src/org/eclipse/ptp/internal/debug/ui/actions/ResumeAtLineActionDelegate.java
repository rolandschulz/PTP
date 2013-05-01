package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * @author clement
 *
 */
public class ResumeAtLineActionDelegate implements IEditorActionDelegate, IViewActionDelegate, IActionDelegate2 {
	private IWorkbenchPart fActivePart = null;
	private IResumeAtLineTarget fPartTarget = null;
	private IAction fAction = null;
	private ISelectionListener fSelectionListener = new DebugSelectionListener();
	protected ISuspendResume fTargetElement = null;

	class DebugSelectionListener implements ISelectionListener {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			fTargetElement = null;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() == 1) {
					Object object = ss.getFirstElement();
					if (object instanceof ISuspendResume) {
						fTargetElement = (ISuspendResume)object;
					}
				}
			}
			update();
		}
	}
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		init(action);
		bindTo(targetEditor);
	}
	public void init(IAction action) {
		this.fAction = action;
		if (action != null) {
			action.setText(Messages.ResumeAtLineActionDelegate_0);
		}
	}
	public void dispose() {
		fActivePart.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener);
		fActivePart = null;
		fPartTarget = null;
	}
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	public void run(IAction action) {
		if (fPartTarget != null && fTargetElement != null) {
			try {
				fPartTarget.resumeAtLine(fActivePart, fActivePart.getSite().getSelectionProvider().getSelection(), fTargetElement);
			}
			catch(CoreException e) {
				ErrorDialog.openError(fActivePart.getSite().getWorkbenchWindow().getShell(), "Resume At Line", "Resume at line failed", e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	public void selectionChanged(IAction action, ISelection selection) {
		this.fAction = action;
		update();
	}
	protected void update() {
		if (fAction == null) {
			return;
		}
		boolean enabled = false;
		if (fPartTarget != null && fTargetElement != null) {
			IWorkbenchPartSite site = fActivePart.getSite();
			if (site != null) {
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				if (selectionProvider != null) {
					ISelection selection = selectionProvider.getSelection();
					enabled = fTargetElement.isSuspended() && fPartTarget.canResumeAtLine(fActivePart, selection, fTargetElement);
				}
			}
		}
		fAction.setEnabled(enabled);
	}
	public void init(IViewPart view) {
		bindTo(view);
	}
	private void bindTo(IWorkbenchPart part) {
		fActivePart = part;
		if (part != null) {
			part.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener);
			fPartTarget = (IResumeAtLineTarget)part.getAdapter(IResumeAtLineTarget.class);
			if (fPartTarget == null) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				// TODO: we could restrict loading to cases when the debugging context is on
				if (adapterManager.hasAdapter(part, IResumeAtLineTarget.class.getName())) {
					fPartTarget = (IResumeAtLineTarget)adapterManager.loadAdapter(part, IResumeAtLineTarget.class.getName());
				}
			}
			// Force the selection update
			ISelection selection = part.getSite().getWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
			fSelectionListener.selectionChanged(part, selection);
		}
		update();		
	}
}
