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

package org.eclipse.ptp.internal.rdt.core;

import org.eclipse.ptp.internal.rdt.core.callhierarchy.CallHierarchySerializationTests;
import org.eclipse.ptp.internal.rdt.core.model.CModelBuilder2Tests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	AdapterTests.class,
	SerializationTests.class,
	CallHierarchySerializationTests.class,
	CModelBuilder2Tests.class,
})
public class AutomatedSuite {
}
