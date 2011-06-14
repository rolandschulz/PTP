<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2008, 2009 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
   IBM - Initial API and implementation
-->

<!--
Produces an Ant build file from the extracted contents of a jardesc file.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:variable name="basedir">${basedir}</xsl:variable>
	<xsl:variable name="jarfile">${jarfile}</xsl:variable>
	<xsl:variable name="tempdir">${tempdir}</xsl:variable>
	<xsl:param name="bindir">bin</xsl:param>
	
	<xsl:template match="/">
		
		<project name="Jardesc Builder" default="jar" basedir=".">
			<property name="jarfile" location="{$basedir}/out.jar"/>
			<property name="tempdir" location="{$basedir}/temp.gather"/>
		
			<target name="jar" depends="gather">
				<jar jarfile="{$jarfile}" basedir="{$tempdir}"/>
				<delete dir="{$tempdir}" failonerror="false"/>
			</target>
			
			<target name="gather">
				<delete dir="{$tempdir}" failonerror="false"/>
				<mkdir dir="{$tempdir}"/>
				<copy todir="{$tempdir}">
					<xsl:apply-templates/>
				</copy>
			</target>
		</project>		
	</xsl:template>
	
	<xsl:template match="text()"/>
	
	<xsl:template match="file">
		<fileset dir="{$basedir}/{@folder}/{$bindir}">
			<include name="{text()}.class"/>
			<include name="{text()}$$*.class"/>
		</fileset>
	</xsl:template>
	
	<xsl:template match="properties_file">
		<fileset dir="{$basedir}/{@folder}/{$bindir}">
			<include name="{text()}"/>
		</fileset>
	</xsl:template>
	
	<xsl:template match="grammar_file">
		<fileset dir="{$basedir}/{@folder}/{$bindir}">
			<include name="{text()}"/>
		</fileset>
	</xsl:template>
</xsl:stylesheet>
