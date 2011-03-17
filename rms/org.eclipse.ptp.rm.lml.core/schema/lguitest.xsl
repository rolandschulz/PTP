<?xml version="1.0" ?>
<!-- 
          LML - LLView markup language 

   Copyright (c) 2011 Forschungszentrum Juelich GmbH
   All rights reserved. This program and the accompanying materials
   are made available under the terms of the Eclipse Public License v1.0
   which accompanies this distribution and is available at
   http://www.eclipse.org/legal/epl-v10.html
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				  xmlns="http://www.w3.org/1999/XSL/Transform"
				  xmlns:lml="http://www.llview.de"
>

<!-- This XSL-Stylesheet can be used for further constraint-checkings
for lml-files. It checks all constraints, which cannot be checked by
the corresponding xml-schema because xpath-expressions are restricted
in xml-schema for optimization

It prints out warnings and errors within the lml-instance-->

<output method="text" indent="yes"/>
<!-- regular expression by which date-values of tables are checked -->
<!-- example 07/20/10 11:40:57 -->
<variable name="dateformat" select="'^\d{2}/\d{2}/\d{2}\s*(\d{2}:\d{2}:\d{2})?$'"/>
	
<!-- works like a function. this template must be called once for every nodedisplay
this template makes sure that min and max-attributes are defined correctly-->
<template name="nodedisplaycheck" match="nodedisplay">
		<!-- first checks are the same for base and referencing nodedisplays-->
		<!-- check min and max against min<=max in scheme and in data-->
		<for-each select="scheme//*[@min>@max]">
	
			Error: in scheme-tag: min-attribute must be lower then max-attribute <value-of select="name()"/> min=<value-of select="@min"/> max=<value-of select="@max"/>
	
		</for-each>

		<for-each select="data//*[@min>@max]">
	
			Error: in data-tag: min-attribute must be lower then max-attribute <value-of select="name()"/> min=<value-of select="@min"/> max=<value-of select="@max"/>
	
		</for-each>

		<!-- every element needs to define at least min or list-->
		<for-each select="*//*[count(@min)=0 and count(@list)=0]">

			Error: in Tag <value-of select="name()"/> neither min nor list defined

		</for-each>

		<!-- if min is defined list is not allowed and vice versa-->
		<for-each select="*//*[count(@min)>0 and count(@list)>0]">

			Error: in Tag <value-of select="name()"/> min and list are defined, but you are only allowed to define exactly one

		</for-each>

		<!-- not allowed: max and ! min -->
		<for-each select="*//*[count(@max)>0 and count(@min)=0]">

			Error: in Tag <value-of select="name()"/> if max is defined, you have to specify min, too

		</for-each>

		<!-- not allowed: step and ! min -->
		<for-each select="*//*[count(@step)>0 and count(@min)=0]">

			Error: in Tag <value-of select="name()"/> if step is defined, you have to specify min, too

		</for-each>

		<!--in data: elX-element is only allowed if at least one elX-element appears in scheme-tag-->
		<variable name="ascheme" select="scheme"/>
		<variable name="adata" select="data"/>

		<for-each select="data//*">
			<variable name="aname" select="name()"/>

			<if test="count($ascheme//*[name()=$aname])=0">
			Error: in data in tag <value-of select="$aname"/>, this is not allowed for this scheme, to deep tree-level
			</if>
		</for-each>
		
		<!-- check tagnames, at least one for every level -->
		<for-each select="distinct-values(scheme//*/name())">
			
			<variable name="alevelname" select="."/>
			
			<if test="count( $ascheme//*[name() = $alevelname and @tagname] ) = 0">
			Error: scheme-level <value-of select="$alevelname"/> does not define a tagname
			</if>
			
		</for-each>
		
		<!-- check map-attribute: count of names must be greater than count of elements, so that implicit name identify elements -->
		<for-each select="scheme//*[@map]">
		
			<variable name="mapcount" select="count(tokenize(@map, ','))"/><!-- could be zero if map="" ATTENTION -->
			
			<choose>
			<when test="@min">
				<choose>
				<when test="@step">
					
					<if test="@max">
						<variable name="listcount" select="floor((@max - @min) div @step) + 1 "/>
						
						<if test="$listcount > 1 and $listcount>$mapcount">
			Error: amount of defined names in map-attribute is lower than number of elements within this list of elements in tag <value-of select="name()"/>. defined elements=<value-of select="$listcount"/>, defined names=<choose><when test="$mapcount=0">1</when><otherwise><value-of select="$mapcount"/></otherwise></choose>
						</if>
						
					</if><!-- otherwise there is only one element. thus map defines at east one element, there is no problem -->
					
				</when>
				<otherwise><!-- no step defined -> step=1-->
				
					<if test="@max">
						<variable name="listcount" select="(@max - @min) + 1"/>
						
						<if test="$listcount > 1 and $listcount>$mapcount">
			Error: amount of defined names in map-attribute is lower than number of elements within this list of elements in tag <value-of select="name()"/>. defined elements=<value-of select="$listcount"/>, defined names=<choose><when test="$mapcount=0">1</when><otherwise><value-of select="$mapcount"/></otherwise></choose>
						</if>
						
					</if><!-- otherwise there is only one element. thus map defines at east one element, there is no problem -->
				</otherwise>
				</choose>
			
			</when>
			<otherwise><!-- list-attribute -->
				<variable name="listcount" select="count(tokenize(@list, ','))"/>
				
				<if test="$listcount > 1 and $listcount>$mapcount">
			Error: amount of defined names in map-attribute is lower than number of elements within this list of elements in tag <value-of select="name()"/>. defined elements=<value-of select="$listcount"/>, defined names=<choose><when test="$mapcount=0">1</when><otherwise><value-of select="$mapcount"/></otherwise></choose>
				</if>
				
			</otherwise>
			</choose>	
		
		</for-each>
		
		<!-- either use mask-attribute or map-attribute but not both in one tag -->
		<for-each select="scheme">
			<for-each select=".//*[@mask][@map]">
			Error: in scheme-definition: It is not allowed to use mask- and map-attributes together. element <value-of select="name()"/> has mask=<value-of select="@mask"/> and map=<value-of select="@map"/>
			</for-each>
		</for-each>
		
		<variable name="nodedisplay" select="."/>
		
		<!-- only one mask or map for every level, consider if this is not too much of constraint
		<for-each select="distinct-values(scheme//*/name())">
			<variable name="tagname" select="."/>
			
			<if test="count( distinct-values( $nodedisplay/scheme//*[name()=$tagname]/@mask ) ) + count( distinct-values( $nodedisplay/scheme//*[name()=$tagname]/@map ) ) >1">
			Error: only one mask or map is allowed for every level, more than one mask or map for level <value-of select="$tagname"/>
			</if>
		</for-each>
		-->
		
		<!-- check if data-elements are allowed by given scheme -->
		<!--<for-each select="data/el1">
			<call-template name="data-elements-allowed">
				<with-param name="schemeel" select="$ascheme"/>
				<with-param name="datael" select="."/>
			</call-template>
		</for-each>-->

