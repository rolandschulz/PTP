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
 *    Alan Morris - initial API and implementation
 *    Wyatt Spear - various modifications
 ****************************************************************************/
package org.eclipse.ptp.perf.tau.perfdmf.views;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.perf.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import edu.uoregon.tau.paraprof.GlobalDataWindow;
import edu.uoregon.tau.paraprof.ParaProf;
import edu.uoregon.tau.paraprof.ParaProfTrial;
import edu.uoregon.tau.paraprof.interfaces.EclipseHandler;
import edu.uoregon.tau.perfdmf.Application;
import edu.uoregon.tau.perfdmf.DBDataSource;
import edu.uoregon.tau.perfdmf.Database;
import edu.uoregon.tau.perfdmf.DatabaseAPI;
import edu.uoregon.tau.perfdmf.Experiment;
import edu.uoregon.tau.perfdmf.Function;
import edu.uoregon.tau.perfdmf.SourceRegion;
import edu.uoregon.tau.perfdmf.Trial;
/**
 * Defines a perfdmf database browser view and associated operations
 * @author wspear
 *
 */
public class PerfDMFView extends ViewPart {
    private TreeViewer viewer;
    private DrillDownAdapter drillDownAdapter;
    //private Action action1; //TODO: Add an 'upload external performance data' option
    private Action refreshAction;
    private Action doubleClickAction;

    private Action paraprofAction;
    private Action launchparaprofAction;
    
    private Action switchDatabaseAction;
    
    private String databaseName=null;

    static {
        ParaProf.insideEclipse = true;
    }

    class TreeNode implements IAdaptable {
        private String name;
        private Object userObject;
        private TreeNode parent;

        private ArrayList<TreeNode> children = new ArrayList<TreeNode>();

        public TreeNode(String name, Object userObject) {
            this.name = name;
            this.userObject = userObject;
        }

        public Object getAdapter(Class adapter) {
            return null;
        }

        public String getName() {
            return name;
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }

        public TreeNode getParent() {
            return parent;
        }

        public String toString() {
            return getName();
        }

        public Object getUserObject() {
            return userObject;
        }

        public void setUserObject(Object userObject) {
            this.userObject = userObject;
        }

        public void addChild(TreeNode child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeNode child) {
            children.remove(child);
            child.setParent(null);
        }

