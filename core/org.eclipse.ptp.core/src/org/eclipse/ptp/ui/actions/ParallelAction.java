package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public abstract class ParallelAction extends Action {
    protected /*static*/ ViewPart viewPart = null;
        
    public ParallelAction(ViewPart view) {
        this(view, false);
    }
    
    public ParallelAction(ViewPart view, boolean isEnable) {
        viewPart = view;
        init(isEnable);
    }
    
	public ParallelAction(ViewPart view, int style) {
		super(null, style);
		viewPart = view;
		init(false);
	}

	public /*static*/ ViewPart getViewPart() {
        return viewPart;
    }
    
    public Shell getShell() {
        return viewPart.getViewSite().getShell();
    }
    
    protected ILaunchManager getLaunchManager() {
        return ParallelPlugin.getDefault().getLaunchManager();
    }
    
    protected abstract void init(boolean isEnable);    
    public abstract void run();    
}