</template>

<!-- special template for checking if data-elements are allowed by the scheme-element -->
<template name="data-elements-allowed">
	<param name="schemeel" />
	<!--
		upper scheme-level, where right scheme has to be searched
	-->
	<param name="datael" /><!-- data-tag which must be checked -->

	<param name="maxlevel" select="'100'"/><!-- untill this level validity will be checked-->

	<variable name="atagname" select="$datael/name()" />
	<!-- current level -->
	<variable name="alevel" select="number(substring($atagname,3))"/>

	<if test="number($maxlevel)>=$alevel"><!-- stop checking on given level -->
	<choose>
		<!-- element definition with list-attribute?-->
		<when test="$datael/@list">

			<for-each select="tokenize($datael/@list,',')">

				<variable name="atoken" select="." />
				<!--
					check validity of current token
				-->

				<!-- check if token in any list-->
				
				<variable name="schemelists" select="$schemeel/*[name()=$atagname and @list and matches(@list, concat('^(\s*\d+\s*,)*\s*', $atoken , '\s*(,\s*\d+\s*)*$')  )]"/>
				
				<if
					test="count( $schemelists ) =0">
					<!-- no list found for atoken, check ranges-->
					<!-- atoken=@min anywhere? -->
					<variable name="schememins" select="$schemeel/*[name()=$atagname and @min and @min=$atoken]"/>
					
					<if
						test="count( $schememins )=0">
						<!-- no min-tag found which is equal to token-->
						<!-- atoken in [@min, @max] anywhere? -->
						<variable name="schemeranges" select="$schemeel/*[name()=$atagname and @min and @max and $atoken>=@min and @max>=$atoken]"/>

						<if
							test="count( $schemeranges )=0">
							<!-- no min-max-range found-->
			Error: No possible definition for data-element <value-of select="$atoken" /> in tag	<value-of select="$atagname" />							
						</if>
						
						<!-- success -->
						<if test="count( $schemeranges )=1">
							<for-each select="$datael/*">
								<call-template name="data-elements-allowed">
									<with-param name="schemeel" select="$schemeranges"/>
									<with-param name="datael" select="."/>
									<with-param name="maxlevel" select="$maxlevel"/>
								</call-template>
							</for-each>
						</if>
						<!-- denied -->
						<if test="count( $schemeranges ) >1">
			Error: Multiple range definitions for data-tag <value-of select="$atagname"/> and element <value-of select="$atoken"/>
						</if>

					</if>
					<!-- success -->
					<if test="count( $schememins )=1">
						<for-each select="$datael/*">
							<call-template name="data-elements-allowed">
								<with-param name="schemeel" select="$schememins"/>
								<with-param name="datael" select="."/>
								<with-param name="maxlevel" select="$maxlevel"/>
							</call-template>
						</for-each>
					</if>
					<!-- denied -->
					<if test="count( $schememins ) >1">
			Error: Multiple min definitions for data-tag <value-of select="$atagname"/> and element <value-of select="$atoken"/> with min equal to token
					</if>		
				</if>
				
				<!-- is there only one possible schemeel? -->
				<if test="count( $schemelists ) =1">
					
					<for-each select="$datael/*">
						<call-template name="data-elements-allowed">
							<with-param name="schemeel" select="$schemelists"/>
							<with-param name="datael" select="."/>
							<with-param name="maxlevel" select="$maxlevel"/>
						</call-template>
					</for-each>
					
				</if>
				<!-- denied -->
				<if test="count( $schemelists ) >1">
			Error: Multiple list definitions for data-tag <value-of select="$atagname"/> and element <value-of select="$atoken"/>
				</if>
				
			</for-each>

		</when>

		<!-- element definition with min-max-attributes?-->
		<when test="$datael/@min">

			<variable name="amin" select="$datael/@min" />

			<!-- check if amin is in any range -->
			<variable name="schemeels"
				select="$schemeel/*[name()=$atagname and ($amin=@min or ($amin>=@min and @max>=$amin and (not(@step) or (($amin - @min) mod @step = 0)) ) )]" />

			<choose>
				<when test="count( $schemeels )=0"><!-- no range found for amin -->
				
				<!-- now list-scheme-elements must be searched -->
				
				<variable name="schemelists" select="$schemeel/*[name()=$atagname and @list and matches(@list, concat('^(\s*\d+\s*,)*\s*', $amin , '\s*(,\s*\d+\s*)*$')  )]"/>
				<variable name="schemelistswithmax" select="$schemelists[ not($datael/@max) or ( $datael/@max and matches(@list, concat('^(\s*\d+\s*,)*\s*', $datael/@max , '\s*(,\s*\d+\s*)*$')  )  ) ]"/>
				
				<choose>
					<when test="count($schemelistswithmax)=0">
			Error: no scheme-definition found for tag <value-of select="$atagname"/> with min=<value-of select="$amin"/> and max=<value-of select="$datael/@max"/>
					</when>
					<when test="count($schemelists)>1">
			Error: multiple list definitions for tag <value-of select="$atagname"/> with min=<value-of select="$amin"/>
					</when>
					<otherwise>
						<!-- exactly one schemelists and one schemelistswithmax -->
						<!-- count elements in scheme-list -->
						
						<variable name="schemelistcount" select="count( tokenize( $schemelistswithmax/@list, ',' ) )"/>
						
						<choose>
							<when test="$datael/@max">
								<choose>
									<when test="$datael/@max - $datael/@min + 1 > $schemelistcount">
			Error: range in tag <value-of select="$atagname"/> describes more elements than list=<value-of select="$schemelistswithmax/@list"/>  => range [<value-of select="$datael/@min"/>, <value-of select="$datael/@max"/>] not covered by scheme-list 
									</when>
									<otherwise>									
										<variable name="tokensbetween" select="count( distinct-values( tokenize( replace( $schemelistswithmax/@list, '\s+', '' ), ',' )[ . >= $datael/@min and $datael/@max >= .] ) )"/>

										<choose>
											<when test="$datael/@max - $datael/@min + 1 > $tokensbetween">
			Error: range in tag <value-of select="$atagname"/> describes other elements than list=<value-of select="$schemelistswithmax/@list"/>  => range [<value-of select="$datael/@min"/>, <value-of select="$datael/@max"/>] not fully covered by scheme-list
											</when>
											<otherwise>
												<!-- range is fully covered by list -->
												
												<for-each select="$datael/*">
												
													<call-template name="data-elements-allowed">
														<with-param name="schemeel" select="$schemelistswithmax"/>
														<with-param name="datael" select="."/>	
														<with-param name="maxlevel" select="$maxlevel"/>						
													</call-template>
													
												</for-each>
											</otherwise>
										</choose>
										
									</otherwise>
								
								</choose>
							</when>
							<otherwise>
								<!-- no max-attribute and min is in scheme-list -->
								
								<for-each select="$datael/*">
							
									<call-template name="data-elements-allowed">
										<with-param name="schemeel" select="$schemelistswithmax"/>
										<with-param name="datael" select="."/>		
										<with-param name="maxlevel" select="$maxlevel"/>					
									</call-template>
								
								</for-each>
							</otherwise>
						</choose>
					</otherwise>
				
				</choose>			

				</when>
				<when test="count( $schemeels)=1"><!-- one range found, check max -->
					<!--amin is ok and is in a range in $ascheme, is max also ok ?-->

					<variable name="newschemeel" select="$schemeels" />
					<!--
						check if max exists. if so max must be lower then scheme-max or
						equal to scheme-min and max must be a allowed multiple of step
						added to min
					-->
					<choose>
						<when test="$datael/@max and (($newschemeel/@max and $datael/@max>$newschemeel/@max) 
												 or (not($newschemeel/@max) and not($datael/@max=$newschemeel/@min))
												 or ($newschemeel/@step and not(($datael/@max - $newschemeel/@min) mod  $newschemeel/@step = 0) ) )">
	
							<!-- there is a range, where min is in but not max -->
			Error: in element <value-of select="$atagname" /> in data-tag. Range is not allowed corresponding to scheme-range. min-attribute is ok but max-attribute not: data-range: [<value-of select="$datael/@min" />, <value-of select="$datael/@max" />] and scheme-range: [<value-of select="$newschemeel/@min" />, <value-of select="$newschemeel/@max" />], step =	<value-of select="$newschemeel/@step" />
						</when>
						<otherwise>
							<!-- everything ok $datael is defined in $newschemeel -->
							
							<for-each select="$datael/*">
							
								<call-template name="data-elements-allowed">
									<with-param name="schemeel" select="$newschemeel"/>
									<with-param name="datael" select="."/>	
									<with-param name="maxlevel" select="$maxlevel"/>						
								</call-template>
							
							</for-each>
							
						</otherwise>
					</choose>

				</when>
				<otherwise><!-- multiple ranges found -->
			Error: multiple possible ranges for element	<value-of select="$atagname" />	in data-tag. data-range: [<value-of select="$datael/@min" />, <value-of select="$datael/@max" />]
				</otherwise>
			</choose>
		</when>

	</choose><!--  list or min-max-attributes -->
	</if><!-- test current level against maxlevel -->

