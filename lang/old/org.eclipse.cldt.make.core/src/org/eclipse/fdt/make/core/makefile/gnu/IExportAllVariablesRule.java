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
package org.eclipse.fdt.make.core.makefile.gnu;

import org.eclipse.fdt.make.core.makefile.ISpecialRule;

/**
 * .EXPORT_ALL_VARIABLES
 * Simply by being mentioned as a target, this tells `make' to export
 * all variables to child processes by default.
 */
public interface IExportAllVariablesRule extends ISpecialRule {
}
