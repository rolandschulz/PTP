/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.tests;

import junit.framework.TestCase;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeBool;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeCharPointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeNamed;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeRange;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeVoid;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;

public class AIFTests extends TestCase {
	public void testTypes() {
		IAIFType t;
		try {
			t = AIFFactory.getAIFType("a8"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeAddress);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("{|;;;}"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeAggregate);
			t = AIFFactory.getAIFType("{a|x=is4,y=f4;;v=pa4;}"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeAggregate);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("[r0,2is4][r-1,3is4][r10,4is4][r-10,5is4][r0,6is4]is4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeArray);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("b1"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeBool);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("c"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeChar);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("pa4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeCharPointer);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("<|>is4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeEnum);
			t = AIFFactory.getAIFType("<a|x=-1,y=0,z=1>is4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeEnum);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("f4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeFloat);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("&is4,f8/f8"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeFunction);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("iu8"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeInt);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("%1/^a4{s1|a=is4,b=>1/;;;}"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeNamed);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("^a4{s1|a=is4,b=f4,c=c;;;}"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypePointer);
			t = AIFFactory.getAIFType("^a4^a4c"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypePointer);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("r-4,10is4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeRange);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("r-4,10is4"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeRange);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType(">3/"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeReference);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("s"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeString);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("(|)"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeUnion);
			t = AIFFactory.getAIFType("(u|a=is4,b=f8)"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeUnion);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
		try {
			t = AIFFactory.getAIFType("v0"); //$NON-NLS-1$
			assertTrue(t instanceof IAIFTypeVoid);
		} catch (AIFFormatException e) {
			fail(e.getMessage());
		}
	}
}
