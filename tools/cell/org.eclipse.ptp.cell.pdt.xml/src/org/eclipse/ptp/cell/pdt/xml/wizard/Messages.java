package org.eclipse.ptp.cell.pdt.xml.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.pdt.xml.wizard.messages"; //$NON-NLS-1$
	public static String AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Message_CouldNotCreateFile;
	public static String AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Title_CouldNotCreateFile;
	public static String AbstractPdtXmlWizard_Init_Dialog_Message_CouldNotParserGroupDefinitionFile;
	public static String AbstractPdtXmlWizard_Init_Dialog_Title_CouldNotParserGroupDefinitionFile;
	public static String AbstractPdtXmlWizard_GenerateXmlConfigFile_Dialog_Message_ProblemRefreshingWorkspace;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
