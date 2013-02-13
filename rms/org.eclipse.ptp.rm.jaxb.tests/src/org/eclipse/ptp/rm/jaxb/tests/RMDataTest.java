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

import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.CommandType;
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
		for (Iterator<Map.Entry<String, AttributeType>> i = map.getAttributes().entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, AttributeType> e = i.next();
			AttributeType a = e.getValue();
			buffer.append(JAXBCoreConstants.LT).append(a.getName()).append(JAXBCoreConstants.GTLT).append(a.getType())
					.append(JAXBCoreConstants.GTLT).append(a.getDefault()).append(JAXBCoreConstants.GTLT).append(a.getChoice())
					.append(JAXBCoreConstants.GTLT).append(a.getMax()).append(JAXBCoreConstants.GTLT).append(a.getMin())
					.append(JAXBCoreConstants.GTLT).append(a.getValidator()).append(JAXBCoreConstants.GTLT)
					.append(a.getDescription()).append(JAXBCoreConstants.GTLT).append(a.getTooltip()).append(a.getValue())
					.append(JAXBCoreConstants.GT).append(JAXBCoreConstants.LINE_SEP);
		}
		System.out.println(buffer);
	}

}
