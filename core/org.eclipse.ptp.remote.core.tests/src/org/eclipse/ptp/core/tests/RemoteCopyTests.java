package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.remotetools.core.RemoteToolsServices;
import org.eclipse.ptp.remote.remotetools.core.environment.PTPTargetControl;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;

public class RemoteCopyTests extends TestCase {
	private static final String USERNAME = "user"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	
	private PTPTargetControl fTargetControl;
	private IRemoteExecutionManager fExecutionManager;
	private IRemoteExecutionTools fExecutionTools;
	private TargetTypeElement fTarget;
	private TargetElement fElement;
	
	public void testCopy() {
		for (int i = 0; i < 50; i++) {
			IRemoteScript script = fExecutionTools.createScript();
			script.setScript("echo hi there");
			RemoteProcess p = null;
			try {
				p = fExecutionTools.executeProcess(script);
			} catch (RemoteExecutionException e) {
				fail();
			} catch (RemoteConnectionException e) {
				fail();
			} catch (CancelException e) {
				fail();
			}
			assertNotNull(p);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					System.out.println("read \""+line+"\"");
					assertTrue(line.startsWith("hi there"));
				}
			} catch (IOException e) {
				fail();
			}
//			try {
//				p.waitFor();
//			} catch (InterruptedException e) {
//			}
		}
		
//		IRemoteScript script = fExecutionTools.createScript();
//		script.setScript("cat > /tmp/xxx.script");
//		RemoteProcess p = null;
//		try {
//			p = fExecutionTools.executeProcess(script);
//		} catch (RemoteExecutionException e) {
//			fail();
//		} catch (RemoteConnectionException e) {
//			fail();
//		} catch (CancelException e) {
//			fail();
//		}
//		assertNotNull(p);
//		BufferedWriter b = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//		try {
//			b.write("this is a test\n");
//			b.write("this is a test2\n");
//			b.close();
//		} catch (IOException e) {
//			fail();
//		}
//		try {
//			p.waitFor();
//		} catch (InterruptedException e) {
//		}
//	
//		script = fExecutionTools.createScript();
//		script.setScript("cat >> /tmp/xxx.script");
//		p = null;
//		try {
//			p = fExecutionTools.executeProcess(script);
//		} catch (RemoteExecutionException e) {
//			fail();
//		} catch (RemoteConnectionException e) {
//			fail();
//		} catch (CancelException e) {
//			fail();
//		}
//		assertNotNull(p);
//		b = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//		try {
//			b.write("this is a test4\n");
//			b.write("this is a test5\n");
//			b.close();
//		} catch (IOException e) {
//			fail();
//		}
//		try {
//			p.waitFor();
//		} catch (InterruptedException e) {
//		}
//		
//		script = fExecutionTools.createScript();
//		script.setScript("cat < /tmp/xxx.script");
//		p = null;
//		try {
//			p = fExecutionTools.executeProcess(script);
//		} catch (RemoteExecutionException e) {
//			fail();
//		} catch (RemoteConnectionException e) {
//			fail();
//		} catch (CancelException e) {
//			fail();
//		}
//		assertNotNull(p);
//		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//		try {
//			String line;
//			while ((line = br.readLine()) != null) {
//				System.out.println("read \""+line+"\"");
//			}
//		} catch (IOException e) {
//			fail();
//		}
//		try {
//			p.waitFor();
//		} catch (InterruptedException e) {
//		}

	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("ptp.localhost-selection", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.login-username", USERNAME); //$NON-NLS-1$
		map.put("ptp.login-password", PASSWORD); //$NON-NLS-1$
		map.put("ptp.connection-address", HOST); //$NON-NLS-1$
		map.put("ptp.connection-port", "22"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.key-path", ""); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.key-passphrase", ""); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.is-passwd-auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.connection-timeout", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.cipher-type", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		
		fTarget = RemoteToolsServices.getTargetTypeElement();
		String id = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
		TargetElement element = new TargetElement(fTarget, HOST, map, id);
		fTarget.addElement(element);
		fTargetControl = (PTPTargetControl)element.getControl();
		fTargetControl.create(new NullProgressMonitor());
		fExecutionManager = fTargetControl.createExecutionManager();
		fExecutionTools = fExecutionManager.getExecutionTools();
		assertNotNull(fExecutionTools);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fExecutionManager.close();
		fTarget.removeElement(fElement);
	}
	
}
