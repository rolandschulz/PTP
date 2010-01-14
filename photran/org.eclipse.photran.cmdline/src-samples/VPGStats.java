import org.eclipse.core.resources.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

public class VPGStats extends CmdLineBase
{
    public static void main(String[] args) throws CoreException
    {
        Util.DISPLAY_WARNINGS = false;
        PhotranVPG.getInstance().start();
        
        PhotranVPG.getInstance().log.printOn(System.out);
        System.out.println();
        Binder.printStatisticsOn(System.out);
        System.out.println();
        PhotranVPG.getDatabase().printStatisticsOn(System.out);
        System.out.println();
        PhotranVPG.getInstance().printModuleSymTabCacheStatisticsOn(System.out);
        
        // We should do this, but we'll omit it for better profiling of the indexer
        //PhotranVPG.getDatabase().close();
    }
}
