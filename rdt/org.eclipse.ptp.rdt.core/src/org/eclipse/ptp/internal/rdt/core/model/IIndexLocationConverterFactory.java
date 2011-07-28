/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.core.index.IIndexLocationConverter;

/**
 * Different converters are needed for the local and remote indexes.
 */
public interface IIndexLocationConverterFactory {

	public IIndexLocationConverter getConverter(ICProject project);
}
