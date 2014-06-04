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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;

public class RMLaunchTest extends TestCase {

	private class TestLaunchConfiguration implements ILaunchConfiguration {

		private final Map<String, Object> store = new TreeMap<String, Object>();

		@Override
		public boolean contentsEqual(ILaunchConfiguration configuration) {
			return false;
		}

		@Override
		public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
			return null;
		}

		@Override
		public void delete() throws CoreException {
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return null;
		}

		@Override
		public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
			Boolean value = (Boolean) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		public int getAttribute(String attributeName, int defaultValue) throws CoreException {
			Integer value = (Integer) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public List getAttribute(String attributeName, List defaultValue) throws CoreException {
			List value = (List) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
			Map value = (Map) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Set getAttribute(String attributeName, Set defaultValue) throws CoreException {
			Set value = (Set) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		public String getAttribute(String attributeName, String defaultValue) throws CoreException {
			String value = (String) store.get(attributeName);
			if (value == null) {
				value = defaultValue;
			}
			return value;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Map getAttributes() throws CoreException {
			return store;
		}

		@Override
		public String getCategory() throws CoreException {
			return null;
		}

		@Override
		public IFile getFile() {
			return null;
		}

		@Override
		public IPath getLocation() {
			return null;
		}

		@Override
		public IResource[] getMappedResources() throws CoreException {
			return null;
		}

		@Override
		public String getMemento() throws CoreException {
			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Set getModes() throws CoreException {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException {
			return null;
		}

		@Override
		public ILaunchConfigurationType getType() throws CoreException {
			return null;
		}

		@Override
		public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
			return null;
		}

		@Override
		public boolean hasAttribute(String attributeName) throws CoreException {
			return false;
		}

		@Override
		public boolean isLocal() {
			return false;
		}

		@Override
		public boolean isMigrationCandidate() throws CoreException {
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public boolean isWorkingCopy() {
			return false;
		}

		@Override
		public ILaunch launch(String mode, IProgressMonitor monitor) throws CoreException {
			return null;
		}

		@Override
		public ILaunch launch(String mode, IProgressMonitor monitor, boolean build) throws CoreException {
			return null;
		}

		@Override
		public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
			return null;
		}

		@Override
		public void migrate() throws CoreException {

		}

		@Override
		public boolean supportsMode(String mode) throws CoreException {
			return false;
		}
	}

	private static final String xml = JAXBCoreConstants.SCHEMA + "tabbed-example.xml"; //$NON-NLS-1$
	private ILaunchController rm;
	private ILaunchConfiguration launchConfig;

	@Override
	public void setUp() {
		/*
		 * You will need to copy all the executables in the org.eclipse.ptp.rm.jaxb.tests/data directory to you home; it seems the
		 * JUnit plugin runner does not actually execute in the directory indicated by the Run Configuration.
		 */
	}

	@Override
	public void tearDown() {
	}

	public void testResourceManager() {
		Job j = new Job("testResourceManager") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					emulateConfigureWizard();
					rm.start(monitor);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ignored) {
					}
					emulateLaunchTab();
					String jobId = rm.submitJob(launchConfig, ILaunchManager.RUN_MODE, monitor);
					System.out.println("SUBMITTED: " + jobId); //$NON-NLS-1$
					IJobStatus status = rm.getJobStatus(jobId, null);
					System.out.println("STATUS: " + status.getState()); //$NON-NLS-1$
					if (status != null) {
						status.getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {
							@Override
							public void streamAppended(String text, IStreamMonitor monitor) {
								System.out.println(text);
							}
						});
					}
					rm.stop();
				} catch (Throwable t) {
					t.printStackTrace();
					assertNotNull(t);
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
		try {
			j.join();
		} catch (InterruptedException ignored) {
		}
	}

	/*
	 * We do here what is done through the wizard.
	 */
	private void emulateConfigureWizard() throws Throwable {
		IRemoteServices localServices = RemoteServices.getLocalServices();
		assert (localServices != null);
		IRemoteConnectionManager localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		IRemoteConnection localConnection = localConnectionManager.getConnection(IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
		assert (localConnection != null);
		rm = LaunchControllerManager.getInstance().getLaunchController(localServices.getId(), localConnection.getName(), xml);
		// JAXBRMConfigurationSelectionWizardPage
		rm.setRMConfigurationURL(JAXBTestsPlugin.getURL(xml));
		// JAXBRMControlConfigurationWizardPage
		rm.getConfiguration();
		// use remote = local
		rm.setRemoteServicesId(localServices.getId());
		rm.setConnectionName(localConnection.getName());
	}

	@SuppressWarnings("unchecked")
	private void emulateLaunchTab() throws Throwable {
		launchConfig = new TestLaunchConfiguration();
		Map<String, Object> env = launchConfig.getAttributes();
		Map<String, String> live = new HashMap<String, String>();
		live.put("FOO_VAR_1", "FOO_VALUE_1"); //$NON-NLS-1$ //$NON-NLS-2$
		live.put("FOO_VAR_2", "FOO_VALUE_2"); //$NON-NLS-1$ //$NON-NLS-2$
		live.put("FOO_VAR_3", "FOO_VALUE_3"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, live);
		env.put(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		env.put(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, "/u/ncsa/arossi/test"); //$NON-NLS-1$
		env.put(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, "/u/ncsa/arossi/test/foo"); //$NON-NLS-1$
		env.put(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, "-v -f /u/ncsa/arossi/test/data"); //$NON-NLS-1$
		env.put("Job_Name", "TestRMLaunch"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("Resource_List.nodes", "1:ppn=8"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("Resource_List.walltime", "00:10:00"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("export_all", true); //$NON-NLS-1$
		env.put(JAXBControlConstants.MPI_CMD, "mpiexec"); //$NON-NLS-1$ 
		env.put(JAXBControlConstants.MPI_ARGS, "-machinefile $PBS_NODEFILE -np 8"); //$NON-NLS-1$ 
		IVariableMap rmVarMap = rm.getEnvironment();
		AttributeType queues = rmVarMap.getAttributes().get("available_queues"); //$NON-NLS-1$ 
		if (queues != null) {
			List<String> q = (List<String>) queues.getValue();
			env.put("destination", q.get(0)); //$NON-NLS-1$
		}
		env.put("directory", "/Users/arossi"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
