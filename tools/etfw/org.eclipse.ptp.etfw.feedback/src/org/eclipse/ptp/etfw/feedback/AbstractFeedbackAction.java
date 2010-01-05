package org.eclipse.ptp.etfw.feedback;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.etfw.feedback.obj.IFeedbackItem;

/**
 * Extend this class to add an action to the toolbar, an action that can be
 * performed on the selected item
 * 
 * @author beth tibbitts
 * 
 */
public abstract class AbstractFeedbackAction {
	private String iconName;
	private String tooltip;
	/**
	 * What is a better arg to run(), the IFeedbackItem, or the IMarker? We'll ask for
	 * implementation of both for now.
	 * If it's the IFeedbackItem, we will have to cache that in the marker object.
	 * 
	 * @param item
	 */
	abstract public void run(IMarker marker);

	abstract public void run(IFeedbackItem item);
	
	abstract public String getPluginId();

	public void addIcon(String iconName) {
		this.iconName=iconName;;
	}
	public String getIcon() {
		return this.iconName;
	}
	public ImageDescriptor getIconImageDescriptor() {
		ImageDescriptor imgDesc=Activator.imageDescriptorFromPlugin(getPluginId(), iconName);
		return imgDesc;
	}

	abstract public String getToolTip();
	/** get text e.g. could be used for a menu item for this action */
	abstract public String getText();

}