        public TreeNode[] getChildren() {
            return children.toArray(new TreeNode[children.size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }

    }

    /*
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */

    IFile getFile(String filename, IResource[] resources) {
        try {
            for (int j = 0; j < resources.length; j++) {
                System.out.println("  considering resource '" + resources[j] + "'");
                if (resources[j] instanceof IFile) {
                    IFile f = (IFile) resources[j];
                    System.out.println("filename = " + f.getName());
                    if (f.getName().equals(filename)) {
                        return f;
                    }
                } else if (resources[j] instanceof IFolder) {
                    System.out.println("recurse on Folder");
                    IFile f = getFile(filename, ((IFolder) resources[j]).members());
                    if (f != null) {
                        return f;
                    }
                } else if (resources[j] instanceof IProject) {
                    System.out.println("recurse on Project");
                    IFile f = getFile(filename, ((IProject) resources[j]).members());
                    if (f != null) {
                        return f;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static IWorkbenchPage getActivePage() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            return window.getActivePage();
        }
        return null;
    }

    /**
     * Returns a list of all found databases in the form NAME - CONNECTION-STRING
     * @return
     */
    public static String[] getDatabaseNames()
    {
    	 List<Database> dbs = Database.getDatabases();
    	 
    	 if(dbs.size()==0)
    		 return null;
    	 
    	 String[] names = new String[dbs.size()];
    	 
    	 Iterator<Database> dit = dbs.iterator();
    	 int i=0;
         while(dit.hasNext())
         {
        	 Database dtest=(Database)dit.next();
        	 names[i]=dtest.getConfig().getName()+" - "+dtest.getConfig().getConnectionString();
        	 i++;
         }
    	 
    	 return names;
    }
    
    /**
     * Returns just the configuration name of the full database identification string provided.
     * @param A database name "name - connection string" as provided by getDatabaseNames()
     * @return
     */
    public static String extractDatabaseName(String name)
    {
    	
   	 List dbs = Database.getDatabases();
	 
	 if(dbs.size()==0)
		 return null;
	 
	 //String[] names = new String[dbs.size()];
	 
	 Iterator dit = dbs.iterator();

     while(dit.hasNext())
     {
    	 Database dtest=(Database)dit.next();
    	 if(name.endsWith(dtest.getConfig().getName()+" - "+dtest.getConfig().getConnectionString()))
    			 return dtest.getConfig().getName();
     }
    	
    	return null;
    }
    
    /**
     * Given the name of a database configuration, returns the associated database and sets the 
     * current databaseName and displayed database name to that database
     * @param name
     * @return
     */
    public Database getDatabase(String name)
    {
    	 List<Database> dbs = Database.getDatabases();
         if (dbs.size() < 1) {
             // do something
             //throw new FileNotFoundException("perfdmf.cfg not found");
        	 databaseName=null;
         	return null;
         }
         
         Iterator<Database> dit = dbs.iterator();
         
         while(dit.hasNext())
         {
        	 Database dtest=(Database)dit.next();
        	 //System.out.println(dtest.getConfig().getName() + " or "+ name);
        	 if(dtest.getConfig().getName().equals(name))
        	 {
        		 databaseName=name;
        		 
        		 if(switchDatabaseAction!=null)
              	   switchDatabaseAction.setText("Using Database: "+ databaseName);
        		 
        		 return(dtest);
        	 }
        	 //System.out.println(dtest.getConfig().getName()+" is "+dtest.getID());
         }
         //TODO:  Find better default behavior?
         databaseName=((Database) dbs.get(0)).getConfig().getName();
         //System.out.println("Specified database not found, using "+databaseName);
         
         if(switchDatabaseAction!=null)
      	   switchDatabaseAction.setText("Using Database: "+ databaseName);
         
         return (Database) dbs.get(0);
    }
    
    private void openSource(String projectName, final SourceRegion sourceLink) {

        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            //IProject[] projects = workspace.getRoot().getProjects();
            IWorkspaceRoot root = workspace.getRoot();

            IFile file = getFile(sourceLink.getFilename(), root.members());

            if (file == null) {
                return;
            }
            IEditorInput iEditorInput = new FileEditorInput(file);
            
            IWorkbenchPage p = getActivePage();
            String editorid="org.eclipse.cdt.ui.editor.CEditor";
            if(file.getContentDescription().toString().indexOf("org.eclipse.photran.core.freeFormFortranSource")>=0||file.getContentDescription().toString().indexOf("org.eclipse.photran.core.fortranSource")>=0)
            	editorid="org.eclipse.photran.ui.FreeFormFortranEditor";
            else
            if(file.getContentDescription().toString().indexOf("org.eclipse.photran.core.fixedFormFortranSource")>=0)
            	editorid="org.eclipse.photran.ui.FixedFormFortranEditor";
            
            IEditorPart part = null;
            if (p != null) {
                part = p.openEditor(iEditorInput, editorid, true);
            }
           
            
            //IEditorPart part = EditorUtility.openInEditor(file);

            TextEditor textEditor = (TextEditor) part;

            final int start = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(
                    sourceLink.getStartLine() - 1);
            final int end = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(
                    sourceLink.getEndLine());

            textEditor.setHighlightRange(start, end - start, true);

            AbstractTextEditor abstractTextEditor = textEditor;

            ISourceViewer viewer = null;

            final Field fields[] = AbstractTextEditor.class.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                if ("fSourceViewer".equals(fields[i].getName())) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    viewer = (ISourceViewer) f.get(abstractTextEditor);
                    break;
                }
            }

            if (viewer != null) {
                viewer.revealRange(start, end - start);
                viewer.setSelectedRange(start, end - start);
            }

        } catch (Throwable t) {
           // t.printStackTrace();
        }
    }

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        private TreeNode invisibleRoot;

        public void refresh(Viewer v) {
            invisibleRoot = null;
            v.refresh();
        }

        public Object getRoot() {
            return invisibleRoot;
        }

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite())) {
                if (invisibleRoot == null)
                {
                    if(!initialize())
                    {
                    	return null;
                    }
                }
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }

