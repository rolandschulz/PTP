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
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.runnable.ManagedFilesJob;
import org.eclipse.ptp.rm.jaxb.core.runnable.ScriptHandler;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ManagedFilesTest extends TestCase implements IJAXBNonNLSConstants {

	private static final String xml = DATA + "rm-pbs-torque_2.3.7.xml"; //$NON-NLS-1$
	private static Control controlData;
	private static Map<String, Object> env;
	private static Map<String, String> live;
	private static boolean appendEnv;
	private static boolean verbose = false;

	private RemoteServicesDelegate delegate;

	@Override
	public void setUp() {
		try {
			JAXBInitializationUtils.validate(xml);
			ResourceManagerData rmdata = JAXBInitializationUtils.initializeRMData(xml);
			controlData = rmdata.getControlData();
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
			Property contents = (Property) env.get(SCRIPT);
			if (contents != null) {
				System.out.println(contents.getValue());
			}
		}
		ManagedFiles files = controlData.getManagedFiles();
		assertNotNull(files);
		try {
			ManagedFilesJob job = new ManagedFilesJob(null, files, delegate);
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
		ScriptHandler job = new ScriptHandler(null, script, live, appendEnv);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException t) {
			t.printStackTrace();
		}

		Property contents = (Property) env.get(SCRIPT);
		assertNotNull(contents);
		System.out.println(contents.getValue());
	}

	private void initializeConnections() {
		delegate = new RemoteServicesDelegate(null, null);
	}

	private void putValue(String name, String value) {
		Property p = new Property();
		p.setName(name);
		p.setValue(value);
		env.put(name, p);
	}

	private void setTestValues() {
		for (String key : env.keySet()) {
			Object target = env.get(key);
			String value = key + "_TEST_VALUE"; //$NON-NLS-1$
			if (target instanceof Property) {
				((Property) target).setValue(value);
			} else if (target instanceof Attribute) {
				((Attribute) target).setValue(value);
			}
		}
		putValue(CONTROL_USER_VAR, "fooUser"); //$NON-NLS-1$
		putValue(CONTROL_ADDRESS_VAR, "abe.ncsa.uiuc.edu"); //$NON-NLS-1$
		putValue(DIRECTORY, "/u/ncsa/arossi/test"); //$NON-NLS-1$ 
		putValue(MPI_CMD, "mpiexec"); //$NON-NLS-1$ 
		putValue(MPI_ARGS, "-np 8"); //$NON-NLS-1$ 
		putValue(EXEC_PATH, "/u/ncsa/arossi/test/foo"); //$NON-NLS-1$ 
		if (verbose) {
			RMDataTest.print(RMVariableMap.getActiveInstance());
		}
	}
}
