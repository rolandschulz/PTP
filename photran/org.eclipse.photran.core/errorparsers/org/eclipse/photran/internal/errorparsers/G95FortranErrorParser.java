package org.eclipse.photran.internal.errorparsers;
import java.util.Stack;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
 * g95 Error Parser -- An error parser for g95
 * 
 * @author Brian Foote 
 */
public class G95FortranErrorParser implements IErrorParser
{
	private IErrorState state;
	private String fileNameString;
	private int lineNumber;
	private Stack lines;

	private interface IErrorState
	{
		//Inner classes see their enclosing instances, so there
		//is no need for a "context" parameter...
		public boolean processLine(String line, ErrorParserManager epm);
	}

	private class InFileSearchState implements IErrorState
	{
		public boolean processLine(String line, ErrorParserManager epm)
		{
			//Andy says there might be a leading blank...
			if (line.startsWith("In file") || line.startsWith(" In file"))
			{
				String[] tokens = line.split(" ");
				tokens = tokens[2].split(":");
				fileNameString = tokens[0];
				String lineNumberString = tokens[1];
				lineNumber = Integer.parseInt(lineNumberString);
				//We'll keep all the lines from here to the Error/Warning line...
				lines = new Stack();
				lines.push(line);
				//Skip the include lines and the column indication for the moment...
				state = new ErrorOrWarningSearchState();
			}
			return false;
		}
	}

	private class ErrorOrWarningSearchState implements IErrorState
	{
		public boolean processLine(String line, ErrorParserManager epm)
		{
			//Andy says there might be a leading blank here too. 
			//I haven't seen 'em...
			if (line.startsWith("Error")
				|| line.startsWith(" Error")
				|| line.startsWith("Warning")
				|| line.startsWith(" Warning"))
			{
				IFile file = epm.findFilePath(fileNameString);
				int severity =
					(line.startsWith("Error") || line.startsWith(" Error"))
						? IMarkerGenerator.SEVERITY_ERROR_RESOURCE
						: IMarkerGenerator.SEVERITY_WARNING;
				//Generate and plant a marker for the message...
				epm.generateMarker(file, lineNumber, line, severity, null);
				//Now, grab the error and marker line. Sit on this information for now...
				//Markers are listed with the last one planted shown first, for some reason...
				//We produce only the one above for now. 
				//Since leading blanks are elided before we see these lines, 
				//we can't count columns. Tabs are an issue here too...
				
				/*String markerLine = (String)*/ lines.pop();
				/*String codeLine = (String)*/ lines.pop();
				
				//Revert to hunting for fresh errors...
				state = new InFileSearchState();
				//Shows up in the console of the debugging session...
				//System.out.println("Hello? " + line);
			}
			else
			{
				//Push this line if it's not the last line for now...
				lines.push(line);
			}
			return false;
		}
	}

	public G95FortranErrorParser()
	{
		//Start in the state where we try to match "In file"...
		state = new InFileSearchState();
	}

	public boolean processLine(String line, ErrorParserManager epm)
	{
		//All we do is pass the buck to our current state's version
		//of process line. It's handy to put a catch-all here, rather
		//than in each state...
		try
		{
			return state.processLine(line, epm);
		}
		catch (Throwable e)
		{
			//Eat whatever is thrown at us...
		}
		return false;
	}
}