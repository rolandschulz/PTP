/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.ptp.internal.rdt.core.model.ModelManipulationTestBase;
import org.eclipse.ptp.internal.rdt.core.tests.util.ModelUtil;

public class SerializationTests extends ModelManipulationTestBase {

	@Override
	protected void manipulate(ICElement element) throws Exception {
		ModelUtil.reconstitute(element);
	}
}
