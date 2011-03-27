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

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Command;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class RMDataTest extends TestCase implements IJAXBNonNLSConstants {

	private static final String rmxml = DATA + "rm-pbs-torque_2.3.7.xml"; //$NON-NLS-1$
	private static final String tokxml = DATA + "tokenizer-examples.xml"; //$NON-NLS-1$

	@Override
	public void setUp() {

	}

	@Override
	public void tearDown() {

	}

	public void testJAXBRMInstantiation() {
		ResourceManagerData rmdata = null;
		try {
			JAXBInitializationUtils.validate(rmxml);
			rmdata = JAXBInitializationUtils.initializeRMData(rmxml);
			if (rmdata != null) {
				RMVariableMap map = RMVariableMap.setActiveInstance(null);
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
			JAXBInitializationUtils.validate(tokxml);
			rmdata = JAXBInitializationUtils.initializeRMData(tokxml);
			if (rmdata != null) {
				List<Command> cmds = rmdata.getControlData().getStartUpCommand();
				for (Command cmd : cmds) {
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
			if (o instanceof Attribute) {
				Attribute ja = (Attribute) o;
				buffer.append(LT).append(GTLT).append(ja.getName()).append(GTLT).append(ja.getType()).append(GTLT)
						.append(ja.getDefault()).append(GTLT).append(ja.getChoice()).append(GTLT).append(ja.getMax()).append(GTLT)
						.append(ja.getMin()).append(GTLT).append(ja.getValidator()).append(GTLT).append(ja.getDescription())
						.append(GTLT).append(ja.getTooltip()).append(ja.getValue()).append(GT).append(LINE_SEP);
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
