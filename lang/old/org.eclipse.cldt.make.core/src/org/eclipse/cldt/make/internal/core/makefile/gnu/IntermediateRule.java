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
package org.eclipse.cldt.make.internal.core.makefile.gnu;

import org.eclipse.cldt.make.core.makefile.gnu.IIntermediateRule;
import org.eclipse.cldt.make.internal.core.makefile.Command;
import org.eclipse.cldt.make.internal.core.makefile.Directive;
import org.eclipse.cldt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cldt.make.internal.core.makefile.Target;

/**
 * .INTERMEDIATE
 *   The targets which `.INTERMEDIATE' depends on are treated as intermediate files.
 *   `.INTERMEDIATE' with no prerequisites has no effect.
 */
public class IntermediateRule extends SpecialRule implements IIntermediateRule {

	public IntermediateRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_INTERMEDIATE), reqs, new Command[0]);
	}

}
