/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
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

package org.eclipse.ptp.isp.views;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.isp.ISPPlugin;
import org.eclipse.ptp.isp.messages.Messages;
import org.eclipse.ptp.isp.preferences.PreferenceConstants;
import org.eclipse.ptp.isp.util.IspUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.ViewPart;

/**
 * The ISP Console View.
 */
public class ISPConsole extends ViewPart {

	public static final String ID = "org.eclipse.ptp.isp.views.ISPConsole"; //$NON-NLS-1$

	private Action clrConsole;
	private Action writeToFile;
	private Action getHelp;
	private MessageConsole msgConsole;
	private TextConsoleViewer txtConViewer;

	/**
	 * Constructor.
	 */
	public ISPConsole() {
	}

	/**
	 * Callback that allows us to create the viewer and initialize it.
	 * 
	 * @param parent
	 *            The parent Composite to this View.
	 * @return void
	 */
	public void createPartControl(Composite parent) {

		// Initializations
		this.msgConsole = new MessageConsole("ISP Console", null); //$NON-NLS-1$
		this.txtConViewer = new TextConsoleViewer(parent, msgConsole);
		this.txtConViewer.setInput(getViewSite());
		this.txtConViewer.setFont(new Font(null, "Courier", 10, SWT.NORMAL)); //$NON-NLS-1$

		// Create actions and connect them to buttons and the context menu
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 * 
	 * @param none
	 * @return void
	 */
	public void setFocus() {
		((Viewer) this.txtConViewer).getControl().setFocus();
	}

	/**
	 * Prepends the specified message to the TextConsole.
	 * 
	 * @param message
	 *            The string to display.
	 * @return void
	 */
	public void write(String message) {
		if (message != null) {

			IDocument doc = this.msgConsole.getDocument();
			// Only show most recent results if preference is set
			if (ISPPlugin.getDefault().getPreferenceStore().getBoolean(
					PreferenceConstants.ISP_PREF_CLRCON)) {
				doc.set(message);
			} else {

				// Append the new message after the old
				String old = doc.get();
				if (old == "") { //$NON-NLS-1$
					doc.set(message);
				} else {
					doc.set(old + "\n\n" + message); //$NON-NLS-1$
				}
			}
		}
	}

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {
		this.clrConsole = new Action() {
			public void run() {
				ISPConsole.this.msgConsole.clearConsole();
			}
		};
		this.clrConsole.setToolTipText(Messages.ISPConsole_5);
		this.clrConsole.setImageDescriptor(ISPPlugin
				.getImageDescriptor("icons/clear-console.gif")); //$NON-NLS-1$

		this.writeToFile = new Action() {
			public void run() {

				// Let the user indicate where to save it
				JFileChooser fc = new JFileChooser();
				JFrame frame = new JFrame();
				int result = fc.showSaveDialog(frame);

				if (result == JFileChooser.CANCEL_OPTION) {
					showMessage(Messages.ISPConsole_7, 0);
				} else if (result == JFileChooser.APPROVE_OPTION) {
					showMessage(Messages.ISPConsole_8, 1);
					File chosenFile = fc.getSelectedFile();
					String add = ".txt"; //$NON-NLS-1$
					if (isTxtFile(chosenFile.toString())) {
						add = ""; //$NON-NLS-1$
					}
					File file = new File(chosenFile + add);
					saveToFile(file);
				} else if (result == JFileChooser.ERROR_OPTION) {
					showMessage(Messages.ISPConsole_11, 2);
				}
			}
		};
		this.writeToFile.setToolTipText(Messages.ISPConsole_12);
		this.writeToFile.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_ETOOL_SAVE_EDIT));

		this.getHelp = new Action() {
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(
						"/org.eclipse.ptp.isp.help/html/output.html"); //$NON-NLS-1$
			}
		};
		this.getHelp.setToolTipText(Messages.ISPConsole_14);
		this.getHelp.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_LCL_LINKTO_HELP));
	}

	/*
	 * Adds MenuListeners to hook selections from the context menu.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISPConsole.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(((Viewer) this.txtConViewer)
				.getControl());
		((Viewer) this.txtConViewer).getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this.txtConViewer);
	}

	/*
	 * Calls finer grained methods, populating the view action bar.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.clrConsole);
		this.clrConsole.setText(Messages.ISPConsole_16);
		manager.add(this.writeToFile);
		this.writeToFile.setText(Messages.ISPConsole_17);
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.ISPConsole_18);
		manager.add(new Separator());

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Populates the view context menu.
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.clrConsole);
		this.clrConsole.setText(Messages.ISPConsole_19);
		manager.add(this.writeToFile);
		this.writeToFile.setText(Messages.ISPConsole_20);
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.ISPConsole_21);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.clrConsole);
		manager.add(this.writeToFile);
		manager.add(this.getHelp);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Writes the contents of the ISP console to the indicated file.
	 */
	private void saveToFile(File file) {
		int length = 0;
		try {
			PrintWriter writer = new PrintWriter(file);
			String content = this.msgConsole.getDocument().get();
			length = content.length();

			for (int i = 0; i < length; i++) {
				writer.print(content.charAt(i));
			}
			writer.println("\n\n"); //$NON-NLS-1$
			writer.flush();
		} catch (IOException e) {
			IspUtilities.showExceptionDialog(Messages.ISPConsole_23, e);
			IspUtilities.logError(Messages.ISPConsole_24, e);
		}
	}

	/*
	 * Displays the appropriate confirmation dialog after attempt to write file.
	 */
	private void showMessage(String message, int type) {
		switch (type) {
		case 0:
			break;
		case 1:
			JOptionPane.showMessageDialog(null, message, "Success", //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
			break;
		case 2:
			JOptionPane.showMessageDialog(null, message, "Error", //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	 * Returns true if the specified filename has a .txt extension, and false
	 * otherwise.
	 */
	private boolean isTxtFile(String name) {
		int index = name.indexOf(".txt"); //$NON-NLS-1$
		if (index == -1) {
			return false;
		}
		return true;
	}

}
