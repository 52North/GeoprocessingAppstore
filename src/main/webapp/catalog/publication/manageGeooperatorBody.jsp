<%--
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
--%>
<% // selectOperationBody.jsp - Selects operation () (tiles definition) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<h:form id="frmCreateMetadata" styleClass="fixedWidth">
	<h:inputHidden value="#{ManageGeooperatorController.prepareView}" />
 
	<% // add %>
	<h:panelGrid columns="2"
		summary="#{gptMsg['catalog.general.designOnly']}"
		styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
 
 		<% //add geooperator %>
  		<h:outputText escape="false" styleClass="prompt"
		value="#{gptMsg['catalog.publication.addGeooperator.prompt']}" />
		<h:outputText value=""/>
 		
 		<h:outputText styleClass="requiredField" value="Name of new geooperator" />
		<h:inputText id="geoopTitle" size="50" maxlength="128" value="#{ManageGeooperatorController.geooperator}" />

		<h:outputText value="Parent of new geooperator" />
		<h:selectOneMenu id="geoopParentSelect" value="#{ManageGeooperatorController.parentgeooperator}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<!-- 
		<h:outputText styleClass="requiredField" value="definition" />
		<h:inputText id="definition" size="50" maxlength="128" value="#{ManageGeooperatorController.definition}" />

		<h:outputText value="scopeNote" />
		<h:inputText id="scopeNote" size="50" maxlength="128" value="#{ManageGeooperatorController.scopeNote}" />		

		<h:outputText value="narrowMatch" />
		<h:selectOneMenu id="narrowMatch" value="#{ManageGeooperatorController.narrowMatch}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="broadMatch" />
		<h:selectOneMenu id="broadMatch" value="#{ManageGeooperatorController.broadMatch}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="closeMatch" />
		<h:selectOneMenu id="closeMatch" value="#{ManageGeooperatorController.closeMatch}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="formalCategories" />
		<h:selectOneMenu id="formalCategories" value="#{ManageGeooperatorController.formalCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="geodataCategories" />
		<h:selectOneMenu id="geodataCategories" value="#{ManageGeooperatorController.geodataCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="geoinformaticsCategories" />
		<h:selectOneMenu id="geoinformaticsCategories" value="#{ManageGeooperatorController.geoinformaticsCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="legacyGISCategories" />
		<h:selectOneMenu id="legacyGISCategories" value="#{ManageGeooperatorController.legacyGISCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="pragmaticCategories" />
		<h:selectOneMenu id="pragmaticCategories" value="#{ManageGeooperatorController.pragmaticCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>
		
		<h:outputText value="technicalCategories" />
		<h:selectOneMenu id="technicalCategories" value="#{ManageGeooperatorController.technicalCategories}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>

		-->
		
		<h:outputText value="" />
		<h:commandButton id="submit"
			value="#{gptMsg['catalog.publication.addMetadata.button.submit']}"
			action="#{ManageGeooperatorController.getNavigationOutcome}"
			actionListener="#{ManageGeooperatorController.processAction}">
			<f:attribute name="submit" value="add" />
		</h:commandButton>
	 
		<% //remove geooperator %>
		<h:outputText value="#{gptMsg['catalog.publication.removeGeooperator.prompt']}" styleClass="prompt"/> 
	 	<h:outputText value=""/>
		 
		<h:outputText value="Name of geooperator" />
		<h:selectOneMenu id="geoopRemoveSelect" value="#{ManageGeooperatorController.removegeooperator}">
			<f:selectItems value="#{ManageGeooperatorController.selectableGeooperators.items}" />
		</h:selectOneMenu>

		<h:outputText value="" />
		<h:commandButton id="submit2"
			value="#{gptMsg['catalog.publication.addMetadata.button.submit']}"
			action="#{ManageGeooperatorController.getNavigationOutcome}"
			actionListener="#{ManageGeooperatorController.processAction}">
			<f:attribute name="submit" value="remove" />
		</h:commandButton>

	</h:panelGrid>

</h:form>