</template>

<!-- give connected id for a cell, columns and the value of the cell and this will check constraints -->
<template name="typecheckforcell">
<param name="aid"/><!-- id of column to which the cell is connected -->
<param name="value"/><!-- value which must be checked -->
<param name="acolumns"/><!-- all column definitions in the table -->

<variable name="atype" select="$acolumns[@id=$aid]/@sort"/>

<if test="$atype = 'date'">
		<if test="not( matches( $value, $dateformat ) )">
	Error: the date-value <value-of select="$value"/> does not match the given dateformat: <value-of select="$dateformat"/>	
		</if>
</if>

<!-- checks for other types numeric, alpha -->

</template>


<!-- main template, which calls the other templates at the right place -->
<template match="lml:lgui">

<if test="not(@layout='true')">
------------------------------------------------------------
Checking tables:

<for-each select="./table">
	<!-- id of current table-->
	<variable name="table" select="@id"/>

	<!--Save column-objects of current table-->
	<variable name="acolumns" select="column"/>

	<for-each select="row">

		<!-- position of current row-->
		<variable name="row" select="position()"/>

		<!-- test if too many cells in a row -->
		<if test="count(cell)>count($acolumns)">
	Error: Too many cells in row <value-of select="$row"/> in table <value-of select="$table"/>, #cells=<value-of select="count(cell)"/> allowed #=<value-of select="count($acolumns)"/>
		</if>
		
		<!-- save current row-node -->
		<variable name="rownode" select="."/>
		<!-- check implicit ids given by position and preceding-sibling-cids for uniqueness and being allowed by column-definitions -->
		<for-each select="cell">
			<!-- save current position among all cells of this row -->
			<variable name="apos" select="position()"/>
			<!-- problem only occures if cid is not defined -->
			<choose>
			<when test="not(@cid)">
				
				<choose>
					<!-- Look for preceding siblings with cids -->
					<when test="count( preceding-sibling::cell[@cid] ) >= 1">
						<!-- get cid of nearest preceding sibling -->					
						<variable name="prevcid" select="preceding-sibling::cell[@cid][position() = 1]/@cid" />
						<!-- get distance between current cell and nearest preceding sibling with cid defined -->
						<variable name="distance" select="$apos - ( count( $rownode/cell[@cid=$prevcid]/preceding-sibling::cell ) + 1 )"/>
						<!--  calculate implicit cid -->
						<variable name="acid" select="$prevcid + $distance"/>
						<!--  is this acid allowed by column-definitions? -->
						<if test="count( $acolumns[@id = $acid] ) = 0">
	Error: implicit cid of cell <value-of select="$apos" /> is not allowed for columns of table <value-of select="$table"/> in row <value-of select="$row"/>
						</if>
						<!-- is acid a duplicate referencing cid?-->
						<if test="count( $rownode/cell[@cid=$acid] ) > 0">
	Error: implicit cid of cell <value-of select="$apos"/> is already used by cell with position <value-of select="count( $rownode/cell[@cid = $acid]/preceding-sibling::cell ) + 1 "/> in table <value-of select="$table"/> in row <value-of select="$row"/>
						</if>
						
						<!-- check values -->
						<call-template name="typecheckforcell">
							<with-param name="aid" select="$acid"/>
							<with-param name="value" select="@value"/>
							<with-param name="acolumns" select="$acolumns"/>
						</call-template>						
						
					</when>
					
					<!-- no preceding cid defined => position is important position()=acid=apos -->
					<otherwise>
						<!--  is acid/apos allowed by column-definitions? -->
						<if test="count( $acolumns[@id = $apos] ) = 0">
	Error: implicit cid of cell <value-of select="$apos" /> is not allowed for columns of table <value-of select="$table"/> in row <value-of select="$row"/>
						</if>
						<!-- is acid/apos a duplicate referencing cid?-->
						<if test="count( $rownode/cell[@cid=$apos] ) > 0">
	Error: implicit cid of cell <value-of select="$apos"/> is already used by cell with position <value-of select="count( $rownode/cell[@cid = $apos]/preceding-sibling::cell ) + 1 "/> in table <value-of select="$table"/> in row <value-of select="$row"/>
						</if>
						
						<!-- check values -->
						<call-template name="typecheckforcell">
							<with-param name="aid" select="$apos"/>
							<with-param name="value" select="@value"/>
							<with-param name="acolumns" select="$acolumns"/>
						</call-template>						
						
						<!-- calculate all acid with preceding cid and compare them with apos -->
						
						<for-each select="$rownode/cell">
						
							<variable name="apos2" select="position()"/>
						
							<if test="not(@cid)">
								<!-- Look for preceding siblings with cids -->
								<if test="count( preceding-sibling::cell[@cid] ) >= 1">
									<!-- get cid of nearest preceding sibling -->					
									<variable name="prevcid" select="preceding-sibling::cell[@cid][position() = 1]/@cid" />
									<!-- get distance between current cell and nearest preceding sibling with cid defined -->
									<variable name="distance" select="$apos2 - ( count( $rownode/cell[@cid=$prevcid]/preceding-sibling::cell ) + 1 )"/>
									<!--  calculate implicit cid -->
									<variable name="acid" select="$prevcid + $distance"/>
									
									<!-- is this implicit cid equal to surrounding implicit cid given by a position? -->
									<if test="$acid=$apos">
	Error: two implicit cids are the same: cells <value-of select="$apos"/> and <value-of select="$apos2"/> in table <value-of select="$table"/> in row <value-of select="$row"/>
									</if>

										
								</if>
							</if>
						
						</for-each>
						
					</otherwise>
				</choose>
			</when>
			<otherwise><!-- @cid is defined -->
				
				<!-- check values -->
				<call-template name="typecheckforcell">
					<with-param name="aid" select="@cid"/>
					<with-param name="value" select="@value"/>
					<with-param name="acolumns" select="$acolumns"/>
				</call-template>				
				
			</otherwise>
			</choose>
		
		</for-each>

	</for-each>

