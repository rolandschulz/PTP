package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.views.AbstractParallelView;
import org.eclipse.ui.part.ViewPart;

/**
 * @author ndebard
 */
public class ShowMyAllocatedNodesAction extends ParallelAction {

	public ShowMyAllocatedNodesAction(ViewPart view) {
		super(view, Action.AS_RADIO_BUTTON);
	}

	protected void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("ShowMyAllocatedNodesAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ShowMyAllocatedNodesAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWMYALLOCNODES_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().isMPIRuning());
	}

	public void run() {
		((AbstractParallelView)getViewPart()).showMyAllocatedNodes();
	}

}
