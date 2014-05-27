<?xml version="1.0" encoding="UTF-8"?>
<!--
 Copyright (c) 2011 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 
 Contributors:
   IBM - Initial API and implementation
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:strip-space elements="*"/>
	<xsl:output omit-xml-declaration="yes"/>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core']">
cdtCoreVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.native']">
cdtCoreNativeVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.lrparser']">
cdtCoreLRParserVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.parser.upc']">
cdtCoreUPCParserVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.lrparser.xlc']">
cdtCoreLRParserXLCVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.aix']">
cdtCoreAIXVersion=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.linux.ppc64']">
cdtCoreLinuxPPC64Version=<xsl:value-of select="@version"/>
	</xsl:template>
	<xsl:template match="artifact[@classifier='osgi.bundle' and @id='org.eclipse.cdt.core.linux.x86']">
cdtCoreLinuxx86Version=<xsl:value-of select="@version"/>
	</xsl:template>
</xsl:stylesheet>
