<xsl:stylesheet version="2.0" xmlns:xsl='http://www.w3.org/1999/XSL/Transform' 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:p="http://maven.apache.org/POM/4.0.0"
	exclude-result-prefixes="p xsi #default">
	
	<xsl:param name="newVersion"/>
	
	<xsl:output encoding="UTF-8" method="xml" indent="yes" />

	<xsl:template match="p:project">
		<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
			<xsl:copy-of select="namespace::*" />
			<xsl:apply-templates />
		</project>
	</xsl:template>

	<xsl:template match="p:version[preceding-sibling::p:groupId='org.eclipse.ptp']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>

	<xsl:template match="p:version[preceding-sibling::p:groupId='org.eclipse.ptp.features']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.aix.ppc']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.linux.ppc']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.linux.x86']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.linux.x86_64']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.macosx.ppc']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.macosx.x86']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.debug.sdm']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.proxy']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.rm.ibm.ll.proxy']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.rm.ibm.pe.proxy']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.rm.slurm.proxy']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.sci']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp.utils']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>
	<xsl:template match="p:version[preceding-sibling::p:artifactId='org.eclipse.ptp']">
		<version><xsl:value-of select="$newVersion"/>-SNAPSHOT</version>
	</xsl:template>

	<xsl:template match="p:*">
	    <xsl:element name="{name()}">
	      <xsl:apply-templates/>
	    </xsl:element>
	</xsl:template>
	
	<xsl:template match="comment()">
   		<xsl:copy/>
	</xsl:template>
	
</xsl:stylesheet>
