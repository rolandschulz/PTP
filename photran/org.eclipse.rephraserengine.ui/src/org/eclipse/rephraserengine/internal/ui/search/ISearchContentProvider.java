/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.rephraserengine.internal.ui.search;

/**
 * @author Doug Schaefer
 *
 * @since 2.0
 */
public interface ISearchContentProvider {

	public void elementsChanged(Object[] elements);

	public void clear();

}
