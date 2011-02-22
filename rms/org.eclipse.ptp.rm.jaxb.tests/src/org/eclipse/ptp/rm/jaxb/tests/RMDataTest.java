package org.eclipse.ptp.rm.jaxb.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;

public class RMDataTest extends TestCase implements IJAXBNonNLSConstants {

	private static final String xml = DATA + "rm-pbs-torque_2.3.7.xml"; //$NON-NLS-1$

	@Override
	public void setUp() {

	}

	@Override
	public void tearDown() {

	}

	public void testJAXBRMInstantiation() {
		ResourceManagerData rmdata = null;
		try {
			JAXBUtils.validate(xml);
			rmdata = JAXBUtils.initializeRMData(xml);
			if (rmdata != null) {
				RMVariableMap.getInstance().print();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			assert (t != null);
		}
	}

}
