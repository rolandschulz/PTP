/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.internal.gem.views;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.internal.gem.GemPlugin;
import org.eclipse.ptp.internal.gem.messages.Messages;
import org.eclipse.ptp.internal.gem.util.GemUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.ViewPart;

/**
 * The GEM Console View.
 */
public class GemConsole extends ViewPart {

	// The ID for this view
	public static final String ID = "org.eclipse.ptp.gem.views.GemConsole"; //$NON-NLS-1$

	private Action clearConsoleAction;
	private Action writeToLocalFileAction;
	private Action getHelpAction;
	private Action terminateOperationAction;
	private MessageConsole msgConsole;
	private StyledText txtConViewer;
	private Thread disableTerminateButtonThread;
	private Thread clearConsoleThread;

	/**
	 * Constructor.
	 * 
	 * @param none
	 */
	public GemConsole() {
		super();
	}

	/**
	 * Brings this ViewPart to the front and gives it focus.
	 * 
	 * @param none
	 * @return none
	 */
	public void activate() {
		final Thread activationThread = new Thread() {
			@Override
			public void run() {
				final IWorkbench wb = PlatformUI.getWorkbench();
				final IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				final IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					page.activate(GemConsole.this);
				}
			}
		};

		// We need to switch to the thread that is allowed to change the UI
		Display.getDefault().syncExec(activationThread);
	}

	/**
	 * Changes status of the terminate process button (Action) to disabled.
	 * 
	 * @param none
	 * @return void
	 */
	public void cancel() {
		Display.getDefault().syncExec(this.disableTerminateButtonThread);
	}

	/**
	 * Clears all text from the Gem Console via the main UI thread.
	 * 
	 * @param none
	 * @return void
	 */
	public void clear() {
		Display.getDefault().syncExec(this.clearConsoleThread);
	}

	/*
	 * Calls finer grained methods, populating the view action bar.
	 */
	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Callback that allows us to create the viewer and initialize it.
	 * 
	 * @param parent
	 *            The parent Composite to this View.
	 * @return void
	 */
	@Override
	public void createPartControl(Composite parent) {

		// Initializations
		this.txtConViewer = new StyledText(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		this.txtConViewer.setFont(new Font(null, "Courier", 10, SWT.NORMAL)); //$NON-NLS-1$

		// Create actions and connect them to buttons
		makeActions();
		contributeToActionBars();
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.clearConsoleAction);
		this.clearConsoleAction.setText(Messages.GemConsole_3);
		manager.add(this.writeToLocalFileAction);
		this.writeToLocalFileAction.setText(Messages.GemConsole_4);
		manager.add(this.getHelpAction);
		this.getHelpAction.setText(Messages.GemConsole_5);
		manager.add(new Separator());

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.terminateOperationAction);
		manager.add(this.clearConsoleAction);
		manager.add(this.writeToLocalFileAction);
		manager.add(this.getHelpAction);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Initializes everything for this view and creates threads to be used by
	 * the main UI thread to perform view updates.
	 * 
	 * @param none
	 * @return void
	 */
	public void init() {

		this.disableTerminateButtonThread = new Thread() {
			@Override
			public void run() {
				GemConsole.this.terminateOperationAction.setEnabled(false);
			}
		};

		this.clearConsoleThread = new Thread() {
			@Override
			public void run() {
				GemConsole.this.txtConViewer.setText(""); //$NON-NLS-1$
			}
		};

		this.terminateOperationAction.setEnabled(true);
	}

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {

		// terminateOperationAction
		this.terminateOperationAction = new Action() {
			@Override
			public void run() {
				GemUtilities.terminateOperation();
			}
		};
		this.terminateOperationAction.setImageDescriptor(GemPlugin.getImageDescriptor("icons/progress_stop.gif")); //$NON-NLS-1$
		this.terminateOperationAction.setToolTipText(Messages.GemAnalyzer_74);
		this.terminateOperationAction.setEnabled(false);

		// clearConsoleAction
		this.clearConsoleAction = new Action() {
			@Override
			public void run() {
				GemConsole.this.txtConViewer.setText(""); //$NON-NLS-1$
			}
		};
		this.clearConsoleAction.setToolTipText(Messages.GemConsole_6);
		this.clearConsoleAction.setImageDescriptor(GemPlugin.getImageDescriptor("icons/clear-console.gif")); //$NON-NLS-1$

		// write ToLocalFileAction
		this.writeToLocalFileAction = new Action() {
			@Override
			public void run() {

				// Let the user indicate where to save local file
				final JFileChooser fc = new JFileChooser();
				final JFrame frame = new JFrame();
				int result = fc.showSaveDialog(frame);

				if (result == JFileChooser.APPROVE_OPTION) {
					final File file = fc.getSelectedFile();
					if (file.exists()) {
						result = fc.showDialog(frame, Messages.GemConsole_7);
					}
					if (result == JFileChooser.APPROVE_OPTION) {
						GemUtilities.saveToLocalFile(file, GemConsole.this.msgConsole.getDocument().get());
					}
				} else if (result == JFileChooser.ERROR_OPTION) {
					GemUtilities.showErrorDialog(Messages.GemConsole_8);
				}
			}
		};
		this.writeToLocalFileAction.setToolTipText(Messages.GemConsole_9);
		this.writeToLocalFileAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));

		// getHelpAction
		this.getHelpAction = new Action() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/org.eclipse.ptp.gem.help/html/consoleView.html"); //$NON-NLS-1$
			}
		};
		this.getHelpAction.setToolTipText(Messages.GemConsole_10);
		this.getHelpAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));
	}

	/**
	 * see org.eclipse.ui.IWorkbenchPart
	 */
	@Override
	public void setFocus() {
		this.txtConViewer.setFocus();
	}

	/**
	 * Appends the specified message to the TextConsole. If the clear console
	 * preference is set, the new message simply replaces the existing console
	 * content.
	 * 
	 * @param message
	 *            The string to append to the existing text in the text console
	 *            viewer of this view part.
	 * @return void
	 */
	public void write(String message) {
		// this.txtConViewer.setText(this.txtConViewer.getText()+message);
		this.txtConViewer.append(message);
		this.txtConViewer.setTopIndex(this.txtConViewer.getLineCount() - 1);
	}

	/**
	 * Writes the string to the console as an error message.
	 * 
	 * @param consoleStdErrMessage
	 *            The string that represents stderr to write to this Console.
	 * @return void
	 */
	public void writeStdErr(String consoleStdErrMessage) {
		final int oldLen = this.txtConViewer.getText().length();
		write(consoleStdErrMessage);

		if (consoleStdErrMessage.length() > 2) {
			final StyleRange styleRange = new StyleRange();
			final Display display = Display.getDefault();
			styleRange.start = oldLen;
			styleRange.length = consoleStdErrMessage.length();
			styleRange.foreground = display.getSystemColor(SWT.COLOR_RED);
			styleRange.fontStyle = SWT.BOLD;
			// styleRange1.fontStyle = SWT.BOLD | SWT.ITALIC;
			this.txtConViewer.setStyleRange(styleRange);
		}
	}

	/**
	 * Writes the string to the console as a standard message.
	 * 
	 * @param consoleStdOutMessage
	 *            The string that represents stdout to write to this Console.
	 * @void
	 */
	public void writeStdOut(String consoleStdOutMessage) {
		write(consoleStdOutMessage);
	}

}
