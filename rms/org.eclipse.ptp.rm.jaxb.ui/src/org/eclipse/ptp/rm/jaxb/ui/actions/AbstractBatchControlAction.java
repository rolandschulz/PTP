package org.eclipse.ptp.rm.jaxb.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;

/**
 * Base class for actions on the job status object.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractBatchControlAction extends AbstractStatusAction {

	protected String operation;

	/*
	 * Restarts the control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			view.callDoControl(status, true, operation);
		} catch (CoreException t) {
			WidgetActionUtils.errorMessage(view.getSite().getShell(), t, Messages.OperationFailed + operation,
					Messages.DoControlError, false);
		}
	}

	/*
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org.
	 * eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action, PersistentCommandJobStatus status) {
		if (status.getStatus().isInteractive()) {
			action.setEnabled(false);
		} else {
			validateState(action, status);
		}
	}

	/**
	 * Enables the action on basis of state semantics.
	 * 
	 * @param action
	 * @param status
	 */
	protected abstract void validateState(IAction action, PersistentCommandJobStatus status);
}
