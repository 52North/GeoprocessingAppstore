<?xml version="1.0"?>

<!-- This file describes detail view for moving code package descriptions -->
<!-- @author Christin Henzen -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:wps="http://www.opengis.net/wps/1.0.0" 
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:ows="http://www.opengis.net/ows/1.1" 
    xmlns:mcp="http://gis.geo.tu-dresden.de/movingcode/1.1.0">
    
	<xsl:output method="html" version="4.01" />
	<xsl:output doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />

	<xsl:template name="header">
		<xsl:choose>
			<xsl:when test="/mcp:functionality/mcp:wps100ProcessDescription/ows:Title">
				<title>
					<xsl:value-of select="/mcp:functionality/mcp:wps100ProcessDescription/ows:Title" />
				</title>
			</xsl:when>
			<xsl:otherwise>
				<title>Metadata</title>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="/"> 
	
		<!-- Package ID, Time stamp, Process version -->
		
		<span class="section">
		<table class="sectionHeader" onclick="mddOnSectionClicked('packagedescription')" summary="This table is for design purposes only.">
   			<tbody>
     				<tr>
     					<td>
         					<input id="mdDetails:packagedescription-chk" type="checkbox" name="mdDetails:packagedescription-chk" checked="checked" style="display:none;" /></td><td>
         					<img id="mdDetails:packagedescription-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
         					<span class="sectionCaption">Common package information</span>
         				</td>
     				</tr>
   			</tbody>
 			</table>
 			<table id="mdDetails:packagedescription-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr><td>
		          <table id="mdDetails:packagedescription-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody> 
						<xsl:for-each
							select="/mcp:packageDescription/@mcp:packageId">
							<tr>
								<td>Package identifier</td>
								<td><i><xsl:value-of select="normalize-space(.)" /></i></td>
							</tr>
						</xsl:for-each> 
						<xsl:for-each
							select="/mcp:packageDescription/@mcp:timestamp">
							<tr>
								<td>Time stamp</td>
								<td><xsl:value-of select="normalize-space(.)" /></td>
							</tr>
						</xsl:for-each> 
						<xsl:for-each
							select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/@wps:processVersion">
							<tr>
								<td>Process version</td>
								<td><xsl:value-of select="normalize-space(.)" /></td>
							</tr>
						</xsl:for-each>
					</tbody>
		          </table>
		         </td>
		      </tr>
		    </tbody>
		  </table>
		</span>
		
		<!-- ID, Title, Abstract -->
		
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('processdescription')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr>
		      	<td>
		          <input id="mdDetails:processdescription-chk" type="checkbox" name="mdDetails:processdescription-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:processdescription-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">General process description</span>
		        </td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:processdescription-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		      	<td>
		          <table id="mdDetails:processdescription-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody>
						<xsl:for-each
							select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ows:Identifier[normalize-space(.)]">
							<tr>
								<td>Identifier</td>
								<td><i><xsl:value-of select="normalize-space(.)" /></i></td>
							</tr>
						</xsl:for-each>
						<xsl:for-each
							select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ows:Title">
							<tr>
								<td>Title</td>
								<td><b><xsl:value-of select="normalize-space(.)" /></b></td>
							</tr>
						</xsl:for-each>
						<xsl:for-each
							select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ows:Abstract">
							<tr>
								<td>Abstract</td>
								<td><xsl:value-of select="normalize-space(.)" /></td>
							</tr>
						</xsl:for-each> 
						
						<!-- Keywords -->
		
						<xsl:choose>
							<xsl:when test="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ows:Metadata"> 
								<tr>
									<td>Keywords</td>
									<td>
										<xsl:for-each select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ows:Metadata/@xlink:title">
											<xsl:choose>
												<xsl:when test="position() = 1">
													<xsl:value-of select="normalize-space(.)" />
												</xsl:when>
												<xsl:otherwise>
													<xsl:text>, </xsl:text>
													<xsl:value-of select="normalize-space(.)" />
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each> 
									</td>
								</tr>		
							</xsl:when>
						</xsl:choose> 
		           	</tbody>
		          </table>
		         </td>
		      </tr>
		    </tbody>
		  </table>
		</span>

		<!-- Data inputs -->

		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('inputs')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr><td>
		          <input id="mdDetails:inputs-chk" type="checkbox" name="mdDetails:inputs-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:inputs-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">Input descriptions</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:inputs-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		      	<td>  
					<xsl:for-each select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/DataInputs/Input">
						<span class="section">
							<table class="sectionBody" style="display:block;">
		 						<tbody> 
									<tr>
										<td>Identifier</td>
										<td>
											<xsl:value-of select="./ows:Identifier"/>
										</td>	
									</tr>
									<tr>
										<td>Title</td>
										<td>
											<xsl:value-of select="./ows:Title"/>
										</td>
									</tr>	
									
									<!-- min/max -->
									
									<xsl:variable name="minMax">
										<xsl:choose>
											<xsl:when test="./@minOccurs">
												<xsl:value-of select="./@minOccurs"/>
											</xsl:when>
											<xsl:otherwise>-</xsl:otherwise>
										</xsl:choose> /
										<xsl:choose>
											<xsl:when test="./@maxOccurs">
												<xsl:value-of select="./@maxOccurs"/>
											</xsl:when>
											<xsl:otherwise>-</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									
									<tr>
										<td>Min / Max</td>
										<td><xsl:value-of select="$minMax"/></td>
									</tr>
											
									<xsl:for-each select="./ows:Abstract">
										<tr>
											<td>Abstract</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each>	
										
									<!-- Complex vs Literal Data -->
									
									<tr>
										<td>Data type</td>	
										<td> 
											<xsl:for-each select="./ComplexData">
												<xsl:choose>
													<xsl:when test="./Default/Format/MimeType">
														Default: <xsl:value-of select="./Default/Format/MimeType"/>
													</xsl:when> 
												</xsl:choose>, <br />
												<xsl:choose>
													<xsl:when test="./Supported/Format/MimeType">
														Supported: <xsl:value-of select="./Supported/Format/MimeType"/>
													</xsl:when> 
												</xsl:choose>
											</xsl:for-each>
											
											<xsl:for-each select="./LiteralData">
												<xsl:choose>
													<xsl:when test="./ows:Datatype">
														<!-- TODO: check -->
														<xsl:value-of select="./ows:Datatype"/> 
													</xsl:when>
												</xsl:choose>
												<xsl:choose>
													<xsl:when test="./ows:Datatype">
														<!-- TODO: check -->
														<xsl:value-of select="./wps:SupportedUOMsType"/> 
													</xsl:when> 
												</xsl:choose>
											</xsl:for-each>
										</td>	
									</tr>	 
									<!-- End Complex vs Literal Data -->
						
								</tbody>
							</table>
						</span>
					</xsl:for-each>   
		        </td>
		      </tr>
		    </tbody>
		  </table>
		</span>
		<!-- End Data Inputs -->			

		<!-- Process Outputs -->
		
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('output')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr><td>
		          <input id="mdDetails:output-chk" type="checkbox" name="mdDetails:output-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:output-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">Output description</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:output-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		      	<td>
		          <table id="mdDetails:output-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody>
           
						<xsl:for-each select="/mcp:packageDescription/mcp:functionality/mcp:wps100ProcessDescription/ProcessOutputs/Output">
							<xsl:for-each select="./ows:Identifier">
								<tr>
									<td>Process identifier</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	
							<xsl:for-each select="./ows:Title">
								<tr>
									<td>Title</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	
							<xsl:for-each select="./ows:Abstract">
								<tr>
									<td>Abstract</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	 
							
							<!-- Complex vs Literal Data -->
							
							<tr>
								<td>Data type</td>	
								<td> 
									<xsl:for-each select="./ComplexOutput">
										<xsl:choose>
											<xsl:when test="./Default/Format/MimeType">
												Default: <xsl:value-of select="./Default/Format/MimeType"/>
											</xsl:when> 
										</xsl:choose>, <br />
										<xsl:choose>
											<xsl:when test="./Supported/Format/MimeType">
												Supported: <xsl:value-of select="./Supported/Format/MimeType"/>
											</xsl:when> 
										</xsl:choose>
									</xsl:for-each>
									
									<xsl:for-each select="./LiteralData">
										<xsl:choose>
											<xsl:when test="./ows:Datatype">
												<!-- TODO: check -->
												<xsl:value-of select="./ows:Datatype"/> 
											</xsl:when>
										</xsl:choose>
										<xsl:choose>
											<xsl:when test="./ows:Datatype">
												<!-- TODO: check -->
												<xsl:value-of select="./wps:SupportedUOMsType"/> 
											</xsl:when> 
										</xsl:choose>
									</xsl:for-each>
								</td>	
							</tr>	 
							<!-- End Complex vs Literal Data -->
							 
						</xsl:for-each>
						<!-- End Output -->
					</tbody>
		          </table></td>
		      </tr>
		    </tbody>
		  </table>
		</span>
		
		<!-- Platforms -->
		
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('platform')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr>
		        <td>
		          <input id="mdDetails:platform-chk" type="checkbox" name="mdDetails:platform-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:platform-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">Platform description</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:platform-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		      	<td>
		          <table id="mdDetails:platform-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody> 
						<xsl:choose>
							<xsl:when test="/mcp:packageDescription/mcp:platform/@mcp:platformId"> 
								<tr>
									<td>Platforms</td>
									<td>
										<xsl:for-each select="/mcp:packageDescription/mcp:platform/@mcp:platformId">
											<xsl:choose>
												<xsl:when test="position() = 1">
													<xsl:value-of select="normalize-space(.)" />
												</xsl:when>
												<xsl:otherwise>
													<xsl:text>, </xsl:text><br />
													<xsl:value-of select="normalize-space(.)" />
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each> 
									</td>
								</tr>		
							</xsl:when>
							
							<xsl:when test="/mcp:packageDescription/mcp:platform/mcp:requiredRuntimeComponent"> 
								<tr>
									<td>Required runtime components</td>
									<td>
										<xsl:for-each select="/mcp:packageDescription/mcp:platform/mcp:requiredRuntimeComponent">
											<xsl:choose>
												<xsl:when test="position() = 1">
													<xsl:value-of select="normalize-space(.)" />
												</xsl:when>
												<xsl:otherwise>
													<xsl:text>, </xsl:text><br />
													<xsl:value-of select="normalize-space(.)" />
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each> 
									</td>
								</tr>		
							</xsl:when>
						</xsl:choose>
					 </tbody>
		          </table>
		        </td>
		      </tr>
		    </tbody>
		  </table>
		</span>		
		<!-- End Platforms -->
		
		<!-- Infrastructure -->
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('infrastructure')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr>
		        <td>
		          <input id="mdDetails:infrastructure-chk" type="checkbox" name="mdDetails:infrastructure-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:infrastructure-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">Infrastructure description</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:infrastructure-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		        <td>
		          <table id="mdDetails:infrastructure-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody> 
						<xsl:for-each select="/mcp:packageDescription/mcp:infrastructure">
							<xsl:for-each select="./mcp:occi.compute.architecture">
								<tr>
									<td>Required computing architecture</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	
							<xsl:for-each select="./mcp:occi.compute.cores">
								<tr>
									<td>Required number of cores</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	
							<xsl:for-each select="./mcp:occi.compute.memory">
								<tr>
									<td>Required amount of memory</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>
							<xsl:for-each select="./mcp:occi.compute.speed">
								<tr>
									<td>Required CPU clock speed</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>
							<xsl:for-each select="./mcp:occi.storage.size">
								<tr>
									<td>Required disk space</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>	
						</xsl:for-each>
					</tbody>
		          </table>
		        </td>
		      </tr>
		    </tbody>
		  </table>
		</span>	
		<!-- End Infrastructure -->
		
		<!-- Exploitation rights -->
		
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('exrights')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr>
		        <td>
		          <input id="mdDetails:exrights-chk" type="checkbox" name="mdDetails:exrights-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:exrights-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">License information</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:exrights-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		        <td>
		          <table id="mdDetails:exrights-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody>
						<xsl:for-each select="/mcp:packageDescription/mcp:exploitationRights">
							<xsl:choose>
							
								<!-- Creative commons -->
								
								<xsl:when test="./mcp:creativeCommonsLicense"> 
								
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:cc.license">
										<tr>
											<td>License URL</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each>
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:dct.title">
										<tr>
											<td>Licensed work title</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each>
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:cc.attributionName">
										<tr>
											<td>Author/Organization</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each>
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:cc.attributionURL">
										<tr>
											<td>Link to organization</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each>
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:dct.source">
										<tr>
											<td>Source</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each> 
									<xsl:for-each select="./mcp:creativeCommonsLicense/mcp:cc.morePermissions">
										<tr>
											<td>Source</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each> 
								</xsl:when>
								
								<!-- Other license document -->
								
								<xsl:when test="./mcp:traditionalLicenseDocument"> 
									<xsl:for-each select="./mcp:traditionalLicenseDocument">
										<tr>
											<td>License document</td>
											<td>
												<xsl:value-of select="."/>
											</td>
										</tr>	
									</xsl:for-each> 
								</xsl:when>
							</xsl:choose>
						</xsl:for-each>
					 </tbody>
		          </table>
		        </td>
		      </tr>
		    </tbody>
		  </table>
		</span>	
		<!-- End Exploitation rights -->
		
		<!-- Workspace -->
		<span class="section">
		  <table class="sectionHeader" onclick="mddOnSectionClicked('workspace')" summary="This table is for design purposes only.">
		    <tbody>
		      <tr>
		        <td>
		          <input id="mdDetails:workspace-chk" type="checkbox" name="mdDetails:workspace-chk" checked="checked" style="display:none;" /></td><td>
		          <img id="mdDetails:workspace-img" src="/geoportal/catalog/images/section_open.gif" /></td><td>
		          <span class="sectionCaption">Workspace descriptions</span></td>
		      </tr>
		    </tbody>
		  </table>
		  <table id="mdDetails:workspace-body" class="sectionBody" style="display:block;">
		    <tbody>
		      <tr>
		        <td>
		          <table id="mdDetails:workspace-params" class="parameters" summary="This table is for design purposes only.">
		            <tbody> 
						<xsl:for-each select="mcp:packageDescription/mcp:workspace"> 
							<xsl:for-each select="./mcp:workspaceRoot">
								<tr>
									<td>Workspace root</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>
							<xsl:for-each select="./mcp:executableLocation">
								<tr>
									<td>Executable location</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>
							
							<xsl:for-each select="./mcp:executableMethodCall">
								<tr>
									<td>Executable method call</td>
									<td>
										<xsl:value-of select="."/>
									</td>
								</tr>	
							</xsl:for-each>
							
							<!-- Container types -->
						
							<xsl:choose>
								<xsl:when test="./mcp:containerType"> 
									<tr>
										<td>Container types</td>
										<td>
											<xsl:for-each select="./mcp:containerType">
												<xsl:choose>
													<xsl:when test="position() = 1">
														<xsl:value-of select="normalize-space(.)" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:text>, </xsl:text><br />
														<xsl:value-of select="normalize-space(.)" />
													</xsl:otherwise>
												</xsl:choose>
											</xsl:for-each> 
										</td>
									</tr>		
								</xsl:when>
							</xsl:choose>	
							<!-- End Container types -->
			 
							<!-- Execution parameters -->
							
							<xsl:for-each select="./mcp:executionParameters">
								<tr><td>Execution parameters</td></tr>
								<tr>
									<td colspan="2">
										<xsl:for-each select="./mcp:parameter"> 
											<span class="section">
												<table class="sectionBody" style="display:block;">
    												<tbody> 
														<tr>
															<td>Position ID</td>
															<td>
																<xsl:value-of select="./mcp:positionID"/>
															</td>
														</tr>
														<tr> 
															<xsl:choose>
																<xsl:when test="./mcp:functionalInputID">
																	<td>Functional Input ID</td>
																	<td>
																		<xsl:value-of select="./mcp:functionalInputID"/>
																	</td>
																</xsl:when>
																<xsl:when test="./mcp:functionalOutputID">
																	<td>Functional Output ID</td>
																	<td>
																		<xsl:value-of select="./mcp:functionalOutputID"/>
																	</td>
																</xsl:when>
															</xsl:choose>  
														</tr>	 
													</tbody>
												</table>
											</span> 
										</xsl:for-each> 
									</td>
								</tr>
							</xsl:for-each>
							<!-- End Execution parameters -->
			 
						</xsl:for-each>
						<!-- End Workspace -->
						
					</tbody>
		          </table>
		        </td>
		      </tr>
		    </tbody>
		  </table>
		</span>	  
	</xsl:template> 
</xsl:stylesheet>