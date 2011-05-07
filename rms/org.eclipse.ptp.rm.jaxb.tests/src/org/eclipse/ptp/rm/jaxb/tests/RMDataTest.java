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

import org.eclipse.ptp.rm.jaxb.core.JAXBRMConstants;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class RMDataTest extends TestCase {

	private static final String rmxml = JAXBRMConstants.DATA + "test-pbs.xml"; //$NON-NLS-1$
	private static final String tokxml = JAXBRMConstants.DATA + "tokenizer-examples.xml"; //$NON-NLS-1$

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
			if (rmdata != null) {
				RMVariableMap map = new RMVariableMap();
				JAXBInitializationUtils.initializeMap(rmdata, map);
				print(map);
				String exp = map.getString(null, "${rm:stagein#description}"); //$NON-NLS-1$
				System.out.println(exp);
				assertEquals(Messages.RMVariableTest_1, exp);
			}
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
			if (rmdata != null) {
				List<CommandType> cmds = rmdata.getControlData().getStartUpCommand();
				for (CommandType cmd : cmds) {
					System.out.println(cmd.getName());
				}
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
				buffer.append(JAXBRMConstants.LT).append(ja.getName()).append(JAXBRMConstants.GTLT).append(ja.getType())
						.append(JAXBRMConstants.GTLT).append(ja.getDefault()).append(JAXBRMConstants.GTLT).append(ja.getChoice())
						.append(JAXBRMConstants.GTLT).append(ja.getMax()).append(JAXBRMConstants.GTLT).append(ja.getMin())
						.append(JAXBRMConstants.GTLT).append(ja.getValidator()).append(JAXBRMConstants.GTLT)
						.append(ja.getDescription()).append(JAXBRMConstants.GTLT).append(ja.getTooltip()).append(ja.getValue())
						.append(JAXBRMConstants.GT).append(JAXBRMConstants.LINE_SEP);
			} else if (o instanceof PropertyType) {
				PropertyType p = (PropertyType) o;
				buffer.append(JAXBRMConstants.LT).append(p.getName()).append(JAXBRMConstants.GTLT).append(p.getValue())
						.append(JAXBRMConstants.GT).append(JAXBRMConstants.LINE_SEP);
			} else {
				buffer.append(JAXBRMConstants.LT).append(e.getKey()).append(JAXBRMConstants.GTLT).append(e.getValue())
						.append(JAXBRMConstants.GT).append(JAXBRMConstants.LINE_SEP);
			}
		}
		System.out.println(buffer);
	}

}
