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
<%// criteria.jsp - Search criteria (JSF include)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%
  com.esri.gpt.framework.jsf.MessageBroker schMsgBroker = com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
	String schContextPath = request.getContextPath();	
	com.esri.gpt.framework.context.RequestContext schContext = com.esri.gpt.framework.context.RequestContext.extract(request);
	com.esri.gpt.catalog.context.CatalogConfiguration schCatalogCfg = schContext.getCatalogConfiguration();
	com.esri.gpt.framework.collection.StringAttributeMap schParameters = schCatalogCfg.getParameters();
	boolean hasSearchHint = false;
	if(schParameters.containsKey("catalog.searchCriteria.hasSearchHint")){	
		String schHasSearchHint = com.esri.gpt.framework.util.Val.chkStr(schParameters.getValue("catalog.searchCriteria.hasSearchHint"));
		hasSearchHint = Boolean.valueOf(schHasSearchHint);
	}
	String schHintPrompt = schMsgBroker.retrieveMessage("catalog.searchCriteria.hintSearch.prompt");
	String VER121 = "v1.2.1";
%>

<% if(hasSearchHint){ %>
  <input type="hidden" id="schContextPath" value="<%=schContextPath %>"/>
  <input type="hidden" id="schHintPrompt" value="<%=schHintPrompt %>"/>
	<script type="text/javascript" src="<%=schContextPath+"/catalog/js/" +VER121+ "/gpt-search-hint.js"%>"></script>	
<% } %>

<% // date picker support %>
<gpt:DatePickerConfig/>

