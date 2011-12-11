<xsl:stylesheet version="2.0" xmlns:xsl='http://www.w3.org/1999/XSL/Transform' 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:p="http://maven.apache.org/POM/4.0.0"
	exclude-result-prefixes="p xsi #default">
	
	<xsl:param name="newVersion"/>
	
	<xsl:output encoding="UTF-8" method="xml" indent="yes" />
	
	<xsl:template match="/p:*">
		<project name="fixModules" default="fix">
			<target name="fix">
				<xsl:template match="p:project">
					<xsl:for-each select="p:modules/p:module">
						<xslt style="fixPom.xsl" in="../{text()}/pom.xml" out="pom.xml.tmp">
							<param name="newVersion" expression="{$newVersion}"/>
						</xslt>
						<move file="pom.xml.tmp" tofile="../{text()}/pom.xml"/>
					</xsl:for-each>
				</xsl:template>
				<xslt style="fixPom.xsl" in="../pom.xml" out="pom.xml.tmp">
					<param name="newVersion" expression="{$newVersion}"/>
				</xslt>
				<move file="pom.xml.tmp" tofile="../pom.xml"/>
			</target>
		</project>
	</xsl:template>
	
</xsl:stylesheet>
