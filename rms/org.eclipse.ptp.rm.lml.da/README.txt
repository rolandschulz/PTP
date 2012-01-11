#*******************************************************************************
#* Copyright (c) 2011 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 


LML_da: LML data access tool
-----------------------------

W.Frings, Forschungszentrum Juelich GmbH, 17 May 2011

Introduction
------------

This tool is part of the new remote system monitoring views of PTP
displaying the status and the batch system usage of the remote
systems.  The new monitoring views are adaption of the new Java based
version of LLview (see also http://www.fzj-juelich.de/jsc/llview).

LML_da extracts from the resource management system of the remote
system information about the batch system usage and store it in a XML
file in LML format. Depending on the availability and access rules
following information will be obtained by LML_da:

 - nodes managed by batch system
 - batch jobs running and waiting in queues
 - information about reservation, rules for job-queue 
   mapping and queue-node mapping 
 - system state (failed components, ...)
 - additional information, e.g. power usage, 
   I/O-throughput, ... (depending on system)   
 - ...


LML_da is a (set of) Perl scripts which uses the standard batch system
query functions to get the above described information. It depends on
the access right given to the user's account under which LML_da is
executed, which information could be obtained.

For large system the queries could take a long time because
information about ALL jobs and ALL nodes of the system are
requested. To prevent an overload of the batch system management
daemons LML_da should be installed by system administrator and ran
only in one instance on the system via crontab (e.g. one a minute).

The following sections will describe the LML-format created by
LML_da, the internal structure of LML_da and the format of the
input files needed to run LML_da.

The LML format:
---------------

LML is the XML format used to describe the machine status of the
remote system. A more detailed description of that format can be found
under:

http://llview.zam.kfa-juelich.de/LML/OnlineDocumentation/lmldoc.html

The structure of LML is specified with a XML schema definition file
which is also available on that web page. In addition, also a
validation tool for LML is available which allows to check LML files
during development of new system adapters.

LML can be used to store different kind of data. LML_da uses LML in
two steps. In the first step LML will be used to store information in
raw format, including only objects and info-data elements.  In the
second step the LML files will also have display elements like tables
and node displays containing the information in form which can
directly be used by the monitoring views of the client.


LML_da structure:
-----------------

The generation of the LML data is processed in a sequence of steps,
executed by the main script LML_da.pl:

    ./LML_da.pl <config-file>

The main steps are the calling the driver scripts, combining the output files,
adding unique color information and generating of the LML intended to use by
the monitoring client client.

1. Driver
---------

To support different types of batch systems LML_da has in addition to
the general a batch system related layer:
    
     - driver scripts for Torque: 
       ./TORQUE/da_<infotype>_LML.pl

     - driver scripts for LoadLeveler: 
     - ./LL/da_<infotype>_LML.pl

     - driver scripts for BlueGene/P running Loadleveler: 
     - ./BGP/LL/da_<infotype>_LML.pl
     - ./BGP/DB2/da_<infotype>_LML.pl
     - ./BGP/RAW/da_<infotype>_LML.pl
 
     - ...	   

Each of the driver scripts could are executed from LML_da.pl and
collects typically one kind of data.  For example,
./TORQUE/da_jobs_info_LML.pl runs the torque utility 'qstat -f' to get
information about running and queued jobs of all users.  Each script
will store the resulting data in a separate LML file, using LML
objects and info-data elements.

Remark: An advantage of separating the queries to individual scripts
is that they are simple to implement and adapt to other or newer
version of batch systems. For supported batch-systems the script has
to be adapted marginally, e.g. if the batch system is not installed in
the default location. Also, if the batch system attributes are changed
or added on a specific system, it could be adapted in the driver
scripts directly. For this, each script contains at table defining the
mapping between attribute names given by batch system and those
accepted by LML.


2. Combiner
-----------

Next, these LML output files of the driver scripts will be combined to
one LML file. This will be done by the LMLcombiner, called from
LML_da: 

     - ./LMLcombiner/LML_combine_obj.pl -o LML_rawfile.xml <files>

Depending on the system type the combiner will also combine or adjust
attributes and check if all mandatory attributes for jobs and nodes
are available.


3. AddColor
-----------

With the next step, a unique color will be defined for all objects of the the
LML raw file. This will be done by the LML_color_obj, called from LML_da:

     - $instdir/LML_color/LML_color_obj.pl -colordefs $instdir/LML_color/default.conf 
       						 -o LML_rawfile_c.xml  LML_rawfile.xml

The reason why the color information are added in this early step of the
workflow is, that for a certain job the color will not changed from one update
to the next update. The node display using this colors will therefore not
'flashes' every update.


4. LML2LML
-----------

In this step the LML file will be generated which is intended to use
by the monitoring client. This will be done by the LML2LML
utility:

     - /LML2LML/LML2LML.pl -v -layout layout.xml -output LML_file.xml LML_rawfile.xml

This resulting LML file will contain for each view a separate element, like <table>
or <nodedisplay> according to the elements requested in the layout file. 



LML_da input files:
-------------------

There are two input files needed to process the LML data. The first
one is the input file of LML_da.pl describing the steps which
should be executed. 

LML_da.pl can handle workflows described by steps and dependencies
between those steps. A simple example is default configuration script
for Torque, defining the four main steps described above.

--------------------------------------------------------------------------------------------
<LML_da_workflow>

<!--- predefined vars: 
      $instdir="./da"
      $stepinfile                 # file in $tmpdir filename contains id of predecessor step
      $stepoutfile                # file in $tmpdir filename contains current id
  -->
<vardefs>
  <var   key="tmpdir"        value="./LMLtmp" />              
  <var   key="permdir"       value="./LMLperm" />             
</vardefs>

<step
   id           = "getdata" 
   active       = "0"
   exec_after   = ""            
   type         = "execute" 
   >            
  <cmd exec="TORQUE/da_system_info_LML.pl               $tmpdir/sysinfo_LML.xml" />
  <cmd exec="TORQUE/da_jobs_info_LML.pl                 $tmpdir/jobs_LML.xml" />
  <cmd exec="TORQUE/da_nodes_info_LML.pl                $tmpdir/nodes_LML.xml" />
</step>

<step
   id           = "combineLML" 
   active       = "1"
   exec_after   = "getdata"               
   type         = "execute" 
   >            
  <cmd exec="$instdir/LMLcombiner/LML_combine_obj.pl -dbdir $permdir/db -v -o $stepoutfile 
                                                            $tmpdir/sysinfo_LML.xml
                                                            $tmpdir/jobs_LML.xml 
                                                            $tmpdir/nodes_LML.xml "
                                                            />
</step>

<step
   id           = "addcolor" 
   active       = "1"
   exec_after   = "combineLML"          
   type         = "execute" 
   >            
  <cmd exec="$instdir/LML_color/LML_color_obj.pl -colordefs $instdir/LML_color/default.conf 
       						 -o         $stepoutfile $stepinfile" />
</step>

<step
   id           = "genLML_std" 
   active       = "1"
   exec_after   = "addcolor"		
   type         = "execute" 
   >		
  <cmd exec="$instdir/LML2LML/LML2LML.pl -v -layout $permdir/layout_std.xml -output $stepoutfile $stepinfile" />
</step>

<step
   id           = "cppermfile1" 
   active       = "1"
   exec_after   = "genLML_std"          
   type         = "execute" 
   >            
  <cmd exec="cp $stepinfile  $permdir/cluster_LML.xml" />
  <cmd exec="/usr/bin/gzip -c -9 $stepinfile > $permdir/cluster_LML.xml.gz" />
</step>

</LML_da_workflow>
--------------------------------------------------------------------------------------------

Due to the different call of the driver script this input file is
batch system related. Therefore, a specific input file for each of the
supported batch system is provided. 

The advantage of describing the process of generating as a workflow
is, that is allows to specify more complex workflows which stores for
example the LML file directly on a web server, store the data in a
history data base, or split the workflow in different part to execute
some steps on another server.

A second input file is needed in the last step of the LML data
generation (LML2LML). This input file (layout) describes the elements
(tables, ...) which should be generated from the raw LML
data. 

The following short example shows the layout definition of a table,
containing the list of running jobs. The layout definition itself is
also defined according to the LML schema and will also be part of the
resulting result LML file.

------------------------------------------------------------------------------------------------------
<tablelayout id="tl_RUN" gid="joblist_RUN">
 <column cid="1" pos="0" width="0.3" active="true" key="totalcores" />
 <column cid="2" pos="1" width="0.2" active="true" key="owner" />
 <column cid="3" pos="2" width="0.2" active="true" key="queue" />
 <column cid="4" pos="3" width="0.1" active="true" key="wall" />
 <column cid="5" pos="5" width="0.1" active="true" key="queuedate" />
 <column cid="6" pos="6" width="0.1" active="true" key="dispatchdate" />
</tablelayout>
------------------------------------------------------------------------------------------------------

For each batch system a default layout file is provided defining two
tables (running and waiting jobs) and the node display. In future
versions the layout definitions will be part of the request sent from
the client to LML_da to support dynamic refinement of the view or
selecting more table rows.

To display the nodes of the system (node display) LML2LML has to know
the how the system is structured in sets of elements (e.g. in
rack,nodes,cores, ...). This will be internally used to build a
hierarchical data structures to storing these elements (tree). For
some systems these structure is directly given, e.g. IBM Blue Gene/P
and the position in the tree structure can obtained by evaluating the
name of the node. For other system, like clusters of SMP node, the
arrangement of node into racks can typically not be obtained from the
batch system information. Therefore LML2LML will accept besides the
layout section of a node display also the scheme part of the node
display, which describes the physical hierarchy of the system. If this
scheme is given LML2LML will sort the nodes according this information
into the tree. If the scheme is not specified (default), LML2LML will
try to sort the nodes in a two0-level tree, containing nodes in the
first level and cores in the second level.

Below is shown an example of the scheme definition for Blue Gene/P,
which spans up a tree of 6 levels.

 <scheme>
      <el1 tagname="row" min="0" max="8" mask="R%01d">
          <el2 tagname="rack" min="0" max="7" mask="%01d">
              <el3 tagname="midplane" min="0" max="1" mask="-M%1d">
                  <el4 tagname="nodecard" min="0" max="15" mask="-N%02d">
                      <el5 tagname="computecard" min="4" max="35" mask="-C%02d">
                          <el6 tagname="core" min="0" max="3" mask="-%01d">
                          </el6>
                      </el5>
                  </el4>
              </el3>
          </el2>
      </el1>
 </scheme>

Prerequisites:
--------------

Following Perl modules will be used by LML_da.pl:

- XML::Simple
- String::Scanf

If these modules are not part of the Perl standard installation, 
packages could be found at CPAN (http://search.cpan.org/).



LLVIEW Data Access Workflow Manager Driver
------------------------------------------

The script LML_da_driver.pl will be used as a driver script for
calling LML_da on the remote site via ssh from monitoring part in
the PTP client, auto-configuration of LML_da (selecting handler
for rms on remote site) and handling of temporary files and
directories.

Usage: 
      LML_da_driver.pl <options> <requestfile> <outputfile>
 or
      LML_da_driver.pl <options> < <requestfile> > <outputfile>

      -rawfile <LML raw file>  : use LML raw file as data source
                                 (default: query system)
      -tmpdir  <dir>           : use this directory for temporary data
                                 (default: ./tmp) 
      -keeptmp                 : keep temporary directory
      -verbose                 : verbose mode
      -quiet                   : prints no messages on stderr


ChangeLog:
----------

1.01:
   - Color manager
   - ...

1.02:
   - LML_da could now handle request containing a LML scheme to describe
     the physical machine layout
   - LML_da new option -demo to generate anonymous job data 
   - Date and time attributes are converted to LML std format
   - redirect log and debug output to stderr instead of stdout

1.03:
   - added driver script LML_da_driver.pl
     - called from from monitoring part of the PTP client via ssh,
     - calls LML_da on the remote site 
     - auto-configuration of LML_da (selecting handler for rms on remote site) 
     - handling of temporary files and directories

1.04:
   - pattern matching in generation of table
   - rms:TORQUE: job status according PTP job status 
   - removed dependency to XML-Parser module
   - bugfix in gen_nodedisplay

1.05:
   - bug fixes 
     - calling perl from perl with correct path
     - parsing PTP related namespace in XML file

1.06:
   - new adapter for OpenMPI
   - bug fixes 
     - name space handling in LML

1.07:
   - bug fixes 
     - built-in parser enhancement
     - recognize empty scheme descriptions

1.08:
   - default_layout adapted (titles, removed inactive tables)
   - title for nodedisplay contains now hostname of remote system
   - LML_da_drives handles now exit messages by generating LML 
     file containing the messages
   - bug fixes:	
      - generates now no data cells for not active columns
      - LL-drives handles now job status correctly

1.09:
   - LML_da_driver manages now a permanent directory (./perm) 
   - persistent color management
     - color db will be stored in a file in the perm-directory
   - first driver for PBSpro controlled systems

1.10:
   - bug fixes

1.11:
   - bug fixes

1.12:
   - bug fixes

1.13:
   - improved reporting: option -verbose control debug output on stderr
     	      		 full debugging report will written $tmpdir/report.log
   - improved support for PBS (vnodes)
   - bug fixes

1.14:
   - Improvements for testing and demonstration purpose 
     - option -test: use existing workflow input in tmpdir
     - options specification file .LML_da_options 
       - will be read if exists in current directory
       - will overwrite command line option
    - incoporated support for new RMS (GridEngine, PE)
      (contributed by others)   
