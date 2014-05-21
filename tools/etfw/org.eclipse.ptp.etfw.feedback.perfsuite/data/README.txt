
To test the PTP ETFw PerfSuite Feedback View plugin with the
"testfeedback" example, you can either use the existing "mhpr.xml"
file in this directory, or generate one in a "live" mode.  Using
the existing one is likely simpler.

1) Use the existing one -- the simpler approach

In Eclipse, create a C project with the three files "Makefile",
"mhpr.xml" and "testfeedback.c" in this directory.  Then, manually
change the file path name in the "mhpr.xml" file from
        /tmp/testfeedback
to the path name corresponding to your Eclipse project created above,
so that the PerfSuite Feedback View plugin can successfully find the
files.  
  ==>Change within eclipse:

     (i) determine exact path to file in your workspace project:
         select a file in the project e.g. mhpr.xml - rightmouse, properties
         In Resource section (shows by default usually) not the "Location" which will be something like
           /home/user1/workspace/project/mhpr.xml
     (ii) edit the mhpr.xml file:
         Double click on mhpr.xml file to edit within eclipse 
           if you are using the XML tools with the XML editor, you should switch to the "Source" tab to edit raw xml source.
                  (or a plain text editor works too)
         Menu: Edit, Find-Replace, or use Ctrl-F, fill in values, select "Replace All" ( or do them indiviually)
  
  ==>Change with command line tools:
    One way to do it with sed is:
    cp mhpr.xml mhpr.xml.0
    sed -i -e 's|/tmp/testfeedback|/home/user1/workspace/testfeedback|g' mhpr.xml
    diff mhpr.xml.0 mhpr.xml
    
Then, select the file "mhpr.xml" in the project explorer window,
right click the mouse.  In the context menu, select "Display PerfSuite
Feedback".  You should see a view named "PerfSuite Feedback Items"
opened.  Double-clicking the lines in the view will bring the file
in and focus on the corresponding lines.

2) Generate a mhpr.xml in a "live" mode -- more complicated approach

For the live mode, you need a working PerfSuite installation and
MPI environment.  In Eclipse, create a C project with the two files
"Makefile" and "testfeedback.c".  Make sure "mpicc", "mpirun",
"psrun" and "psprocess" are in your paths.  Then run "make" either
outside of Eclipse or from within Eclipse to build, run and process
the generated PerfSuite XML files.  The final generated file is
named "mhpr.xml".

Then, select the file "mhpr.xml" in the project explorer window,
right click the mouse.  In the context menu, select "Display PerfSuite
Feedback".  You should see a view named "PerfSuite Feedback Items"
generated.  Double-clicking the lines in the view will bring the file
in and focus on the corresponding lines.

