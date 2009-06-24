import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Binder;

public class VPGStats extends CmdLineBase
{
    public static void main(String[] args) throws CoreException
    {
        PhotranVPG.getInstance().start();
        
        PhotranVPG.getInstance().log.printOn(System.out);
        System.out.println();
        Binder.printStatisticsOn(System.out);
        System.out.println();
        PhotranVPG.getDatabase().printStatisticsOn(System.out);
    }
}
