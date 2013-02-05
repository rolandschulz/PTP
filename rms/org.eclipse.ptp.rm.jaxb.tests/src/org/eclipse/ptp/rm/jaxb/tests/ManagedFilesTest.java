/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.tests;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.control.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFileType;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFilesType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.ScriptType;

public class ManagedFilesTest extends TestCase implements ILaunchController {

	private static final String xml = JAXBControlConstants.DATA + "pbs-test-local.xml"; //$NON-NLS-1$
	private static ControlType controlData;
	private static Map<String, AttributeType> env;
	private static Map<String, String> live;
	private static boolean verbose = false;
	private RMVariableMap rmVarMap;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#control(java.lang.String , java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceManagerData getConfigurationData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConnectionName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl#getEnvironment()
	 */
	@Override
	public IVariableMap getEnvironment() {
		return rmVarMap;
	}

	@Override
	public ICommandJob getInteractiveJob() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IJobStatus getJobStatus(String jobId, boolean force, IProgressMonitor monitor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IJobStatus getJobStatus(String jobId, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getRemoteServicesId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void runCommand(String command, String resetValue, ILaunchConfiguration configuration) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInteractiveJob(ICommandJob interactiveJob) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUp() {
		try {
			JAXBTestsPlugin.validate(xml);
			ResourceManagerData rmdata = JAXBInitializationUtils.initializeRMData(JAXBTestsPlugin.getURL(xml));
			controlData = rmdata.getControlData();
			rmVarMap = new RMVariableMap();
			JAXBInitializationUtils.initializeMap(rmdata, rmVarMap);
			env = rmVarMap.getAttributes();
			System.out.println(env);
			live = new HashMap<String, String>();
			live.put("FOO_VAR_1", "FOO_VALUE_1"); //$NON-NLS-1$ //$NON-NLS-2$
			live.put("FOO_VAR_2", "FOO_VALUE_2"); //$NON-NLS-1$ //$NON-NLS-2$
			live.put("FOO_VAR_3", "FOO_VALUE_3"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		setTestValues();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobControl#submitJob(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String submitJob(ILaunchConfiguration launchConfig, String mode, IProgressMonitor monitor) throws CoreException {
		return null;
	}

	@Override
	public void tearDown() {
		controlData = null;
	}

	public void testManagedFiles() {
		composeScript();
		if (verbose) {
			AttributeType contents = env.get(JAXBControlConstants.SCRIPT);
			if (contents != null) {
				System.out.println(contents.getValue());
			}
		}
		ManagedFilesType files = null;
		if (controlData.getManagedFiles().size() > 0) {
			files = controlData.getManagedFiles().get(0);
		}
		files = maybeAddManagedFileForScript(files);
		assertNotNull(files);
		try {
			ManagedFilesJob job = new ManagedFilesJob(null, files, this);
			job.setOperation(ManagedFilesJob.Operation.COPY);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException t) {
				t.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
		try {
			ManagedFilesJob job = new ManagedFilesJob(null, files, this);
			job.setOperation(ManagedFilesJob.Operation.DELETE);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException t) {
				t.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	private void composeScript() {
		ScriptType script = controlData.getScript();
		assertNotNull(script);
		ScriptHandler job = new ScriptHandler(null, script, rmVarMap, live, false);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}

		AttributeType contents = env.get(JAXBControlConstants.SCRIPT);
		assertNotNull(contents);
	}

	private ManagedFilesType maybeAddManagedFileForScript(ManagedFilesType files) {
		AttributeType scriptVar = rmVarMap.get(JAXBControlConstants.SCRIPT);
		AttributeType scriptPathVar = rmVarMap.get(JAXBControlConstants.SCRIPT_PATH);
		if (scriptVar != null || scriptPathVar != null) {
			if (files == null) {
				files = new ManagedFilesType();
				files.setFileStagingLocation(JAXBControlConstants.ECLIPSESETTINGS);
			}
			List<ManagedFileType> fileList = files.getFile();
			ManagedFileType scriptFile = null;
			if (!fileList.isEmpty()) {
				for (ManagedFileType f : fileList) {
					if (f.getName().equals(JAXBControlConstants.SCRIPT_FILE)) {
						scriptFile = f;
						break;
					}
				}
			}
			if (scriptFile == null) {
				scriptFile = new ManagedFileType();
				scriptFile.setName(JAXBControlConstants.SCRIPT_FILE);
				fileList.add(scriptFile);
			}
			scriptFile.setResolveContents(false);
			scriptFile.setUniqueIdPrefix(true);
			if (scriptPathVar != null) {
				scriptFile.setPath(String.valueOf(scriptPathVar.getValue()));
				scriptFile.setDeleteSourceAfterUse(false);
			} else {
				scriptFile.setContents(JAXBControlConstants.OPENVRM + JAXBControlConstants.SCRIPT + JAXBControlConstants.PD
						+ JAXBControlConstants.VALUE + JAXBControlConstants.CLOSV);
				scriptFile.setDeleteSourceAfterUse(true);
			}
		}
		return files;
	}

	private void putValue(String name, String value) {
		AttributeType p = new AttributeType();
		p.setName(name);
		p.setValue(value);
		env.put(name, p);
	}

	private void setTestValues() {
		for (String key : env.keySet()) {
			AttributeType target = env.get(key);
			String value = key + "_TEST_VALUE"; //$NON-NLS-1$
			target.setValue(value);
		}
		putValue(JAXBControlConstants.CONTROL_USER_VAR, "fooUser"); //$NON-NLS-1$
		putValue(JAXBControlConstants.CONTROL_ADDRESS_VAR, "abe.ncsa.uiuc.edu"); //$NON-NLS-1$
		putValue(JAXBControlConstants.DIRECTORY, "/u/ncsa/arossi/test"); //$NON-NLS-1$ 
		putValue(JAXBControlConstants.MPI_CMD, "mpiexec"); //$NON-NLS-1$ 
		putValue(JAXBControlConstants.MPI_ARGS, "-np 8"); //$NON-NLS-1$ 
		putValue(JAXBControlConstants.EXEC_PATH, "/u/ncsa/arossi/test/foo"); //$NON-NLS-1$ 
		if (verbose) {
			RMDataTest.print(rmVarMap);
		}
	}

	@Override
	public String getControlId() {
		return null;
	}

	@Override
	public ResourceManagerData getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setConnectionName(String connName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRemoteServicesId(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRMConfigurationURL(URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws CoreException {
		// TODO Auto-generated method stub

	}
}
