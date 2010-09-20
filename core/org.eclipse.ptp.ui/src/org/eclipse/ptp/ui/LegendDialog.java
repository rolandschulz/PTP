/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.ui;

import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class LegendDialog extends Dialog 
{
	private String rmStateText[] = {
		Messages.LegendDialog_Starting,
		Messages.LegendDialog_Started,
		Messages.LegendDialog_Stopping,
		Messages.LegendDialog_Stopped,
		Messages.LegendDialog_Suspended,
		Messages.LegendDialog_ErrorRM
	};
	
	private String machineStateText[] = {
		Messages.LegendDialog_UpM,
		Messages.LegendDialog_DownM,
		Messages.LegendDialog_AlertM,
		Messages.LegendDialog_ErrorM,
		Messages.LegendDialog_UnknownM
	};

	private String jobStateText[] = {
		Messages.LegendDialog_StartedJ,
		Messages.LegendDialog_RunningJ,
		Messages.LegendDialog_SuspendedJ,
		Messages.LegendDialog_CompletedJ,
	};
	
	private String nodeStateText[] = {
		Messages.LegendDialog_Up,
		Messages.LegendDialog_Down,
		Messages.LegendDialog_Error,
		Messages.LegendDialog_Unknown,
		Messages.LegendDialog_ProcessRunning,
		Messages.LegendDialog_ProcessTerminated,
	};
	
	private String processStateText[] = {
		Messages.LegendDialog_StartedP,
		Messages.LegendDialog_RunningP,
		Messages.LegendDialog_SuspendedP,
		Messages.LegendDialog_CompletedP,
	};
	
	/**
	 * @param parent
	 */
	public LegendDialog(Shell parent) {
		/* we DON'T want this to be modal */
		this(parent, SWT.DIALOG_TRIM | SWT.NONE);
	}
	
	public LegendDialog(Shell parent, int style) {
		super(parent, style);
		setText(Messages.LegendDialog_LegendTitle);
	}
	
	public String open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	private void createContents(final Shell shell) {
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		shell.setLayout(grid);
		
		/* the RM box */
		Group box = new Group(shell, SWT.BORDER);
		box.setText(Messages.LegendDialog_ResourceManagerIcons);
		FillLayout fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		box.setLayoutData(data);

		for (int i = 0; i < rmStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
			RowLayout r = new RowLayout();
			r.wrap = true;
			r.pack = true;
			r.justify = false;
			r.marginBottom = 0;
			r.marginLeft = 5;
			r.marginRight = 5;
			r.marginTop = 0;
			r.spacing = 10;
			c.setLayout(r);
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.rmImages[i]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(rmStateText[i]);
		}
		
		/* the machines box */
		box = new Group(shell, SWT.BORDER);
		box.setText(Messages.LegendDialog_MachineIcons);
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < machineStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
			RowLayout r = new RowLayout();
			r.wrap = true;
			r.pack = true;
			r.justify = false;
			r.marginBottom = 0;
			r.marginLeft = 5;
			r.marginRight = 5;
			r.marginTop = 0;
			r.spacing = 10;
			c.setLayout(r);
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.machineImages[i]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(machineStateText[i]);
		}
		
		/* the node box */
		box = new Group(shell, SWT.BORDER);
		box.setText(Messages.LegendDialog_NodeIcons);
		GridLayout g = new GridLayout(2, true);
		box.setLayout(g);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		box.setLayoutData(data);

		/* layout column first instead of row first */
		for (int j = 0; j < nodeStateText.length/2; j++) {
			for (int i = 0 ; i < 2; i++) {
				Composite c = new Composite(box, SWT.NONE);
				RowLayout r = new RowLayout();
				r.wrap = true;
				r.pack = true;
				r.justify = false;
				r.marginBottom = 0;
				r.marginLeft = 5;
				r.marginRight = 5;
				r.marginTop = 0;
				r.spacing = 10;
				c.setLayout(r);
				CLabel b = new CLabel(c, SWT.FLAT);
				b.setImage(ParallelImages.nodeImages[i*nodeStateText.length/2 + j][0]);
				CLabel l = new CLabel(c, SWT.LEFT);
				l.setText(nodeStateText[i*nodeStateText.length/2 + j]);
			}
		}
		
		/* the job box */
		box = new Group(shell, SWT.BORDER);
		box.setText(Messages.LegendDialog_JobIcons);
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);

		for (int i = 0; i < jobStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
			RowLayout r = new RowLayout();
			r.wrap = true;
			r.pack = true;
			r.justify = false;
			r.marginBottom = 0;
			r.marginLeft = 5;
			r.marginRight = 5;
			r.marginTop = 0;
			r.spacing = 10;
			c.setLayout(r);
			CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.jobImages[i][0]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(jobStateText[i]);
		}
				
		/* the process box */
		box = new Group(shell, SWT.BORDER);
		box.setText(Messages.LegendDialog_ProcessIcons);
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		box.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		box.setLayoutData(data);
		
		for (int i = 0; i < processStateText.length; i++) {
			/* one horizontal box */
			Composite c = new Composite(box, SWT.NONE);
			RowLayout r = new RowLayout();
			r.wrap = true;
			r.pack = true;
			r.justify = false;
			r.marginBottom = 0;
			r.marginLeft = 5;
			r.marginRight = 5;
			r.marginTop = 0;
			r.spacing = 10;
			c.setLayout(r);
	        CLabel b = new CLabel(c, SWT.FLAT);
			b.setImage(ParallelImages.procImages[i][0]);
			CLabel l = new CLabel(c, SWT.LEFT);
			l.setText(processStateText[i]);
		}
		
        Button close = new Button(shell, SWT.PUSH);
        close.setText(Messages.LegendDialog_Close);
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.horizontalSpan = 2;
		close.setLayoutData(data);

        close.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		shell.close();
        	}
        });
                    
        shell.setDefaultButton(close);
	}
}