        public Object getParent(Object child) {
            return ((TreeNode) child).getParent();
        }

        public Object[] getChildren(Object parent) {
            return ((TreeNode) parent).getChildren();
        }

        public boolean hasChildren(Object parent) {
            return ((TreeNode) parent).hasChildren();
        }
        
        
        /*
         * We will set up a dummy model to initialize tree heararchy.
         * In a real code, you will connect to a real model and
         * expose its hierarchy.
         */
        private boolean initialize() {

            try {

                ParaProf.eclipseHandler = new EclipseHandler() {

                    public boolean openSourceLocation(ParaProfTrial ppTrial, final Function function) {
                        System.out.println("Opening Source Code for " + function);
                        //                        openSource(null,function.getSourceLink());

                        Display.getDefault().asyncExec(new Runnable() {

                            public void run() {
                                openSource(null, function.getSourceLink());
                            }

                        });
                        return true;
                    }

                };

                invisibleRoot = new TreeNode("", null);
                //String perfdmf = System.getProperty("user.home") + "/.ParaProf/perfdmf.cfg";

               DatabaseAPI dbApi = new DatabaseAPI();
               Database database=getDatabase(databaseName);
               if(database==null)
               {   
            	   invisibleRoot.addChild(new TreeNode("none",null));
            	   return true;
               }
               
               
                dbApi.initialize(database);
                //dbApi.initialize(perfdmf, false);


                for (Iterator<Application> it = dbApi.getApplicationList().iterator(); it.hasNext();) {
                    Application app = (Application) it.next();
                    dbApi.setApplication(app);
                    //System.out.println("> " + app.getName());

                    TreeNode root = new TreeNode(app.getName(), app);
                    for (Iterator<Experiment> it2 = dbApi.getExperimentList().iterator(); it2.hasNext();) {
                        Experiment exp = (Experiment) it2.next();
                        dbApi.setExperiment(exp);
                        //System.out.println("-> " + exp.getName());

                        TreeNode tp = new TreeNode(exp.getName(), exp);

                        for (Iterator<Trial> it3 = dbApi.getTrialList().iterator(); it3.hasNext();) {
                            Trial trial = (Trial) it3.next();
                            //System.out.println("--> " + trial.getName());
                            TreeNode to = new TreeNode(trial.getName(), trial);
                            tp.addChild(to);
                        }
                        root.addChild(tp);
                    }
                    invisibleRoot.addChild(root);

                }
                dbApi.terminate();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            
            return true;
        }
    }

    class ViewLabelProvider extends LabelProvider {

        public String getText(Object obj) {
            return obj.toString();
        }

        public Image getImage(Object obj) {
            String imageKey = ISharedImages.IMG_OBJ_FOLDER;

            if (((TreeNode) obj).getUserObject() instanceof Trial) {
                imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }
    }

    class NameSorter extends ViewerSorter {
    }

    /**
     * The constructor.
     */
    public PerfDMFView() {
        PerfDMFUIPlugin.registerPerfDMFView(this);
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                PerfDMFView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        //manager.add(action1);  //TODO: Add an 'upload external performance data' option
        manager.add(new Separator());
        manager.add(refreshAction);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(paraprofAction);
        manager.add(refreshAction);
       // manager.add(action1);// //TODO: Add an 'upload external performance data' option
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
    	manager.add(switchDatabaseAction);
        manager.add(launchparaprofAction);
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
    }

