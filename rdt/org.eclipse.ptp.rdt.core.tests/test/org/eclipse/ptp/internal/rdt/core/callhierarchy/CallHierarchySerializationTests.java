/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.callhierarchy;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.ptp.internal.rdt.core.tests.util.ModelUtil;

@SuppressWarnings("restriction")
public class CallHierarchySerializationTests extends TestCase {
	
	
	public void testCalledByResult() throws Exception {
		CalledByResult result = new CalledByResult();
		CalledByResult result2 = ModelUtil.reconstitute(result);
		assertEquals(result.getElements().length, result2.getElements().length);
	}
	
	
	public void testCallsToResult() throws Exception {
		CallsToResult result = new CallsToResult();
		CallsToResult result2 = ModelUtil.reconstitute(result);
		assertTrue(Arrays.equals(result.getElementSets(), result2.getElementSets()));
	}
	
	
	public void testCElementSet() throws Exception {
		CElementSet set = new CElementSet(new ICElement[0]);
		CElementSet set2 = ModelUtil.reconstitute(set);
		assertEquals(set, set2);
	}
}