</for-each>
</if>

<if test="not(@layout='true')">
------------------------------------------------------------
Checking tablelayouts:

<for-each select="tablelayout">

<variable name="layoutid" select="@id"/>
<variable name="gid" select="@gid"/>
<variable name="orgtable" select="/lml:lgui/table[@id=$gid]"/>

<!-- check if cid-attributes of tablelayout go with id-attributes of table  -->
<for-each select="column[not(@active='false')]/@cid">

	<variable name="acid" select="."/>
	<variable name="countids" select="count( $orgtable/column[@id=$acid] )" />
	
	<if test="$countids=0">
	Error: in tablelayout <value-of select="$layoutid"/>: cid-attribute <value-of select="$acid"/> is not allowed for table <value-of select="$orgtable/@id"/>
	</if>	

</for-each>

<!-- check pos in {0..#columns-1}. pos-attribute can not comprise a value higher then maximum of positions -->
<variable name="maxpos" select="count($orgtable/column)-1"/>

<for-each select="column/@pos">

	<variable name="apos" select="."/>
	<if test="$apos>$maxpos">
	Error: in tablelayout <value-of select="$layoutid"/>: pos-value is not allowed pos=<value-of select="$apos"/>, but pos must be lower or equal to <value-of select="$maxpos"/>
	</if>

</for-each>

<!-- check sorted is only once not equal to "false", because the table can only be sorted by one column -->
<if test="count( column[@sorted!='false'] ) > 1">
	Error: in tablelayout <value-of select="$layoutid"/>: the table can only be sorted by one column, this layout defines <value-of select="count( column[@sorted!='false'] )"/> columns by which the table should be sorted
</if>

</for-each>
</if>

------------------------------------------------------------
Checking infoboxlayouts:

<for-each select="infoboxlayout">

<variable name="layoutid" select="@id"/>

<!-- check sorted is only once not equal to "false", because the table can only be sorted by one column -->
<if test="count( ./*[@sorted!='false'] ) > 1">
	Error: in infoboxlayout <value-of select="$layoutid"/>: the infoboxtable can only be sorted by one column, this layout defines <value-of select="count( ./*[@sorted!='false'] )"/> columns by which the table should be sorted
</if>

</for-each>

------------------------------------------------------------
Checking textlayouts:

<for-each select="textlayout">

<variable name="layoutid" select="@id"/>
<variable name="gid" select="@gid"/>
<variable name="orgtext" select="/lml:lgui/text[@id=$gid]"/>

<!-- check if referred text-tag contains info-tag, otherwise error-->
<if test="not( $orgtext/info )">
	Error: in textlayout <value-of select="$layoutid"/>: textlayouts can only be defined for text-objects with key-value-pairs through info-tag in it
</if>

</for-each>

<if test="not(@layout='true')">
------------------------------------------------------------
Checking usagebars:

<for-each select="usagebar">
	
	usagebar <value-of select="@id"/>

	<!-- total amount of cpucounts -->
	<variable name="total" select="@cpucount"/>

	<!-- cpucount-value must be equal to sum of cpucount-values of inner job-tags -->
	<if test="sum(job/@cpucount) != $total">
		Error: sum of cpucount-attributes of job-tags is not equal to allowed total cpucount given by the cpucount-attribute of the usagebar, total=<value-of select="$total"/>, sum=<value-of select="sum(job/@cpucount)"/>
	</if>

	<!-- cpucount-value of a job-tag must be equal to sum of cpucount-values of inner jobpart-tags -->
	<for-each select="./job">
		<!-- save position of job-tag -->
		<variable name="pos" select="position()"/>
		
		<if test="count(jobpart)>0">

			<variable name="jobtotal" select="@cpucount"/>
	
			<if test="sum(jobpart/@cpucount) != $jobtotal">
		Error: sum of cpucount-attributes in jobparts is not equal to allowed total cpucount given by the cpucount-attribute of the job-tag with oid <value-of select="@oid"/>, total=<value-of select="$jobtotal"/>, sum=<value-of select="sum(jobpart/@cpucount)"/>, job-tag-nr=<value-of select="$pos"/>
			</if>

		</if>
	</for-each>

</for-each>
</if>

<if test="not(@layout='true')">
------------------------------------------------------------
Checking nodedisplays:
	
	Base Nodedisplays:
		<for-each select="nodedisplay[count(@refto)=0]">
		
		Nodedisplay <value-of select="@id"/>
			<!-- call template for nodedisplays-->
			<apply-templates select="."/>

			<!--special checks only for base nodedisplays-->

			<!-- base nodedisplays are not allowed to use refid-attributes-->

			<for-each select=".//*[@refid]">
			Error: base nodedisplays are not allowed to use refid-attributes, but used in <value-of select="name()"/> refid=<value-of select="@refid"/>
			</for-each>

			<!-- base nodedisplay must define an oid for every system-element in the data-tag-->
			<for-each select="data//*[count(@oid)=0]">

			Error: In a basic nodedisplay every data-element must define oid: No oid in <value-of select="name()"/>

			</for-each>
			<!-- oids must refer to objects of type job-->
			<for-each select="data//*">

				<variable name="oid" select="@oid"/>

				<if test="/lml:lgui/objects/object[@id=$oid]/@type!='job'">
			Error: oid-reference must refer to a job-object: oid=<value-of select="$oid"/> type=<value-of select="/lml:lgui/objects/object[@id=$oid]/@type"/> in <value-of select="name()"/>
				</if>

			</for-each>

			<!--name-attributes must not refer to jobs or queues, but only physical elements in the system-->
			<for-each select="data//*[@name]">

				<variable name="name" select="@name"/>

				<if test="/lml:lgui/objects/object[@id=$name]/@type='job' or /lml:lgui/objects/object[@id=$name]/@type='queue'">
			Error: name-attribute must refer to an object, which must not be of type queue or job: name=<value-of select="$name"/> in <value-of select="name()"/>
				</if>

			</for-each>


			<!-- it is not allowed to use the name-attribute within lists of elements 
				=> name can only be used if all upper elements have max=min -->
			<for-each select="data//*[@max>@min]">
				<variable name="mult" select="name()"/>
				<for-each select="./descendant-or-self::node()[@name]">
			Error: reference to an object not allowed in a list of elements: name in element <value-of select="name()"/> with name=<value-of select="@name"/>. Multiple definitions in element <value-of select="$mult"/>
				</for-each>
			</for-each>

		</for-each>

	Referencing Nodedisplays:
		<for-each select="nodedisplay[count(@refto)>0]">
			
		Nodedisplay <value-of select="@id"/>
			<!-- checks which are the same for both types of nodedisplays-->
			<apply-templates select="."/>

			<!-- special checks for nodedisplay-references-->
			<!-- it is not allowed to use the refid-attribute within lists of elements 
				=> refid can only be used if all upper elements have max=min -->
			<for-each select="data//*[@max>@min]">
				<variable name="mult" select="name()"/>
				<for-each select="./descendant-or-self::node()[@refid]">
			Error: reference to a physical element not allowed in a list of elements: refid in element <value-of select="name()"/> with refid=<value-of select="@refid"/>. Multiple definitions in element <value-of select="$mult"/>
				</for-each>
			</for-each>

			<!-- referencing nodedisplays are not allowed to use attribute oid-->
			<for-each select=".//*[@oid]">
			Error: referencing nodedisplays are not allowed to use oid-attributes, but used in <value-of select="name()"/> oid=<value-of select="@oid"/>
			</for-each>

			<!-- referencing nodedisplays are not allowed to use attribute name-->
			<for-each select=".//*[@name]">
			Error: referencing nodedisplays are not allowed to use name-attributes, but used in <value-of select="name()"/> name=<value-of select="@name"/>
			</for-each>

			<!-- referencing nodedisplays are not allowed to use attribute status-->
			<for-each select=".//*[@status]">
			Error: referencing nodedisplays are not allowed to use status-attributes, but used in <value-of select="name()"/> status=<value-of select="@status"/>
			</for-each>

		</for-each>
</if>

<if test="not(@layout='true')">
------------------------------------------------------------
Checking charts:

	<for-each select=".//chart">
	
	<variable name="cid" select="@id"/>
	<!--check min-max attributes in x-tag within axes-->
	<for-each select="axes/x">
		<if test="@min>@max">
	Error: in chart <value-of select="$cid"/> in x-axis-definition: min >= max, min=<value-of select="@min"/> max=<value-of select="@max"/>
		</if>
	</for-each>
	<!--check min-max attributes in y-tags within axes-->
	<for-each select="axes/y">
		<if test="@min>@max">
	Error: in chart <value-of select="$cid"/> in y-axis-definition: min >= max, min=<value-of select="@min"/> max=<value-of select="@max"/>
		</if>
	</for-each>

	</for-each>
</if>

------------------------------------------------------------
Checking layouts:
	<for-each select="splitlayout">

	Splitlayout <value-of select="@id"/>
		
		<for-each select=".//left|.//right|.//bottom|.//top">
			<!-- a splitpane, which has lower elements, must not define gid -->
			<if test="count(./*)>0 and @gid">
		Error: in element with tagname <text>"</text><value-of select="name()"/><text>"</text>, a wrapper-splitplane cannot be connected with a graphical object, gid=<value-of select="@gid"/>
			</if>
			<!-- a leave-node within splitlayout must not define the divpos-attribute-->
			<if test="count(./*)=0 and @divpos">
		Error: in element with tagname <text>"</text><value-of select="name()"/><text>"</text>, a leave within in this layout is not allowed to define divpos, divpos=<value-of select="@divpos"/>
			</if>
			<!-- a leave-node within splitlayout must define gid-->
			<if test="count(./*)=0 and count(@gid)=0">
		Error: in element with tagname <text>"</text><value-of select="name()"/><text>"</text>, a leave component must be connected to a graphical element through the attribute gid
			</if>
			<!-- a splitpane-tag contains zero or two children-->
			<if test="count(./*)!=0 and count(./*)!=2">
		Error: in element with tagname <text>"</text><value-of select="name()"/><text>"</text>, every element is either empty or has two children, this one has <value-of select="count(./*)"/> children
			</if>
		</for-each>

	</for-each>
<!-- Eine Leerzeile am Ende-->
<text>

</text>
	
</template>

</xsl:stylesheet>