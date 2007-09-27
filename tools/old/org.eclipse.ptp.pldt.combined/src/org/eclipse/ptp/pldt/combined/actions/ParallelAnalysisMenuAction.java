package org.eclipse.ptp.pldt.combined.actions;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.pldt.combined.CombinedActivator;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;

/**
 * Toolbar dropdown menu for parallel analysis actions
 * @author tibbitts
 *
 */
public class ParallelAnalysisMenuAction implements IWorkbenchWindowPulldownDelegate {
	Menu menu;

	/**
	 * setup up what actions are in this pulldown toolbar menu
	 */
	public Menu getMenu(Control parent) {
		System.out.println("getMenu");
		if (true/*menu == null*/) {
			menu = new Menu(parent);
			IAction[] actions = getActions();
			
			// hack hack hack put fake menu items for screenshots
			MenuItem menuitem = new MenuItem(menu, SWT.NONE);
			menuitem.setText("Show MPI Artifacts");
			//MpiPlugin mpiPlugin=MpiPlugin.getDefault();
			
			CombinedActivator ca = CombinedActivator.getDefault();
			Image iconImage=createImage(ca,"icons/artifact.gif");

			System.out.println("mpiImage: "+iconImage);
			menuitem.setImage(iconImage);
			
			menuitem = new MenuItem(menu,SWT.None);
			menuitem.setText("Show OpenMP Artifacts");
			iconImage=createImage(ca,"icons/artifact_turq.gif");
			menuitem.setImage(iconImage);
			
			menuitem = new MenuItem(menu,SWT.NONE);
			menuitem.setText("MPI Barrier Analysis");
			iconImage = createImage(ca, "icons/barrier.gif");
			menuitem.setImage(iconImage);
			
			
			
		}
		return menu;
	}
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(MpiIDs.MPI_VIEW_ID, path);
    }

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		//MpiPlugin mpiPlugin;

	}
	ImageDescriptor getImageDescriptor(AbstractUIPlugin plugin, String iconName){
		Bundle bundle = plugin.getBundle();
		Path path = new Path(iconName);

		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor id = ImageDescriptor.createFromURL(url);
		return id;
	}
	Image createImage(AbstractUIPlugin plugin, String iconName){
		ImageDescriptor id = getImageDescriptor(plugin, iconName);
		Image image = null;
		if(id !=null){
			image = id.createImage();
		}
		return image;
	}

	public void run(IAction action) {
		System.out.println("parallel analysis menu action");

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
	
	public IAction[] getActions(){
		return createActions(getParallelAnalysisActionElements());
	}
	// modelled after:  in CWizardRegistry, see also NewProjectDropDownAction and AbstractWizardDropDownAction
//  public static IAction[] getProjectWizardActions() {
//    return createActions(getProjectWizardElements());
//}
    

	public IAction[] createActions(IConfigurationElement[] foo){
		return null;
	}
	
	/**
	 * Returns extension data for all the C/C++ type wizards contributed to the workbench.
	 *     <wizard
	 *         name="My C Wizard"
	 *         icon="icons/cwiz.gif"
	 *         category="org.eclipse.cdt.ui.newCWizards"
	 *         id="xx.MyCWizard">
	 *         <class class="org.xx.MyCWizard">
	 *             <parameter name="ctype" value="true" />
	 *         </class> 
	 *         <description>
	 *             My C Wizard
	 *         </description>
	 *      </wizard>
	 * 
	 * @return an array of IConfigurationElement
	 */
	//public static IConfigurationElement[] getTypeWizardElements() {
	public static IConfigurationElement[] getParallelAnalysisActionElements() {
		/*
		List elemList = new ArrayList();
	    IConfigurationElement[] elements = getAllWizardElements();
	    for (int i = 0; i < elements.length; ++i) {
			IConfigurationElement element = elements[i];
			if (isTypeWizard(element)) {
			    elemList.add(element);
            }
	    }
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
		*/
		return null;
	}
	/*
	public static IConfigurationElement[] getAllWizardElements() {
		List elemList = new ArrayList();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, PL_NEW);
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];
				if (element.getName().equals(TAG_WIZARD)) {
				    String category = element.getAttribute(ATT_CATEGORY);
				    if (category != null &&
				        (category.equals(CUIPlugin.CCWIZARD_CATEGORY_ID)
				           || category.equals(CUIPlugin.CWIZARD_CATEGORY_ID))) {
			            elemList.add(element);
				    }
				}
			}
		}
		return (IConfigurationElement[]) elemList.toArray(new IConfigurationElement[elemList.size()]);
	}
    */
}