    private boolean openInParaProf(Trial trial) {
        try {

        	DatabaseAPI dbApi = new DatabaseAPI();

        	Database database = getDatabase(databaseName);
        	if(database==null)
        		return false;
        	dbApi.initialize(database);


            dbApi.setTrial(trial.getID());
            DBDataSource dbDataSource = new DBDataSource(dbApi);
            dbDataSource.load();
            trial.setDataSource(dbDataSource);

            ParaProf.initialize();

            ParaProfTrial ppTrial = new ParaProfTrial(trial);

            ppTrial.getTrial().setDataSource(dbDataSource);
            ppTrial.finishLoad();

            GlobalDataWindow gdw = new GlobalDataWindow(ppTrial, null);
            gdw.setVisible(true);
            dbApi.terminate();
            

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    private void makeActions() {
        /*action1 = new Action() {
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("sampleview.views.SampleView");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                //addProfile("This Project", "/tmp/profiles");

            }
        };
        action1.setText("Do something cool!");
        action1.setToolTipText("Action 1 tooltip");
        action1.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/refresh.gif"));*/   //TODO: Add an 'upload external performance data' option

        refreshAction = new Action() {
            public void run() {
                ((ViewContentProvider) viewer.getContentProvider()).refresh(viewer);
            }
        };
        refreshAction.setText("Refresh");
        refreshAction.setToolTipText("Refresh Data");
        refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/refresh.gif"));

        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();

                TreeNode to = (TreeNode) obj;

                if (to.getUserObject() != null) {
                    openInParaProf((Trial) to.getUserObject());
                }
                //showMessage("Double-click detected on " + obj.toString());
            }
        };
        
        switchDatabaseAction=new Action(){
        	

        	public void run(){
        		ListDialog dblist = new ListDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        		ArrayContentProvider dbs=new ArrayContentProvider();
        		LabelProvider dl=new LabelProvider();
        		
        		String[] names=getDatabaseNames();
        		
        		dblist.setHelpAvailable(false);
        		dblist.setContentProvider(dbs);
        		dblist.setLabelProvider(dl);
        		dblist.setTitle("Select Database");
        		dblist.setInput(names);
        		dblist.open();
        		
        		Object[] result = dblist.getResult();
        		
        		if(result!=null&&result.length>=1)
        		{
        			databaseName=extractDatabaseName(result[0].toString());
        			 ((ViewContentProvider) viewer.getContentProvider()).refresh(viewer);
        		}
        	}
        
    };
        switchDatabaseAction.setText("Using Database: "+databaseName);
        switchDatabaseAction.setToolTipText("Select another database");

        launchparaprofAction = new Action() {
            public void run() {
                ParaProf.initialize();
                ParaProf.paraProfManagerWindow.setVisible(true);
            }
        };
        launchparaprofAction.setText("Launch ParaProf");
        launchparaprofAction.setToolTipText("Launch ParaProf");
        launchparaprofAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/pp.gif"));

        paraprofAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                TreeNode node = (TreeNode) obj;
                Trial trial = (Trial) node.getUserObject();
                openInParaProf(trial);
            }
        };
        paraprofAction.setText("Open in ParaProf");
        paraprofAction.setToolTipText("Open in ParaProf");
        paraprofAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/pp.gif"));

    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }
