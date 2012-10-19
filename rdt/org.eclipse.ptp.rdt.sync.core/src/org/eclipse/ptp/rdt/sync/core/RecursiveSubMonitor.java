package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Submonitor capable of recursive task reporting. A new subtask is appended to the parent's subtask
 *
 */
public class RecursiveSubMonitor implements IProgressMonitor {
	private final SubMonitor subMonitor;
	private String subTaskName = ""; //$NON-NLS-1$
	private RecursiveSubMonitor parent = null;
	
	private RecursiveSubMonitor(SubMonitor subMon) {
		subMonitor = subMon;
	}
	
	private RecursiveSubMonitor(SubMonitor subMon, RecursiveSubMonitor p) {
		subMonitor = subMon;
		parent = p;
	}
	
	/**
	 * Get the name of the subtask - trivial but essential for recursive task reporting to work
	 * @return name of subtask
	 */
	public String getSubTaskName() {
		return subTaskName;
	}
	
	/**
	 * Return parent monitor or null if parent does not exist or is not a RecursiveSubMonitor
	 * @return parent
	 */
	public RecursiveSubMonitor getParentMonitor() {
		return parent;
	}

	/**
	 * Intercept setting of subtask name to store it and prepend parent's subtask
	 * @param name
	 */
	public void subTask(String name) {
		if (parent != null) {
			subTaskName = parent.getSubTaskName() + " : " + name; //$NON-NLS-1$
		} else {
			subTaskName = name;
		}
		subMonitor.subTask(subTaskName);
	}
	
	/**
	 * Intercept creating of child monitors to store the parent in the new monitor
	 * @param totalWork
	 * @return new monitor
	 */
	public RecursiveSubMonitor newChild(int totalWork) {
		return new RecursiveSubMonitor(subMonitor.newChild(totalWork), this);
	}

	/**
	 * Intercept creating of child monitors to store the parent in the new monitor
	 * @param totalWork
	 * @param suppressFlags
	 * @return new monitor
	 */
	public RecursiveSubMonitor newChild(int totalWork, int suppressFlags) {
		return new RecursiveSubMonitor(subMonitor.newChild(totalWork, suppressFlags), this);
	}

	/**
	 * Convert the underlying submonitor as before but wrap it in a recursive submonitor. Preserve the parent if necessary.
	 * @param monitor
	 * @return
	 */
	public static RecursiveSubMonitor convert(IProgressMonitor monitor) {
		// Preserve parent if monitor is recursive
		RecursiveSubMonitor parent = null;
		if (monitor instanceof RecursiveSubMonitor) {
			parent = ((RecursiveSubMonitor) monitor).getParentMonitor();
		}
		return new RecursiveSubMonitor(SubMonitor.convert(monitor), parent);
	}

	/**
	 * Convert the underlying submonitor as before but wrap it in a recursive submonitor. Preserve the parent if necessary.
	 * @param monitor
	 * @return
	 */
	public static RecursiveSubMonitor convert(IProgressMonitor monitor, int work) {
		// Preserve parent if monitor is recursive
		RecursiveSubMonitor parent = null;
		if (monitor instanceof RecursiveSubMonitor) {
			parent = ((RecursiveSubMonitor) monitor).getParentMonitor();
		}
		return new RecursiveSubMonitor(SubMonitor.convert(monitor, work), parent);
	}

	/**
	 * Convert the underlying submonitor as before but wrap it in a recursive submonitor. Preserve the parent if necessary.
	 * @param monitor
	 * @return
	 */
	public static RecursiveSubMonitor convert(IProgressMonitor monitor, String taskName, int work) {
		// Preserve parent if monitor is recursive
		RecursiveSubMonitor parent = null;
		if (monitor instanceof RecursiveSubMonitor) {
			parent = ((RecursiveSubMonitor) monitor).getParentMonitor();
		}
		return new RecursiveSubMonitor(SubMonitor.convert(monitor, taskName, work), parent);
	}

	// Boilerplate forwarding functions - let the wrapped submonitor do most of the work 
	public RecursiveSubMonitor setWorkRemaining(int workRemaining) {
		subMonitor.setWorkRemaining(workRemaining);
		return this;
	}

	public boolean isCanceled() {
		return subMonitor.isCanceled();
	}

	public void setTaskName(String name) {
		subMonitor.setTaskName(name);
	}

	public void beginTask(String name, int totalWork) {
		subMonitor.beginTask(name, totalWork);
	}

	public void done() {
		subMonitor.done();
	}

	public void internalWorked(double work) {
		subMonitor.internalWorked(work);
	}

	public void worked(int work) {
		subMonitor.worked(work);
	}

	public void setCanceled(boolean b) {
		subMonitor.setCanceled(b);
	}

	public void clearBlocked() {
		subMonitor.clearBlocked();

	}

	public void setBlocked(IStatus reason) {
		subMonitor.setBlocked(reason);
	}
}
