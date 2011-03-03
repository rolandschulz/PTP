/**
 * 
 */
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.views.JobsListView;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Richard Maciel
 * 
 */
public class TerminateJobFromListAction extends Action {

	private JobsListView view;

	/**
	 * 
	 */
	public TerminateJobFromListAction(JobsListView view) {
		super(Messages.TerminateJobFromListAction_0, IAction.AS_PUSH_BUTTON);
		setToolTipText(Messages.TerminateJobFromListAction_1);
		setEnabled(false);
		setId(Messages.TerminateJobFromListAction_0);

		this.view = view;

		setImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_NORMAL);
		setDisabledImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_DISABLE);

		view.getViewer().addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Sanity check over viewer data
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();

					if (sel.isEmpty())
						setEnabled(false);
					else {
						Object[] selJobs = sel.toArray();

						// Only enables if at least one is running
						boolean running = false;
						for (int i = 0; i < selJobs.length; i++) {
							IPJob job = (IPJob) selJobs[i];

							if (job.getState() != JobAttributes.State.COMPLETED)
								running = true;
						}

						if (running)
							setEnabled(true);
						else
							setEnabled(false);

					}

				}
			}
		});
	}

	public void updateTerminateJobState() {
		// Sanity check over viewer data
		if (view.getViewer().getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) view.getViewer().getSelection();

			if (sel.isEmpty())
				setEnabled(false);
			else {
				Object[] selJobs = sel.toArray();

				// Only enables if at least one is running
				boolean running = false;
				for (int i = 0; i < selJobs.length; i++) {
					IPJob job = (IPJob) selJobs[i];

					if (job.getState() != JobAttributes.State.COMPLETED)
						running = true;
				}

				if (running)
					setEnabled(true);
				else
					setEnabled(false);
			}
		}

	}

	/**
	 * Get Shell
	 * 
	 * @return
	 */
	public Shell getShell() {
		return view.getViewSite().getShell();
	}

	@Override
	public void run() {
		TableViewer viewer = view.getViewer();

		// Sanity check over viewer data
		if (viewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();

			// Must select at least one item
			if (sel.size() == 0)
				return;

			Object[] selJobs = sel.toArray();

			// Iterate over all selected items, killing the respective jobs
			for (int i = 0; i < selJobs.length; i++) {
				try {
					IPJob job = (IPJob) selJobs[i];

					IResourceManagerControl rm = job.getResourceManager();
					if (job.getState() != JobAttributes.State.COMPLETED) {
						rm.control(job.getID(), IResourceManagerControl.TERMINATE_OPERATION, null);
					}
					// TODO: Look for job change event to wait for jobs to be
					// finished.
					viewer.update(selJobs[i], null);

				} catch (CoreException e) {
					ErrorDialog.openError(getShell(), Messages.TerminateJobFromListAction_2, Messages.TerminateJobFromListAction_3,
							e.getStatus());

				}
			}

		}
	}
}
