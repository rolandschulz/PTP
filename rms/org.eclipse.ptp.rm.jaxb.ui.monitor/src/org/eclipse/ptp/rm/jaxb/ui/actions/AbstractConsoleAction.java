package org.eclipse.ptp.rm.jaxb.ui.actions;

import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Base class for actions on the job status object.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractConsoleAction implements IObjectActionDelegate {
	protected boolean error;
	protected JobStatusData status;
	protected IViewPart view;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (status != null) {
			String contents = getContents();
			IOConsole console = new IOConsole(getPath(), null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
			console.activate();
			IOConsoleOutputStream stream = console.newOutputStream();
			try {
				stream.write(contents.getBytes());
			} catch (IOException t) {
				MessageDialog.openError(view.getSite().getShell(), Messages.ConsoleWriteError, t.getMessage());
			} finally {
				try {
					stream.flush();
					stream.close();
				} catch (IOException t) {
				}
			}
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

		status = (JobStatusData) ((IStructuredSelection) selection).getFirstElement();
		if (getReady()) {
			action.setEnabled(true);
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
		view = (IViewPart) targetPart;
	}

	/**
	 * @return correct file contents
	 */
	protected String getContents() {
		if (error) {
			return status.getStatus().getJobError();
		}
		return status.getStatus().getJobOutput();
	}

	/**
	 * @return correct file path
	 */
	protected String getPath() {
		if (error) {
			return status.getErrorPath();
		}
		return status.getOutputPath();
	}

	/**
	 * @return correct file status
	 */
	protected boolean getReady() {
		if (error) {
			return status.getErrReady();
		}
		return status.getOutReady();
	}
}
