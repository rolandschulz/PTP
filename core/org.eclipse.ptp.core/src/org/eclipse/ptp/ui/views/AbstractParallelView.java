package org.eclipse.ptp.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.ptp.launch.core.IParallelLaunchListener;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public abstract class AbstractParallelView extends ViewPart implements IParallelLaunchListener {
   	/* colors so that we can hold them in one place and everyone can
   	 * access them
   	 */
   	protected Color red, green, yellow, cyan, magenta, gray, black, white, blue;

    protected final int ASYN_STYLE = 0; 
	protected final int BUSY_STYLE = 1; 
	protected final int SYN_STYLE = 2; 

	protected ILaunchManager launchManager = ParallelPlugin.getDefault().getLaunchManager();
    protected List editorList = new ArrayList(0);
    
    public AbstractParallelView() {
        launchManager.addParallelLaunchListener(this);
        createColor();
    }
    
    private void createColor() {
    	Display display = Display.getCurrent();
        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        yellow = display.getSystemColor(SWT.COLOR_YELLOW);
        cyan = display.getSystemColor(SWT.COLOR_CYAN);
        gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        black = display.getSystemColor(SWT.COLOR_BLACK);
        white = display.getSystemColor(SWT.COLOR_WHITE);        
        blue = display.getSystemColor(SWT.COLOR_BLUE);
        magenta = display.getSystemColor(SWT.COLOR_MAGENTA);
    }
    
    public void dispose() {
        launchManager.removeParallelLaunchListener(this);
        //removerAllProcessViewer(BUSY_STYLE);
        //editorList.clear();
        //editorList = null;
        super.dispose();
    }
    
    protected void openProcessViewer(final IPProcess element) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    IEditorPart part = ParallelPlugin.getActivePage().openEditor(new ProcessEditorInput(element), UIUtils.ParallelProcessViewer_ID);
                    //if (!editorList.contains(part))
                        //editorList.add(part);
                } catch (PartInitException e) {
                    System.out.println("ParallelProcessesView - PartInitException err: " + e.getMessage());
                }
            }
        };
        execStyle(BUSY_STYLE, runnable);    	
    }
    
    protected void removerAllProcessViewer(int style) {
        Runnable runnable = new Runnable() {
            public void run() {
                for (Iterator i=editorList.iterator(); i.hasNext();) {
                    ParallelPlugin.getActivePage().closeEditor((IEditorPart)i.next(), false);
                }
                editorList.clear();
            }
        };
        execStyle(style, runnable);
    }
    
    protected void removerAllProcessViewer() {
        removerAllProcessViewer(ASYN_STYLE);
    }
    
    protected void execStyle(int style, Runnable runnable) {
        switch (style) {
            case BUSY_STYLE:
                BusyIndicator.showWhile(getViewSite().getShell().getDisplay(), runnable);
                break;
            case SYN_STYLE:
                getViewSite().getShell().getDisplay().syncExec(runnable);
                break;
            default:
                getViewSite().getShell().getDisplay().asyncExec(runnable);
                break;
        }
    }
        
    protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = columns;
        gridLayout.makeColumnsEqualWidth = isEqual;
        gridLayout.marginHeight = mh;
        gridLayout.marginWidth = mw;
        return gridLayout;
    }
    
    protected GridData spanGridData(int style, int space) {
        GridData gd = null;
        if (style == -1)
            gd = new GridData();
        else
            gd = new GridData(style);
        gd.horizontalSpan = space;
        return gd;
    }
    
    protected GridData createDefaultGridData(int style) {
        GridData data = new GridData(style);
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        return data;
    }
    
    public void searchForNode(int node) { }
    public void searchForProcess(int process) { }
    public void showAllNodes() { }
    public void showMyAllocatedNodes() { }
    public void showMyUsedNodes() { }
    public void showProcesses() { }
    
    public abstract void registerViewer();
    public abstract void selectReveal(IPElement element);
}