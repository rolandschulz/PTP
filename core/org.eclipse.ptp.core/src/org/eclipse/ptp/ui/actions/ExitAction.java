package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public class ExitAction extends ParallelAction {
	private IWorkbenchWindow window;
	private static ExitAction instance = null;

	public ExitAction(ViewPart viewPart) {
	    super(viewPart);
	}
	
	public ExitAction(ViewPart viewPart, boolean isEnable) {
	    super(viewPart, isEnable);	    
	}

	public void init(boolean isEnable) {
	    /*
	    this.setText(UIMessage.getResourceString("ExitAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ExitAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_EXIT_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().getCurrentState() == LaunchManager.STATE_RUN);
	    */
	}
	
	public void run() {
	    System.out.println("exit action - run");
	    try {
	        getLaunchManager().mpiexit();
	    } catch (CoreException e) {
	        System.out.println("exit action error: " + e.getMessage());
	    }
	}
}