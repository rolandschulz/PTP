/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TerminalInfoView extends ViewPart {

	/**
	 * Multiple views support connection to multiple machines.
	 */
	private static List<TerminalInfoView> views = new ArrayList<TerminalInfoView>();

	public TerminalInfoView() {
		super();
		addToList(this);
	}

	private static synchronized void addToList(TerminalInfoView view) {
		views.add(view);
	}

	private static synchronized Iterable<TerminalInfoView> getViews() {
		return new ArrayList<TerminalInfoView>(views);
	}

	private final static int NENTRIES = 100;
	private TabFolder fTabs;
	private Map<String, TabForm> fTabItems = new HashMap<String, TabForm>();

	private static class TabRow {
		private Button fGo;
		private Button fCheck;
		private Text fText;
		private Button fDel;

		TabRow(Composite c, final String connectionName, final int n) {
			Display display = PlatformUI.getWorkbench().getDisplay();
			int props = (n % 2 == 0 ? SWT.BORDER : SWT.NONE);

			GridData gdleft = new GridData();
			GridData gdright = new GridData();
			GridData gdcenter = new GridData();
			gdcenter.horizontalAlignment = GridData.FILL;
			gdcenter.grabExcessHorizontalSpace = true;
			gdleft.horizontalAlignment = GridData.BEGINNING;
			gdright.horizontalAlignment = GridData.END;
			gdleft.grabExcessHorizontalSpace = gdright.grabExcessHorizontalSpace = false;

			fGo = new Button(c, props);
			fGo.setLayoutData(gdleft);
			fGo.setText(Integer.toString(n));

			fCheck = new Button(c, props | SWT.CHECK);
			fCheck.setLayoutData(gdleft);

			fText = new Text(c, props);
			fText.setLayoutData(gdcenter);

			fDel = new Button(c, props);
			fDel.setLayoutData(gdright);
			fDel.setText("X"); //$NON-NLS-1$

			if (n % 2 == 0) {
				fDel.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
				fGo.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
				fText.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			}
			fGo.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					try {
						OutputStream out = MachineManager.getOutputStream(connectionName);
						out.write((fText.getText() + "\n").getBytes()); //$NON-NLS-1$
						out.flush();
					} catch (IOException e) {
						Activator.log(e);
					}
				}
			});

			fGo.pack();
			fCheck.pack();
			fText.pack();
			fDel.pack();
		}
	}

	private static class TabForm {
		TabItem item;
		TabRow[] rows = new TabRow[NENTRIES];

		public void swap(int i, int j) {
			boolean sel = rows[i].fCheck.getSelection();
			rows[i].fCheck.setSelection(rows[j].fCheck.getSelection());
			rows[j].fCheck.setSelection(sel);

			String txt = rows[i].fText.getText();
			rows[i].fText.setText(rows[j].fText.getText());
			rows[j].fText.setText(txt);
		}
	}

	public void createPartControl(final Composite parent) {
		fTabs = new TabFolder(parent, SWT.NONE);

		updateSync(this);
	}

	@Override
	public void setFocus() {
	}

	private static class Command {
		final String connectionName;
		final String text;

		Command(String conn, String text) {
			this.connectionName = conn;
			this.text = text;
		}
	}

	private static LinkedList<TerminalInfoView.Command> commands = new LinkedList<TerminalInfoView.Command>();

	/**
	 * As the user types in commands, they are immediately updated
	 * and reflected in the visual history.
	 */
	private synchronized void update() {
		// Check to see the gui is initialized
		if (fTabs == null || fTabs.isDisposed() || !fTabs.isEnabled())
			return;
		while (commands.size() > 0) {
			Command c = commands.removeFirst();
			TabForm form = init(c);

			int j = 0;
			for (int i = 0; i < NENTRIES - 1; i++) {
				if (form.rows[i].fCheck.getSelection()) {
					if (i > j) {
						form.swap(i, j);
					}
					j++;
				}
			}
			for (int i = NENTRIES - 1; i > j; i--) {
				if (!form.rows[i - 1].fText.getText().equals("")) { //$NON-NLS-1$
					form.swap(i, i - 1);
				}
			}
			form.rows[j].fText.setText(c.text.trim());
		}
		fTabs.pack(true);
		fTabs.getParent().layout(true);
	}

	private synchronized TabForm init(final Command c) {
		TabForm form = fTabItems.get(c.connectionName);
		if (form == null) {
			form = new TabForm();
			form.item = new TabItem(fTabs, SWT.NULL);
			form.item.setText(c.connectionName);
			ScrolledComposite scomp = new ScrolledComposite(fTabs, SWT.V_SCROLL | SWT.BORDER);
			Composite comp = new Composite(scomp, SWT.NULL);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;

			scomp.setExpandHorizontal(true);
			scomp.setAlwaysShowScrollBars(true);
			scomp.setContent(comp);

			Layout gl0 = new FillLayout();
			scomp.setLayout(gl0);

			GridLayout gl = new GridLayout();
			gl.numColumns = 4;
			comp.setLayout(gl);

			comp.setLayoutData(gd);
			final TabForm finalForm = form;
			for (int i = 0; i < NENTRIES; i++) {
				final int nn = i;
				form.rows[i] = new TabRow(comp, c.connectionName, i);
				form.rows[i].fDel.addListener(SWT.Selection, new Listener() {

					public void handleEvent(Event event) {
						for (int j = nn; j < NENTRIES - 1; j++) {
							finalForm.swap(j, j + 1);
						}
					}
				});
			}

			form.item.setControl(scomp);
			fTabItems.put(c.connectionName, form);

			Composite parent = scomp.getParent();
			parent.setLayout(gl0);

			comp.pack(true);
			scomp.pack(true);
		}
		return form;
	}

	private static void updateSync(final TerminalInfoView view) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {

			public void run() {
				view.update();
			}
		});
	}

	public static synchronized void addToHistory(String connectionName, final String cmd) {
		commands.addLast(new Command(connectionName, cmd));
		for(TerminalInfoView view : getViews()) {
			updateSync(view);
		}
	}
}
