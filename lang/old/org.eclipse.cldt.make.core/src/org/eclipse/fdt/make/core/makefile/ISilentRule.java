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
 * .SILENT
 * Prerequisites of this special target are targets themselves; this shall case
 * commands associated with them not to be written to the standard output before
 * they are executed.
 */
public interface ISilentRule extends ISpecialRule {
}
