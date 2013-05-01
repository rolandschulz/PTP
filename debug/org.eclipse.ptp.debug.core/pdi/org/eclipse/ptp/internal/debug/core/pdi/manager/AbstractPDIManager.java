package org.eclipse.ptp.internal.debug.core.pdi.manager;


import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManager;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;


/**
 * @author clement
 *
 */
public abstract class AbstractPDIManager extends SessionObject implements IPDIManager {
	boolean autoUpdate;

	public AbstractPDIManager(IPDISession session, boolean update) {
		super(session, null);
		autoUpdate = update;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoUpdate = update;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}
	
	/**
	 * @param tasks
	 * @throws PDIException
	 */
	public abstract void update(TaskSet tasks) throws PDIException;
	
	/**
	 * 
	 */
	public abstract void shutdown();
}
