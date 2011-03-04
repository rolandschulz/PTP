package org.eclipse.ptp.rm.jaxb.tests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ManagedFilesTest extends TestCase implements IJAXBNonNLSConstants {

	private static final String xml = DATA + "rm-pbs-torque_2.3.7.xml"; //$NON-NLS-1$
	private static Control controlData;
	private static Map<String, Object> env;
	private static Map<String, String> live;
	private static boolean appendEnv;
	private static boolean verbose = false;

	private static IRemoteServices localServices;
	private static IRemoteConnectionManager localConnectionManager;
	private static IRemoteConnection localConnection;
	private static IRemoteFileManager localFileManager;
	private static IRemoteFileManager remoteFileManager;

	@Override
	public void setUp() {
		try {
			JAXBInitializationUtils.validate(xml);
			ResourceManagerData rmdata = JAXBInitializationUtils.initializeRMData(xml);
			controlData = rmdata.getControl();
			RMVariableMap map = RMVariableMap.setActiveInstance(null);
			JAXBInitializationUtils.initializeMap(rmdata, map);
			env = map.getVariables();
			appendEnv = true;
			live = new HashMap<String, String>();
			live.put("FOO_VAR_1", "FOO_VALUE_1"); //$NON-NLS-1$ //$NON-NLS-2$
			live.put("FOO_VAR_2", "FOO_VALUE_2"); //$NON-NLS-1$ //$NON-NLS-2$
			live.put("FOO_VAR_3", "FOO_VALUE_3"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable t) {
			t.printStackTrace();
			assertNotNull(t);
		}
		setTestValues();
		if (getName().equals("testManagedFiles")) {//$NON-NLS-1$
			initializeConnections();
		}
	}

	@Override
	public void tearDown() {
		controlData = null;
	}

	public void testManagedFiles() {
		composeScript();
		if (verbose) {
			System.out.println(env.get(SCRIPT));
		}
		ManagedFiles files = controlData.getManagedFiles();
		assertNotNull(files);
		try {
			ManagedFilesJob job = new ManagedFilesJob(files, localFileManager, remoteFileManager);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException t) {
				t.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			assertNotNull(t);
		}
	}

	private void composeScript() {
		Script script = controlData.getScript();
		assertNotNull(script);
		ScriptHandler job = new ScriptHandler(script, live, appendEnv);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}
		Object contents = env.get(SCRIPT);
		assertNotNull(contents);
	}

	private void initializeConnections() {
		localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		/*
		 * Since it's a local service, it doesn't matter which parameter is
		 * passed
		 */
		localConnection = localConnectionManager.getConnection(ZEROSTR);
		assert (localConnection != null);
		localFileManager = localServices.getFileManager(localConnection);
		assert (localFileManager != null);
		remoteFileManager = localFileManager;
	}

	private void setTestValues() {
		for (String key : env.keySet()) {
			Object target = env.get(key);
			String value = key + "_TEST_VALUE"; //$NON-NLS-1$
			if (target instanceof Property) {
				((Property) target).setValue(value);
			} else if (target instanceof JobAttribute) {
				((JobAttribute) target).setValue(value);
			}
		}
		env.put(CONTROL_USER_VAR, "fooUser"); //$NON-NLS-1$
		env.put(MONITOR_USER_VAR, "fooUser"); //$NON-NLS-1$
		env.put(CONTROL_ADDRESS_VAR, "abe.ncsa.uiuc.edu"); //$NON-NLS-1$
		env.put(MONITOR_ADDRESS_VAR, "abe.ncsa.uiuc.edu"); //$NON-NLS-1$
		env.put(DIRECTORY, "/u/ncsa/arossi/test"); //$NON-NLS-1$ 
		env.put(MPI_CMD, "mpiexec"); //$NON-NLS-1$ 
		env.put(MPI_ARGS, "-np 8"); //$NON-NLS-1$ 
		env.put(EXEC_PATH, "/u/ncsa/arossi/test/foo"); //$NON-NLS-1$ 
		if (verbose) {
			RMDataTest.print(RMVariableMap.getActiveInstance());
		}
	}
}
