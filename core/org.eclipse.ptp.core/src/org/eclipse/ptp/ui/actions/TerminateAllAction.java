package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public class TerminateAllAction extends ParallelAction {
	private IWorkbenchWindow window;
	private static ExitAction instance = null;

	public TerminateAllAction(ViewPart viewPart) {
	    super(viewPart);
	}
	
	public TerminateAllAction(ViewPart viewPart, boolean isEnable) {
	    super(viewPart, isEnable);	    
	}

	public void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("TerminationAllAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("TerminationAllAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_TERMINATE_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_TERMINATE_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_TERMINATE_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().getCurrentState() == ILaunchManager.STATE_RUN);
	}
	
	public void run() {
	    //System.out.println("Stop all processes - run");
		try {
	        getLaunchManager().mpiabort();
		} catch (CoreException e) {
		    System.out.println("Error in terminate all processes: " + e.getMessage());
		}
	}
}
