package org.eclipse.ptp.debug.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExpression;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.ui.views.IToolTipProvider;

/**
 * @author clement
 * 
 */
public class PVariableManager {
	private final Map<String, List<PVariableInfo>> jobVariableMap = new HashMap<String, List<PVariableInfo>>();
	private final UpdateVariableJob upVariableJob = new UpdateVariableJob();

	public void shutdown() {
		jobVariableMap.clear();
		upVariableJob.cancelAll();
	}

	public PVariableInfo[] getPVariableInfo() {
		List<PVariableInfo> aList = new ArrayList<PVariableInfo>();
		for (List<PVariableInfo> list : jobVariableMap.values()) {
			aList.addAll(list);
		}
		return aList.toArray(new PVariableInfo[0]);
	}

	/**
	 * @since 5.0
	 */
	public PVariableInfo[] getPVariableInfo(String jobId) {
		List<PVariableInfo> infoList = jobVariableMap.get(jobId);
		if (infoList == null) {
			return new PVariableInfo[0];
		}

		return infoList.toArray(new PVariableInfo[0]);
	}

	/**
	 * @since 5.0
	 */
	public boolean isPVariableEnable(String jobId, String varname) {
		List<PVariableInfo> infoList = jobVariableMap.get(jobId);
		if (infoList != null) {
			for (PVariableInfo info : infoList.toArray(new PVariableInfo[0])) {
				if (info.getName().equals(varname)) {
					return info.isEnabled();
				}
			}
		}
		return false;
	}

	public void updateVariableStatus(PVariableInfo info, boolean enabled) throws CoreException {
		info.setEnabled(enabled);
		getSession(info.getJobId()).getPDISession().getExpressionManager().updateStatusMultiExpressions(info.getName(), enabled);
	}

