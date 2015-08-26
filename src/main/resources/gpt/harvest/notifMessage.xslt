<?xml version="1.0" encoding="UTF-8"?>
<!--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:template match="/notification">
<html>
	<body>
		This email is to notify you that repository <xsl:value-of select="repositoryName"/> has been harvested.<br/>
		<br/>
        <xsl:if test="normalize-space(reportLink) != ''">
          You can view harvest report following the link <a href="{reportLink}"><xsl:value-of select="reportLink"/></a><br/>
          <br/>
        </xsl:if>
		Thank you.
	</body>
</html>
</xsl:template>
</xsl:stylesheet>
