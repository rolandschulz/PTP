/*******************************************************************************
 * Copyright (c) 2010 Los Alamos National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	LANL - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.utils.core;

/**
 * @author Randy M. Roberts
 * @since 2.0
 * 
 */
public interface ICopier<T> {
	T copy(T t);
}
