package org.eclipse.ptp.remote.core.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ptp.core.tests.FileStoreTests;
import org.eclipse.ptp.core.tests.RemoteConnectionTests;
import org.eclipse.ptp.core.tests.SFTPTests;

public class FileManagerTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(FileManagerTestSuite.class.getName());
		
		suite.addTestSuite(FileStoreTests.class);
		suite.addTestSuite(RemoteConnectionTests.class);
		suite.addTestSuite(SFTPTests.class);
		return suite;
	}

}
