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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ptp.rm.jaxb.control.internal.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;

public class RMDataTest extends TestCase {

	private static final String rmxml = JAXBCoreConstants.DATA + "pbs-test-local.xml"; //$NON-NLS-1$
	private static final String tokxml = JAXBCoreConstants.DATA + "tokenizer-examples.xml"; //$NON-NLS-1$

	@Override
	public void setUp() {

	}

	@Override
	public void tearDown() {

	}

	public void testJAXBRMInstantiation() {
		ResourceManagerData rmdata = null;
		try {
			JAXBTestsPlugin.validate(rmxml);
			rmdata = JAXBInitializationUtils.initializeRMData(JAXBTestsPlugin.getURL(rmxml));
			assertNotNull(rmdata);
			RMVariableMap map = new RMVariableMap();
			JAXBInitializationUtils.initializeMap(rmdata, map);
			print(map);
			String exp = map.getString(null, "${ptp_rm:stagein#description}"); //$NON-NLS-1$
			System.out.println("testJAXBRMInstantiation: " + exp); //$NON-NLS-1$
			assertEquals(Messages.RMVariableTest_1, exp);
		} catch (Throwable t) {
			t.printStackTrace();
			assert (t != null);
		}
	}

	public void testJAXBTokenizerInstantiation() {
		ResourceManagerData rmdata = null;
		try {
			JAXBTestsPlugin.validate(tokxml);
			rmdata = JAXBInitializationUtils.initializeRMData(JAXBTestsPlugin.getURL(tokxml));
			assertNotNull(rmdata);
			List<CommandType> cmds = rmdata.getControlData().getStartUpCommand();
			for (CommandType cmd : cmds) {
				System.out.println("testJAXBTokenizerInstantiation: " + cmd.getName()); //$NON-NLS-1$
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
			if (o instanceof AttributeType) {
				AttributeType ja = (AttributeType) o;
				buffer.append(JAXBCoreConstants.LT).append(ja.getName()).append(JAXBCoreConstants.GTLT).append(ja.getType())
						.append(JAXBCoreConstants.GTLT).append(ja.getDefault()).append(JAXBCoreConstants.GTLT)
						.append(ja.getChoice()).append(JAXBCoreConstants.GTLT).append(ja.getMax()).append(JAXBCoreConstants.GTLT)
						.append(ja.getMin()).append(JAXBCoreConstants.GTLT).append(ja.getValidator())
						.append(JAXBCoreConstants.GTLT).append(ja.getDescription()).append(JAXBCoreConstants.GTLT)
						.append(ja.getTooltip()).append(ja.getValue()).append(JAXBCoreConstants.GT)
						.append(JAXBCoreConstants.LINE_SEP);
			} else if (o instanceof PropertyType) {
				PropertyType p = (PropertyType) o;
				buffer.append(JAXBCoreConstants.LT).append(p.getName()).append(JAXBCoreConstants.GTLT).append(p.getValue())
						.append(JAXBCoreConstants.GT).append(JAXBCoreConstants.LINE_SEP);
			} else {
				buffer.append(JAXBCoreConstants.LT).append(e.getKey()).append(JAXBCoreConstants.GTLT).append(e.getValue())
						.append(JAXBCoreConstants.GT).append(JAXBCoreConstants.LINE_SEP);
			}
		}
		System.out.println(buffer);
	}

}
