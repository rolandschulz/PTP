package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsServices;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

public class RemoteToolsTests extends TestCase {
	private static final String USERNAME = "greg"; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$

	private ITargetControl fTargetControl;
	private IRemoteExecutionManager fExecutionManager;
	private IRemoteExecutionTools fExecutionTools;
	private TargetTypeElement fTarget;
	private TargetElement fElement;

	public void testScriptExecute() {
		for (int i = 0; i < 10; i++) {
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
					System.out.println("read \"" + line + "\"");
					assertTrue(line.startsWith("hi there"));
				}
			} catch (IOException e) {
				fail();
			}
			try {
				p.waitFor();
			} catch (InterruptedException e) {
			}
		}
	}

	public void testScriptIO() {

		IRemoteScript script = fExecutionTools.createScript();
		script.setScript("cat > /tmp/xxx.script");
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
		BufferedWriter b = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		try {
			b.write("this is a test\n");
			b.write("this is a test2\n");
			b.close();
		} catch (IOException e) {
			fail();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
		}

		script = fExecutionTools.createScript();
		script.setScript("cat >> /tmp/xxx.script");
		p = null;
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
		b = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		try {
			b.write("this is a test4\n");
			b.write("this is a test5\n");
			b.close();
		} catch (IOException e) {
			fail();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
		}

		script = fExecutionTools.createScript();
		script.setScript("cat < /tmp/xxx.script");
		p = null;
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
			int count = 0;
			while ((line = br.readLine()) != null) {
				System.out.println("read \"" + line + "\"");
				count++;
			}
			assertTrue(count == 4);
		} catch (IOException e) {
			fail();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
		}

	}

	public void testScriptCancel() {

		IRemoteScript script = fExecutionTools.createScript();
		script.setScript("while true; do sleep 1; done");
		RemoteProcess p = null;
		try {
			p = fExecutionTools.executeProcess(script);
		} catch (RemoteExecutionException e) {
			fail(e.getLocalizedMessage());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		} catch (CancelException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(p);
		/*
		 * Give script time to start
		 */
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
		}
		p.destroy();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
		}
	}

	public void testBug300435() {
		IRemoteFileTools fileTools;
		String DIR_NAME = "/tmp/my path";
		try {
			fileTools = fExecutionManager.getRemoteFileTools();
			fileTools.createDirectory(DIR_NAME, new NullProgressMonitor());
			IRemoteItem[] items = fileTools.listItems("/tmp", new NullProgressMonitor());
			boolean found = false;
			for (IRemoteItem item : items) {
				if (item.getPath().equals(DIR_NAME)) {
					found = true;
					break;
				}
			}
			assertTrue(found);
			fileTools.removeDirectory(DIR_NAME, new NullProgressMonitor());
			fileTools.createFile(DIR_NAME, new NullProgressMonitor());
			fileTools.removeFile(DIR_NAME, new NullProgressMonitor());
		} catch (RemoteOperationException e) {
			fail(e.getLocalizedMessage());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		} catch (CancelException e) {
			fail(e.getLocalizedMessage());
			// } catch (IOException e) {
			// fail(e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		ConfigFactory factory = new ConfigFactory();
		ITargetConfig config = factory.createTargetConfig();
		config.setConnectionAddress(HOST);
		config.setLoginUsername(USERNAME);
		config.setPasswordAuth(true);

		fTarget = RemoteToolsServices.getTargetTypeElement();
		String id = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
		TargetElement element = new TargetElement(fTarget, HOST, factory.getAttributes(), id);
		fTarget.addElement(element);
		fTargetControl = element.getControl();
		fTargetControl.create(new NullProgressMonitor());
		fExecutionManager = fTargetControl.createExecutionManager();
		fExecutionTools = fExecutionManager.getExecutionTools();
		assertNotNull(fExecutionTools);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fExecutionManager.close();
		fTarget.removeElement(fElement);
	}

}
