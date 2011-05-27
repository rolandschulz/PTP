package org.eclipse.ptp.rm.lml.ui.views;

import java.util.List;
import java.util.Stack;

import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.ui.providers.DisplayNode;
import org.eclipse.ptp.rm.lml.ui.providers.LguiWidget;
import org.eclipse.ptp.rm.lml.ui.providers.NodedisplayComp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Parent composite of NodedisplayComp
 * This class allows to zoom or switch the viewports of
 * nodedisplaycomps. Every nodedisplaycomp is connected with
 * one instance of this class. NodedisplayView represents one zoomable
 * NodedisplayComp.
 */
public class NodedisplayView extends LguiWidget{

	private Nodedisplay model;//LML-Data-model for this view
	
	private ScrolledComposite scrollpane;//creates scrollbars surrounding nodedisplay
	private NodedisplayComp root=null;//root nodedisplay which is currently shown
	
	private Stack<String> zoomstack = new Stack<String>();//Saves zoom-levels to zoom out later, saves full-implicit name of nodes to create Displaynodes from these ids
	
	//Cursors for showing processing
	private Cursor waitcursor;//Cursor to show while processing
	private Cursor defaultcursor;//default cursor
	
	/**
	 * Create a composite as surrounding component for NodedisplayComps.
	 * This class encapsulates zooming functionality. It saves a stack
	 * 
	 * @param pmodel
	 * @param parent
	 */
	public NodedisplayView(ILguiItem lgui, Nodedisplay pmodel, Composite parent){
		
		super(lgui, parent, SWT.None);		
		
		model=pmodel;
		
		setLayout(new FillLayout());
		
		scrollpane=new ScrolledComposite(this, SWT.H_SCROLL | SWT.V_SCROLL);
		
		if(lgui!=null && model!=null)
			root=new NodedisplayComp(lgui, model, this, SWT.None);
		
		//Create cursors
		defaultcursor=this.getCursor();
		waitcursor = new Cursor(this.getDisplay() , SWT.CURSOR_WAIT);
		
		addResizeListenerForScrollPane();
	}
	
	/**
	 * Adds a listener, which changes scrollbar-increments on
	 * every resize.
	 */
	private void addResizeListenerForScrollPane(){
		
		scrollpane.addControlListener(new ControlListener() {
			
			public void controlResized(ControlEvent e) {
				ScrollBar xbar = scrollpane.getHorizontalBar();
				ScrollBar ybar = scrollpane.getVerticalBar();
				if(xbar!=null){
					xbar.setPageIncrement( xbar.getThumb()/2 );
					xbar.setIncrement( xbar.getThumb()/5 );
				}
				if(ybar!=null){
					ybar.setPageIncrement( ybar.getThumb()/2 );
					ybar.setIncrement( ybar.getThumb()/5 );
				}
			}
			
			public void controlMoved(ControlEvent e) {
			}
		});
		
	}
	
	/**
	 * Data has been updated. The new nodedisplay-model is needed.
	 * This function searches for the nodedisplay-instance, which 
	 * is the successor of the last shown nodedisplay. 
	 * 
	 * @return new Nodedisplay-model
	 */
	private Nodedisplay getNewModel(){
		
		if(lgui==null)
			return null;
		
		String nodedisplayId="";
		if(model!=null)
			nodedisplayId = model.getId();
		
		Nodedisplay res=lgui.getNodedisplayAccess().getNodedisplayById(nodedisplayId);
		
		if(res==null){
			List<Nodedisplay> nodedisplays = lgui.getNodedisplayAccess().getNodedisplays();
			
			if(nodedisplays.size() > 0){
				res=nodedisplays.get(0);
			}
		}
		
		return res;
	}
	
	
	/**
	 * Call this update if lguiitem changes. This update
	 * is calles if another system is monitored.
	 * 
	 * @param lgui new data-manager
	 */
	public void update(ILguiItem lgui){
		this.lgui=lgui;
		
		update();
	}
	
	/**
	 * Update view and repaint current data.
	 * This is done by creating a completely new nodedisplay.
	 * Tries to go to the implicitname, which was shown
	 * before.
	 */
	public void update(){
		super.update();
		
		String shownImpName=null;
		/*if(root!=null)
			shownImpName = root.getShownImpname();*/
		restartZoom();
		model=getNewModel();
		
		if(model!=null)
			goToImpname(shownImpName, true);
	}
	
	/**
	 * @return access to scrollpane, which contains the root-nodedisplay
	 */
	public ScrolledComposite getScrollPane(){
		return scrollpane;
	}
	
	/**
	 * The stack which saves the last zoom-levels is restarted
	 */
	public void restartZoom(){
		zoomstack = new Stack<String>();
	}
	
	/**
	 * Go one level higher in zoomstack
	 */
	public void zoomOut(){
		this.setCursor(waitcursor);
		
		if(! zoomstack.isEmpty()){
			String impname=zoomstack.pop();
			//Get back null-values
			if(impname.equals(""))
				impname=null;
			
			//Switch view to node with impname
			goToImpname(impname);
		}
		
		this.setCursor(defaultcursor);
	}
	
	public void zoomIn(String impname){
		if(root==null)
			return;
		
		this.setCursor(waitcursor);
		
		String oldshown=root.getShownImpname();
		
		if(goToImpname(impname)){
			if(oldshown==null)//Not allowed to insert null-values into ArrayDeque
				oldshown="";
			zoomstack.push(oldshown);
		}
		
		this.setCursor(defaultcursor);
	}
	
	/**
	 * @return currently shown nodedisplaycomp
	 */
	public NodedisplayComp getRootNodedisplay(){
		return root;
	}
	
	
	/**
	 * Set node with impname as implicit name as root-node within this nodedisplay-panel.
	 * Call this function only if model did not changed.
	 * @param impname implicit name of a node, which identifies every node within a nodedisplay
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpname(String impname){
		return goToImpname(impname, false);
	}
	
	/**
	 * Set node with impname as implicit name as root-node within this nodedisplay-panel
	 * @param impname implicit name of a node, which identifies every node within a nodedisplay
	 * @param modelChanged if true a new nodedisplay is forced to be created, otherwise only
	 * 			if the new impname differs from currently shown impname
	 * @return true, if root was changed, otherwise false
	 */
	public boolean goToImpname(String impname, boolean modelChanged){
		
		if(lgui==null)
			return false;
		
		String shownimpname=null;
		if(root!=null)
			shownimpname=root.getShownImpname();
		
		//A new panel has to be created if the model is new
		if(!modelChanged){
			//Do not create a new panel if panel is already on the right view
			if(shownimpname==null){
				if(impname==null){
					return false;
				}
			}
			else if(shownimpname.equals(impname)){//Do not create new panel, if current viewport is the same to which this panel should be set
				return false;
			}
		}
		
		NodedisplayComp newcomp=null;
		
		if(root!=null){
			root.dispose();//Delete old root-element
			System.gc();
		}
		
		if(impname!=null){
			DisplayNode newnode=DisplayNode.getDisplayNodeFromImpName(lgui, impname, model);
			
			newcomp=new NodedisplayComp(lgui, model, newnode, this, SWT.None);
		}
		else newcomp=new NodedisplayComp(lgui, model, this, SWT.None);//if impname is null => go up to root-level
		
		root=newcomp;
		
		this.layout();
		root.layout();
		
		return true;
	}
	
	public void dispose(){
		//Dispose created cursor
		waitcursor.dispose();
	}
	
}
