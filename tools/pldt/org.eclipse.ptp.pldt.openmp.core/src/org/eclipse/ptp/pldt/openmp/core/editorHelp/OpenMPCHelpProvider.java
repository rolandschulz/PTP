package org.eclipse.ptp.pldt.openmp.core.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpProviderImpl;

/**
 * 
 * This class implements ICHelpProvider and provides OpenMP information <br>
 * (F1, hover, content assist, etc.)
 * 
 * @author tibbitts
 */

public class OpenMPCHelpProvider extends CHelpProviderImpl {

	public void initialize() {
		helpBook = new OpenMPCHelpBook();
	}
}
