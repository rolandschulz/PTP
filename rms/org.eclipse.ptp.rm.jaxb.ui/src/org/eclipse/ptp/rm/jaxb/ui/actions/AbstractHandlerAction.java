package org.eclipse.ptp.rm.jaxb.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;

/**
 * Base class for actions on the job status object.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractHandlerAction implements IObjectActionDelegate, IJAXBUINonNLSConstants {
	protected ICommandJobRemoteOutputHandler handler;
	protected boolean error;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (handler != null) {
			String contents = handler.getFileContents();
			IOConsole console = new IOConsole(handler.getRemoteFilePath(), null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
			console.activate();
			console.getInputStream().appendData(contents);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}

		PersistentCommandJobStatus status = (PersistentCommandJobStatus) ((IStructuredSelection) selection).getFirstElement();
		if (getReady(status)) {
			action.setEnabled(true);
			setHandler(status);
		} else {
			action.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	protected boolean getReady(PersistentCommandJobStatus status) {
		if (error) {
			return status.getErrReady();
		}
		return status.getOutReady();
	}

	protected void setHandler(PersistentCommandJobStatus status) {
		if (error) {
			handler = status.getErrorHandler();
		} else {
			handler = status.getOutputHandler();
		}
	}
}
