/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw.toolopts;

//import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.etfw.toolopts.ToolOption;
import org.eclipse.ptp.etfw.toolopts.ToolPane;
import org.eclipse.ptp.etfw.toolopts.ToolPaneListener;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;
import org.eclipse.ptp.internal.etfw.toolopts.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A factory-class for the generation and management of External Tools and their subordinate elements
 * 
 * @author wspear
 * 
 */
public class ToolMaker {

	/**
	 * Creates ExternalTools
	 * 
	 * @param tooldef
	 *            The xml file containing the definition of one or more tool-panes
	 * @return The array of defined but uninitialized ToolPanes defined in the provided xml file
	 * @since 4.0
	 */
	public static ExternalToolProcess[] makeTools(IFileStore tooldef) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// factory.setValidating(false);
		ToolParser tparser = new ToolParser();
		try {
			try {
				factory.newSAXParser().parse(tooldef.openInputStream(EFS.NONE, null), tparser);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SAXParseException e) {
			System.err.println(Messages.ToolMaker_ErrorInWorkflowDefinition + e.getSystemId() + Messages.ToolMaker_AtLine
					+ e.getLineNumber() + Messages.ToolMaker_Column + e.getColumnNumber());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ExternalToolProcess[] tparr = new ExternalToolProcess[tparser.externalToolList.size()];
		tparser.externalToolList.toArray(tparr);
		return tparr;
	}

	/**
	 * Finishes the initialization of a ToolOption
	 * 
	 * @param toolopt
	 *            The ToolOption to be finished
	 * @return The finished ToolOption
	 */
	protected static ToolOption finishToolOption(ToolOption toolopt, String paneID) {

		// if(toolopt.type>7)
		// toolopt.type=0;
		String upname;
		if (toolopt.optName != null) {

		} else if (toolopt.optLabel != null) {
			toolopt.optName = toolopt.optLabel;
		} else {
			return null;
		}
		upname = paneID + toolopt.optID.toUpperCase();

		toolopt.confArgString = upname + ToolsOptionsConstants.TOOL_CONFIG_ARGUMENT_SUFFIX;
		toolopt.confStateString = upname + ToolsOptionsConstants.TOOL_CONFIG_STATE_SUFFIX;
		toolopt.confDefString = upname + ToolsOptionsConstants.TOOL_CONFIG_DEFAULT_SUFFIX;

		toolopt.optionLine = new StringBuffer(toolopt.optName);
		if (toolopt.type > 0) {
			toolopt.optionLine.append("=\"\"");//  +="=\"\""; //$NON-NLS-1$
		}
		toolopt.optionLine.append(' ');// +=' ';

		return toolopt;
	}

	/**
	 * Initializes a tool pane within a composite provided by the user
	 * 
	 * @param comp
	 *            The composite where the tool pane will be created
	 * @param pane
	 *            The pane containing the elements to be displayed
	 * @param browseListener
	 *            The listener for the browse buttons used in the pane
	 * @param checkListener
	 *            The listener for the check boxes and value fields used in the pane
	 */
	public static void makeToolPane(Composite comp, ToolPane pane, SelectionListener browseListener, ToolPaneListener checkListener) {
		comp.setLayout(createGridLayout(3, false, 0, 0));
		comp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		createVerticalSpacer(comp, 3);

		if (pane.displayOptions) {
			pane.showOpts = new Text(comp, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			GridData showOptGD = new GridData();
			showOptGD.horizontalAlignment = SWT.FILL;
			showOptGD.verticalAlignment = SWT.FILL;
			showOptGD.horizontalSpan = 3;
			showOptGD.grabExcessHorizontalSpace = true;
			// showOptGD.grabExcessVerticalSpace=true;
			showOptGD.minimumHeight = pane.showOpts.getLineHeight() * 3;
			showOptGD.heightHint = pane.showOpts.getLineHeight() * 3;

			pane.showOpts.setEditable(false);
			pane.showOpts.setLayoutData(showOptGD);
		}

		Composite invis = new Composite(comp, SWT.NONE);
		invis.setVisible(false);

		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		invis.setLayoutData(gridData);

		for (int i = 0; i < pane.options.length; i++) {
			if (pane.options[i].visible) {
				displayToolOption(comp, pane.options[i], pane.browseListener, checkListener);
			} else {
				displayToolOption(invis, pane.options[i], pane.browseListener, checkListener);
			}
		}
		invis.setSize(0, 0);
		invis.moveBelow(null);
		// createVerticalSpacer(comp, 3);
	}

	private static void initializeCheckLabel(Composite comp, ToolOption toolOpt) {
		if (!toolOpt.required) {
			toolOpt.unitCheck = createCheckButton(comp, toolOpt.optLabel);
			toolOpt.unitCheck.setToolTipText(toolOpt.toolTip);
		} else {
			toolOpt.reqLabel = new Label(comp, SWT.NONE);
			toolOpt.reqLabel.setText(toolOpt.optLabel);
			toolOpt.reqLabel.setToolTipText(toolOpt.toolTip);
		}
	}

	/**
	 * Initializes a single tool option
	 * 
	 * @param comp
	 *            The composite where the ToolOption is to be displayed
	 * @param toolOpt
	 *            The ToolOption to be displayed
	 * @param browseListener
	 *            The listener that defines behavior for this tool's browse buttons, if any
	 * @param checkListener
	 *            The listener that defines behavior for this tool's check boxe and value field, if any
	 */
	protected static void displayToolOption(Composite comp, ToolOption toolOpt, SelectionListener browseListener,
			ToolPaneListener checkListener) {

		initializeCheckLabel(comp, toolOpt);

		// If this option is a boolean or a toggle we don't need any widgets but the checkbox
		if (toolOpt.type == ToolOption.BOOL || toolOpt.type == ToolOption.TOGGLE) {

			new Label(comp, SWT.NULL);
			new Label(comp, SWT.NULL);
		}
		// If this option is text only we just need the argbox
		else if (toolOpt.type == ToolOption.TEXT) {
			toolOpt.argbox = new Text(comp, SWT.BORDER | SWT.SINGLE);
			toolOpt.argbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toolOpt.argbox.setToolTipText(toolOpt.valueToolTip);
			if (checkListener != null) {
				toolOpt.argbox.addModifyListener((ModifyListener) checkListener);
			}

			new Label(comp, SWT.NULL);
		}
		// This is a widget with a browse button, so build it accordingly
		else if (toolOpt.type == ToolOption.DIR || toolOpt.type == ToolOption.FILE) {
			toolOpt.argbox = new Text(comp, SWT.BORDER | SWT.SINGLE);
			toolOpt.argbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toolOpt.argbox.setToolTipText(toolOpt.valueToolTip);

			if (checkListener != null) {
				toolOpt.argbox.addModifyListener((ModifyListener) checkListener);
			}
			toolOpt.browser = createPushButton(comp, Messages.ToolMaker_Browse);
			if (browseListener != null) {
				toolOpt.browser.addSelectionListener(browseListener);
			}
		} else if (toolOpt.type == ToolOption.COMBO) {
			toolOpt.combopt = new Combo(comp, SWT.NULL);
			toolOpt.combopt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toolOpt.combopt.setToolTipText(toolOpt.valueToolTip);
			toolOpt.combopt.setItems(toolOpt.items);
			toolOpt.combopt.select(toolOpt.defNum);
			if (checkListener != null) {
				toolOpt.combopt.addModifyListener((ModifyListener) checkListener);
			}
			new Label(comp, SWT.NULL);
		} else if (toolOpt.type == ToolOption.NUMBER) {
			toolOpt.numopt = new Spinner(comp, SWT.NULL);
			toolOpt.numopt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			toolOpt.numopt.setToolTipText(toolOpt.valueToolTip);
			toolOpt.numopt.setMaximum(toolOpt.maxNum);
			toolOpt.numopt.setMinimum(toolOpt.minNum);
			if (checkListener != null) {
				toolOpt.numopt.addModifyListener((ModifyListener) checkListener);
			}

			new Label(comp, SWT.NULL);
		}

		if (checkListener != null && toolOpt.unitCheck != null) {
			toolOpt.unitCheck.addSelectionListener(checkListener);
		}
	}

	/**
	 * Creates a checkbox
	 * 
	 * @param parent
	 *            The composite where the checkbox is created
	 * @param label
	 *            The label of the checkbox
	 * @return The created checkbox
	 */
	protected static Button createCheckButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		button.setFont(parent.getFont());
		// SWTUtil.setButtonDimensionHint(button);
		return button;
	}

	/**
	 * Creates a button
	 * 
	 * @param parent
	 *            The composite where the button is created
	 * @param label
	 *            The label of the button
	 * @return The created button
	 */
	protected static Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());

		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);
		// SWTUtil.setButtonDimensionHint(button);
		return button;
	}

	/**
	 * Creates either a file or directory broswer window, depending on the type of ToolOption, and puts the selected value in the
	 * option's value field
	 * 
	 * @param opt
	 *            The option whose value is being browsed for
	 */
	public static void optBrowse(ToolOption opt) {

		String dialogText = opt.valueToolTip;
		if (dialogText == null) {
			dialogText = opt.optLabel;
			if (dialogText == null) {
				dialogText = ""; //$NON-NLS-1$
			}
		}

		if (opt.type == ToolOption.DIR) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(dialogText);

			String correctPath = opt.argbox.getText();// getFieldContent(tauArch.getText());
			if (correctPath != null) {
				IFileStore path = EFS.getLocalFileSystem().getStore(new Path(correctPath));
				if (path.fetchInfo().exists()) {
					dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path.getParent().toURI().getPath());
				}
			}

			String selectedPath = dialog.open();
			if (selectedPath != null) {
				opt.argbox.setText(selectedPath);
			}
		} else if (opt.type == ToolOption.FILE) {
			FileDialog dialog = new FileDialog(getShell());
			if (opt.fileLike != null) {
				String[] filter = { opt.fileLike };
				dialog.setFilterExtensions(filter);
			}
			dialog.setText(dialogText);

			String correctPath = opt.argbox.getText();// getFieldContent(tauArch.getText());
			if (correctPath != null) {
				IFileStore path = EFS.getLocalFileSystem().getStore(new Path(correctPath));
				if (path.fetchInfo().exists()) {
					dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path.getParent().toURI().getPath());
				}
			}

			String selectedPath = dialog.open();
			if (selectedPath != null) {
				opt.argbox.setText(selectedPath);
			}
		}

	}

	/**
	 * Creates a new shell from the workbench's current display
	 * 
	 * @return A fresh shell
	 */
	protected static Shell getShell() {
		Display thisDisplay = PlatformUI.getWorkbench().getDisplay();// .getCurrent();//.getDefault();

		Shell s = thisDisplay.getActiveShell();
		if (s == null) {
			Shell[] shells = thisDisplay.getShells();
			s = shells[0];
		}

		return new Shell(s);
	}

	/**
	 * Returns a new GridLayout
	 * 
	 * @param columns
	 *            Number of columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected static GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected static GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	protected static void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		label.setFont(comp.getFont());
	}
}
