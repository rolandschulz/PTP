/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * An ITranslationUnit that has changes in its contents which have not been
 * saved to the underlying resource.
 */
public interface IRemoteWorkingCopy extends ITranslationUnit {
	
	/**
	 * Returns the contents of the buffer as a <code>String</code>.
	 * @return the contents of the buffer.
	 */
	String getText();
}
