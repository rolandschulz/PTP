/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.make.core.makefile;

/**
 * .IGNORE
 * Prerequisites of this special target are targets themselves; this shall cause errors
 * from commands associated with them to be ignored in the same manner as specified by the -i option.
 */
public interface IIgnoreRule extends ISpecialRule {
}
