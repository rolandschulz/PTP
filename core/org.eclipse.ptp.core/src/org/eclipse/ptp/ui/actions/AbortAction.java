package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public class AbortAction extends ParallelAction {
	private IWorkbenchWindow window;
	private static AbortAction instance = null;

	public AbortAction(ViewPart viewPart) {
	    super(viewPart);
	}
	
	public AbortAction(ViewPart viewPart, boolean isEnable) {
	    super(viewPart, isEnable);	    
	}

	public void init(boolean isEnable) {
	    /*
	    this.setText(UIMessage.getResourceString("AbortAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("AbortAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_ABORT_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_ABORT_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_ABORT_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().getCurrentState() == LaunchManager.STATE_RUN);
	    */
	}
	
	public void run() {
	    System.out.println("abort action - run");
	    try {
	        getLaunchManager().mpiabort();
	    } catch (CoreException e) {
	        System.out.println("abort action error: " + e.getMessage());
	    }
	}
}