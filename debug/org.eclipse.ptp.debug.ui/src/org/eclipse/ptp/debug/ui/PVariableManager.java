package org.eclipse.ptp.debug.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
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
	private Map<String, List<PVariableInfo>> jobVariableMap = new HashMap<String,List<PVariableInfo>>();
	private UpdateVariableJob upVariableJob = new UpdateVariableJob();
	
	public void shutdown() {
		jobVariableMap.clear();
		upVariableJob.cancelAll();
	}
	public PVariableInfo[] getPVariableInfo() {
		List<PVariableInfo> aList = new ArrayList<PVariableInfo>();
		for (Iterator<List<PVariableInfo>> i=jobVariableMap.values().iterator(); i.hasNext();) {
			aList.addAll(i.next());
		}
		return aList.toArray(new PVariableInfo[0]);
	}
	public PVariableInfo[] getPVariableInfo(IPJob job) {
		List<PVariableInfo> infoList = jobVariableMap.get(job.getID());
		if (infoList == null)
			return new PVariableInfo[0];

		return infoList.toArray(new PVariableInfo[0]);
	}
	public boolean isPVariableEnable(IPJob job, String varname) {
		List<PVariableInfo> infoList = jobVariableMap.get(job.getID());
		if (infoList != null) {
			for (PVariableInfo info : infoList.toArray(new PVariableInfo[0])) {
				if (info.getName().equals(varname))
					return info.isEnabled();
			}
		}
		return false;
	}
	public void updateVariableStatus(PVariableInfo info, boolean enabled) throws CoreException {
		info.setEnabled(enabled);
		getSession(info.getJob()).getPDISession().getExpressionManager().updateStatusMultiExpressions(info.getName(), enabled);
	}
	public void updateVariableStatus(IPJob job, String varname, boolean enabled) throws CoreException {
		PVariableInfo info = findVariableInfo(job, varname);
		if (info == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(Messages.PVariableManager_0, varname), null));

		updateVariableStatus(info, enabled);
	}
	public void addVariable(IPJob job, String varname, boolean enabled) throws CoreException {
		if (findVariableInfo(job, varname) != null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(Messages.PVariableManager_1, varname), null));
		
		List<PVariableInfo> infoList = jobVariableMap.get(job.getID());
		if (infoList == null) {
			infoList = new ArrayList<PVariableInfo>();
			jobVariableMap.put(job.getID(), infoList);
		}
		IPSession session = getSession(job);
		session.getPDISession().getExpressionManager().createMutliExpressions(session.getTasks(), varname, enabled);
		infoList.add(new PVariableInfo(job, varname, enabled));
	}
	public void removeVariable(IPJob job) {
		removeVariable(job.getID());
	}
	public void removeVariable(String job_id) {
		jobVariableMap.remove(job_id);
	}
	public void removeVariable(IPJob job, String varname) throws CoreException {
		PVariableInfo info = findVariableInfo(job, varname);
		if (info == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, NLS.bind(Messages.PVariableManager_0, varname), null));

		jobVariableMap.get(job.getID()).remove(info);
		getSession(job).getPDISession().getExpressionManager().removeMutliExpressions(varname);
	}
	public void updateVariable(IPJob job, String varname, String newvarname, boolean enabled) throws CoreException {
		if (newvarname != null) {
			removeVariable(job, varname);
			addVariable(job, newvarname, enabled);
		}
		else {
			updateVariableStatus(job, varname, enabled);
		}
	}
	public void updateValues(IPJob job) {
		try {
			updateValues(job, getSession(job).getTasks());
		}
		catch (CoreException ce) {
			ce.printStackTrace();
		}
	}
	public void updateValues(final IPJob job, final BitList tasks) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				  try {
					  IPDISession session = getSession(job).getPDISession();
					  BitList targetTasks = session.getTaskManager().getSuspendedTasks(tasks);
					  if (targetTasks.isEmpty())
						  monitor.done();
					  else
						  session.getExpressionManager().updateMultiExpressions(targetTasks, monitor);
				  }
				  catch (CoreException ce) {
					  throw new InterruptedException(ce.getMessage());
				  }
				  catch (PDIException e) {
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
	public String getValue(final IPJob job, final int task, final IToolTipProvider provider) {
		try {
			IPSession session = getSession(job);
			IPDIExpression[] expressions = session.getPDISession().getExpressionManager().getMultiExpressions(task);
			if (expressions == null || expressions.length == 0)
				return ""; //$NON-NLS-1$

			IPProcess p = job.getProcessByIndex(task);
			if (p == null || p.getState() != ProcessAttributes.State.SUSPENDED)
				return ""; //$NON-NLS-1$
			
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
									  IPSession session = getSession(job);
									  session.getPDISession().getExpressionManager().updateMultiExpressions(session.getTasks(task), monitor);
								  }
								  catch (CoreException ce) {
									  throw new InterruptedException(ce.getMessage());
								  }
								  catch (PDIException e) {
									  //throw new InterruptedException(e.getMessage());
								  }
								  if (provider != null)
									  provider.update(null, getValue(job, task, null));
							  }
						};
						queueRunnable(runnable);
						display.append(Messages.PVariableManager_2);
					}
					else {
						display.append(aif.getValue().getValueString());
					}
				}
				catch (PDIException e) {
					display.append(e.getMessage());
				}
				catch (AIFException ae) {
					display.append(ae.getMessage());
				}
				display.append("<br>"); //$NON-NLS-1$
			}
			return display.toString();
		}
		catch (CoreException e) {
			return e.getMessage();
		}
	}
		
	public void resetValues(final IPJob job) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				  try {
					  IPSession session = getSession(job);
					  session.getPDISession().getExpressionManager().cleanMultiExpressions(session.getTasks(), monitor);
				  }
				  catch (CoreException ce) {
					  throw new InterruptedException(ce.getMessage());
				  }
				  catch (PDIException e) {
					  throw new InterruptedException(e.getMessage());
				  }
			  }
		};
		queueRunnable(runnable);
	}
	public void resetValue(final IPJob job, final BitList tasks) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				  try {
					  IPSession session = getSession(job);
					  session.getPDISession().getExpressionManager().cleanMultiExpressions(tasks, monitor);
				  }
				  catch (CoreException ce) {
					  throw new InterruptedException(ce.getMessage());
				  }
				  catch (PDIException e) {
					  throw new InterruptedException(e.getMessage());
				  }
			  }
		};
		queueRunnable(runnable);
	}
	
	private PVariableInfo findVariableInfo(IPJob job, String varname) {
		List<PVariableInfo> infoList = jobVariableMap.get(job.getID());
		if (infoList != null) {
			for (PVariableInfo info : infoList.toArray(new PVariableInfo[0])) {
				if (info.getName().equals(varname)) {
					return info;
				}
			}
		}
		return null;
	}
	private IPSession getSession(IPJob job) throws CoreException {
		IPSession session = PTPDebugCorePlugin.getDebugModel().getSession(job);
		if (session == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, Messages.PVariableManager_3, null));
		
		return session;
	}
	public class PVariableInfo {
		private IPJob job;
		private String name;
		private boolean enabled;
		
		public PVariableInfo(IPJob job, String name, boolean enabled) {
			this.job = job;
			this.name = name;
			this.enabled = enabled;
		}
		public IPJob getJob() {
			return job;
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
		private Vector<IRunnableWithProgress> fRunnables;
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
		public boolean shouldRun() {
			return !fRunnables.isEmpty();
		}
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
				}
				catch (Exception e) {
					if (failed == null)
						failed = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, Messages.PVariableManager_5, null);
					failed.add(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, Messages.PVariableManager_5, e));
				}
				monitor.worked(1);
			}
			monitor.done();
			if (failed == null)
				return Status.OK_STATUS;
			
			return failed;
		}
	}
}