	/**
	 * @since 5.0
	 */
	public void updateVariableStatus(String jobId, String varname, boolean enabled) throws CoreException {
		PVariableInfo info = findVariableInfo(jobId, varname);
		if (info == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(
					Messages.PVariableManager_0, varname), null));
		}

		updateVariableStatus(info, enabled);
	}

	/**
	 * @since 5.0
	 */
	public void addVariable(String jobId, String varname, boolean enabled) throws CoreException {
		if (findVariableInfo(jobId, varname) != null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(
					Messages.PVariableManager_1, varname), null));
		}

		List<PVariableInfo> infoList = jobVariableMap.get(jobId);
		if (infoList == null) {
			infoList = new ArrayList<PVariableInfo>();
			jobVariableMap.put(jobId, infoList);
		}
		IPSession session = getSession(jobId);
		session.getPDISession().getExpressionManager().createMutliExpressions(session.getTasks(), varname, enabled);
		infoList.add(new PVariableInfo(jobId, varname, enabled));
	}

	public void removeVariable(String job_id) {
		jobVariableMap.remove(job_id);
	}

	/**
	 * @since 5.0
	 */
	public void removeVariable(String jobId, String varname) throws CoreException {
		PVariableInfo info = findVariableInfo(jobId, varname);
		if (info == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(
					Messages.PVariableManager_0, varname), null));
		}

		jobVariableMap.get(jobId).remove(info);
		getSession(jobId).getPDISession().getExpressionManager().removeMutliExpressions(varname);
	}

	/**
	 * @since 5.0
	 */
	public void updateVariable(String jobId, String varname, String newvarname, boolean enabled) throws CoreException {
		if (newvarname != null) {
			removeVariable(jobId, varname);
			addVariable(jobId, newvarname, enabled);
		} else {
			updateVariableStatus(jobId, varname, enabled);
		}
	}

	/**
	 * @since 5.0
	 */
	public void updateValues(String jobId) {
		try {
			updateValues(jobId, getSession(jobId).getTasks());
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	/**
	 * @since 5.0
	 */
	public void updateValues(final String jobId, final TaskSet tasks) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IPDISession session = getSession(jobId).getPDISession();
					TaskSet targetTasks = session.getTaskManager().getSuspendedTasks(tasks);
					if (targetTasks.isEmpty()) {
						monitor.done();
					} else {
						session.getExpressionManager().updateMultiExpressions(targetTasks, monitor);
					}
				} catch (CoreException ce) {
					throw new InterruptedException(ce.getMessage());
				} catch (PDIException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		};
		try {
			new ProgressMonitorDialog(PTPDebugUIPlugin.getActiveWorkbenchShell()).run(true, true, runnable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * @since 5.0
	 */
	public String getValue(final IPJobStatus job, final int task, final IToolTipProvider provider) {
		try {
			IPSession session = getSession(job.getJobId());
			IPDIExpression[] expressions = session.getPDISession().getExpressionManager().getMultiExpressions(task);
			if (expressions == null || expressions.length == 0) {
				return ""; //$NON-NLS-1$
			}

			final String pState = job.getProcessState(task);
			if (!pState.equals(IPJobStatus.SUSPENDED)) {
				return ""; //$NON-NLS-1$
			}

			StringBuffer display = new StringBuffer();
			for (IPDIExpression expression : expressions) {
				display.append("<i>"); //$NON-NLS-1$
				display.append(expression.getExpressionText());
				display.append("</i>"); //$NON-NLS-1$
				display.append(" = "); //$NON-NLS-1$
				try {
					IAIF aif = expression.getAIF();
					if (aif == null) {
						IRunnableWithProgress runnable = new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								try {
									IPSession session = getSession(job.getJobId());
									session.getPDISession().getExpressionManager()
											.updateMultiExpressions(session.getTasks(task), monitor);
								} catch (CoreException ce) {
									throw new InterruptedException(ce.getMessage());
								} catch (PDIException e) {
									// throw new
									// InterruptedException(e.getMessage());
								}
								if (provider != null) {
									provider.update(task, getValue(job, task, null));
								}
							}
						};
						queueRunnable(runnable);
						display.append(Messages.PVariableManager_2);
					} else {
						display.append(aif.getValue().getValueString());
					}
				} catch (PDIException e) {
					display.append(e.getMessage());
				} catch (AIFException ae) {
					display.append(ae.getMessage());
				}
				display.append("<br>"); //$NON-NLS-1$
			}
			return display.toString();
		} catch (CoreException e) {
			return e.getMessage();
		}
	}

	/**
	 * @since 4.0
	 */
	public void resetValues(final String jobId) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IPSession session = getSession(jobId);
					session.getPDISession().getExpressionManager().cleanMultiExpressions(session.getTasks(), monitor);
				} catch (CoreException ce) {
					throw new InterruptedException(ce.getMessage());
				} catch (PDIException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		};
		queueRunnable(runnable);
	}

	/**
	 * @since 4.0
	 */
	public void resetValue(final String jobId, final TaskSet tasks) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IPSession session = getSession(jobId);
					session.getPDISession().getExpressionManager().cleanMultiExpressions(tasks, monitor);
				} catch (CoreException ce) {
					throw new InterruptedException(ce.getMessage());
				} catch (PDIException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		};
		queueRunnable(runnable);
	}

	private PVariableInfo findVariableInfo(String jobId, String varname) {
		List<PVariableInfo> infoList = jobVariableMap.get(jobId);
		if (infoList != null) {
			for (PVariableInfo info : infoList.toArray(new PVariableInfo[0])) {
				if (info.getName().equals(varname)) {
					return info;
				}
			}
		}
		return null;
	}

	private IPSession getSession(String jobId) throws CoreException {
		IPSession session = PTPDebugCorePlugin.getDebugModel().getSession(jobId);
		if (session == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR,
					Messages.PVariableManager_3, null));
		}

		return session;
	}

	public class PVariableInfo {
		private final String jobId;
		private final String name;
		private boolean enabled;

		/**
		 * @since 5.0
		 */
		public PVariableInfo(String jobId, String name, boolean enabled) {
			this.jobId = jobId;
			this.name = name;
			this.enabled = enabled;
		}

		/**
		 * @since 5.0
		 */
		public String getJobId() {
			return jobId;
		}

		public String getName() {
			return name;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	/*************************************************
	 * Notify Job
	 *************************************************/
	public void queueRunnable(IRunnableWithProgress runnable) {
		upVariableJob.addRunnable(runnable);
	}

	private class UpdateVariableJob extends Job {
		private final Vector<IRunnableWithProgress> fRunnables;

		public UpdateVariableJob() {
			super(Messages.PVariableManager_4);
			setSystem(true);
			fRunnables = new Vector<IRunnableWithProgress>(10);
		}

		public void cancelAll() {
			fRunnables.clear();
			super.cancel();
		}

		public void addRunnable(IRunnableWithProgress runnable) {
			synchronized (fRunnables) {
				fRunnables.add(runnable);
			}
			schedule();
		}

		@Override
		public boolean shouldRun() {
			return !fRunnables.isEmpty();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			IRunnableWithProgress[] runnables;
			synchronized (fRunnables) {
				runnables = fRunnables.toArray(new IRunnableWithProgress[0]);
				fRunnables.clear();
			}
			MultiStatus failed = null;
			monitor.beginTask(getName(), runnables.length);
			for (IRunnableWithProgress runnable : runnables) {
				try {
					runnable.run(monitor);
				} catch (Exception e) {
					if (failed == null) {
						failed = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR,
								Messages.PVariableManager_5, null);
					}
					failed.add(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(),
							PTPDebugCorePlugin.INTERNAL_ERROR, Messages.PVariableManager_5, e));
				}
				monitor.worked(1);
			}
			monitor.done();
			if (failed == null) {
				return Status.OK_STATUS;
			}

			return failed;
		}
	}
}
