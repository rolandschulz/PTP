package org.eclipse.ptp.internal.etfw.launch;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX = Activator.getUniqueIdentifier() + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	// Plug-in Image Registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	private static URL fgIconBaseURL;
	static {
		fgIconBaseURL = Platform.getBundle(Activator.getUniqueIdentifier()).getEntry("/icons/"); //$NON-NLS-1$
	}

	public static final String IMG_PERFORMANCE_TAB = NAME_PREFIX + "performance_tab.png"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_PERFORMANCE_TAB = createManaged(IMG_PERFORMANCE_TAB);

	public static Image getImage(String key) {
		return imageRegistry.get(key);
	}

	private static ImageDescriptor createManaged(String name) {
		return createManaged(imageRegistry, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);

		return result;
	}

	private static URL makeIconFileURL(String name) {
		try {
			return new URL(fgIconBaseURL, name);
		} catch (MalformedURLException e) {
			Activator.log(e);
			return null;
		}

	}
}
