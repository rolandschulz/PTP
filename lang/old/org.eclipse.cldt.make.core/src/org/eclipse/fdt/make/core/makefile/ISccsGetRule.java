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
 * .SCCS_GET
 * The application shall ensure that this special target is specified without prerequesites.
 * The commands specified with this target shall replace the default
 * commands associated with this special target.
 */
public interface ISccsGetRule extends ISpecialRule {
}
