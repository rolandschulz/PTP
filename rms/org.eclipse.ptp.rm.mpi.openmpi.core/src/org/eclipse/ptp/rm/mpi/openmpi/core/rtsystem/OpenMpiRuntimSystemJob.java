/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.rm.core.Activator;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.core.rtsystem.DefaultToolRuntimeSystemJob;
import org.eclipse.ptp.rm.core.utils.InputStreamListenerToOutputStream;
import org.eclipse.ptp.rm.core.utils.InputStreamObserver;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiLaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMpiProcessMap.Process;

public class OpenMpiRuntimSystemJob extends DefaultToolRuntimeSystemJob {
	Object lock1 = new Object();

	private InputStreamObserver stderrObserver;
	private InputStreamObserver stdoutObserver;

	/** Information parsed from launch command. */
	OpenMpiProcessMap map;

	/** Mapping of processes created by this job. */
//	private Map<String,String> processMap = new HashMap<String, String>();

	/** Process with rank 0 (zero) that prints all output. */
//	private String rankZeroProcessID;

	/**
	 * Process IDs created by this job. The first process (zero index) is special,
	 * because it is always created.
	 */
	String processIDs[];

	/** Exception raised while parsing mpi map information. */
	IOException parserException = null;

	public OpenMpiRuntimSystemJob(String jobID, String name, AbstractToolRuntimeSystem rtSystem, AttributeManager attrMgr) {
		super(jobID, name, rtSystem, attrMgr);
	}

