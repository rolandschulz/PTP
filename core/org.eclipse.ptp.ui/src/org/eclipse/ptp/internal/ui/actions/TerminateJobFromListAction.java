/**
 * 
 */
package org.eclipse.ptp.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.views.JobsListView;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Richard Maciel
 *
 */
public class TerminateJobFromListAction extends Action {

	JobsListView view;

	/**
	 * 
	 */
	public TerminateJobFromListAction(JobsListView view) {
		super(UIMessage.getResourceString("TerminateJobFromListAction.button.text"), IAction.AS_PUSH_BUTTON);
		setToolTipText(UIMessage.getResourceString("TerminateJobFromListAction.button.tooltip")); //"Terminate all selected jobs");
		setEnabled(false);
		setId(UIMessage.getResourceString("TerminateJobFromListAction.button.text"));

		this.view = view;

		setImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_NORMAL);
		setDisabledImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_DISABLE);
		
		view.getViewer().addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Sanity check over viewer data
				if(event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					
					if(sel.isEmpty())
						setEnabled(false);
					else {
						Object [] selJobs = sel.toArray();

						// Only enables if at least one is running
						boolean running = false;
						for(int i=0; i < selJobs.length; i++) {
								IPJob job = (IPJob)selJobs[i];
								
								if(!job.isTerminated())
									running = true;
						}
						
						if(running)
							setEnabled(true);
						else
							setEnabled(false);
						
					}
						
				}
			}
		});
		
/*		view.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Sanity check over viewer data
				if(event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					
					if(sel.isEmpty())
						setEnabled(false);
					else {
						Object [] selJobs = sel.toArray();

						// Only enables if at least one is running
						boolean running = false;
						for(int i=0; i < selJobs.length; i++) {
								IPJob job = (IPJob)selJobs[i];
								
								if(!job.isTerminated())
									running = true;
						}
						
						if(running)
							setEnabled(true);
						else
							setEnabled(false);
						
					}
						
				}
			}
		});
*/	}

	public void updateTerminateJobState() {
		// Sanity check over viewer data
		if(view.getViewer().getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)view.getViewer().getSelection();
			
			if(sel.isEmpty())
				setEnabled(false);
			else {
				Object [] selJobs = sel.toArray();

				// Only enables if at least one is running
				boolean running = false;
				for(int i=0; i < selJobs.length; i++) {
						IPJob job = (IPJob)selJobs[i];
						
						if(!job.isTerminated())
							running = true;
				}
				
				if(running)
					setEnabled(true);
				else
					setEnabled(false);
			}
		}
		
	}		
	
	/** Get Shell
	 * @return
	 */
	public Shell getShell() {
		return view.getViewSite().getShell();
	}

	public void run() {
		TableViewer viewer = view.getViewer();

		// Sanity check over viewer data
		if(viewer.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();

			IModelManager mm = PTPCorePlugin.getDefault().getModelManager();

			// Must select at least one item
			if(sel.size() == 0)
				return;

			Object [] selJobs = sel.toArray();

			// Iterate over all selected items, killing the respective jobs
			for(int i=0; i < selJobs.length; i++) {
				try {
					IPJob job = (IPJob)selJobs[i];
					
					IResourceManager rm = job.getQueue().getResourceManager();
					if(!job.isTerminated())
						rm.terminateJob(job);
					// TODO: Look for job change event to wait for jobs to be finished.
					viewer.update(selJobs[i], null);
					
				} catch (CoreException e) {
					//ErrorDialog.openError(getShell(), "Terminate Job Error", "Cannot terminate the job.", e.getStatus());
					ErrorDialog.openError(getShell(), UIMessage.getResourceString("TerminateJobFromListAction.msgbox.title"),
							UIMessage.getResourceString("TerminateJobFromListAction.msgbox.message"),
							e.getStatus());
					
				}
			}


		}
	}


	/*//IManager manager = view.getUIManager();
		//if (manager instanceof AbstractUIManager) {
			boolean terminate = MessageDialog.openConfirm(getShell(),
					UIMessage.getResourceString("TerminateJobAction.Title"), //$NON-NLS-1$
					UIMessage.getResourceString("TerminateJobAction.Question") //$NON-NLS-1$
					//+ ((JobManager)manager).getJob().getName()
					+ UIMessage.getResourceString("TerminateJobAction.Confirm")); //$NON-NLS-1$
			if (terminate) {
				try {
					((JobManager)manager).terminateJob();
				} catch (CoreException e) {
					ErrorDialog.openError(getShell(), "Terminate Job Error", "Cannot terminate the job.", e.getStatus());
				}
			}*/
	//}
}
