/*******************************************************************************
 * Copyright (c) 2009, 2011 University of Utah School of Computing
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

package org.eclipse.ptp.gem.views;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
import org.eclipse.ptp.gem.preferences.PreferenceConstants;
import org.eclipse.ptp.gem.util.GemUtilities;
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
 * The GEM Console View.
 */
public class GemConsole extends ViewPart {

	public static final String ID = "org.eclipse.ptp.gem.views.GemConsole"; //$NON-NLS-1$

	private Action clrConsole;
	private Action writeToLocalFile;
	private Action getHelp;
	private MessageConsole msgConsole;
	private TextConsoleViewer txtConViewer;

	/**
	 * Constructor.
	 */
	public GemConsole() {
		super();
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
		this.msgConsole = new MessageConsole("GEM Console", null); //$NON-NLS-1$
		this.txtConViewer = new TextConsoleViewer(parent, this.msgConsole);
		this.txtConViewer.setInput(getViewSite());
		this.txtConViewer.setFont(new Font(null, "Courier", 10, SWT.NORMAL)); //$NON-NLS-1$

		// Create actions and connect them to buttons and the context menu
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	/*
	 * Populates the view context menu.
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.clrConsole);
		this.clrConsole.setText(Messages.GemConsole_0);
		manager.add(this.writeToLocalFile);
		this.writeToLocalFile.setText(Messages.GemConsole_1);
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.GemConsole_2);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Populates the view pull-down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.clrConsole);
		this.clrConsole.setText(Messages.GemConsole_3);
		manager.add(this.writeToLocalFile);
		this.writeToLocalFile.setText(Messages.GemConsole_4);
		manager.add(this.getHelp);
		this.getHelp.setText(Messages.GemConsole_5);
		manager.add(new Separator());

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Contributes icons and actions to the tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.clrConsole);
		manager.add(this.writeToLocalFile);
		manager.add(this.getHelp);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Adds MenuListeners to hook selections from the context menu.
	 */
	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GemConsole.this.fillContextMenu(manager);
			}
		});
		final Menu menu = menuMgr.createContextMenu(((Viewer) this.txtConViewer).getControl());
		((Viewer) this.txtConViewer).getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this.txtConViewer);
	}

	/*
	 * Creates the actions associated with the action bar buttons and context
	 * menu items.
	 */
	private void makeActions() {
		this.clrConsole = new Action() {
			@Override
			public void run() {
				GemConsole.this.msgConsole.clearConsole();
			}
		};
		this.clrConsole.setToolTipText(Messages.GemConsole_6);
		this.clrConsole.setImageDescriptor(GemPlugin.getImageDescriptor("icons/clear-console.gif")); //$NON-NLS-1$

		this.writeToLocalFile = new Action() {
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
		this.writeToLocalFile.setToolTipText(Messages.GemConsole_9);
		this.writeToLocalFile.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));

		this.getHelp = new Action() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/org.eclipse.ptp.gem.help/html/output.html"); //$NON-NLS-1$
			}
		};
		this.getHelp.setToolTipText(Messages.GemConsole_10);
		this.getHelp.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 * 
	 * @param none
	 * @return void
	 */
	@Override
	public void setFocus() {
		((Viewer) this.txtConViewer).getControl().setFocus();
	}

	/**
	 * Appends the specified message to the TextConsole. If the clear console
	 * preference is set, the new message simply replaces the existing console
	 * content.
	 * 
	 * @param message
	 *            The string to display in this console.
	 * @return void
	 */
	public void write(String message) {
		if (message != null) {
			final IDocument doc = this.msgConsole.getDocument();

			// Only show most recent results if preference is set
			if (GemPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GEM_PREF_CLRCON)) {
				doc.set(message);
			} else {
				// Append the new message after the old
				final String old = doc.get();
				doc.set((old.equals("")) ? message : old + "\n\n" + message); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

}