/*
    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(), "Performance Data View", message);
    }*/

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
    
    
    public boolean showProfile(String project, String projectType, String trialName){//,String dbname
    
    	ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();

        // reloads the tree
        vcp.refresh(viewer);

        Object[] objs;
        objs = vcp.getChildren(vcp.getRoot());

        for (int i = 0; i < objs.length; i++) {
            TreeNode node = (TreeNode) objs[i];
            if (((Application) node.getUserObject()).getName().equals(project)) {
                viewer.setExpandedState(node, true);
                Object[] expObjs = node.getChildren();
                for (int j = 0; j < expObjs.length; j++) {
                    TreeNode expNode = (TreeNode) expObjs[j];
                    if (((Experiment) expNode.getUserObject()).getName().equals(projectType)) {
                        viewer.setExpandedState(expNode, true);

                        Object[] trialObjs = expNode.getChildren();
                        for (int k = 0; k < trialObjs.length; k++) {
                            TreeNode trialNode = (TreeNode) trialObjs[k];
                            if (((Trial) trialNode.getUserObject()).getName().equals(trialName)) {
                                StructuredSelection selection = new StructuredSelection(trialNode);
                                viewer.setSelection(selection);
                            }
                        }
                    }
                }
            }
        }
    	
    	return true;
    }
    /*
    public boolean addProfile(String project, String projectType, String trialName, String directory, String dbname) {
    	DatabaseAPI dbApi=null;
    	try {
            File[] dirs = new File[1];
            dirs[0] = new File(directory);

            //TODO: This is a kludge.  Find out how to make this work across systems.
            if(System.getProperty("os.name").toLowerCase().trim().indexOf("aix")<0){
            	//System.setProperty("jaxp.debug", "1");
            	System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
            											//	org.apache.xalan.processor.TransformerFactoryImpl
            	//System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
            }
            
            DataSource dataSource = UtilFncs.initializeDataSource(dirs, DataSource.TAUPROFILE, false);
            
            
            // initialize database
            dbApi = new DatabaseAPI();
            //String perfdmf = System.getProperty("user.home") + "/.ParaProf/perfdmf.cfg";
            //dbApi.initialize(perfdmf, false);

            Database database = getDatabase(dbname);//= (Database) dbs.get(0);
            if(database==null)
            	return false;
            
            databaseName=dbname;
             dbApi.initialize(database);


            // create the trial
            Trial trial = new Trial();
            trial.setDataSource(dataSource);
            dataSource.load();
            trial.setMetaData(dataSource.getMetaData());

//            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
//            String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
//            sdf.setTimeZone(TimeZone.getDefault());

            trial.setName(trialName);///sdf.format(cal.getTime()));
            Experiment exp = dbApi.getExperiment(project, projectType, true);//"Experiment"
            trial.setExperimentID(exp.getID());

            //System.out.println("METADATA COUNT: "+dataSource.getMetaData().size());
            
            
            
            // upload the trial
            dbApi.uploadTrial(trial);
            
            dbApi.terminate();

            ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();

            // reloads the tree
            vcp.refresh(viewer);

            Object[] objs;
            objs = vcp.getChildren(vcp.getRoot());

            for (int i = 0; i < objs.length; i++) {
                TreeNode node = (TreeNode) objs[i];
                if (((Application) node.getUserObject()).getID() == exp.getApplicationID()) {
                    viewer.setExpandedState(node, true);
                    Object[] expObjs = node.getChildren();
                    for (int j = 0; j < expObjs.length; j++) {
                        TreeNode expNode = (TreeNode) expObjs[j];
                        if (((Experiment) expNode.getUserObject()).getID() == exp.getID()) {
                            viewer.setExpandedState(expNode, true);

                            Object[] trialObjs = expNode.getChildren();
                            for (int k = 0; k < trialObjs.length; k++) {
                                TreeNode trialNode = (TreeNode) trialObjs[k];
                                if (((Trial) trialNode.getUserObject()).getID() == trial.getID()) {
                                    StructuredSelection selection = new StructuredSelection(trialNode);
                                    viewer.setSelection(selection);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Throwable t) {
        	dbApi.terminate();
            if (t instanceof DatabaseException) {
                ((DatabaseException) t).getException().printStackTrace();
            }
            t.printStackTrace();
            return false;
        }

        return true;
    }*/
}