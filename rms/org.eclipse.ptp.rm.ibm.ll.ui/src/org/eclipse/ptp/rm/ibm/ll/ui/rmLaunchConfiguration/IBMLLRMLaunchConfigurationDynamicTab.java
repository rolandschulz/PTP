/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.ui.rmLaunchConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.BigIntegerAttribute;
import org.eclipse.ptp.core.attributes.BigIntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringSetAttribute;
import org.eclipse.ptp.core.attributes.StringSetAttributeDefinition;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceManager;
import org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IIBMLLResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.ll.ui.internal.ui.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

public class IBMLLRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {
	private static final int INFO_MESSAGE = 0;
	private static final int TRACE_MESSAGE = 1;
	private static final int WARNING_MESSAGE = 2;
	private static final int ERROR_MESSAGE = 3;
	private static final int FATAL_MESSAGE = 4;
	private static final int ARGS_MESSAGE = 5;

	private static int state_trace = 0; /* 0=message off, 1=message on */
	private static int state_info = 0; /* 0=message off, 1=message on */
	private static int state_warning = 1; /* 0=message off, 1=message on */
	private static int state_error = 1; /* 0=message off, 1=message on */
	private static int state_args = 0; /* 0=message off, 1=message on */
	private static int state_fatal = 1; /* 0=message off, 1=message on */

	/*
	 * The following constants define the names of all attributes which may be
	 * known by the IBMLL proxy.
	 */
	private static final String LL_PTP_JOB_COMMAND_FILE = "LL_PTP_JOB_COMMAND_FILE";
	private static final String LL_PTP_JOB_COMMAND_FILE_TEMPLATE = "LL_PTP_JOB_COMMAND_FILE_TEMPLATE";
	private static final String LL_PTP_SUBMIT_MODE = "LL_PTP_SUBMIT_MODE";
	private static final String LL_PTP_CLASS = "LL_PTP_CLASS";
	private static final String LL_PTP_COMMENT = "LL_PTP_COMMENT";
	private static final String LL_PTP_ERROR = "LL_PTP_ERROR";
	private static final String LL_PTP_INPUT = "LL_PTP_INPUT";
	private static final String LL_PTP_OUTPUT = "LL_PTP_OUTPUT";
	private static final String LL_PTP_INITIALDIR = "LL_PTP_INITIALDIR";
	private static final String LL_PTP_JOB_NAME = "LL_PTP_JOB_NAME";
	private static final String LL_PTP_JOB_TYPE = "LL_PTP_JOB_TYPE";
	private static final String LL_PTP_NETWORK_MPI = "LL_PTP_NETWORK_MPI";
	private static final String LL_PTP_NETWORK_LAPI = "LL_PTP_NETWORK_LAPI";
	private static final String LL_PTP_NETWORK_MPI_LAPI = "LL_PTP_NETWORK_MPI_LAPI";
	private static final String LL_PTP_REQUIREMENTS = "LL_PTP_REQUIREMENTS";
	private static final String LL_PTP_RESOURCES = "LL_PTP_RESOURCES";
	private static final String LL_PTP_SHELL = "LL_PTP_SHELL";
	private static final String LL_PTP_TASK_GEOMETRY = "LL_PTP_TASK_GEOMETRY";
	private static final String LL_PTP_BULK_XFER = "LL_PTP_BULK_XFER";
	private static final String LL_PTP_LARGE_PAGE = "LL_PTP_LARGE_PAGE";
	private static final String LL_PTP_NODE_MIN = "LL_PTP_NODE_MIN";
	private static final String LL_PTP_NODE_MAX = "LL_PTP_NODE_MAX";
	private static final String LL_PTP_BLOCKING = "LL_PTP_BLOCKING";
	private static final String LL_PTP_TOTAL_TASKS = "LL_PTP_TOTAL_TASKS";
	private static final String LL_PTP_WALLCLOCK_HARD = "LL_PTP_WALLCLOCK_HARD";
	private static final String LL_PTP_WALLCLOCK_SOFT = "LL_PTP_WALLCLOCK_SOFT";
	private static final String LL_PTP_TASKS_PER_NODE = "LL_PTP_TASKS_PER_NODE";
	private static final String LL_PTP_EXECUTABLE = "LL_PTP_EXECUTABLE";
	private static final String LL_PTP_ENVIRONMENT = "LL_PTP_ENVIRONMENT";
	private static final String LL_PTP_MAX_INT = "LL_PTP_MAX_INT";

	/*
	 * End of attribute name list.
	 */
	private static final String ENABLE_STATE = "ENABLE_STATE";
	private static final RMLaunchValidation success = new RMLaunchValidation(
			true, "");

	private static final int LL_PTP_JOB_COMMAND_FILE_SELECTOR = 18;
	private static final int LL_PTP_SUBMIT_MODE_RADIOBOX = 100;
	private static final int LL_PTP_JOB_COMMAND_FILE_TEMPLATE_SELECTOR = 19;
	private static final int LL_PTP_JOB_TYPE_SELECTOR = 20;

	private static final int KBYTE = 1024;
	private static final int MBYTE = 1024 * 1024;
	private static final int GBYTE = 1024 * 1024 * 1024;

	private boolean ignoreModifyEvents = false;
	private EventMonitor eventMonitor;
	private Composite mainPanel;
	private TabFolder tabbedPane;
	private ILaunchConfigurationWorkingCopy currentLaunchConfig;
	private IResourceManager currentRM;
	private BooleanRowWidget llSubmitMode;
	private boolean allFieldsValid = true;
	private String errorMessage;
	private Vector<Object> activeWidgets;
	private IRemoteConnection remoteConnection;
	private IRemoteServices remoteService;
	private IRemoteUIServices remoteUIService;
	private Shell parentShell;

	private Composite generalTabPane = null;
	private Composite schedulingBasicTabPane = null;
	private Composite schedulingRequirementsTabPane = null;
	private Composite schedulingResourcesTabPane = null;
	private Composite runtimeTabPane = null;
	private Composite nodesNetworkTabPane = null;
	private Composite limitsTabPane = null;

	private FileSelectorRowWidget llJobCommandFile = null;
	private FileSelectorRowWidget llJobCommandFileTemplate = null;

	/*
	 * Widgets for General Tab
	 */
	private TextRowWidget llComment = null;
	private TextRowWidget llJobName = null;
	private ComboRowWidget llJobType = null;
	/*
	 * Widgets for Scheduling (Basic) Tab
	 */
	private TextRowWidget llClass = null;
	private ComboRowWidget llLargePage = null;
	/*
	 * Widgets for Scheduling (Requirements) Tab
	 */
	private TextRowWidget llRequirements = null;
	/*
	 * Widgets for Scheduling (Resources) Tab
	 */
	private TextRowWidget llResources = null;
	/*
	 * Widgets for Runtime
	 */
	private TextRowWidget llError = null;
	private TextRowWidget llInput = null;
	private TextRowWidget llOutput = null;
	private TextRowWidget llInitialDir = null;
	private TextRowWidget llShell = null;
	private TextRowWidget llExecutable = null;
	private TextRowWidget llEnvironment = null;
	/*
	 * Widgets for Nodes/Network Tab
	 */
	private TextRowWidget llBlocking = null;
	private ComboRowWidget llBulkxfer = null;
	private TextRowWidget llNetwork_mpi = null;
	private TextRowWidget llNetwork_lapi = null;
	private TextRowWidget llNetwork_mpi_lapi = null;
	private TextRowWidget llNodeMin = null;
	private TextRowWidget llNodeMax = null;
	private TextRowWidget llTaskGeometry = null;
	private TextRowWidget llTasksPerNode = null;
	private TextRowWidget llTotalTasks = null;
	/*
	 * Widgets for Limits Tab Note: llWallClockLimitHard and
	 * llWallClockLimitSoft are duplicated here.
	 */
	private TextRowWidget llWallClockLimitHard = null;
	private TextRowWidget llWallClockLimitSoft = null;

	Preferences myPreferences = null;
	/**
	 * Exception class intended for use in validating fields within this panel.
	 * When a validation error occurs, the validation code should create and
	 * throw a ValidationException, which is intended to be caught by the top
	 * level validation method.
	 */
	public class ValidationException extends Exception {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private ValidationException() {
			throw new IllegalAccessError(
					"ValidationException default constructor should not be called");
		}

		/**
		 * Create a ValidationException with error message
		 *
		 * @param message
		 *            The error message
		 */
		public ValidationException(String message) {
			super(message);
		}
	}

