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
package org.eclipse.cldt.make.core.makefile;

/**
 * There are several kinds of rules: Inference rules, target rules
 * Some make provides special rules for example:
 * .DEFAULT, .IGNORE etc ...
 */
public interface IRule extends IParent {
	/**
	 *  Array of command for the rule.
	 * @return
	 */
	ICommand[] getCommands();

	/**
	 * The rule target name.
	 * @return
	 */
	ITarget getTarget();

}
