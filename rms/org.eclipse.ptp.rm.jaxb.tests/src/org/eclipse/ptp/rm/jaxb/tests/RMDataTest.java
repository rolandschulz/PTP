package org.eclipse.ptp.rm.jaxb.tests;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.JobAttribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
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
				print(RMVariableMap.getInstance());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			assert (t != null);
		}
	}

	static void print(RMVariableMap map) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Map.Entry<String, Object>> i = map.getVariables().entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Object> e = i.next();
			Object o = e.getValue();
			if (o instanceof JobAttribute) {
				JobAttribute ja = (JobAttribute) o;
				buffer.append(LT).append(ja.getId()).append(GTLT).append(ja.getName()).append(GTLT).append(ja.getType())
						.append(GTLT).append(ja.getDefault()).append(GTLT).append(ja.getChoice()).append(GTLT).append(ja.getMax())
						.append(GTLT).append(ja.getMin()).append(GTLT).append(ja.getValidator()).append(GTLT)
						.append(ja.getDescription()).append(GTLT).append(ja.getTooltip()).append(ja.getValue()).append(GT)
						.append(LINE_SEP);
			} else if (o instanceof Property) {
				Property p = (Property) o;
				buffer.append(LT).append(p.getName()).append(GTLT).append(p.getValue()).append(GT).append(LINE_SEP);
			} else {
				buffer.append(LT).append(e.getKey()).append(GTLT).append(e.getValue()).append(GT).append(LINE_SEP);
			}
		}
		System.out.println(buffer);
	}

}
