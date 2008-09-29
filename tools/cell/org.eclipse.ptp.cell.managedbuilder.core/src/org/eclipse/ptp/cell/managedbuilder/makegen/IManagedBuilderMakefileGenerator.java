/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.managedbuilder.makegen;

/**
 * This interface will add some options needed in Cell and not present in
 * org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator.
 * 
 * 
 * @author laggarcia
 * @since 1.1.0
 */
public interface IManagedBuilderMakefileGenerator extends
		org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator {

	public final String IMPORT_SPU_OBJS_MAKEFILE_NAME = "import_spu.mk"; //$NON-NLS-1$

	public final String SPU_OBJS_DIR_NAME = "spu_objs"; //$NON-NLS-1$

	// Generation error codes
	public static final int SPACES_IN_PATH = 0;

	public static final int NO_SOURCE_FOLDERS = 1;

	public static final int FILE_DOESNT_EXIST = 2;

}
