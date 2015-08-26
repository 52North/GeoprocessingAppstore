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
<% // gxeEditorBody.jsp - Geoportal XML editor page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%
  com.esri.gpt.framework.jsf.MessageBroker gxeMsgBroker = 
    com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
  String gxeDefinitionKey = 
    com.esri.gpt.framework.util.Val.chkStr((String)request.getAttribute("gxeDefinitionKey"));
  String gxeDefinitionLocation = 
    com.esri.gpt.framework.util.Val.chkStr((String)request.getAttribute("gxeDefinitionLocation"));
  String gxeOpenDocumentId = 
    com.esri.gpt.framework.util.Val.chkStr((String)request.getAttribute("gxeOpenDocumentId"));
  String gxeNewDocumentId = com.esri.gpt.framework.util.UuidUtil.makeUuid();
  
  String gxeLabelSave = gxeMsgBroker.retrieveMessage("catalog.publication.editMetadata.button.submit");
  String gxeLabelSaveDraft = gxeMsgBroker.retrieveMessage("catalog.publication.editMetadata.button.saveAsDraft");
  String gxeLabelReq = gxeMsgBroker.retrieveMessage("catalog.general.requiredFieldNote");
%>

<% // interactive map configuration %>
<% com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = 
   com.esri.gpt.framework.context.RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap();%>
<script type="text/javascript">
  var gptMapConfig = new GptMapConfig();
  gptMapConfig.mapServiceURL = "<%=imConfig.getMapServiceUrl()%>";
  gptMapConfig.mapServiceType = "<%=imConfig.getMapServiceType()%>";
  gptMapConfig.geometryServiceURL = "<%=imConfig.getGeometryServiceUrl()%>";
  gptMapConfig.locatorURL = "<%=imConfig.getLocatorUrl()%>";
  gptMapConfig.locatorSingleFieldParameter = "<%=imConfig.getLocatorSingleFieldParameter()%>";
  gptMapConfig.locatorGraphicURL = "<%=request.getContextPath()%>/catalog/images/pushpin_red.gif";
  gptMapConfig.mapVisibleLayers = "<%=imConfig.getMapVisibleLayers()%>";
  var gxeLocale = "<%=request.getLocale().toString()%>";
</script>

<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/gpt/gxe/gxe.css"></link>
<script type="text/javascript" src="<%=request.getContextPath()%>/gpt/GptCore.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/gpt/gxe/gemet.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/gpt/gxe/gxe.js"></script>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt" rendered="false"
  value="#{gptMsg['catalog.publication.editMetadata.prompt']}"/>
  
<f:verbatim>

