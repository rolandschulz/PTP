package org.eclipse.ptp.internal.rdt.ui.wizards.settings;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * Defines the methods that the strategies can call on the 
 * wizard page.
 * 
 * This interface exists mainly so that it can be implemented by a 
 * mock object, which makes testing the strategies easy.
 * 
 * @author Mike Kucera
 */
public interface IProjectSettingsWizardPage {

	static final String FILENAME_EXTENSION = "xml"; //$NON-NLS-1$
	

	/**
	 * Sets the input list for the displayed list of settings processors.
	 */
	void setDisplayedSettingsProcessors(List<ISettingsProcessor> processors);
	
	/**
	 * Returns a list of all the available settings processors.
	 */
	List<ISettingsProcessor> getSettingsProcessors();
	
	/**
	 * Returns a list of the settings processors that were selected by the user.
	 */
	List<ISettingsProcessor> getSelectedSettingsProcessors();
	
	/**
	 * Returns the contents of the file path text box.
	 */
	String getDestinationFilePath();

	/**
	 * Returns the configuration that was selected by the user.
	 * 
	 * The selected project can be determined by calling 
	 * ICCOnfigurationDescription.getProjectDescription().getProject();
	 */
	ICConfigurationDescription getSelectedConfiguration();
	
	/**
	 * Causes an error dialog to be shown to the user.
	 */
	void showErrorDialog(String dialogTitle, String message);

	/**
	 * Displays a message at the top of the wizard page.
	 * @param flag a constant from the IMessageProvider interface
	 */
	void setMessage(String message, int flag);


}
