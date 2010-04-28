/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.preferences;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.pbs.core.PBSJobAttributes;
import org.eclipse.ptp.rm.pbs.core.PBSPreferenceManager;
import org.eclipse.ptp.rm.pbs.ui.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.ui.dialogs.ComboEntryDialog;
import org.eclipse.ptp.rm.pbs.ui.dialogs.ScrollingEditableMessageDialog;
import org.eclipse.ptp.rm.pbs.ui.providers.AttributeContentProvider;
import org.eclipse.ptp.rm.pbs.ui.providers.AttributeLabelProvider;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigurationUtils;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigurationUtils.SuffixFilter;
import org.eclipse.ptp.rm.pbs.ui.utils.WidgetUtils;
import org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;

/**
 * Allows the user to choose which PBS attributes to display and configure in
 * the Launch Tab. This is done by loading preset attribute lists from
 * configuration files and by checking the ones the user wishes to give values
 * to (to display). The user can also edit, copy and delete existing
 * configuration (template) files.
 * 
 * @author arossi
 */
public class PBSPreferencePage extends AbstractRemoteRMPreferencePage {
	/*
	 * Associated with the delete button.
	 */
	private class DeleteConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/*
		 * Deletes the selected configuration file, and updates the combo box
		 * selections, as well as clearing the table viewer if the deleted file
		 * was the one selected.
		 */
		public void widgetSelected(SelectionEvent e) {
			try {
				Shell shell = getShell();
				String[] availableTemplates = getAvailableTemplates();
				ComboEntryDialog comboDialog = new ComboEntryDialog(shell, "Choose Configuration to Delete", availableTemplates);
				if (comboDialog.open() == Window.CANCEL)
					return;
				String name = comboDialog.getChoice();
				File configFile = new File(getTemplateDir(), name);
				if (configFile.exists())
					configFile.delete();
				updateAvailableTemplates();
				if (name.equals(templates.getText())) {
					comboListener.toggleIgnore();
					templates.deselectAll();
					comboListener.toggleIgnore();
					fireModifyTemplateChoice(null);
				}
			} catch (Throwable t) {
				t.printStackTrace();
				WidgetUtils.errorMessage(getShell(), t, "Cannot delete configuration: " + t.getMessage(),
						"DELETE ATTRIBUTE CONFIGURATION ERROR", false);
			}
		}
	}

	/*
	 * Associated with the edit button.
	 */
	private class EditConfigurationSelectionAdapter implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		/*
		 * Opens the selected file for editing. If a new name is provided, the
		 * changes are written to a new file by that name. Else changes are
		 * overwritten to the existing file.
		 */
		public void widgetSelected(SelectionEvent e) {
			FileWriter fw = null;
			try {
				Shell shell = getShell();
				String newName = null;
				String oldName = null;
				String[] availableTemplates = getAvailableTemplates();
				ComboEntryDialog comboDialog = new ComboEntryDialog(shell, "EDIT/COPY CONFIGURATION", availableTemplates);
				if (comboDialog.open() == Window.CANCEL)
					return;
				oldName = comboDialog.getChoice();

				InputDialog nameDialog = new InputDialog(shell, "NEW CONFIGURATION?", "Name [.properties]", null, null);
				if (nameDialog.open() == Window.CANCEL) {
					newName = oldName;
				} else {
					newName = nameDialog.getValue();
					if (newName.length() == 0) {
						throw new IllegalArgumentException("Click 'Cancel' to edit the same file,"
								+ " or provide a non-empty string as name");
					}
					if (!newName.endsWith(".properties"))
						newName = newName + ".properties";
				}

				String contents = loadTemplateAttributes(oldName, null);

				ScrollingEditableMessageDialog dialog = new ScrollingEditableMessageDialog(getShell(), "EDIT ATTRIBUTES", contents);
				if (dialog.open() == Window.CANCEL)
					return;
				contents = dialog.getValue();
				validateContents(contents);

				File configFile = new File(getTemplateDir(), newName);
				if (configFile.exists())
					configFile.delete();
				fw = new FileWriter(configFile, false);
				fw.write(dialog.getValue());
				fw.flush();
				updateAvailableTemplates();

				if (newName.equals(templates.getText())) {
					fireModifyTemplateChoice(newName);
				}
			} catch (Throwable t) {
				t.printStackTrace();
				WidgetUtils.errorMessage(getShell(), t, "Cannot edit configuration: " + t.getMessage(),
						"EDIT ATTRIBUTE CONFIGURATION ERROR", false);
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException t) {
						t.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * Associated with the selection of a choice of configuration file.
	 */
	private class TemplateComboModifyListener implements ModifyListener {
		private boolean ignore = false;

		/*
		 * Note that if the ignore flag is toggled, no action is taken. For the
		 * sequence of actions on update, see fireModifyTemplateChoice(String
		 * choice).
		 */
		public void modifyText(ModifyEvent e) {
			if (!ignore()) {
				Combo source = (Combo) e.getSource();
				String choice = source.getText();
				fireModifyTemplateChoice(choice);
			}
		}

		private synchronized boolean ignore() {
			return ignore;
		}

		private synchronized void toggleIgnore() {
			ignore = !ignore;
		}
	}

	private static final String FEATURE_PLUGIN = "org.eclipse.ptp.rm.pbs-feature";

	private static final String CONFIG_DIR = "attrconf";

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Fields
	 */

	private static Map<String, AttributePlaceholder> selectedAttributes;
	private TemplateComboModifyListener comboListener;
	private CheckboxTableViewer preferences;
	private Map<String, AttributePlaceholder> templateAttributes;
	private Combo templates;

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Public superclass methods
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#
	 * getPreferences()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Preferences getPreferences() {
		return PBSPreferenceManager.getPreferences();
	}

	@Override
	public void init(IWorkbench workbench) {
		templateAttributes = new TreeMap<String, AttributePlaceholder>();
	}

	/*
	 * (non-Javadoc) Exports the temporary map to the selected map accessible
	 * statically.
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public void performApply() {
		doApply();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		defaultSetting();
		updateApplyButton();
	}

	/*
	 * (non-Javadoc) Saves the preferences to underlying store.
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage#performOk
	 * ()
	 */
	@Override
	public boolean performOk() {
		savePreferences();
		return true;
	}

	/*
	 * (non-Javadoc) Two things are saved: the last choice of template file, and
	 * whether the attributes were checked (selected).
	 * 
	 * @see org.eclipse.ptp.remote.ui.preferences.AbstractRemotePreferencePage#
	 * savePreferences()
	 */
	@Override
	public void savePreferences() {
		PBSPreferenceManager.setTemplatePreference(templates.getText().trim());
		StringBuffer selected = new StringBuffer();
		Map<String, Boolean> map = getSelectedFromTable();
		for (Iterator<Entry<String, Boolean>> i = map.entrySet().iterator(); i.hasNext();) {
			if (selected.length() > 0)
				selected.append(",");
			Entry<String, Boolean> e = i.next();
			selected.append(e.getKey()).append("=").append(e.getValue());
		}
		PBSPreferenceManager.setSelectedAttributes(selected.toString());
		PBSPreferenceManager.savePreferences();
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Protected Superclass Methods
	 */

	@Override
	protected Control createContents(Composite parent) {
		Group templateContainer = WidgetUtils.createFillingGroup(parent, "ATTRIBUTE CONFIGURATION", 4, 1, false);
		comboListener = new TemplateComboModifyListener();
		templates = WidgetUtils.createItemCombo(templateContainer, "TYPE", getAvailableTemplates(), "",
				"Select a template for the attributes to be displayed", comboListener, 1);
		WidgetUtils.createButton(templateContainer, "Edit", null, SWT.PUSH, 1, new EditConfigurationSelectionAdapter());
		WidgetUtils.createButton(templateContainer, "Delete", null, SWT.PUSH, 1, new DeleteConfigurationSelectionAdapter());

		Group preferencesContainer = WidgetUtils.createFillingGroup(parent, "DISPLAY PBS ATTRIBUTES", 1, 1, false);
		Table t = WidgetUtils.createFillingTable(preferencesContainer, 2, 300, 1, SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
		preferences = new CheckboxTableViewer(t);
		preferences.setContentProvider(new AttributeContentProvider());
		preferences.setLabelProvider(new AttributeLabelProvider());
		WidgetUtils.addTableColumn(preferences, "NAME", SWT.LEFT);
		WidgetUtils.addTableColumn(preferences, "TOOL TIP", SWT.LEFT);
		preferences.getTable().setHeaderVisible(true);

		loadSaved(true);
		updatePreferencePage();
		return parent;
	}

	@Override
	protected boolean isValidSetting() {
		return true;
	}

	/*
	 * (non-Javadoc) Note that the checkboxes are set here, after the input has
	 * been updated (it doesn't seem to work inside the label provider, for
	 * instance).
	 * 
	 * @see org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage#
	 * updatePreferencePage()
	 */
	@Override
	protected void updatePreferencePage() {
		preferences.setInput(templateAttributes);
		setCheckedFromModel();
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Auxiliaries
	 */

	/*
	 * Swaps internal attributes for the static ones.
	 */
	private void doApply() {
		synchronized (PBSPreferencePage.class) {
			if (selectedAttributes == null)
				selectedAttributes = new TreeMap<String, AttributePlaceholder>();
			else
				selectedAttributes.clear();
			selectedAttributes.putAll(templateAttributes);
		}
	}

	/*
	 * A full update which populates the checkbox table viewer with the
	 * attributes from the selected configuration file.
	 */
	private void fireModifyTemplateChoice(String choice) {
		loadTemplateAttributes(choice, templateAttributes);
		updateSelectedAttributes(getSelectedFromTable());
		updatePreferencePage();
	}

	/*
	 * Looks in the known location for all properties files and loads their
	 * names.
	 */
	private String[] getAvailableTemplates() {
		List<String> templateNames = new ArrayList<String>();
		try {
			File attrconf = getTemplateDir();
			SuffixFilter filter = new SuffixFilter(".properties");
			File[] properties = attrconf.listFiles(filter);
			for (int i = 0; i < properties.length; i++) {
				templateNames.add(properties[i].getName());
			}
		} catch (URISyntaxException t) {
			t.printStackTrace();
		}
		return templateNames.toArray(new String[0]);
	}

	/*
	 * Returns a mapping of the selected (checked) attributes based on stored
	 * preferences.
	 */
	private Map<String, Boolean> getSelectedFromStore() {
		Map<String, Boolean> selected = new HashMap<String, Boolean>();
		String choices = PBSPreferenceManager.getSelectedAttributes();
		if (choices != null) {
			String[] properties = choices.split(",");
			for (int i = 0; i < properties.length; i++) {
				String[] nvp = properties[i].split("=");
				Boolean v = nvp.length == 1 ? false : new Boolean(nvp[1]);
				selected.put(nvp[0], v);
			}
		}
		return selected;
	}

	/*
	 * Returns a mapping of the selected (checked) attributes based on the
	 * checked elements of the table viewer.
	 */
	private Map<String, Boolean> getSelectedFromTable() {
		Map<String, Boolean> selected = new HashMap<String, Boolean>();
		Object[] ap = preferences.getCheckedElements();
		if (ap != null) {
			for (int i = 0; i < ap.length; i++) {
				AttributePlaceholder chosenAttr = (AttributePlaceholder) ap[i];
				chosenAttr.setChecked(true);
				selected.put(chosenAttr.getName(), true);
			}
		}
		return selected;
	}

	/*
	 * The known location is fixed here.
	 */
	private File getTemplateDir() throws URISyntaxException {
		return new File(ConfigurationUtils.findFeatureDir(FEATURE_PLUGIN), CONFIG_DIR);
	}

	/*
	 * When called from the static accessor, display is turned off, because the
	 * widgets have not been constructed on the temporary page.
	 */
	private void loadSaved(boolean display) {
		String choice = PBSPreferenceManager.getTemplatePreference();
		if (choice != null && display) {
			String[] items = templates.getItems();
			for (int i = 0; i < items.length; i++) {
				if (choice.equals(items[i])) {
					comboListener.toggleIgnore();
					templates.select(i);
					comboListener.toggleIgnore();
					break;
				}
			}
		}

		loadTemplateAttributes(choice, templateAttributes);
		updateSelectedAttributes(getSelectedFromStore());
	}

	/*
	 * Reads in properties from the file, but then places them in a TreeMap for
	 * lexicographical ordering. From this map, the template map is populated
	 * with attribute placeholder objects mapped against the property names.
	 */
	private String loadTemplateAttributes(String choice, Map<String, AttributePlaceholder> map) {
		if (choice == null) {
			map.clear();
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String lineSeparator = System.getProperty("line.separator");
		try {
			File f = new File(getTemplateDir(), choice);
			if (!f.exists())
				return sb.toString();
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			Map<Object, Object> sorted = new TreeMap<Object, Object>();
			sorted.putAll(p);
			if (map != null)
				map.clear();
			AttributePlaceholder ap = null;
			for (Iterator<Map.Entry<Object, Object>> i = sorted.entrySet().iterator(); i.hasNext();) {
				Map.Entry<Object, Object> e = i.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				ap = new AttributePlaceholder();
				ap.setName(key);
				ap.setToolTip(value);
				if (map != null) {
					map.put(key, ap);
				} else {
					sb.append(key).append("=").append(value).append(lineSeparator);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return sb.toString();
	}

	/*
	 * Selects on the table viewer based on the value of the checked field on
	 * the attribute placeholder.
	 */
	private void setCheckedFromModel() {
		for (Iterator<AttributePlaceholder> i = templateAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			preferences.setChecked(ap, ap.getChecked());
		}
	}

	/*
	 * Sets the value of the checked field on the attribute placeholder based on
	 * whether the corresponding row of the table viewer is checked.
	 */
	private void setCheckedOnModel(Map<String, Boolean> selected) {
		for (Iterator<AttributePlaceholder> i = templateAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder attr = i.next();
			Boolean chosen = selected.get(attr.getName());
			if (chosen != null && chosen.booleanValue()) {
				attr.setChecked(true);
			} else {
				attr.setChecked(false);
			}
		}
	}

	/*
	 * Called after the Edit and Delete actions; refreshes the choices in the
	 * combo box.
	 */
	private void updateAvailableTemplates() {
		comboListener.toggleIgnore();
		String text = templates.getText();
		templates.setItems(getAvailableTemplates());
		templates.setText(text);
		comboListener.toggleIgnore();
	}

	/*
	 * Constructs an actual IAttribute for each placeholder, based on whether
	 * there exists a valid definition.
	 */
	private void updateSelectedAttributes(Map<String, Boolean> selected) {
		Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		for (Iterator<Map.Entry<String, AttributePlaceholder>> i = templateAttributes.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, AttributePlaceholder> e = i.next();
			String key = e.getKey();
			IAttributeDefinition<?, ?, ?> def = defs.get(key);
			if (def == null) {
				// should not happen, but we pass the error by
				continue;
			}

			AttributePlaceholder ap = e.getValue();
			try {
				ap.setAttribute(def.create());
			} catch (IllegalValueException t) {
				t.printStackTrace();
			}
		}
		setCheckedOnModel(selected);
	}

	/*
	 * Called after Edit action. Tests the contents to see that there are no
	 * invalid attribute names, based on the static definitions.
	 */
	private void validateContents(String contents) throws Throwable {
		Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		Properties p = new Properties();
		p.load(new ByteArrayInputStream(contents.getBytes()));
		for (Iterator<Object> i = p.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (!defs.containsKey(key)) {
				throw new NoSuchElementException("cannot find definition for " + key);

			}
		}
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * Static access
	 */

	/**
	 * Get the attributes known at compile time, checked against the template
	 * configuration file, from preferences.
	 * 
	 * @return map of the attributes enabled by the user.
	 */
	public static Map<String, AttributePlaceholder> getSelectedAttributes() {
		synchronized (PBSPreferencePage.class) {
			if (selectedAttributes == null) {
				PBSPreferencePage tempPage = new PBSPreferencePage();
				tempPage.init(null);
				tempPage.loadSaved(false);
				selectedAttributes = new TreeMap<String, AttributePlaceholder>();
				selectedAttributes.putAll(tempPage.templateAttributes);
			}
		}
		return selectedAttributes;
	}
}