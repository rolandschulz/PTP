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
 * IMacroDefinitions are in the form:
 * string1 = [string2]
 */
public interface IMacroDefinition  extends IDirective {

	/**
	 * Returns the name of the macro
	 * @return
	 */
	String getName();

	/**
	 * Returns the value of the macro
	 * @return
	 */
	StringBuffer getValue();

	/**
	 * The macro is a built-in
	 * @return
	 */
	boolean isFromDefault();

	/**
	 * The macro was found in a Makefile.
	 * @return
	 */
	boolean isFromMakefile();

	/**
	 * The macro came from the environment.
	 * @return
	 */
	boolean isFromEnviroment();

	/**
	 * The macro came from the make command option -e
	 * @return
	 */
	boolean isFromEnvironmentOverride();

	/**
	 * The macro was pass from an option to make.
	 * @return
	 */
	boolean isFromCommand();
}
