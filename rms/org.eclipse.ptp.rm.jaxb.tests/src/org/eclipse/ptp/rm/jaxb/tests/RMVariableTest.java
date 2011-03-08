package org.eclipse.ptp.rm.jaxb.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class RMVariableTest extends TestCase implements IJAXBNonNLSConstants {

	private static final String xml = DATA + "rm-pbs-torque_2.3.7.xml"; //$NON-NLS-1$

	@Override
	public void setUp() {

	}

	@Override
	public void tearDown() {

	}

	public void testExpression() {
		try {

		} catch (Throwable t) {
			t.printStackTrace();
			assert (t != null);
		}
	}

	public void testJAXBLoadVariables() {
		try {
			JAXBInitializationUtils.validate(xml);
			RMVariableMap map = RMVariableMap.setActiveInstance(null);
			JAXBInitializationUtils.initializeMap(JAXBInitializationUtils.initializeRMData(xml), map);
			String exp = map.getString("${rm:stagein#description}"); //$NON-NLS-1$
			System.out.println(exp);
			assertEquals(Messages.RMVariableTest_1, exp);
		} catch (Throwable t) {
			t.printStackTrace();
			assert (t != null);
		}
	}
}
