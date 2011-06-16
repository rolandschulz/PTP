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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.ptp.internal.rdt.core.model.ModelAdapter;
import org.eclipse.ptp.internal.rdt.core.model.ModelManipulationTestBase;

@SuppressWarnings("restriction")
public class AdapterTests extends ModelManipulationTestBase {

	@Override
	protected void manipulate(ICElement element) throws Exception {
		ICElement adapted = ModelAdapter.adaptElement(null, element, -1, false);
		assertEquals(element.getElementType(), adapted.getElementType());
		assertEquals(element.getElementName(), adapted.getElementName());
		
		if (element instanceof ISourceReference) {
			ISourceReference reference1 = (ISourceReference) element;
			ISourceReference reference2 = (ISourceReference) adapted;
			assertEquals(reference1.getSourceRange(), reference2.getSourceRange());
		}
	}
}