<% // scripting functions %>
<f:verbatim>
  <style type="text/css">
    .valignUp {
      vertical-align: top;
    }
  </style>

 <script type="text/javascript">
    // &filter parameter based on window.location.href
    function scAppendExtendedFilter(sUrlParams, bIsRemoteCatalog) {
      if (bIsRemoteCatalog == false) {
        var f = scGetExtendedFilter();
        if ((typeof(f) != "undefined") && (f != null) && (f.length > 0)) {
          if (sUrlParams.length > 0) sUrlParams += "&";
          sUrlParams += "filter="+ encodeURIComponent(f); 
        }
      }
      return sUrlParams;
    }
    
    function scGetExtendedFilter() {
      var q = dojo.queryToObject(window.location.search.slice(1));
      var f = q.filter;
      if ((typeof(f) != "undefined") && (f != null)) {
        f = dojo.trim(f);
        if (f.length > 0)
          return f;
      }     
      return null;
    }
  </script>

  <script type="text/javascript">   
    function srCorrectThumbs() {
      var els = document.getElementsByTagName("IMG");
      if (els == null) 
        return; 
  
      for (var i = 0; i < els.length; i = i + 1) { 
        if (typeof(els[i].id) != 'undefined' &&
          els[i].id.toLowerCase().match("thumbnail")  == "thumbnail") {
          els[i].onerror = "GptUtils.checkImgError(this)";
          els[i].setAttribute("onerror", "GptUtils.checkImgError(this)");
        }
      }
    }
    dojo.addOnLoad(srCorrectThumbs);

    // Replaces all links that are target = _top (Currently just the view Details
    // link)

    dojo.addOnLoad(function() {
      dojo.query("a.resultsLink").forEach(
	      function(item) {
	        if (item != null && typeof(item.target) == 'string' &&
	          item.target.toLowerCase() == "_top") {
	          item.target = "_blank";
	        }
	   });
    }
  ); 
  </script>
  <script type="text/javascript">

    dojo.require("dojo._base.NodeList");
    dojo.require("dijit.Dialog");
    dojo.require("dojo._base.html"); 
    
    $(document).ready(function() {
      var dpCfg = new DatePickerConfig();
      dpCfg.initialize();
      dpCfg.attach("frmSearchCriteria:scDateFrom");
      dpCfg.attach("frmSearchCriteria:scDateTo");      
    });

    /**
    Makes a valid html id out of the a string
    **/
    function srNormalizeId(id) {
      var strId = id.replace(/[^A-Za-z0-9_]/g, "");
      strId = "sc" + strId;
      return strId;
    }
 
    var _scRdbIndex = null;
    var _scRpsIndex = null;
    var contextPath = "<%=request.getContextPath()%>";

    /* Updating of sites once the ok button is clicked  */
    function scUpdateHsites(isCancel) {
    
      var rdbIndex = _scRdbIndex;
      var rpsIndex = _scRpsIndex;
      isCancel = GptUtils.valChkBool(isCancel);
  
      if (rdbIndex == null || rpsIndex == null) 
      	return;
    
      var siteName = "";
      var siteProfile = "";
      var siteUrl = "";
      var shsIndex = "";
  
      if (rdbIndex < 0) {
      	siteName = csDefaultSiteLabel;
      } else {
      	siteName = _scSearchSites.rows[rdbIndex].name; 
      	siteUrl = _scSearchSites.rows[rdbIndex].url;
      }
  
      if (isCancel == false) {
        var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");
        var elSiteProfile = document.getElementById("frmSearchCriteria:_harvestSiteProfile");
        var elSiteUrl = document.getElementById("frmSearchCriteria:_harvestSiteUrl");
 
        if (elSiteName.value != siteName) {
        	var elResults = document.getElementById("frmSearchCriteria:srResultsPanel");
          	if (elResults != null) {
            	elResults.style.display = "none";
            	elResults.style.visibility = "hidden";
          	}
        }
    
        elSiteProfile.value = siteProfile;
        elSiteUrl.value = siteUrl; 
        scInitComponents();
      }
    }

    /** Initializes visual text fields to show user which site is being searched **/
    function scInitTextFields() {
      var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");     
      var name = "";
  
      if (elSiteName != null && GptUtils.exists(elSiteName.value)) {
        name = elSiteName.value;
      }
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName");
      if (elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) 
        elPrntTxt.removeChild(elPrntTxt.firstChild); 
      if (elPrntTxt != null) 
        elPrntTxt.appendChild(document.createTextNode(name));
   
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName2");
      if (elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) 
        elPrntTxt.removeChild(elPrntTxt.firstChild); 
      if (elPrntTxt != null) 
        elPrntTxt.appendChild(document.createTextNode(name));
     
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName3");
      if(elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) 
        elPrntTxt.removeChild(elPrntTxt.firstChild); 
      if(elPrntTxt != null) 
        elPrntTxt.appendChild(document.createTextNode(name));
    }

    /** Initialization of page components **/
    function scInitComponents() { 
      scInitTextFields(); 
      scReconfigureCriteria();
      if (typeof(rsInsertReviews) != 'undefined') 
          rsInsertReviews(); 
      if ((typeof(itemCart) != "undefined") && (itemCart != null)) 
        itemCart.connectToSearchResults(); 
      
	  if(typeof(rsGetQualityOfService) != 'undefined') {
  	      try { rsGetQualityOfService(); } 
  	      catch(error) {
	  	 	console.log("unable to fetch quality of service info : ", error);
	  	  }
      } 
    }
    dojo.addOnLoad(scInitComponents);
    
    function scIsRemoteCatalog() {
      var elSiteId = document.getElementById("frmSearchCriteria:_harvestSiteId");
      if (elSiteId != null && GptUtils.valChkStr(elSiteId.value, "") == "local") 
     	return false; 
      return true;
    }
    
    /** Reconfigure the GUI critieria **/
    function scReconfigureCriteria() { 
      var blnRemoteCatalog = scIsRemoteCatalog(); 
      var el = document.getElementById("frmSearchCriteria:_authUserNamePassword"); 

      el = document.getElementById("frmSearchCriteria:_pngDataThemes");
      scShow(!blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngModDateSection");
      scShow(!blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngSortSection");
      scShow(!blnRemoteCatalog, el);
    
      el = document.getElementById("frmSearchCriteria:_pngCtypeRemote");
      scShow(blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngCtypeLocal");
      scShow(!blnRemoteCatalog, el);
    
      el = document.getElementById("frmSearchCriteria:_pngModDateSection");
      scShow(!blnRemoteCatalog, el);
    }
     
    function scShow(boolVal, el, bCanCollapseBlock) {
      if (GptUtils.exists(el) != true) 
        return; 
      if (typeof(bCanCollapseBlock) != 'boolean') 
        bCanCollapseBlock = true; 
      if (boolVal == true) {
        el.style.display = "";
        el.style.visibility = "visible";
      } else {
        if (bCanCollapseBlock == true) 
          el.style.display = "none"; 
        el.style.visibility = "hidden";
      }
    }

    var _sHarvestSites;
    var _scSearchSites; 
    
    function scGetHarvesterSitesHandler(data) {
      if (typeof(data) == 'undefined' || data == null) 
      	data = ""; 
      _scSearchSites = dojo.eval("[{" + data + "}]");
         if (typeof(_scSearchSites.length) != 'undefined' && _scSearchSites.length == 1) 
           _scSearchSites = _scSearchSites[0]; 
         if (typeof(_scSearchSites.rows) == 'undefined') 
           _scSearchSites.rows = new Array(); 
         
         for (var i = 0; i < _scSearchSites.rows.length; i++) {
         	if (typeof(_scSearchSites.rows[i].uuid) == 'undefined') 
          		_scSearchSites.rows[i].uuid = _scSearchSites.rows[i].id; 
         } 
     }
 
    //Some inputs are in a hidden div so this method makes helps in exposing
    //the values to their input hidden counterparts
    function updateHiddenValue(elInvisible, hiddenId) {
      _scAddOptions[elInvisible.id + ''] = { "comp" : elInvisible, "id" : GptUtils.valChkStr(hiddenId) };
    }
    
    //Updates hidden values associated with values that are now
    //not displayed by the dialog.  These values will not be
    //sent to the server because they are not displayed.  So hidden
    //values take these values so that the values can be posted to the server
    function updateHiddenValuesMultiple(elInvisible, hiddenId, isCancel) { 
      var delimeter = "|"; 
      if (!GptUtils.exists(elInvisible)) {
        GptUtils.logl(GptUtils.log.Level.WARNING, "updateHiddenValueMultiple recieved invalid input");
        return;
      }
      var name = '';
      if (typeof(elInvisible) == 'string') 
      	name = elInvisible;
      else if (GptUtils.exists(elInvisible.name)) 
        name = elInvisible.name;
      
      var els = document.getElementsByName(name);
      if (els == null || !GptUtils.exists(els.length) || els.length <= 0) {
        GptUtils.logl(GptUtils.log.Level.WARNING, "Could not find elements with name = " + elInvisible.name);
        return;
      }
      var id = hiddenId;
      var el = document.getElementById(id);
      if (el == null || typeof(el) == 'undefined') {
        GptUtils.logl(GptUtils.log.Level.WARNING, "updateHiddenValue missing: id = " + id);
        return;
      }
      
      if (GptUtils.valChkBool(isCancel) == false) { 
      	el.value = "";
        for (var i = 0; i < els.length; i++) {
          if (GptUtils.valChkStr(els[i].value) != "" && els[i].checked == true) {
          	el.value = el.value + delimeter + els[i].value;
          }
        }
      } else { 
        for (var j = 0; j < els.length; j++) {
          els[j].checked = false;
        }
        var tokenized = GptUtils.valChkStr(el.value).split(delimeter);
        for (var i = 0; i < tokenized.length; i++) {
          for (var j = 0; j < els.length; j++) {
            if (els[j].value == tokenized[i]) 
              els[j].checked = true; 
          }
        }
      }
    }

    /** When advanced options is closed by the buttons, this action takes place. **/
    function scAdvOptDialog(isCancel) {
      scUpdateAdditionalOptions(isCancel);
      updateHiddenValuesMultiple('frmSearchCriteria:scSelTheme', 'frmSearchCriteria:scSelThemeHidden', isCancel);
    }
    /** Shows a dialog (the advanced search dialog) @sId is the string id **/

    function scShowDialog(sId, bShow) { 
      var dj = dijit.byId(sId);
      dj.refreshOnShow = true;
      if (GptUtils.valChkBool(bShow) == true) {
        if (dj.closeText) {
          dj.closeText.setAttribute("title","");
          dj.closeButtonNode.setAttribute("title", "");
        }
        dj.startup();
    
        // need to do this to reset the width and height
        dj.containerNode.style.width = "";
        dj.containerNode.style.height = "";
        dj.show();
    
        var iPWidth = parseInt(dj.domNode.style.width);
        var iCWidth = parseInt(dj.containerNode.style.width);
        var maxWidth = parseInt(dj.domNode.style.maxWidth);
        dj.containerNode.style.width = "100%";
        if (maxWidth != NaN && iCWidth != NaN && iCWidth > maxWidth)  
          dj.containerNode.style.width = maxWidth - 10 + "px";  
      } else 
        dj.hide(); 
      var func = dj.onCancel;
  
      if (typeof(dj.attachedCancel) == 'undefined') { 
        if (dj.open == true) 
        	dj.onCancel = function() {
          		if (dj.id == "crtAdvOptnsContent") 
            		scAdvOptDialog(false); 
        		if (typeof(func) != 'undefined') 
          			func(); 
      		}
      	dj.attachedCancel = true;
      }
  }
 
  var tmpAoiMinX;
  var tmpAoiMinY;
  var tmpAoiMaxX;
  var tmpAoiMaxY; 
  var tmpAoiWkid;
  
  /** Gets the Rest Url params in a string  **/
  function scReadRestUrlParams() {
    var restParams = "";
    var bIsRemoteCatalog = scIsRemoteCatalog();
     
    //pragmatics + geodata (geoop)
    var scPragmatic = GptUtils.valChkStr(dojo.byId('frmSearchCriteria:scSelPragmatic').value);
    var scGeodata = GptUtils.valChkStr(dojo.byId('frmSearchCriteria:scSelGeodata').value);
   	
    var geoopString = "&geooperator="; 
   	if (scPragmatic != "") {
   		geoopString += encodeURIComponent(scPragmatic); 
   	}
   	if (scGeodata != "") {
   		if (geoopString != "&geooperator=") geoopString += ",";
   		geoopString += encodeURIComponent(scGeodata);
   	}
   	
  	//legacy gis (geoop) 
   	if (dojo.byId("djtContentGeopGRASS").checked) {
   		if (geoopString != "&geooperator=") geoopString += ",";
   		geoopString += encodeURIComponent(dojo.byId("djtContentGeopGRASS").value);
   	}
   	if (dojo.byId("djtContentGeopArcGIS").checked) {
   		if (geoopString != "&geooperator=") geoopString += ",";
   		geoopString += encodeURIComponent(dojo.byId("djtContentGeopArcGIS").value);
   	}
   	if (dojo.byId("djtContentGeopPCRaster").checked) {
   		if (geoopString != "&geooperator=") geoopString += ",";
   		geoopString += encodeURIComponent(dojo.byId("djtContentGeopPCRaster").value);
   	}
 
   	if (geoopString != "&geooperator=") restParams += geoopString;
   	
   	//platform (mcp)
   	var platString = "&platform=";
   	if (dojo.byId("djtContentPlatJava18").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatJava18").value);
   	}
   	if (dojo.byId("djtContentPlatJava17").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatJava17").value);
   	}
   	if (dojo.byId("djtContentPlatJava16").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatJava16").value);
   	}
   	if (dojo.byId("djtContentPlatJava15").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatJava15").value);
   	}
   	if (dojo.byId("djtContentPlatAA103").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatAA103").value);
   	}
   	if (dojo.byId("djtContentPlatAA102").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatAA102").value);
   	}
   	if (dojo.byId("djtContentPlatAA101").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatAA101").value);
   	}
   	if (dojo.byId("djtContentPlatAA100").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatAA100").value);
   	}
   	if (dojo.byId("djtContentPlatPy34").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatPy34").value);
   	}
   	if (dojo.byId("djtContentPlatPy27").checked) {
   		if (platString != "&platform=") platString += ",";
   		platString += encodeURIComponent(dojo.byId("djtContentPlatPy27").value);
   	}

   	if (platString != "&platform=") restParams += platString;
   	
   	//container (mcp) 
   	var contString = "&containertype=";
   	if (dojo.byId("djtContentContJava").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContJava").value);
   	}
   	if (dojo.byId("djtContentContPython").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContPython").value);
   	}
   	if (dojo.byId("djtContentContCsharp").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContCsharp").value);
   	}
   	if (dojo.byId("djtContentContArcTB").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContArcTB").value);
   	}
   	if (dojo.byId("djtContentContGDAL").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContGDAL").value);
   	}
   	if (dojo.byId("djtContentContR").checked) {
   		if (contString != "&containertype=") contString += ",";
   		contString += encodeURIComponent(dojo.byId("djtContentContR").value);
   	}
   	
   	if (contString != "&containertype=") restParams += contString;
    
   	//technical (geoop)
   	//TODO
   	
   	//common input
    var scText = GptUtils.valChkStr(dojo.byId('frmSearchCriteria:scText').value); 
    if (scText != "") {
    	if (restParams != "")
    		restParams += "&searchText="+ encodeURIComponent(scText);
    	else
    		restParams += "searchText="+ encodeURIComponent(scText);
    }
     
    // &filter parameter based on window.location.href
    restParams = scAppendExtendedFilter(restParams,bIsRemoteCatalog);
    
    var node = dojo.byId("frmSearchCriteria:srExpandResults");
    if (typeof(node) != 'undefined' && node != null && (node.checked == "checked" || node.checked == true)) 
      restParams += "&expandResults=true";
    
    return restParams;
  }

  /** Does a search on the specified page number @param page = The Search page **/
  function scSetPageTo(page) {
  	dojo.byId("frmSearchCriteria:scCurrentPage").setAttribute("value", page + '');
    scDoAjaxSearch();
    return false;
  }

  /* Does an ajax search and injects the results into the page
   */
  var _xhrSearch;
  var _lastSearch = "";
  function scDoAjaxSearch(clear, searchUrl) { 
    if (_xhrSearch) {
      try {
        _xhrSearch.cancel();
      } catch(err) {
        //GptUtils.logl(GptUtils.log.Level.WARNING,
        //"Error while cancelling search post" + err);
      }
    }
  
    // Add loading gif in results container
    var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
    dojo.query("#cmPlPgpGptMessages").empty(); 
    dojo.style(elLoadingGif, "visibility", "visible");
    
    var restUrlParams = scReadRestUrlParams();
    if (typeof(clear) == 'boolean' && clear == true) 
      restUrlParams = '';
    
    if (typeof(searchUrl) == 'string') 
      restUrlParams = searchUrl;
    
    var urlToSearch = contextPath + "/rest/find/document?" + restUrlParams;
    
    if (_csSearchTimeOut > 0) 
      urlToSearch += "&maxSearchTimeMilliSec=" + _csSearchTimeOut; 
    urlToSearch +=  "&f=searchpageresults";  
    
    var el = dojo.byId("frmSearchCriteria:scSearchUrl");
    el.setAttribute("value", urlToSearch); 
    _xhrSearch = dojo.xhrGet({ 
      url: urlToSearch, 
      load: dojo.hitch(this, function (data) {
        if (typeof(clear) == 'boolean' && clear == true) {
          window.location = contextPath + "/catalog/search/search.page";
          return;
        }
        if (typeof(clear) == 'boolean' && clear == true) {
          restUrlParams = '';
        }
        if (typeof(data) != 'string' || data == null || data.length < 1 || data.toLowerCase().indexOf("text/javascript") < 0) {
          throw new Error("No data recieved from server");
        } 
        scInitTextFields();
        scReconfigureCriteria();
        var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
        dojo.style(elLoadingGif, "visibility", "hidden");
        data = data.replace(/\r\n|\r/g, '');
        data = data.replace(/<\/form>/i, "");
        data = data.replace(/<form.*>/gi, "");
        data = data.replace(/<\/html>/i, "");
        data = data.replace(/<html.*>/gi, "");
        data = data.replace(/<\/body>/i, "");
        data = data.replace(/<body.*>/gi, "");
        
        // I.E. and Chrome cannot execute javascript when we add the html
        // via dojo.addContent. Below extracts javascript and executes it.
        // Fragile.  Dependent on our jscript compnent having a certain 
        // comment in front of it and being a one liner jscript.
        var jScript = ""; 
        var jScripts = data.match(/\/\* Component .*;/gi);
        if(jScripts == null) {
          throw new Error("No data recieved from server");
        }
        
        for (var i = 0; i < jScripts.length; i++) {
          jScript = jScripts[i];
          jScript = jScript.replace(/\/\* Comp.*var/gi, "");
          eval(jScript);
        }
        
        data = data.replace(/\/\* Component .*;/gi, "");
        data = data.replace(/document.forms\['frmSearchCriteria'].submit/mgi,
        "scDoAjaxSearch");
        data = data.replace(/name="frmSearchCriteria"/mgi,
        "name=\"suppressed_frmSearchCriteria");
        data = data.replace(/name="frmSearchCriteria:_idcl"/mgi,
        "name=\"suppressed_frmSearchCriteria");
        dojo.query("#frmSearchCriteria\\:srResultsPanel").empty();
        dojo.query("#frmSearchCriteria\\:srResultsPanel").addContent(data);
        var node = dojo.byId("frmSearchCriteria:srResultsPanel");
        dojo.style(node, "visibility", "visible");
        dojo.style(node, "display", "block");
        if (typeof(rsInsertReviews) != 'undefined') 
          rsInsertReviews(); 
        if ((typeof(itemCart) != "undefined") && (itemCart != null)) 
          itemCart.connectToSearchResults();

		if (typeof(rsGetQualityOfService) != 'undefined') {
  	      try { rsGetQualityOfService(); } catch(error) {
	  	 	console.log("unable to fetch quality of service info : ", error);
	  	  }
        }

        //scShowDistrSearchSites(false);
        try {
          //scMap.clearFootPrints();
          //scMap.drawFootPrints();
        } catch(error) {
          GptUtils.logl(GptUtils.log.Level.WARNING, "Error on save criteria" + error);
        }
        aoiMinX = parseInt(tmpAoiMinX);
        aoiMinY = parseInt(tmpAoiMinY);
        aoiMaxX = parseInt(tmpAoiMaxX);
        aoiMaxY = parseInt(tmpAoiMaxY);
        aoiWkid = parseInt(tmpAoiWkid);
       
      }),
      preventCache: true,
      error: function(args) {
        scInitTextFields();
        scReconfigureCriteria();
        var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
        dojo.style(elLoadingGif, "visibility", "hidden");
        if (args.dojoType =='cancel') { return;
        } else {
          dojo.query("#frmSearchCriteria\\:srResultsPanel").empty();
          dojo.query("#cmPlPgpGptMessages").addContent(
          "<div style=\"width: 800px;\" class=\"errorMessage searchInjectedError\">" + args.message +
            "<a href=\""+ this.url + "\" target=\"_blank\">" + this.url + "</a>"   +
            "</div>" + "<br/>" + 
          "<div style=\"width: 800px;\" class=\"errorMessage searchInjectedError\">" + 
              csErrorLabel +
          "</div>"   
          ); 
        }
      } 
    }); 
    return false;
  }
  
  </script>
</f:verbatim> 

<gpt:jscriptVariable 
  id="_csExteriorRepositories"
  quoted="false"
  value="#{SearchFilterHarvestSites.jscriptForeignSites}"
  variableName="csExteriorRepositories"/>

<gpt:jscriptVariable 
  id="_csDefaultSiteLabel"
  quoted="true"
  value="#{gptMsg['catalog.search.searchSite.defaultsite']}"
  variableName="csDefaultSiteLabel"/>

<gpt:jscriptVariable 
  id="_csErrorLabel"
  quoted="true"
  value="#{gptMsg['com.esri.gpt.catalog.search.SearchException']}"
  variableName="csErrorLabel"/>

<gpt:jscriptVariable 
  id="_csSearchTimeOut"
  quoted="true"
  value="#{SearchController.searchConfig.timeOut}"
  variableName="_csSearchTimeOut"/>

<gpt:jscriptVariable 
  id="_csDefaultSearchSite"
  quoted="true"
  value="#{SearchController.searchConfig.timeOut}"
  variableName="_csDefaultSearchSite"/> 

<% // search text and submit button %>
<h:panelGrid columns="4">
  <h:outputLabel for="scText" value="#{gptMsg['catalog.search.search.lblSearch']}"/>
  <h:inputText id="scText"
               value="#{SearchController.searchCriteria.searchFilterKeyword.searchText}"
               maxlength="4000" styleClass="searchBox" />
  <h:commandButton id="btnDoSearch" rendered="true"
                   onclick="javascript:scSetPageTo(1); return false;"
                   value="#{gptMsg['catalog.search.search.btnSearch']}"
                   action="#{SearchController.getNavigationOutcome}"
                   actionListener="#{SearchController.processAction}">
    <f:attribute name="#{SearchController.searchEvent.event}"
                 value="#{SearchController.searchEvent.eventExecuteSearch}" />
    <f:attribute name="onSearchPage" value="true"/>
  </h:commandButton>
  <h:graphicImage
    id="loadingGif" 
    style="visibility: hidden;"
    url="/catalog/images/loading.gif" alt="" 
    width="30px">
  </h:graphicImage>
   <f:verbatim>
   	<div id="hints"></div>
   </f:verbatim>
</h:panelGrid> 

<h:outputText id="brkscLnkAdditionals" escape="false" rendered="true" value="<br/>"/>

<h:panelGroup id="_pngGeoopPragmatic">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputLabel for="scSelPragmatic" id="scLblPragmatic" value="#{gptMsg['catalog.search.filterGeooperator.pragmatic']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid id="scPnlPragmatic">
    <h:selectOneMenu id="scSelPragmatic"
                     value="#{SearchFilterGeooperators.geooperator}"
                     onchange="console.log('changed');" >
      <f:selectItem itemValue=""
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.default']}" />
      <f:selectItem itemValue="Transport route planning"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.transport']}" />
      <f:selectItem itemValue="Modeling paths"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.modeling']}" />
       
    </h:selectOneMenu>
  </h:panelGrid>
</h:panelGroup> 
 
<h:panelGroup id="_pngGeoopGeodata">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputLabel for="scSelGeodata" id="scLblGeodata" value="#{gptMsg['catalog.search.filterGeooperator.geodata']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid id="scPnlGeodata">
    <h:selectOneMenu id="scSelGeodata"
                     value="#{SearchFilterGeooperators.geooperator}"
                     onchange="javascript:updateHiddenValue(this)" >
      <f:selectItem itemValue=""
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.default']}" />
      <f:selectItem itemValue="Raster"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.raster']}" />
      <f:selectItem itemValue="Vector"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.vector']}" />
      <f:selectItem itemValue="Network"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.network']}" />
      <f:selectItem itemValue="TIN"
                    itemLabel="#{gptMsg['catalog.search.filterGeooperator.tin']}" />
    </h:selectOneMenu>
  </h:panelGrid>
</h:panelGroup> 
 
<h:panelGroup id="dockContentGeoopSearch">
	<f:verbatim>
		<div id="djtContentGeopSearches" style="width:400px;">
	      <div id="contentGeopTitle">  
            <table width="100%" cellpadding="0" cellspacing="0">   
            	<tr><td><h3>What is your GIS preference?</h3></td></tr>
                <tr><td><input type="checkbox" id="djtContentGeopGRASS" value="GRASS"/>GRASS</td></tr>                 
                <tr><td><input type="checkbox" id="djtContentGeopArcGIS" value="ArcGIS"/>ArcGIS</td></tr>
                <tr><td><input type="checkbox" id="djtContentGeopPCRaster" value="PCRaster"/>PCRaster</td></tr>                 
             </table> 
	      </div>
		</div>
	</f:verbatim>
</h:panelGroup> 

<h:panelGroup id="dockContentPlatformSearch">
	<f:verbatim>
		<div id="djtContentPlatformSearches" style="width:400px;">
	      <div id="contentPlatformTitle">  
            <table width="100%" cellpadding="0" cellspacing="0">   
            	<tr><td colspan=2><h3>Which software do you have installed?</h3></td></tr>
                <tr><td><input type="checkbox" id="djtContentPlatJava18" value="java18"/>Java 1.8</td><td><input type="checkbox" id="djtContentPlatAA103" value="arcgis103analysis"/>ArcGIS Analysis 10.3</td></tr>                 
                <tr><td><input type="checkbox" id="djtContentPlatJava17" value="java17"/>Java 1.7</td><td><input type="checkbox" id="djtContentPlatAA102" value="arcgis102analysis"/>ArcGIS Analysis 10.2</td></tr>
                <tr><td><input type="checkbox" id="djtContentPlatJava16" value="java16"/>Java 1.6</td><td><input type="checkbox" id="djtContentPlatAA101" value="arcgis101analysis"/>ArcGIS Analysis 10.1</td></tr>
                <tr><td><input type="checkbox" id="djtContentPlatJava15" value="java15"/>Java 1.5</td><td><input type="checkbox" id="djtContentPlatAA100" value="arcgis100analysis"/>ArcGIS Analysis 10.0</td></tr>  
                <tr><td style="padding-top:5px;"><input type="checkbox" id="djtContentPlatPy34" value="gisPython342"/>Python 3.4</td></tr>            
                <tr><td><input type="checkbox" id="djtContentPlatPy27" value="gisPython27"/>Python 2.7</td></tr>                
             </table> 
	      </div>
		</div>
	</f:verbatim>
</h:panelGroup>

<h:panelGroup id="dockContentContainerSearch">
	<f:verbatim>
		<div id="djtContentContainerSearches" style="width:400px;">
	      <div id="contentContainerTitle">  
            <table width="100%" cellpadding="0" cellspacing="0">   
            	<tr><td colspan=2><h3>What are your preferred languages and libraries?</h3></td></tr>               
                <tr><td><input type="checkbox" id="djtContentContJava" value="java"/>Java</td><td><input type="checkbox" id="djtContentContArcTB" value="arctoolbox"/>Arc Toolbox</td></tr>
                <tr><td><input type="checkbox" id="djtContentContPython" value="python"/>Python</td><td><input type="checkbox" id="djtContentContGDAL" value="gdal"/>GDAL</td></tr>
                <tr><td><input type="checkbox" id="djtContentContCsharp" value="csharp"/>C#</td><td><input type="checkbox" id="djtContentContR" value="r"/>R</td></tr>                  
             </table> 
	      </div>
		</div>
	</f:verbatim>
</h:panelGroup>
 
<h:panelGroup id="dockContentGeoopTechnicalSearch">
	<f:verbatim>
		<div id="djtContentGeoopTechnicalSearches" style="width:400px;">
	      <div id="contentTechnicalTitle">  
            <table width="100%" cellpadding="0" cellspacing="0">   
            	<tr><td colspan=2><h3>Where should your proccess be online available?</h3></td></tr>               
                <tr><td><input type="checkbox" id="djtContentTechAGSOnline" value="agsonline"/>ArcGIS Online</td></tr>
                <tr><td><input type="checkbox" id="djtContentTechWPS" value="wps"/>Web Processing Service (WPS)</td></tr>
            </table> 
	      </div>
		</div>
	</f:verbatim>
</h:panelGroup> 
 
<h:outputText escape="false" value="</div>"/>
 
<h:inputHidden id="scCurrentPage"
               value="#{SearchController.searchCriteria.searchFilterPageCursor.currentPage}"/>
<h:inputHidden id="scRecordsPerPage"
               value="#{SearchController.searchCriteria.searchFilterPageCursor.recordsPerPage}"/>
<h:inputHidden id="scSearchUrl" 
               value="#{SearchController.searchFilterHarvestSites.searchUrl}"/>
               