	/**
	 * Internal class which handles events of interest to this panel
	 */
	private class EventMonitor implements ModifyListener, SelectionListener {
		public EventMonitor() {
			print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
					+ ":EventMonitor entered.");
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":EventMonitor returning.");
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
					+ ":widgetDefaultSelected entered.");
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":widgetDefaultSelected returning.");
		}

		/**
		 * Handle events sent when registered buttons are clicked
		 */
		public void widgetSelected(SelectionEvent e) {
			Object widgetData;

			print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
					+ ":widgetSelected entered.");
			// Determine which button was clicked based on the data value stored
			// in that button object
			// and handle processing for that button.
			widgetData = e.widget.getData(WidgetAttributes.BUTTON_ID);
			if (widgetData == null) {
				// A widget other than a file selector browse button was
				// clicked. Just call
				// fireContentsChanged to drive enabling the Apply/Revert
				// buttons. Since the widget is supposed
				// to be a button widget, no validation of user data is needed.
				fireContentsChanged();
			} else {

				switch (((Integer) widgetData).intValue()) {
				case LL_PTP_JOB_COMMAND_FILE_SELECTOR:
					getInputFile(llJobCommandFile,
							"File.llJobCommandFileTitle",
							"llJobCommandFilePath");
					break;
				case LL_PTP_JOB_COMMAND_FILE_TEMPLATE_SELECTOR:
					getInputFile(llJobCommandFileTemplate,
							"File.llJobCommandFileTemplateTitle",
							"llJobCommandFileTemplatePath");
					break;
				case LL_PTP_SUBMIT_MODE_RADIOBOX:
					setLaunchPanelMode();
					validateAllFields();
					break;
				case LL_PTP_JOB_TYPE_SELECTOR:
					validateAllFields();
					break;
				}

			}
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":widgetSelected returning.");
		}

		/**
		 * Handle events sent when registered Text and Combo widgets have their
		 * text field modified.
		 */
		public void modifyText(ModifyEvent e) {
			print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
					+ ":modifyText entered.");
			// Text and Combo widgets send ModifyEvents any time their text
			// value is modified, including
			// when the value is modified by a setText() call. The only time
			// ModifyEvents are of interest is
			// when the user has entered text. Code which calls setText() on a
			// widget should set the
			// ignoreModifyEvents before calling setText() and reset
			// ignoreModifyEvents after the call.
			setFieldValidationRequired((Widget) e.getSource());
			if (!ignoreModifyEvents) {
				validateAllFields();
			}

			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":modifyText returning.");
		}
	}

	public IBMLLRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":IBMLLRMLaunchConfigurationDynamicTab entered.");
		myPreferences = IBMLLPreferenceManager.getPreferences();

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_TRACE_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_trace = 1;
		} else {
			state_trace  = 0;
		}

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_INFO_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_info = 1;
		} else {
			state_info  = 0;
		}

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_WARNING_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_warning = 1;
		} else {
			state_warning  = 0;
		}

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_ERROR_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_error = 1;
		} else {
			state_error  = 0;
		}

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_ARGS_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_args = 1;
		} else {
			state_args  = 0;
		}

		if ((myPreferences.getString(IBMLLPreferenceConstants.GUI_FATAL_MESSAGE)).equals(IBMLLPreferenceConstants.LL_YES)) {
			state_fatal = 1;
		} else {
			state_fatal  = 0;
		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":IBMLLRMLaunchConfigurationDynamicTab returning.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.ptp.rmsystem.IResourceManager,
	 *      org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm,
			IPQueue queue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":canSave entered.");
		if (allFieldsValid) {
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":canSave returning.");
			return success;
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":canSave returning.");
		return new RMLaunchValidation(false, errorMessage);
	}

	/**
	 * Get the directory path from the launch configuration
	 *
	 * @param attrName
	 *            Launch configuration attribute name for this directory
	 * @return Directory path
	 */
	private String getFileDialogPath(String attrName) {
		String dir;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getFileDialogPath entered.");
		dir = "/";
		if (currentLaunchConfig != null) {
			try {
				dir = currentLaunchConfig.getAttribute(attrName, "/");
			} catch (CoreException e) {
				dir = "/";
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getFileDialogPath returning.");
		return dir;
	}

	/**
	 * Save directory path in the launch configuration
	 *
	 * @param attrName
	 *            Launch configuration attribute name for this directory
	 * @param path
	 *            Directory path
	 */
	private void saveFileDialogPath(String attrName, String path) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":saveFileDialogPath entered.");
		if (currentLaunchConfig != null) {
			currentLaunchConfig.setAttribute(attrName, path);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":saveFileDialogPath returning.");
	}

	/**
	 * Display a file selector dialog prompting the user for the path of an
	 * input file. If the user clicks 'open', then set the pathname into the
	 * text field of the specified FileSelector object.
	 *
	 * @param selector
	 *            The FileSelector object to hold path name
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getInputFile(FileSelectorRowWidget selector, String titleID,
			String pathAttrID) {
		String selectedFile = null;
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getInputFile entered.");
		if (remoteUIService != null) {
			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			selectedFile = fmgr.browseFile(parentShell, Messages.getString(titleID),
							getFileDialogPath(pathAttrID)).toString();
		}
		if (selectedFile != null) {
			saveFileDialogPath(pathAttrID, selectedFile);
			selector.setPath(selectedFile);
			selector.setFocus();
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getInputFile returning.");
	}

	/**
	 * Display a file selector dialog prompting the user for the path of an
	 * output file. If the user clicks 'save', then set the pathname into the
	 * text field of the specified FileSelector object.
	 *
	 * @param selector
	 *            The FileSelector object to hold path name
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getOutputFile(FileSelectorRowWidget selector,
			String titleID, String pathAttrID) {
		String selectedFile = null;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getOutputFile entered.");
		if (remoteUIService != null) {
			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			selectedFile = fmgr.browseFile(parentShell, Messages.getString(titleID),
							getFileDialogPath(pathAttrID)).toString();
		}
		if (selectedFile != null) {
			saveFileDialogPath(pathAttrID, selectedFile);
			selector.setPath(selectedFile);
			selector.setFocus();
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getOutputFile returning.");
	}

	/**
	 * Display a directory selector dialog prompting the user for the pathname
	 * of a directory. If the user clocks 'ok', then set the pathname into the
	 * text field of the specified FileSelector.
	 *
	 * @param selector
	 *            FileSelector object to be updated
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getDirectory(FileSelectorRowWidget selector, String titleID,
			String pathAttrID) {
		String selectedFile = null;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getDirectory entered.");
		if (remoteUIService != null) {
			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			selectedFile = fmgr.browseDirectory(parentShell, Messages.getString(titleID),
							getFileDialogPath(pathAttrID)).toString();
		}
		if (selectedFile != null) {
			String parentDir;

			parentDir = new File(selectedFile).getParent();
			if (parentDir == null) {
				saveFileDialogPath(pathAttrID, "/");
			} else {
				saveFileDialogPath(pathAttrID, parentDir);
			}
			selector.setPath(selectedFile);
			selector.setFocus();
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getDirectory returning.");
	}

	/**
	 * Mark the validation state for the specified widget to indicate that the
	 * widget value must be validated.
	 *
	 * @param source
	 *            The widget to validate.
	 */
	protected void setFieldValidationRequired(Widget source) {
		// Iterate thru the list of widgets looking for the widget which needs
		// to be validated. When found, set
		// that widget's validation state to indicate validation is needed.
		// Widget class needs
		// to be checked since although these widgets perform similar functions,
		// they do not comprise a set of
		// logically related widgets that can be easily organized in a class
		// hierarchy.
		Iterator<Object> i;

		i = activeWidgets.iterator();
		while (i.hasNext()) {
			Object widget;

			widget = i.next();
			if (widget instanceof BooleanRowWidget) {
				if (((BooleanRowWidget) widget).isMatchingWidget(source)) {
					((BooleanRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof CheckboxRowWidget) {
				if (((CheckboxRowWidget) widget).isMatchingWidget(source)) {
					((CheckboxRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof ComboRowWidget) {
				if (((ComboRowWidget) widget).isMatchingWidget(source)) {
					((ComboRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof DualFieldRowWidget) {
				if (((DualFieldRowWidget) widget).isMatchingWidget(source)) {
					((DualFieldRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof FileSelectorRowWidget) {
				if (((FileSelectorRowWidget) widget).isMatchingWidget(source)) {
					((FileSelectorRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof TextRowWidget) {
				if (((TextRowWidget) widget).isMatchingWidget(source)) {
					((TextRowWidget) widget).setValidationRequired();
					return;
				}
			}
		}
	}

	/**
	 * Mark all widget's validation state to indicate that the widget value has
	 * changed, meaning validation is required.
	 */
	private void markAllFieldsChanged() {
		Iterator<Object> i;

		i = activeWidgets.iterator();
		while (i.hasNext()) {
			Object widget;

			widget = i.next();
			if (widget instanceof BooleanRowWidget) {
				((BooleanRowWidget) widget).setValidationRequired();
			} else if (widget instanceof CheckboxRowWidget) {
				((CheckboxRowWidget) widget).setValidationRequired();
			} else if (widget instanceof ComboRowWidget) {
				((ComboRowWidget) widget).setValidationRequired();
			} else if (widget instanceof DualFieldRowWidget) {
				((DualFieldRowWidget) widget).setValidationRequired();
			} else if (widget instanceof FileSelectorRowWidget) {
				((FileSelectorRowWidget) widget).setValidationRequired();
			} else if (widget instanceof TextRowWidget) {
				((TextRowWidget) widget).setValidationRequired();
			}
		}
	}

	/**
	 * Disable the tab pane widget and all children of the tab pane. Calling
	 * setEnabled(false) on the tab pane widget disables the tab pane and
	 * prevents interaction with child widgets, but does not change the visible
	 * state of the child widget. This method changes the state of all widgets
	 * to correctly indicate they are disabled.
	 *
	 * @param widget
	 *            The widget to be disabled.
	 */
	private void disableTabPaneWidget(Control widget) {
		Control children[];

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":disableTabPaneWidget entered.");
		// For any Composite widget, recursively call this method for each child
		// of the Composite. This must be
		// done before disabling the Composite since disabling the Composite
		// also marks its children disabled
		// and the real enable/disable state of the child cannot be preserved.
		if (widget instanceof Composite) {
			children = ((Composite) widget).getChildren();
			for (int i = 0; i < children.length; i++) {
				disableTabPaneWidget(children[i]);
			}
		}
		// Remember the current state of the widget, then disable it.
		widget.setData(ENABLE_STATE, Boolean.valueOf(widget.isEnabled()));
		widget.setEnabled(false);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":disableTabPaneWidget returning.");
	}

	/**
	 * Restore widget back to its previous enable/disable state
	 *
	 * @param widget
	 *            The widget whose state is to be restored.
	 */
	private void restoreTabPaneWidgetState(Control widget) {
		Control children[];
		Boolean state;
		boolean enableFlag;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":restoreTabPaneWidgetState entered.");
		// Get widget's previous enable/disable state. If there is no saved
		// state, such as when initially
		// creating the parallel tab in basic mode, then enable the widget.
		state = (Boolean) widget.getData(ENABLE_STATE);
		if (state == null) {
			enableFlag = true;
		} else {
			enableFlag = state.booleanValue();
		}
		widget.setEnabled(enableFlag);
		// Recursively call this method to handle children of a Composite
		// widget. Note that ordering of processing
		// here does not matter since enabling a Composite widget does not
		// automatically enable its children.
		if (widget instanceof Composite) {
			children = ((Composite) widget).getChildren();
			for (int i = 0; i < children.length; i++) {
				restoreTabPaneWidgetState(children[i]);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":restoreTabPaneWidgetState returning.");
	}

	/**
	 * Restore widget back to its previous enable/disable state
	 *
	 * @param widget
	 *            The widget whose state is to be restored.
	 */
	private void enableTabPaneWidgetState(Control widget) {
		Control children[];
		Boolean state;
		boolean enableFlag;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":enableTabPaneWidgetState entered.");
		// Enable the widget.
		widget.setEnabled(true);
		// Recursively call this method to handle children of a Composite
		// widget. Note that ordering of processing
		// here does not matter since enabling a Composite widget does not
		// automatically enable its children.
		if (widget instanceof Composite) {
			children = ((Composite) widget).getChildren();
			for (int i = 0; i < children.length; i++) {
				enableTabPaneWidgetState(children[i]);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":enableTabPaneWidgetState returning.");
	}

	/**
	 * Set launch panel mode based on llSubmitMode setting. If checked, then set
	 * submit mode, where the user supplies a LL setup script. Otherwise set
	 * basic mode, where the user chooses LL options from a tabbed dialog panel.
	 */
	protected void setLaunchPanelMode() {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setLaunchPanelMode entered.");
		if (llSubmitMode != null) {
			if (llSubmitMode.getValue().equals("Advanced")) {
				if (llJobCommandFile != null) {
					llJobCommandFile.setEnabled(true);
				}
				if (llJobCommandFileTemplate != null) {
					llJobCommandFileTemplate.setEnabled(false);
				}
				disableTabPaneWidget(tabbedPane);
			} else {
				if (llJobCommandFile != null) {
					llJobCommandFile.setEnabled(false);
				}
				if (llJobCommandFileTemplate != null) {
					llJobCommandFileTemplate.setEnabled(true);
				}
				enableTabPaneWidgetState(tabbedPane);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setLaunchPanelMode returning.");

	}

	/**
	 * Create a text widget in the tabbed view. The text field spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return TextRowWidget entry widget
	 */
	private TextRowWidget createTextWidget(Composite parent,
			IResourceManager rm, String id) {
		TextRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createTextWidget entered.");
		widget = null;
		attr = rm.getAttributeDefinition(id);
		if (attr != null) {
			widget = new TextRowWidget(parent, id, attr);
			widget.addModifyListener(eventMonitor);
			widget.setValidationRequired();
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createTextWidget returning.");
		return widget;
	}

	/**
	 * Create a text widget in the tabbed view. The text field spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id1
	 *            Attribute id for first rm attribute this widget represents
	 * @param id2
	 *            Attribute id for second rm attribute this widget represents
	 * @return Text entry widget
	 */
	private DualFieldRowWidget createDualField(Composite parent,
			IResourceManager rm, String id1, String id2) {
		DualFieldRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr1;
		IAttributeDefinition<?, ?, ?> attr2;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createDualField entered.");
		widget = null;
		attr1 = rm.getAttributeDefinition(id1);
		attr2 = rm.getAttributeDefinition(id2);
		if ((attr1 != null) && (attr2 != null)) {
			widget = new DualFieldRowWidget(parent, id1, id2, attr1, attr2);
			widget.addModifyListener(eventMonitor);
			widget.setValidationRequired();
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createDualField returning.");
		return widget;
	}

	/**
	 * Create a checkbox widget in the tabbed view. The checkbox is in column 2
	 * and column 3 is a filler (Label) widget. To ensure consistent alignment,
	 * this method allocates extra horizontal space to the 2nd column. The label
	 * and tooltip text are obtained from the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Checkbox button for this attribute
	 */
	private CheckboxRowWidget createCheckbox(Composite parent,
			IResourceManager rm, String id) {
		CheckboxRowWidget widget;
		StringAttributeDefinition attrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createCheckbox entered.");
		widget = null;
		attrDef = (StringAttributeDefinition) rm.getAttributeDefinition(id);
		if (attrDef != null) {
			widget = new CheckboxRowWidget(parent, id, attrDef);
			widget.setValidationRequired();
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createCheckbox returning.");
		return widget;
	}

	/**
	 * Create a radio button pair in the tabbed view. The label, button labels,
	 * and tooltip text are obtained from the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Checkbox button for this attribute
	 */
	private BooleanRowWidget createBooleanOption(Composite parent,
			IResourceManager rm, String id) {
		BooleanRowWidget widget;
		StringSetAttributeDefinition attrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createBooleanOption entered.");
		widget = null;
		attrDef = (StringSetAttributeDefinition) rm.getAttributeDefinition(id);
		if (attrDef != null) {
			widget = new BooleanRowWidget(parent, id, attrDef,
					LL_PTP_SUBMIT_MODE_RADIOBOX);
			widget.setValidationRequired();
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createBooleanOption returning.");
		return widget;
	}

	/**
	 * Create a text field and pushbutton in this row. The text field is in
	 * column 2 and the pushbutton in column 3. The user either fills in the
	 * text field with a pathname, or clicks the button to pop up a file
	 * selector dialog that then fills in the text field. To ensure consistent
	 * alignment, this method allocates extra horizontal space to the 2nd
	 * column. The label and tooltip text are obtained from the attribute
	 * definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @param selectorID
	 *            Identifier used to identify the browse button associated with
	 *            this widget
	 * @return Text entry field for this attribute
	 */
	private FileSelectorRowWidget createFileSelector(Composite parent,
			IResourceManager rm, String id, int selectorID) {
		FileSelectorRowWidget widget;
		StringAttributeDefinition attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createFileSelector entered.");
		widget = null;
		attr = (StringAttributeDefinition) rm.getAttributeDefinition(id);
		if (attr != null) {
			widget = new FileSelectorRowWidget(parent, id, selectorID, attr);
			widget.setData(id);
			widget.setValidationRequired();
			widget.addModifyListener(eventMonitor);
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createFileSelector returning.");
		return widget;
	}

	/**
	 * Create a combobox widget in the tabbed view. The widget spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return ComboRowWidget used by this attribute
	 */
	private ComboRowWidget createCombobox(Composite parent,
			IResourceManager rm, String id, int selector_id) {
		ComboRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createCombobox entered.");
		widget = null;
		attr = rm.getAttributeDefinition(id);
		if (attr != null) {
			widget = new ComboRowWidget(parent, id, attr, true, selector_id);
			widget.setValidationRequired();
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createCombobox returning.");
		return widget;
	}

	/**
	 * Create an editable combobox in the tabbed view. The widget spans columns
	 * 2 and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 *
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param rm
	 *            Resource manager used by this launch config
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Editable ComboRowWidget used by this attribute
	 */
	private ComboRowWidget createEditableCombobox(Composite parent,
			IResourceManager rm, String id, int selector_id) {
		ComboRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createEditableCombobox entered.");
		widget = null;
		attr = rm.getAttributeDefinition(id);
		if (attr != null) {
			widget = new ComboRowWidget(parent, id, attr, false, selector_id);
			widget.setValidationRequired();
			widget.addSelectionListener(eventMonitor);
			widget.addModifyListener(eventMonitor);
			activeWidgets.add(widget);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createEditableCombobox returning.");
		return widget;
	}

	/**
	 * Reset all widgets within this pane to null as part of panel
	 * initialization. Depending on OS and operation mode (with or without
	 * LoadLeveler), some widgets will not appear on the panels, where the set
	 * of attribute definitions sent by the proxy determines that set. New
	 * widgets will be generated only when a corresponding attribute definition
	 * is sent by the proxy. Any code which accesses a widget should ensure the
	 * widget is not null before accessing the widget object.
	 */
	private void clearAllWidgets() {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":clearAllWidgets entered.");
		llComment = null;
		llJobName = null;
		llJobType = null;
		llClass = null;
		llLargePage = null;
		llWallClockLimitHard = null;
		llWallClockLimitSoft = null;
		llRequirements = null;
		llResources = null;
		llError = null;
		llInput = null;
		llOutput = null;
		llInitialDir = null;
		llShell = null;
		llExecutable = null;
		llEnvironment = null;
		llBlocking = null;
		llBulkxfer = null;
		llNetwork_mpi = null;
		llNetwork_lapi = null;
		llNetwork_mpi_lapi = null;
		llNodeMin = null;
		llNodeMax = null;
		llTaskGeometry = null;
		llTasksPerNode = null;
		llTotalTasks = null;

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":clearAllWidgets returning.");

	}

	/**
	 * Create the layout object for a pane in the TabFolder
	 *
	 * @return Layout for use in the tabbed pane
	 */
	private Layout createTabPaneLayout() {
		GridLayout layout;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createTabPaneLayout entered.");
		layout = new GridLayout(4, false);
		layout.marginWidth = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 4;
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createTabPaneLayout returning.");
		return layout;
	}

	/**
	 * Create the tasks tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createGeneralTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createGeneralTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		generalTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(generalTabPane);
		tab.setText(Messages.getString("GeneralTab.title"));
		generalTabPane.setLayout(createTabPaneLayout());
		llComment = createTextWidget(generalTabPane, rm, LL_PTP_COMMENT);
		llJobName = createTextWidget(generalTabPane, rm, LL_PTP_JOB_NAME);
		llJobType = createCombobox(generalTabPane, rm, LL_PTP_JOB_TYPE, LL_PTP_JOB_TYPE_SELECTOR);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createGeneralTab returning.");
	}

	/**
	 * Create the I/O tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createSchedulingBasicTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createSchedulingBasicTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		schedulingBasicTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(schedulingBasicTabPane);
		tab.setText(Messages.getString("SchedulingBasicTab.title"));
		schedulingBasicTabPane.setLayout(createTabPaneLayout());
		llClass = createTextWidget(schedulingBasicTabPane, rm, LL_PTP_CLASS);
		llLargePage = createCombobox(schedulingBasicTabPane, rm,
				LL_PTP_LARGE_PAGE, 0);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createSchedulingBasicTab returning.");
	}

	/**
	 * Create the diagnostics tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createSchedulingRequirementsTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createSchedulingRequirementsTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		schedulingRequirementsTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(schedulingRequirementsTabPane);
		tab.setText(Messages.getString("SchedulingRequirementsTab.title"));
		schedulingRequirementsTabPane.setLayout(createTabPaneLayout());
		llRequirements = createTextWidget(schedulingRequirementsTabPane, rm,
				LL_PTP_REQUIREMENTS);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createSchedulingRequirementsTab returning.");
	}

	/**
	 * Create the debug tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createSchedulingResourcesTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createSchedulingResourcesTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		schedulingResourcesTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(schedulingResourcesTabPane);
		tab.setText(Messages.getString("SchedulingResourcesTab.title"));
		schedulingResourcesTabPane.setLayout(createTabPaneLayout());
		llResources = createTextWidget(schedulingResourcesTabPane, rm,
				LL_PTP_RESOURCES);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createSchedulingResourcesTab returning.");
	}

	/**
	 * Create the system resources tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createRuntimeTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createRuntimeTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		runtimeTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(runtimeTabPane);
		tab.setText(Messages.getString("RuntimeTab.title"));
		runtimeTabPane.setLayout(createTabPaneLayout());
		llError = createTextWidget(runtimeTabPane, rm, LL_PTP_ERROR);
		llOutput = createTextWidget(runtimeTabPane, rm, LL_PTP_OUTPUT);
		llInput = createTextWidget(runtimeTabPane, rm, LL_PTP_INPUT);
		llExecutable = createTextWidget(runtimeTabPane, rm, LL_PTP_EXECUTABLE);
		llInitialDir = createTextWidget(runtimeTabPane, rm, LL_PTP_INITIALDIR);
		llEnvironment = createTextWidget(runtimeTabPane, rm, LL_PTP_ENVIRONMENT);
		llShell = createTextWidget(runtimeTabPane, rm, LL_PTP_SHELL);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createRuntimeTab returning.");
	}

	/**
	 * Create the node allocation tab of the attributes pane
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createNodesNetworkTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createNodesNetworkTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		nodesNetworkTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(nodesNetworkTabPane);
		tab.setText(Messages.getString("NodesNetworkTab.title"));
		nodesNetworkTabPane.setLayout(createTabPaneLayout());
		llBlocking = createTextWidget(nodesNetworkTabPane, rm, LL_PTP_BLOCKING);
		llBulkxfer = createCombobox(nodesNetworkTabPane, rm,
				LL_PTP_BULK_XFER, 0);
		llNetwork_mpi = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_NETWORK_MPI);
		llNetwork_lapi = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_NETWORK_LAPI);
		llNetwork_mpi_lapi = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_NETWORK_MPI_LAPI);
		llNodeMin = createTextWidget(nodesNetworkTabPane, rm, LL_PTP_NODE_MIN);
		llNodeMax = createTextWidget(nodesNetworkTabPane, rm, LL_PTP_NODE_MAX);
		llTaskGeometry = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_TASK_GEOMETRY);
		llTasksPerNode = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_TASKS_PER_NODE);
		llTotalTasks = createTextWidget(nodesNetworkTabPane, rm,
				LL_PTP_TOTAL_TASKS);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createNodesNetworkTab returning.");
	}

	/**
	 * Create the first performance tab of the attributes pane. Due to the
	 * number of performance related attributes, there are two performance tabs.
	 *
	 * @param rm
	 *            resource manager associated with this launch configuration
	 */
	private void createLimitsTab(IResourceManager rm) {
		TabItem tab;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createLimitsTab entered.");
		tab = new TabItem(tabbedPane, SWT.NONE);
		limitsTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(limitsTabPane);
		tab.setText(Messages.getString("LimitsTab.title"));
		limitsTabPane.setLayout(createTabPaneLayout());
		llWallClockLimitHard = createTextWidget(limitsTabPane, rm,
				LL_PTP_WALLCLOCK_HARD);
		llWallClockLimitSoft = createTextWidget(limitsTabPane, rm,
				LL_PTP_WALLCLOCK_SOFT);

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createLimitsTab returning.");
	}

	/**
	 * Create a pane containing the submit mode radio box
	 *
	 * @param rm
	 *            The resource manager associated with this launch configuration
	 */
	private void createModeBox(IResourceManager rm) {
		GridData gd;
		GridLayout layout;
		Composite pane;

		pane = new Composite(mainPanel, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginWidth = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 4;
		pane.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		pane.setLayoutData(gd);

		llSubmitMode = createBooleanOption(pane, rm, LL_PTP_SUBMIT_MODE);
		if (llSubmitMode != null) {
			llSubmitMode.addSelectionListener(eventMonitor);
			llJobCommandFile = createFileSelector(pane, rm,
					LL_PTP_JOB_COMMAND_FILE, LL_PTP_JOB_COMMAND_FILE_SELECTOR);
			llJobCommandFileTemplate = createFileSelector(pane, rm,
					LL_PTP_JOB_COMMAND_FILE_TEMPLATE,
					LL_PTP_JOB_COMMAND_FILE_TEMPLATE_SELECTOR);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createModeBox returning.");

	}

	/**
	 * This method creates all of the GUI elements of the resource-manager
	 * specific pane within the parallel tab of the launch configuration dialog.
	 *
	 * @param parent
	 *            This control's parent
	 * @param rm
	 *            The resource manager associated with this launch configuration
	 * @param queue
	 *            Currently selected queue
	 */
	public void createControl(Composite parent, IResourceManager rm,
			IPQueue queue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":createControl entered.");
		IIBMLLResourceManagerConfiguration config;
		IRemoteConnectionManager connMgr;

		config = (IIBMLLResourceManagerConfiguration) ((AbstractResourceManager) rm)
				.getConfiguration();
		remoteService = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				config.getRemoteServicesId());
		remoteUIService = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteService);
		connMgr = remoteService.getConnectionManager();
		remoteConnection = connMgr.getConnection(config.getConnectionName());
		parentShell = parent.getShell();
		clearAllWidgets();
		activeWidgets = new Vector<Object>();
		eventMonitor = new EventMonitor();
		mainPanel = new Composite(parent, SWT.NONE);
		mainPanel.setLayout(new GridLayout(1, false));
		createModeBox(rm);
		tabbedPane = new TabFolder(mainPanel, SWT.TOP);
		createGeneralTab(rm);
		createSchedulingBasicTab(rm);
		createSchedulingRequirementsTab(rm);
		createSchedulingResourcesTab(rm);
		createRuntimeTab(rm);
		createNodesNetworkTab(rm);
		createLimitsTab(rm);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":createControl returning.");
		currentRM = rm;
	}

	/**
	 * Add an attribute to the set of launch attributes if not same as default
	 * sent from proxy
	 *
	 * @param rm
	 *            The resource manager associated with the current launch
	 *            configuration
	 * @param config
	 *            The current launch configuration
	 * @param attrs
	 *            The attributes vector containing the set of launch attributes
	 * @param attrName
	 *            The name of the attribute to be added to launch attributes
	 */
	private void addAttribute(IResourceManager rm, ILaunchConfiguration config,
			Vector<StringAttribute> attrs, String attrName) {
		String attrValue;
		String defaultValue;
		StringAttribute attr;
		StringAttributeDefinition attrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":addAttribute entered.");
		if (rm.getAttributeDefinition(attrName) != null) {
			try {
				attrValue = config.getAttribute(attrName, "");
			} catch (CoreException e) {
				attrValue = "";
			}

			defaultValue = getAttrDefaultValue(rm, attrName);

			if ((attrValue.trim().length() > 0)
					&& ((!attrValue.equals(defaultValue))
							|| (attrName.equals("LL_PTP_JOB_TYPE"))
							|| (attrName
									.equals("LL_PTP_JOB_COMMAND_FILE_TEMPLATE"))
							|| (attrName.equals("LL_PTP_SUBMIT_MODE"))
							|| (attrName.equals("LL_PTP_CLASS"))
							|| (attrName.equals("LL_PTP_INPUT"))
							|| (attrName.equals("LL_PTP_OUTPUT"))
							|| (attrName.equals("LL_PTP_ERROR"))
							|| (attrName.equals("LL_PTP_ENVIRONMENT"))
							|| (attrName.equals("LL_PTP_JOB_TYPE")) || (attrName
							.equals("LL_PTP_JOB_COMMAND_FILE")))) {
				attrDef = new StringAttributeDefinition(attrName, "", "",
						false, "");
				attr = new StringAttribute(attrDef, attrValue);
				attrs.add(attr);
			}

		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":addAttribute returning.");
	}

	/**
	 * Get the set of attributes to be used as launch attributes
	 *
	 * @param rm
	 *            The resource manager associated with the current launch
	 *            configuration
	 * @param queue
	 *            The current queue (not used for LL since there is only a
	 *            single queue)
	 * @param configuration
	 *            The current launch configuration
	 */
	public IAttribute<String, StringAttribute, StringAttributeDefinition>[] getAttributes(
			IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration, String mode) throws CoreException {
		Vector<StringAttribute> attrs;
		StringAttribute attrArray[];

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getAttributes entered.");
		attrs = new Vector<StringAttribute>();
		attrArray = new StringAttribute[0];

		Map<String, StringAttribute> allAttrs;
		Set<String> attrNames;
		Iterator<String> i;
		String name;

		allAttrs = configuration.getAttributes();
		attrNames = allAttrs.keySet();
		i = attrNames.iterator();
		while (i.hasNext()) {
			name = i.next();
			if (name.startsWith("LL_PTP_")) {
				addAttribute(rm, configuration, attrs, name);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getAttributes returning.");
		return attrs.toArray(attrArray);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getControl()
	 */
	public Control getControl() {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getControl entered.");
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getControl returning.");
		return mainPanel;
	}

	/**
	 * Get the default value for an attribute from the resource manager, giving
	 * preference to a user override of the default value (which the user does
	 * by setting of the corresponding environment variable before starting the
	 * proxy.) The user's override is passed to the front end by the proxy as a
	 * string attribute where the leading 'MP_' of the attribute name is
	 * replaced with 'EN_'
	 *
	 * @param rm
	 *            The resource manager currently associated with the launch
	 *            configuration
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrLocalDefaultValue(IResourceManager rm, String attrName) {
		IAttributeDefinition<?, ?, ?> attrDef;
		String localDefaultEnv;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getAttrLocalDefaultValue entered.");
		localDefaultEnv = attrName.replaceFirst("^MP_", "EN_");
		attrDef = rm.getAttributeDefinition(localDefaultEnv);
		if (attrDef != null) {
			try {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrLocalDefaultValue returning.");
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
			}
		}
		attrDef = rm.getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrLocalDefaultValue returning.");
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrLocalDefaultValue returning.");
				return "";
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getAttrLocalDefaultValue returning.");
		return "";
	}

	/**
	 * Get the default value for an attribute from the resource manager
	 *
	 * @param rm
	 *            The resource manager currently associated with the launch
	 *            configuration
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrDefaultValue(IResourceManager rm, String attrName) {
		IAttributeDefinition<?, ?, ?> attrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getAttrDefaultValue entered.");
		attrDef = rm.getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrDefaultValue returning.");
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrDefaultValue returning.");
				return "";
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getAttrDefaultValue returning.");
		return "";
	}

	/**
	 * Get the attribute value for the specified attribute. If the value is
	 * stored in the launch configuration, that value is used. Otherwise the
	 * default value from the resource manager is used.
	 *
	 * @param config
	 *            The current launch configuration
	 * @param rm
	 *            The resource manager currently associated with the launch
	 *            configuration
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrInitialValue(ILaunchConfiguration config,
			IResourceManager rm, String attrName) {
		String value;
		IAttributeDefinition<?, ?, ?> rmAttrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getAttrInitialValue entered.");
		try {
			value = config.getAttribute(attrName, "_no_value_");
		} catch (CoreException e) {
			value = "_no_value_";
		}
		if (value.equals("_no_value_")) {
			// Get the default attribute value, where that default may be the
			// value
			// specified by the user as an override to the LL default value.
			value = getAttrLocalDefaultValue(rm, attrName);
		}
		// If an attribute is defined as an integer attribute, then determine if
		// the attribute is evenly divisible by 1G, 1M or 1K, and if so, then
		// convert the
		// value accordingly. The tests must be done largest to smallest so that
		// the largest conversion factor is used. The attribute value may
		// already be
		// in the form 999[gGmMkK]. Converting that to a long will result in a
		// NumberFormatException, so a try/catch block is required, where the
		// string
		// value is returned in thecase of a NumberFormatException
		rmAttrDef = rm.getAttributeDefinition(attrName);
		if (rmAttrDef instanceof IntegerAttributeDefinition
				|| rmAttrDef instanceof BigIntegerAttributeDefinition) {
			long intVal;

			try {
				intVal = Long.valueOf(value);
				if (intVal != 0) {
					if ((intVal % GBYTE) == 0) {
						print_message(TRACE_MESSAGE, "<<< "
								+ this.getClass().getName()
								+ ":getAttrInitialValue returning.");
						return String.valueOf(intVal / GBYTE) + "G";
					} else {
						if ((intVal % MBYTE) == 0) {
							print_message(TRACE_MESSAGE, "<<< "
									+ this.getClass().getName()
									+ ":getAttrInitialValue returning.");
							return String.valueOf(intVal / MBYTE) + "M";
						} else {
							if ((intVal % KBYTE) == 0) {
								print_message(TRACE_MESSAGE, "<<< "
										+ this.getClass().getName()
										+ ":getAttrInitialValue returning.");
								return String.valueOf(intVal / KBYTE) + "K";
							}
						}
					}
				}
			} catch (NumberFormatException e) {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getAttrInitialValue returning.");
				return value;
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":getAttrInitialValue returning.");
		return value;
	}

	/**
	 * Set checkbox to checked state if attribute has value equal to checkValue
	 * otherwise set it unchecked
	 *
	 * @param checkbox
	 *            The checkbox to set
	 * @param attrValue
	 *            The attribute value to check
	 * @param checkValue
	 *            The value corresponding to a checked checkbox
	 */
	private void setValue(CheckboxRowWidget checkbox, String attrValue,
			String checkValue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (checkbox != null) {
			if (attrValue.equals(checkValue)) {
				checkbox.setSelection(true);
			} else {
				checkbox.setSelection(false);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	private void setValue(BooleanRowWidget option, String checkValue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (option != null) {
			option.setValue(checkValue);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	/**
	 * Set the text value for a Text widget to the specified value if the widget
	 * is not null.
	 *
	 * @param widget
	 *            The widget to set
	 * @param value
	 *            The value to be set
	 */
	private void setValue(TextRowWidget widget, String value) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (widget != null) {
			widget.setValue(value);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	/**
	 * Set the text value for a DualField widget to the specified value if the
	 * widget is not null.
	 *
	 * @param widget
	 *            The widget to set
	 * @param value1
	 *            The value to be set in field 1
	 * @param value2
	 *            The value to be set in field 2
	 */
	private void setValue(DualFieldRowWidget widget, String value1,
			String value2) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (widget != null) {
			widget.setValue(value1, value2);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	/**
	 * Set the text value for a ComboRowWidget to the specified value if the
	 * widget is not null.
	 *
	 * @param widget
	 *            The widget to set
	 * @param value
	 *            The value to be set
	 */
	private void setValue(ComboRowWidget widget, String value) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (widget != null) {
			widget.setValue(value);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	/**
	 * Set the pathname for a file selector if the file selector is not null
	 *
	 * @param selector
	 *            File selector to be updated
	 * @param path
	 *            Pathname
	 */
	private void setValue(FileSelectorRowWidget selector, String path) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setValue entered.");
		if (selector != null) {
			selector.setPath(path);
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setValue returning.");
	}

	/**
	 * Set initial values for all widgets
	 *
	 * @param configuration
	 *            The current launch configuration
	 * @param rm
	 *            The resource manager currently associated with the launch
	 *            configuration
	 */
	private void setInitialValues(ILaunchConfiguration config,
			IResourceManager rm) {
		Object widget;
		Iterator<Object> i;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setInitialValues entered.");
		// All Text and Combo widgets have ModifyListeners registered on them in
		// order to invoke field validation
		// when contents of the widget's text field change. The ModifyListener
		// is invoked for each widget as a
		// result of calling the setText() method for the widget. The resulting
		// ModifyEvent results in calling
		// validateAllFields(), which invokesfireContentsChanged(), which in
		// turn results in performApply() being
		// invoked. The performApply() method saves all widget's values into the
		// current launch configuration.
		// Since at the time that setInitialValues is called, widgets may be
		// blank, this results in storing
		// blanks for all attributes in the launch configuration, wiping out the
		// saved values in the launch
		// configuration.
		// To avoid this, ignoreModifyEvents is set, so that the ModifyListener
		// does nothing.
		ignoreModifyEvents = true;
		i = activeWidgets.iterator();
		while (i.hasNext()) {
			widget = i.next();
			if (widget instanceof FileSelectorRowWidget) {
				setValue((FileSelectorRowWidget) widget, getAttrInitialValue(
						config, rm, (String) ((FileSelectorRowWidget) widget)
								.getData()));
			} else if (widget instanceof DualFieldRowWidget) {
				setValue((DualFieldRowWidget) widget, getAttrInitialValue(
						config, rm, (String) ((DualFieldRowWidget) widget)
								.getData1()), getAttrInitialValue(config, rm,
						(String) ((DualFieldRowWidget) widget).getData2()));
			} else if (widget instanceof TextRowWidget) {
				setValue((TextRowWidget) widget, getAttrInitialValue(config,
						rm, ((TextRowWidget) widget)
								.getData(WidgetAttributes.ATTR_NAME)));
			} else if (widget instanceof ComboRowWidget) {
				setValue((ComboRowWidget) widget, getAttrInitialValue(config,
						rm, ((ComboRowWidget) widget)
								.getData(WidgetAttributes.ATTR_NAME)));
			} else if (widget instanceof BooleanRowWidget) {
				setValue((BooleanRowWidget) widget, getAttrInitialValue(config,
						rm, ((BooleanRowWidget) widget).getData()));
			} else if (widget instanceof CheckboxRowWidget) {
				setValue((CheckboxRowWidget) widget, getAttrInitialValue(
						config, rm, ((CheckboxRowWidget) widget)
								.getData(WidgetAttributes.ATTR_NAME)), "yes");
			}

		}
		setLaunchPanelMode();
		// Setup complete, re-enable ModifyListener
		ignoreModifyEvents = false;
		markAllFieldsChanged();
		// All fields need to be validated because a different resource manager
		// may have been selected, and therefore
		// values saved in the launch configuration, such as pathnames may no
		// longer be valid.
		validateAllFields();

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setInitialValues returning.");
	}

	/**
	 * Set state for widgets based on dependencies between widget values. At the
	 * point this method is called, all widgets are in enabled state, so it is
	 * only necessary to disable widgets.
	 */
	private void setInitialWidgetState() {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setInitialWidgetState entered.");

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setInitialWidgetState returning.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#initializeFrom(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.ptp.rmsystem.IResourceManager,
	 *      org.eclipse.ptp.core.IPQueue,
	 *      org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control,
			IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":initializeFrom entered.");
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			currentLaunchConfig = (ILaunchConfigurationWorkingCopy) configuration;
		}
		setInitialValues(configuration, rm);
		setInitialWidgetState();
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":initializeFrom returning.");
		return success;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 *      org.eclipse.ptp.rmsystem.IResourceManager,
	 *      org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":isValid entered.");
		if (allFieldsValid) {
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":isValid returning.");
			return success;
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":isValid returning.");
		return new RMLaunchValidation(false, errorMessage);
	}

	/**
	 * Store the value from a Text widget into the specified launch
	 * configuration if the widget is not null
	 *
	 * @param config
	 *            The launch configuration
	 * @param rm
	 *            The resource manager currently used by the launch
	 *            configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			IResourceManager rm, String attr, TextRowWidget control) {
		IAttributeDefinition<?, ?, ?> attrDef;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			String attrValue;

			attrDef = rm.getAttributeDefinition(attr);
			try {
				if ((attrDef instanceof IntegerAttributeDefinition)
						|| (attrDef instanceof BigIntegerAttributeDefinition)) {
					attrValue = getIntegerValue(control.getValue());
				} else {
					attrValue = control.getValue();
				}
				config.setAttribute(attr, attrValue);
			} catch (NumberFormatException e) {
				// If the field has an invalid numeric value, then don't save it
				// in the launch configuration
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	/**
	 * Store the value from a DialField widget into the specified launch
	 * configuration if the widget is not null
	 *
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			String attr1, String attr2, DualFieldRowWidget control) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			String value[];

			value = control.getValue();
			config.setAttribute(attr1, value[0].trim());
			config.setAttribute(attr2, value[1].trim());
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	/**
	 * Store the value from a ComboRowWidget into the specified launch
	 * configuration if the widget is not null
	 *
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			String attr, ComboRowWidget control) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	/**
	 * Store the value from a file selector into the specified launch
	 * configuration if the file selector is not null
	 *
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			String attr, FileSelectorRowWidget control) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	/**
	 * Store the value from a Button widget into the specified launch
	 * configuration if the widget is not null
	 *
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 * @param trueVal
	 *            The value to set if the button is selected
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			String attr, CheckboxRowWidget control, String trueVal,
			String falseVal) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			config.setAttribute(attr, (control.getSelection() ? trueVal
					: falseVal));
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	private void setConfigAttr(ILaunchConfigurationWorkingCopy config,
			String attr, BooleanRowWidget control) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setConfigAttr entered.");
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setConfigAttr returning.");
	}

	/**
	 * Save the values entered in this panel in the launch configuration
	 *
	 * @param config
	 */
	private void saveConfigurationData(ILaunchConfigurationWorkingCopy config,
			IResourceManager rm) {
		Object widget;
		Iterator<Object> i;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":saveConfigurationData entered.");
		i = activeWidgets.iterator();
		while (i.hasNext()) {
			widget = i.next();
			if (widget instanceof TextRowWidget) {
				setConfigAttr(config, rm, ((TextRowWidget) widget)
						.getData(WidgetAttributes.ATTR_NAME),
						(TextRowWidget) widget);
			} else if (widget instanceof ComboRowWidget) {
				setConfigAttr(config, (String) ((ComboRowWidget) widget)
						.getData(WidgetAttributes.ATTR_NAME),
						(ComboRowWidget) widget);
			} else if (widget instanceof CheckboxRowWidget) {
				setConfigAttr(config, (String) ((CheckboxRowWidget) widget)
						.getData(WidgetAttributes.ATTR_NAME),
						(CheckboxRowWidget) widget, "yes", "no");
			} else if (widget instanceof BooleanRowWidget) {
				setConfigAttr(config, (String) ((BooleanRowWidget) widget)
						.getData(), (BooleanRowWidget) widget);
			} else if (widget instanceof FileSelectorRowWidget) {
				setConfigAttr(config, (String) ((FileSelectorRowWidget) widget)
						.getData(), (FileSelectorRowWidget) widget);
			} else if (widget instanceof DualFieldRowWidget) {
				setConfigAttr(config, (String) ((DualFieldRowWidget) widget)
						.getData1(), (String) ((DualFieldRowWidget) widget)
						.getData2(), (DualFieldRowWidget) widget);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":saveConfigurationData returning.");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 *      org.eclipse.ptp.rmsystem.IResourceManager,
	 *      org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(
			ILaunchConfigurationWorkingCopy configuration, IResourceManager rm,
			IPQueue queue) {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":performApply entered.");
		currentLaunchConfig = configuration;
		saveConfigurationData(configuration, rm);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":performApply returning.");
		return new RMLaunchValidation(true, "");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 *      org.eclipse.ptp.rmsystem.IResourceManager,
	 *      org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(
			ILaunchConfigurationWorkingCopy config, IResourceManager rm,
			IPQueue queue) {
		IAttribute<?, ?, ?> rmAttrs[];

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":setDefaults entered.");
		currentLaunchConfig = config;
		rmAttrs = rm.getAttributes();
		for (int i = 0; i < rmAttrs.length; i++) {
			try {
				config.setAttribute(rmAttrs[i].getDefinition().getId(),
						rmAttrs[i].getDefinition().create().getValueAsString());
			} catch (IllegalValueException e) {
			}
		}
		// setDefaultValues(config, rm);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":setDefaults returning.");
		return success;
	}

	/**
	 * Handle validation of all fields in the tabbed pane of the launch
	 * configuration's parallel tab.
	 */
	protected void validateAllFields() {
		// This method is the top level driver for validating the fields in the
		// tabbed pane. It is called when a field in the tabbed pane is
		// modified, via a ModifyListener registered on
		// each text or editable combobox widget It calls a validation method
		// for each tab in the pane. Validation
		// should be done in tab order, left to right,
		// All validation is done in the scope of a try block which catches any
		// ValidationException propagated up
		// from lower level methods. The idea is to validate the fields within
		// tab order left to right, then within each
		// tab, from top to bottom. Each validation method will throw a
		// ValidationException if that field fails
		// validation, where the thrown exception will stop further validation
		// of the pane. This structure allows fields
		// to be easily moved to another pane, or to reorder fields within a
		// pane. Using an exception to terminate
		// validation also avoids cluttering the logic of the validation method
		// with deeply nested 'if ... else ...'
		// logic.
		// If all fields are valid, then the allFieldsValid flag is set so that
		// the isValid() and canSave() methods can
		// easily check panel validity.
		// Validation of valid dependencies between fields should be performed
		// as a second step after all fields have
		// been individually validated for correct values.
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateAllFields entered.");
		try {
			if (llSubmitMode != null && llSubmitMode.getValue().equals("Advanced")) {
				validateInputPath(llJobCommandFile, "Invalid.llJobCommandFile");
			} else {
				validateInputPath(llJobCommandFileTemplate,
						"Invalid.llJobCommandFileTemplate");
				validateGeneralTab();
				validateSchedulingBasicTab();
				validateSchedulingRequirementsTab();
				validateSchedulingResourcesTab();
				validateRuntimeTab();
				validateNodesNetworkTab();
				validateLimitsTab();
			}
			allFieldsValid = true;
		} catch (ValidationException e) {
			errorMessage = e.getMessage();
			allFieldsValid = false;
		}
		fireContentsChanged();
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateAllFields returning.");
	}

	/**
	 * Validate fields in task specification tab
	 *
	 * @throws ValidationException
	 */
	private void validateGeneralTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateGeneralTab entered.");
		validateAlphaNumeric(llJobName, "Invalid.llJobName");
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateGeneralTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in the I/O tab, top to
	 * bottom
	 *
	 * @throws ValidationException
	 */
	private void validateSchedulingBasicTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateSchedulingBasicTab entered.");
		validateAlphaNumeric(llClass, "Invalid.llClass");
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateSchedulingBasicTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in diagnostic tab, top to
	 * bottom
	 *
	 * @throws ValidationException
	 */
	private void validateSchedulingRequirementsTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateSchedulingRequirementsTab entered.");
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateSchedulingRequirementsTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in the debug tab, top to
	 * bottom
	 *
	 * @throws ValidationException
	 */
	private void validateSchedulingResourcesTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateSchedulingResourcesTab entered.");
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateSchedulingResourcesTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in the system resources
	 * tab, top to bottom
	 *
	 * @throws ValidationException
	 */
	private void validateRuntimeTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateRuntimeTab entered.");
//no checks will be performed on the following widgets at this time
//		llError = createTextWidget(runtimeTabPane, rm, LL_PTP_ERROR);
//		llOutput = createTextWidget(runtimeTabPane, rm, LL_PTP_OUTPUT);
//		llInput = createTextWidget(runtimeTabPane, rm, LL_PTP_INPUT);
//		llExecutable = createTextWidget(runtimeTabPane, rm, LL_PTP_EXECUTABLE);
//		llInitialDir = createTextWidget(runtimeTabPane, rm, LL_PTP_INITIALDIR);
//		llEnvironment = createTextWidget(runtimeTabPane, rm, LL_PTP_ENVIRONMENT);
//		llShell = createTextWidget(runtimeTabPane, rm, LL_PTP_SHELL);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateRuntimeTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in the node allocation
	 * tab, top to bottom
	 *
	 * @throws ValidationException
	 */
	private void validateNodesNetworkTab() throws ValidationException {

		String tpn = "";
		String tpn_default = "";
		String tt = "";
		String tt_default = "";
		String jt = "";
		String jt_default = "";
		String nm = "";
		String nm_default = "";
		String nmx = "";
		String nmx_default = "";
		String b = "";
		String b_default = "";
		String tg = "";
		String tg_default = "";

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNodesNetworkTab entered.");
		if (llTaskGeometry != null) {
			tg = llTaskGeometry.getValue();
			if (tg.length() > 0) {
			  tg_default = llTaskGeometry.getDefaultValue();
			}
		}

		if (llTasksPerNode != null) {
			tpn = llTasksPerNode.getValue();
			if (tpn.length() > 0) {
			  tpn_default = llTasksPerNode.getDefaultValue();
			}
		}

		if (llTotalTasks != null) {
			tt = llTotalTasks.getValue();
			if (tt.length() > 0) {
			  tt_default = llTotalTasks.getDefaultValue();
			}
		}

		if (llJobType != null) {
			jt = llJobType.getValue();
			if (jt.length() > 0) {
			  jt_default = llJobType.getDefaultValue();
			}
		}

		if (llNodeMin != null) {
			nm = llNodeMin.getValue();
			if (nm.length() > 0) {
			  nm_default = llNodeMin.getDefaultValue();
			}
		}

		if (llNodeMax != null) {
			nmx = llNodeMax.getValue();
			if (nmx.length() > 0) {
			  nmx_default = llNodeMax.getDefaultValue();
			}
		}

		if (llBlocking != null) {
			b = llBlocking.getValue();
			if (b.length() > 0) {
			  b_default = llBlocking.getDefaultValue();
			}
		}

		if ((tpn.equals(tpn_default) == false) &&
		   (tt.equals(tt_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTasksPerNode_llTotalTasks"));
		}

		if ((tg.equals(tg_default) == false) &&
		    (jt.equalsIgnoreCase("Parallel") == false)) {
		    throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llJobType"));
		    }

		if ((tg.equals(tg_default) == false) &&
				(tpn.equals(tpn_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llTasksPerNode"));
		}

		if ((tg.equals(tg_default) == false) &&
				(tt.equals(tt_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llTotalTasks"));
		}

		if ((tg.equals(tg_default) == false) &&
				(nm.equals(nm_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llNodeMin"));
		}

		if ((tg.equals(tg_default) == false) &&
				(nmx.equals(nmx_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llNodeMax"));
		}

		if ((tg.equals(tg_default) == false) &&
				(b.equals(b_default) == false)) {
			throw new ValidationException(Messages.getString("Invalid.llTaskGeometry_llBlocking"));
		}

		validatePositiveOrUnlimitedNumeric(llBlocking,"Invalid.llBlocking");

//no checks will be performed on the following widgets at this time
//		llNetwork_mpi = createTextWidget(nodesNetworkTabPane, rm,
//				LL_PTP_NETWORK_MPI);
//		llNetwork_lapi = createTextWidget(nodesNetworkTabPane, rm,
//				LL_PTP_NETWORK_LAPI);
//		llNetwork_mpi_lapi = createTextWidget(nodesNetworkTabPane, rm,
//				LL_PTP_NETWORK_MPI_LAPI);
//		validateNumericRange(llNodeMin, LL_PTP_NODE_MIN, "Invalid.llNodeMin");
//		validateNumericRange(llNodeMax, LL_PTP_NODE_MAX, "Invalid.llNodeMax");
//no checks will be performed on the following widgets at this time
//		llTaskGeometry = createTextWidget(nodesNetworkTabPane, rm,
//				LL_PTP_TASK_GEOMETRY);

		validateNumericMinMax(llNodeMin, LL_PTP_NODE_MIN, "Invalid.llNodeMin", llNodeMax, LL_PTP_NODE_MAX, "Invalid.llNodeMax"); 
		validatePositiveNumeric(llTasksPerNode,"Invalid.llTasksPerNode");
		validatePositiveNumeric(llTotalTasks,"Invalid.llTotalTasks");

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNodesNetworkTab returning.");
	}

	/**
	 * Validate all text and editable combobox fields in performance tab 1, top
	 * to bottom
	 *
	 * @throws ValidationException
	 */
	private void validateLimitsTab() throws ValidationException {
		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateLimitsTab entered.");
		validateClockValue(llWallClockLimitHard,"Invalid.llWallClockLimitHard");
		validateClockValue(llWallClockLimitSoft,"Invalid.llWallClockLimitSoft");


		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateLimitsTab returning.");
	}

	/**
	 * Validate that an input file is accessible
	 *
	 * @param selector
	 *            The file selector containing the pathname
	 * @param errorID
	 *            id of the error string used if file is inaccessible
	 * @throws ValidationException
	 */
	private void validateInputPath(FileSelectorRowWidget selector,
			String errorID) throws ValidationException {
		String path;

		if ((selector != null) && selector.isEnabled()
				&& selector.isValidationRequired()) {
			path = selector.getValue();
			if (path.length() == 0) {
				selector.resetValidationState();
				return;
			}
			try {
				validateInputPath(path, errorID);
				selector.resetValidationState();
			} catch (ValidationException e) {
				selector.setFieldInError();
				throw e;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Validate that an output file is accessible
	 *
	 * @param selector
	 *            The file selector containing the pathname
	 * @param errorID
	 *            id of the error string used if the file is inaccessible
	 * @throws ValidationException
	 */
//	private void validateOutputPath(FileSelectorRowWidget selector,
//			String errorID) throws ValidationException {
//		String path;
//
//		if ((selector != null) && selector.isEnabled()
//				&& selector.isValidationRequired()) {
//			path = selector.getValue();
//			if (path.length() == 0) {
//				selector.resetValidationState();
//				return;
//			}
//			try {
//				validateOutputPath(path, errorID);
//				selector.resetValidationState();
//			} catch (ValidationException e) {
//				selector.setFieldInError();
//				throw e;
//			}
//		}
//	}

	/**
	 * Validate that in input file is accessible
	 *
	 * @param path
	 *            Pathname of the input file
	 * @param errorID
	 *            id of the error string used if the file is inaccessible
	 * @throws ValidationException
	 * @throws IOException
	 */
	private void validateInputPath(String path, String errorID)
			throws ValidationException, IOException {
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		testPath = new Path(path);
		if (!testPath.isValidPath(path)) {
			throw new ValidationException(Messages.getString(errorID));
		}
		remoteResource = remoteService.getFileManager(remoteConnection)
				.getResource(testPath, new NullProgressMonitor());
		fileInfo = remoteResource.fetchInfo();
		if ((!fileInfo.exists()) || (fileInfo.isDirectory())) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Validate that an output file is accessible
	 *
	 * @param path
	 *            Pathname of the output file
	 * @param errorID
	 *            id of the error string used if the file is not accessible
	 * @throws ValidationException
	 * @throws IOException
	 */
	private void validateOutputPath(String path, String errorID)
			throws ValidationException, IOException {
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		testPath = new Path(path);
		if (!testPath.isValidPath(path)) {
			throw new ValidationException(Messages.getString(errorID));
		}
		remoteResource = remoteService.getFileManager(remoteConnection)
				.getResource(testPath, new NullProgressMonitor());
		fileInfo = remoteResource.fetchInfo();
		if (fileInfo.isDirectory()) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Validate that the directory pathname is valid
	 *
	 * @param selector
	 *            File selector containing the directory name
	 * @param errorID
	 *            id of the error string used if the directory is invalid
	 * @throws ValidationException
	 * @throws IOException
	 */
	private void validateDirectory(FileSelectorRowWidget selector,
			String errorID) throws ValidationException, IOException {
		String path;
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		if ((selector != null) && selector.isEnabled()
				&& selector.isValidationRequired()) {
			path = selector.getValue();
			try {
				if (path.length() == 0) {
					selector.resetValidationState();
					return;
				}

				testPath = new Path(path);
				if (!testPath.isValidPath(path)) {
					throw new ValidationException(Messages.getString(errorID));
				}
				remoteResource = remoteService.getFileManager(remoteConnection)
						.getResource(testPath, new NullProgressMonitor());
				fileInfo = remoteResource.fetchInfo();
				if (!fileInfo.isDirectory()) {
					throw new ValidationException(Messages
							.getString(errorID));
				}
				selector.resetValidationState();
			} catch (ValidationException e) {
				selector.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Verify that the value selected or entered in an editable combobox is a
	 * valid value, as determined by checking the attribute definition for the
	 * attribute.
	 *
	 * @param widget
	 *            The combobox to be checked
	 * @param attrName
	 *            The attribute name
	 * @return true if the value is a valid selection, false otherwise
	 */
	private boolean isValidListSelection(ComboRowWidget widget, String attrName) {
		StringSetAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		StringSetAttribute attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":isValidListSelection entered.");
		attrDef = (StringSetAttributeDefinition) currentRM
				.getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				attr = attrDef.create(widget.getValue());
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":isValidListSelection returning.");
				return true;
			} catch (IllegalValueException e) {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":isValidListSelection returning.");
				return false;
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":isValidListSelection returning.");
		return true;
	}

	/**
	 * Validate that the minValue is less than or equal to the maxValue.
	 *
	 * @param controlMin
	 *            The min Text widget to be verified
	 * @param attrNameMin
	 *            The name of the min attribute
	 * @param errorIDMin
	 *            The id of the error message used if controlMin validation fails
	 * @param controlMax
	 *            The max Text widget to be verified
	 * @param attrNameMax
	 *            The name of the max attribute
	 * @param errorIDMax
	 *            The id of the error message used if controlMax validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericMinMax(TextRowWidget controlMin, String attrNameMin, String errorIDMin,
			TextRowWidget controlMax, String attrNameMax,
			String errorIDMax) throws ValidationException {
		String strMin = "", strMax = "";
		int iMin = 0, iMax = 0;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNumericMinMax entered.");
				
		if (controlMin != null) {
			strMin = controlMin.getValue();
			if (strMin.length() > 0) {
				iMin = validatePositiveNumeric(strMin, errorIDMin);
			}
		}
		if (controlMax != null) {
			strMax = controlMax.getValue();
			if (strMax.length() > 0) {
				iMax = validatePositiveNumeric(strMax, errorIDMax);
			}
		}
		if ((strMin.length() > 0) && (strMax.length() > 0)) {
			if (iMin > iMax) {
				throw new ValidationException(Messages.getString("Invalid.llNodeMinMax"));
			}
		}
		
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNumericMinMax returning.");
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 *
	 * @param control
	 *            The Text widget to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(TextRowWidget control, String attrName,
			String errorID) throws ValidationException {
		String value;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNumericRange entered.");
		if (control != null) {
			value = control.getValue();
			if (value.length() > 0) {
				validateNumericRange(value, attrName, errorID);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNumericRange returning.");
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 *
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(String value, String attrName,
			String errorID) throws ValidationException {
		int testValue;
		int len;
		char suffix;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNumericRange entered.");
		len = value.length();
		suffix = value.charAt(len - 1);
		if (Character.isDigit(suffix)) {
			try {
				testValue = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} else {
			try {
				testValue = Integer.valueOf(value.substring(0, len - 1));
				if ((suffix == 'G') || (suffix == 'g')) {
					testValue = testValue * GBYTE;
				} else if ((suffix == 'M') || (suffix == 'm')) {
					testValue = testValue * MBYTE;
				} else if ((suffix == 'K') || (suffix == 'k')) {
					testValue = testValue * KBYTE;
				} else {
					throw new ValidationException(Messages.getString(errorID));
				}
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString(errorID));
			}
		}
		validateNumericRange(testValue, attrName, errorID);
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNumericRange returning.");
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 *
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(int value, String attrName, String errorID)
			throws ValidationException {
		IntegerAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		IntegerAttribute attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNumericRange entered.");
		attrDef = (IntegerAttributeDefinition) currentRM
				.getAttributeDefinition(attrName);
		try {
			attr = attrDef.create(value);
		} catch (IllegalValueException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNumericRange returning.");
	}

	/**
	 * Convert a string which may have a suffix 'k', 'm' or 'g' to it's actual
	 * numeric value, multiplying by the appropriate multiplier
	 *
	 * @param value
	 *            The number to be converted
	 * @return The converted number
	 */
	private String getIntegerValue(String value) {
		int testValue;
		int len;
		char suffix;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":getIntegerValue entered.");
		testValue = 0;
		len = value.length();
		if (len == 0) {
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":getIntegerValue returning.");
			return "";
		} else {
			suffix = value.charAt(len - 1);
			if (Character.isDigit(suffix)) {
				print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
						+ ":getIntegerValue returning.");
				return value;
			} else {
				if (len >= 2) {
					testValue = Integer.valueOf(value.substring(0, len - 1));
					if ((suffix == 'G') || (suffix == 'g')) {
						testValue = testValue * GBYTE;
					} else if ((suffix == 'M') || (suffix == 'm')) {
						testValue = testValue * MBYTE;
					} else if ((suffix == 'K') || (suffix == 'k')) {
						testValue = testValue * KBYTE;
					} else {
						print_message(TRACE_MESSAGE, "<<< "
								+ this.getClass().getName()
								+ ":getIntegerValue returning.");
						return "";
					}
				}
			}
			print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
					+ ":getIntegerValue returning.");
			return String.valueOf(testValue);
		}
	}

	/**
	 * Validate that a BigInteger value is within the range allowed for the
	 * attribute.
	 *
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateLongNumericRange(String value, String attrName,
			String errorID) throws ValidationException {
		BigIntegerAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		BigIntegerAttribute attr;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateLongNumericRange entered.");
		attrDef = (BigIntegerAttributeDefinition) currentRM
				.getAttributeDefinition(attrName);
		try {
			attr = attrDef.create(value);
		} catch (IllegalValueException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateLongNumericRange returning.");
	}

	/**
	 * Validate a String's value to verify it is within the allowed range
	 *
	 * @param value
	 *            String to be verified
	 * @param lowLimit
	 *            Low limit of range
	 * @param highLimit
	 *            High limit of range
	 * @param errorID
	 *            id of the error message used if value is not in allowable
	 *            range
	 * @throws ValidationException
	 */
	private void validateNumericRange(String value, int lowLimit,
			int highLimit, String errorID) throws ValidationException {
		int n;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateNumericRange entered.");
		try {
			n = Integer.valueOf(value);
			if ((n < lowLimit) || (n > highLimit)) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateNumericRange returning.");
	}

	/**
	 * Print a message: Info, Trace, Arg and Warning messages go to stdout.
	 * Error and Fatal messages go to stderr. *
	 */
	private void print_message(int type, String message) {
		switch (type) {
		case INFO_MESSAGE:
			if (state_info == 1) {
				System.out.println("Info: " + message);
			}
			break;
		case TRACE_MESSAGE:
			if (state_trace == 1) {
				System.out.println("Trace: " + message);
			}
			break;
		case WARNING_MESSAGE:
			if (state_warning == 1) {
				System.out.println("Warning: " + message);
			}
			break;
		case ARGS_MESSAGE:
			if (state_args == 1) {
				System.out.println("Args: " + message);
			}
			break;
		case ERROR_MESSAGE:
			if (state_error == 1) {
				System.err.println("Error: " + message);
			}
			break;
		case FATAL_MESSAGE:
			if (state_fatal == 1) {
				System.err.println("Fatal " + message);
			}
			break;
		default:
			System.out.println(message);
			break;
		}
	}

	private void validateAlphaNumeric(TextRowWidget control, String errorID) throws ValidationException {
		String value;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateAlphaNumeric entered.");
		if (control != null) {
			value = control.getValue();
			if (value.length() > 0) {
				validateAlphaNumeric(value, errorID);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateAlphaNumeric returning.");
	}

	private void validateAlphaNumeric(String value, String errorID) throws ValidationException {
		int i;
		String stripped = value.trim();

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateAlphaNumeric entered.");
		for (i=0; i<stripped.length(); i++) {
			if (stripped.matches("[a-zA-Z_0-9]*") == false) {
				throw new ValidationException(Messages.getString(errorID));
			}
		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateAlphaNumeric returning.");
	}

	private void validatePositiveOrUnlimitedNumeric(TextRowWidget control, String errorID) throws ValidationException {
		String value;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validatePositiveOrUnlimitedNumeric entered.");
		if (control != null) {
			value = control.getValue();
			if (value.trim().equalsIgnoreCase("unlimited")) {
				// nothing to do - unlimited is valid value
			} else {
			if (value.length() > 0) {
				validatePositiveOrUnlimitedNumeric(value, errorID);
			} }
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validatePositiveOrUnlimitedNumeric returning.");
	}

	private void validatePositiveOrUnlimitedNumeric(String value, String errorID) throws ValidationException {
		int n;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validatePositiveOrUnlimitedNumeric entered.");
		try {
			n = Integer.valueOf(value);
			if ((n < 1) || (n > Integer.MAX_VALUE)) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(Messages.getString(errorID));
		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validatePositiveOrUnlimitedNumeric returning.");
	}
	private void validatePositiveNumeric(TextRowWidget control, String errorID) throws ValidationException {
		String value;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validatePositiveNumeric entered.");
		if (control != null) {
			value = control.getValue();
			if (value.length() > 0) {
				validatePositiveNumeric(value, errorID);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validatePositiveNumeric returning.");
	}

	private int validatePositiveNumeric(String value, String errorID) throws ValidationException {
		int n = 0;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validatePositiveNumeric entered.");
		try {
			n = Integer.valueOf(value);
			if ((n < 0) || (n > Integer.MAX_VALUE)) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(Messages.getString(errorID));
		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validatePositiveNumeric returning.");
		return n;
	}

	private void validateClockValue(TextRowWidget control, String errorID) throws ValidationException {
		String value;

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateClockValue entered.");
		if (control != null) {
			value = control.getValue();
			if (value.length() > 0) {
				validateClockValue(value, errorID);
			}
		}
		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateClockValue returning.");
	}

	private void validateClockValue(String value, String errorID) throws ValidationException {
		int i;
		String stripped = value.trim();

		print_message(TRACE_MESSAGE, ">>> " + this.getClass().getName()
				+ ":validateClockValue entered.");
		for (i=0; i<stripped.length(); i++) {
			if (stripped.matches("[0-9]+(:[0-9]+){0,2}") == false) {
				throw new ValidationException(Messages.getString(errorID));
			}
		}

		print_message(TRACE_MESSAGE, "<<< " + this.getClass().getName()
				+ ":validateClockValue returning.");
	}
}