	@Override
	protected void doExecutionStarted() throws CoreException {
		/*
		 * Create a zero index job.
		 */
		final OpenMpiRuntimeSystem rtSystem = (OpenMpiRuntimeSystem) getRtSystem();
		final IPJob ipJob = PTPCorePlugin.getDefault().getUniverse().getResourceManager(rtSystem.getRmID()).getQueueById(rtSystem.getQueueID()).getJobById(getJobID());
		final String zeroIndexProcessID = rtSystem.createProcess(getJobID(), "Open MPI run", 0);
		processIDs = new String[] { zeroIndexProcessID } ;

		/*
		 * Listener that saves stdout.
		 */
		final PipedOutputStream stdoutOutputStream = new PipedOutputStream();
		final PipedInputStream stdoutInputStream = new PipedInputStream();
		try {
			stdoutInputStream.connect(stdoutOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}
		final InputStreamListenerToOutputStream stdoutPipedStreamListener = new InputStreamListenerToOutputStream(stdoutOutputStream);
		
		Thread stdoutThread = new Thread() {
			@Override
			public void run() {
				BufferedReader stdoutBufferedReader = new BufferedReader(new InputStreamReader(stdoutInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stdoutBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
//							System.out.println(line);
						}
						line = stdoutBufferedReader.readLine();
					}
				} catch (IOException e) {
					PTPCorePlugin.log(e);
				} finally {
					if (stdoutObserver != null) {
						stdoutObserver.removeListener(stdoutPipedStreamListener);
					}
					try {
						stdoutOutputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
					try {
						stdoutInputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
				}
			}
		};

		/*
		 * Listener that saves stderr.
		 */
		final PipedOutputStream stderrOutputStream = new PipedOutputStream();
		final PipedInputStream stderrInputStream = new PipedInputStream();
		try {
			stderrInputStream.connect(stderrOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}
		final InputStreamListenerToOutputStream stderrPipedStreamListener = new InputStreamListenerToOutputStream(stderrOutputStream);
		Thread stderrThread = new Thread() {
			@Override
			public void run() {
				final BufferedReader stderrBufferedReader = new BufferedReader(new InputStreamReader(stderrInputStream));
				IPProcess ipProc = ipJob.getProcessById(zeroIndexProcessID);
				try {
					String line = stderrBufferedReader.readLine();
					while (line != null) {
						synchronized (lock1) {
							ipProc.addAttribute(ProcessAttributes.getStderrAttributeDefinition().create(line));
//							ipProc.addAttribute(ProcessAttributes.getStdoutAttributeDefinition().create(line));
//							System.err.println(line);
						}
						line = stderrBufferedReader.readLine();
					}
				} catch (IOException e) {
					PTPCorePlugin.log(e);
				} finally {
					if (stderrObserver != null) {
						stderrObserver.removeListener(stderrPipedStreamListener);
					}
					try {
						stderrOutputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
					try {
						stderrInputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
				}
			}
		};

		/*
		 * Thread that parses map information.
		 */
		final PipedOutputStream parserOutputStream = new PipedOutputStream();
		final PipedInputStream parserInputStream = new PipedInputStream();
		try {
			parserInputStream.connect(parserOutputStream);
		} catch (IOException e) {
			assert false; // This exception is not possible
		}
		final InputStreamListenerToOutputStream parserPipedStreamListener = new InputStreamListenerToOutputStream(parserOutputStream);
		Thread parserThread = new Thread() {
			@Override
			public void run() {
				OpenMpiResourceManagerConfiguration configuration = (OpenMpiResourceManagerConfiguration) getRtSystem().getRmConfiguration();
				try {
					// Parse stdout or stderr, depending on mpi 1.2 or 1.3
					if (configuration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
						map = OpenMpiProcessMapText12Parser.parse(parserInputStream);
					} else if (configuration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_13)) {
						map = OpenMpiProcessMapXml13Parser.parse(parserInputStream);
					} else {
						assert false;
					}
				} catch (IOException e) {
					/*
					 * If output could not be parsed, the kill the mpi process.
					 */
					parserException = e;
					process.destroy();
				} finally {
					if (stderrObserver != null) {
						stderrObserver.removeListener(parserPipedStreamListener);
					}
					try {
						parserOutputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
					try {
						parserInputStream.close();
					} catch (IOException e) {
						PTPCorePlugin.log(e);
					}
				}
			}
		};

		/*
		 * Create and start listeners.
		 */
		stdoutThread.start();
		stderrThread.start();
		parserThread.start();

		stderrObserver = new InputStreamObserver(process.getErrorStream());
		stdoutObserver = new InputStreamObserver(process.getInputStream());

		stdoutObserver.addListener(stdoutPipedStreamListener);
		stderrObserver.addListener(stderrPipedStreamListener);

		// Parse stdout or stderr, depending on mpi 1.2 or 1.3
		OpenMpiResourceManagerConfiguration configuration = (OpenMpiResourceManagerConfiguration) getRtSystem().getRmConfiguration();
		if (configuration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_12)) {
			stderrObserver.addListener(parserPipedStreamListener);
		} else if (configuration.getVersionId().equals(OpenMpiResourceManagerConfiguration.VERSION_13)) {
			stdoutObserver.addListener(parserPipedStreamListener);
		} else {
			assert false;
		}

		stderrObserver.start();
		stdoutObserver.start();

		try {
//			parserThread.start();
			parserThread.join();
		} catch (InterruptedException e) {
			// Do nothing.
		}

		if (parserException != null) {
			process.destroy();
			throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Failed to parse Open Mpi run command output.", parserException));
		}

		/*
		 * Copy job attributes from map.
		 */
		rtSystem.changeJob(getJobID(), map.getAttributeManager());

		/*
		 * Copy process attributes from map.
		 */
		List<Process> newProcesses = map.getProcesses();
		processIDs = new String[newProcesses.size()];
		for (Process newProcess : newProcesses) {
			String nodename = newProcess.getNode().getName();
			String nodeID = rtSystem.getNodeIDforName(nodename);
			if (nodeID == null) {
				process.destroy();
				throw new CoreException(new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Hostnames from Open MPI output do not match expected hostname.", parserException));
			}

			String processName = newProcess.getName();
			int processIndex = newProcess.getIndex();
			String processID = null;
			if (processIndex == 0) {
				processID = zeroIndexProcessID;
			} else {
				processID = rtSystem.createProcess(getJobID(), processName, processIndex);
			}
			processIDs[processIndex] = processID;

			AttributeManager attrMgr = new AttributeManager();
			attrMgr.addAttribute(ElementAttributes.getNameAttributeDefinition().create(processName));
			attrMgr.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.RUNNING));
			attrMgr.addAttributes(newProcess.getAttributeManager().getAttributes());
			rtSystem.changeProcess(processID, attrMgr);
		}
	}

	@Override
	protected void doWaitExecution() throws CoreException {
		try {
			stderrObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			stdoutObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void doTerminateJob() {
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}
	}

	@Override
	protected void doExecutionFinished() throws CoreException {
		AttributeManager attrMrg = new AttributeManager();
		attrMrg.addAttribute(ProcessAttributes.getStateAttributeDefinition().create(ProcessAttributes.State.EXITED));

		for (String processId : processIDs) {
			rtSystem.changeProcess(processId, attrMrg);
		}
	}

	@Override
	protected void doExecutionCleanUp() {
		if (process != null) {
			process.destroy();
		}
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}
	}

	@Override
	protected IAttribute<?, ?, ?>[] getExtraSubstitutionVariables() throws CoreException {
		List<IAttribute<?, ?, ?>> newAttributes = new ArrayList<IAttribute<?,?,?>>();
		ArrayAttribute<String> environmentAttribute = getAttrMgr().getAttribute(JobAttributes.getEnvironmentAttributeDefinition());

		if (environmentAttribute != null) {
			List<String> environment = environmentAttribute.getValue();
			int p = 0;
			String keys[] = new String[environment.size()];
			for (String var : environment) {
				int i = var.indexOf('=');
				String key = var.substring(0, i);
				keys[p++] = key;
			}
			newAttributes.add(OpenMpiLaunchAttributes.getEnvironmentKeysDefinition().create(keys));
		}

		//${envKeys:-x : -x :::}
		newAttributes.add(OpenMpiLaunchAttributes.getEnvironmentArgsDefinition().create());

		return newAttributes.toArray(new IAttribute<?, ?, ?>[newAttributes.size()]);
	}

	@Override
	protected IAttributeDefinition<?, ?, ?>[] getDefaultSubstitutionAttributes() {
		IAttributeDefinition<?, ?, ?>[] attributesFromSuper = super.getDefaultSubstitutionAttributes();
		IAttributeDefinition<?, ?, ?>[] moreAttributes = new IAttributeDefinition[] {
				OpenMpiLaunchAttributes.getEnvironmentKeysDefinition(), OpenMpiLaunchAttributes.getEnvironmentArgsDefinition()
			};
		IAttributeDefinition<?, ?, ?>[]  allAttributes = new IAttributeDefinition[attributesFromSuper.length+moreAttributes.length];
	   System.arraycopy(attributesFromSuper, 0, allAttributes, 0, attributesFromSuper.length);
	   System.arraycopy(moreAttributes, 0, allAttributes, attributesFromSuper.length, moreAttributes.length);
	   return allAttributes;
	}
}
