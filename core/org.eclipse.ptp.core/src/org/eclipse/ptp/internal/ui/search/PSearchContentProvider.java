package org.eclipse.ptp.internal.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.launch.core.ParallelLaunchAdapter;

/**
 * @author Clement
 *
 */
public abstract class PSearchContentProvider implements IStructuredContentProvider {
	protected PSearchResult result;
	protected final Object[] EMPTY_ARR= new Object[0];

    private ParallelLaunchAdapter launchAdapter = new ParallelLaunchAdapter() {
        public void run() {
            result.removeAll();
            refresh();
        }
        public void abort() {
            refresh();
        }
        public void exit() {
            result.removeAll();
            refresh();
        }
    	public void start() {
    	    result.removeAll();
            refresh();
    	}
    	public void stopped() {
            refresh();
    	}
        
        public void execStatusChangeEvent(Object object) {
            refresh();
        }
        public void sysStatusChangeEvent(Object object) {
            refresh();
        }
        public void processOutputEvent(Object object) {
            refresh();
        }
        public void errorEvent(Object object) {
            refresh();
        }
        public void updatedStatusEvent() {
            refresh();
        }
    };
    
    public PSearchContentProvider() {
        ParallelPlugin.getDefault().getLaunchManager().addParallelLaunchListener(launchAdapter);
    }

	public void dispose() {
        ParallelPlugin.getDefault().getLaunchManager().removeParallelLaunchListener(launchAdapter);
	}
	
	public Object[] getElements(Object inputElement) {
		return null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		initialize((PSearchResult) newInput);
	}

	protected void initialize(PSearchResult result) {
		this.result= result;
	}

	public abstract void elementsChanged(Object[] updatedElements);
	public abstract void clear();
	public abstract void refresh();
}
