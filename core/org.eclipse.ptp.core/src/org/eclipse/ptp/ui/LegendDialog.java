package org.eclipse.ptp.ui;


import org.eclipse.ptp.ui.views.ParallelNodeStatusView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

/**
 * @author ndebard
 */
public class LegendDialog extends Dialog 
{
	/**
	 * @param parent
	 */
	public LegendDialog(Shell parent) {
		/* we DON'T want this to be modal */
		this(parent, SWT.DIALOG_TRIM | SWT.NONE);
	}
	
	public LegendDialog(Shell parent, int style) {
		super(parent, style);
		setText("Legend");
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
		shell.setLayout(new GridLayout(1, true));
		
		/* the top box */
		Group top = new Group(shell, SWT.BORDER);
		top.setText("Node Colors");
		FillLayout fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		top.setLayout(fill);
		FormData fd ;
		GridData data = new GridData(GridData.FILL_BOTH);
		top.setLayoutData(data);
		CLabel text;

		/* one horizontal box */
		Composite c = new Composite(top, SWT.NONE);
        FormLayout layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		CLabel b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_DOWN][ParallelNodeStatusView.NOT_SELECTED]);
		CLabel l = new CLabel(c, SWT.LEFT);
		l.setText("DOWN");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_UP][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("UNALLOCATED");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_USER_ALLOC_EXCL][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ALLOCATED TO YOU EXCLUSIVELY, BUT IDLE");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_USER_ALLOC_SHARED][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ALLOCATED TO YOU SHARED, BUT IDLE");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);

		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_OTHER_ALLOC_EXCL][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ALLOCATED TO SOMEONE ELSE EXCLUSIVELY");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_OTHER_ALLOC_SHARED][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ALLOCATED TO SOMEONE ELSE SHARED");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_RUNNING][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("JOB RUNNING");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_EXITED][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("JOB STOPPED");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_ERROR][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ERROR");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		c = new Composite(top, SWT.NONE);
        layout = new FormLayout();        
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.NODE_UNKNOWN][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("UNKNOWN / UNDEFINED");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* the top box */
		Group middle = new Group(shell, SWT.BORDER);
		middle.setText("Process Colors");
		fill = new FillLayout(SWT.VERTICAL);
		fill.marginHeight = 5;
		fill.marginWidth = 5;
		middle.setLayout(fill);
		data = new GridData(GridData.FILL_BOTH);
		middle.setLayoutData(data);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_STARTING][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("STARTING");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_RUNNING][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("RUNNING");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_EXITED][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("EXITED NORMALLY");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_EXITED_SIGNAL][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("EXITED WITH SIGNAL");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_STOPPED][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("STOPPED");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
		/* one horizontal box */
		c = new Composite(middle, SWT.NONE);
        layout = new FormLayout();     
        c.setLayout(layout);
        /* the contents of the horizontal box */
		b = new CLabel(c, SWT.FLAT);
		b.setImage(ParallelNodeStatusView.statusImages[ParallelNodeStatusView.PROC_ERROR][ParallelNodeStatusView.NOT_SELECTED]);
		l = new CLabel(c, SWT.LEFT);
		l.setText("ERROR");
		/* formdata stuff so they take up the right amount of space */
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(20);
		fd.left = new FormAttachment(0);
		b.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.bottom = new FormAttachment(100);
		fd.right = new FormAttachment(100);
		fd.left = new FormAttachment(b);
		l.setLayoutData(fd);
		
        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		close.setLayoutData(data);

        close.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		shell.close();
        	}
        });
                    
        shell.setDefaultButton(close);
	}
}