<script type="text/javascript">

  var gxeContext = new gxe.Context();
  
  //hide file uplaoder until code uplaod is implemented for changes and not only for new creations
  window.onload = function () { 
	  var sDocumentId = "<%=gxeOpenDocumentId%>";  //if id exists -> change mode; hide button for upload
	  if (sDocumentId !== "") { 
		  var fileInput = document.getElementById ("uploader");	
		  fileInput.style.visibility = 'hidden'; 
	  }
  }; 
  
  if (typeof(dojo) != 'undefined') {
    dojo.addOnLoad(function() {
      gxeContext.contextPath = "<%=request.getContextPath()%>";
      gxeContext.gptMapConfig = gptMapConfig;
      gxeContext.newDocumentId = "<%=gxeNewDocumentId%>";
      var sKey = "<%=gxeDefinitionKey%>";
      var sLocation = "<%=gxeDefinitionLocation%>";
      var sDocumentId = "<%=gxeOpenDocumentId%>";
      var elHtmlParent = dojo.byId("gxeDocument");
      var elLoading = dojo.byId("gxeLoading");
  
      var bOpenDocument = (sDocumentId.length > 0);
      var sParam = null;
      var sParamValue = null;
      if (sKey.length > 0) {
        sParam = "key";
        sParamValue = sKey;
      } else if (sLocation.length > 0) {
        sParam = "loc";
        sParamValue = sLocation;
      }
      if (sParam == null) return;

      elLoading.style.visibility = "visible";
      var gxeClient = new gxe.Client();
      gxeClient.queryDefinition(gxeContext,sParam,sParamValue,function(responseObject,ioArgs) {
        var cfgDefinition = responseObject;
        gxe.cfg.initialize(cfgDefinition);
        if (bOpenDocument) {
          gxeClient.queryDocument(gxeContext,sDocumentId,function(responseObject,ioArgs) {
            var dom = responseObject;
            elHtmlParent.style.display = "none";
            gxeContext.buildUI(cfgDefinition,elHtmlParent,dom);
            elLoading.parentNode.removeChild(elLoading);
            elHtmlParent.style.display = "block";
          });
        } else {
          elHtmlParent.style.display = "none";
          gxeContext.buildUI(cfgDefinition,elHtmlParent,null);
          elLoading.parentNode.removeChild(elLoading);
          elHtmlParent.style.display = "block";
        }
      });
      
    });
  }

  //edit mode
  function gxeSaveDocument(asDraft) {
    var xmlGenerator = new gxe.xml.Generator();
    var sXml = xmlGenerator.generate(gxeContext,asDraft); 
    if (!xmlGenerator.hadValidationErrors) {
      var gxeClient = new gxe.Client();
      gxeClient.saveDocument(gxeContext,null,sXml,asDraft);
    }
  }
  
  function xmlAndUplaod(){
	  var id = gxeContext.openDocumentId; //if id exists -> change mode
	  if (id != null) { //redirect- edit mode because id is known
		  gxeSaveDocument(false); 
		  return;
	  }
	  //create mode
										//else create mode  	  
	  id = gxeContext.newDocumentId;  //TODO: real Package ID or created id for saving?
	  var xmlGenerator = new gxe.xml.Generator();
	  var sXml = xmlGenerator.generate(gxeContext,"false");
	  if (!xmlGenerator.hadValidationErrors) {		
		  console.log("valid");
	  //var blob = new Blob([file], { type: "application/zip"} );
	  var form = new FormData();
	  var fileInput = document.getElementById ("file");
	  var file = fileInput.files[0];
	  		if(file==null){
	  			var error = document.getElementById('cmPlMsgsPageMessages');
	  			error.className = "errorMessage";
	  			error.innerHTML = "no file chosen";
	  			return;
	  		}
	  form.append("file", file);
	  form.append("id", id);
	  form.append("xml", sXml);
	  
	  
	  //show loading dialog
	  var nd = document.createElement("p");
    nd.appendChild(document.createTextNode("uploading..."));
	    var dialog = new dijit.Dialog({
	        title: gxeContext.getI18NString("client.saving.title"),
	        style: "width: 300px; display: none;",
	        content: nd
	      });
	      dojo.addClass(dialog.domNode,"tundra");
	      dialog.show();
	  //PUT
	  var u = gxeContext.contextPath+"/rest/manage/document?";
	  var request = new XMLHttpRequest();
	  request.open("PUT", u);
	  request.send(form);
	  request.onreadystatechange=function(){
	       if (request.readyState==4 && request.status==201){
	          //console.log('xhr.readyState=',request.readyState);
	          //console.log('xhr.status=',request.status);
	          //console.log('response=',request.responseText);
	          var successMessage = document.getElementById('cmPlMsgsPageMessages');
	    		successMessage.className = "successMessage";
	    		successMessage.innerHTML = "Moving Code Package successfully created";
	       //error handling
	       } else if(request.status==409) {
	    	   var successMessage = document.getElementById('cmPlMsgsPageMessages');
	    		successMessage.className = "errorMessage";
	    		successMessage.innerHTML = "Validation Error";
	    	   
	       }
	       else{
	    	   var successMessage = document.getElementById('cmPlMsgsPageMessages');
	    		successMessage.className = "errorMessage";
	    		successMessage.innerHTML = "unknown XML Error";
	    	   
	       }
	    }; 
      dialog.hide();
      dialog.destroy();
	  }else{
		  console.log("valid errors");
	  }
	  
  }
  
  function GetFileInfo () {
      var fileInput = document.getElementById ("file");
      for (var i = 0; i < fileInput.files.length; i++) {
        	var file = fileInput.files[i];
            	if ('name' in file) {
                      console.log("name: " + file.name);
                  }
                  else {
                	  console.log("name: " + file.fileName);
                  }
                  if ('size' in file) {
                	  console.log("size: " + file.size);
                  }
                  else {
                	  console.log("size2: " + file.fileSize);
                  }    
      } 
  }

</script>
  <div id="gxeControlBar" class="gxeControlBar">
  <!--    <input type="button" value="<%=gxeLabelSaveDraft%>" onclick="gxeSaveDocument(true)"/>
    <input type="button" value="<%=gxeLabelSave%>" onclick="gxeSaveDocument(false)"/>    -->
<div id="uploader"><input type="file" id="file" name="file"
accept="application/xml,text/xml,application/zip,text/zip" onchange="GetFileInfo()"/></div>
 <input type="button" value="upload Archiv" onclick="xmlAndUplaod()"/>
    <img id="gxeLoading" src="<%=request.getContextPath()%>/catalog/images/loading.gif" 
      style="margin:10px; width:30px; height:30px; display:block; visibility:hidden;"/>    
  </div>
	<div id="gxeMessageArea" class="gxeMessageArea"></div>
	<div id="gxeDocument" class="gxeDocument gxeStandardView"></div>
  <div class="requiredFieldNote"><%=gxeLabelReq%></div>

</f:verbatim>

