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
package org.eclipse.fdt.make.internal.core.makefile.gnu;

import org.eclipse.fdt.make.core.makefile.gnu.IPhonyRule;
import org.eclipse.fdt.make.internal.core.makefile.Command;
import org.eclipse.fdt.make.internal.core.makefile.Directive;
import org.eclipse.fdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.fdt.make.internal.core.makefile.Target;

/**
 * .PHONY
 *     The prerequisites of the special target `.PHONY' are considered to be phony targets.
 *     When it is time to consider such a target, `make' will run its commands unconditionally, regardless of
 *     whether a file with that name exists or what its last-modification time is.
 */
public class PhonyRule extends SpecialRule implements IPhonyRule {

	public PhonyRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_PHONY), reqs, new Command[0]);
	}

}
