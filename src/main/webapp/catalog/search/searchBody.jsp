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
<%// searchBody.jsp - Create search criteria (JSF body)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<f:verbatim>
  <style type="text/css">
  .columnsTable td {vertical-align: top;}
</style>
</f:verbatim>

<%
  // interactive map configuration , 
  // the ArcGIS Server Javascript API is explicitly loaded for this page within centeredLayout.jsp,
  // loading the jsapi within the <head> tag reduces flicker on the search page
  com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = com.esri.gpt.framework.context.RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap();
%>
<script type="text/javascript">
  dojo.require("dojo.cookie");
  dojo.require("dijit.form.Form");
  
  var _cookieLabel = "searchPageCookie";
  
  var _frmDjSearchCriteria = null;
  
  function serilizeFormToCookie() {  }
  function deserializeFormFromCookie() { } 

  var gptMapConfig = new GptMapConfig();
  gptMapConfig.mapServiceURL = "<%=imConfig.getMapServiceUrl()%>";
  gptMapConfig.mapServiceType = "<%=imConfig.getMapServiceType()%>";
  gptMapConfig.geometryServiceURL = "<%=imConfig.getGeometryServiceUrl()%>";
  gptMapConfig.locatorURL = "<%=imConfig.getLocatorUrl()%>";
  gptMapConfig.locatorSingleFieldParameter = "<%=imConfig.getLocatorSingleFieldParameter()%>";
  gptMapConfig.locatorGraphicURL = "<%=request.getContextPath()%>/catalog/images/pushpin_red.gif";
  gptMapConfig.mapVisibleLayers = "<%=imConfig.getMapVisibleLayers()%>";
  gptMapConfig.mapInitialExtent = "<%=imConfig.getMapInitialExtent()%>";
</script>

<f:verbatim>
    
<script type="text/javascript" >

var _initialMinX = null;
var _initialMinY = null;
var _initialMaxX = null;
var _initialMaxY = null;
dojo.declare("SearchMap", null, {
  _gptMap: null,
  _gptMapToolbar: null,
  _gptLocator: null,
  _gptInpEnv: null,


  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.drawFootPrints = dojo.hitch(this,this.drawFootPrints);
    this.clearFootPrints = dojo.hitch(this,this.clearFootPrints);
    this.highlightFootPrint = dojo.hitch(this,this.highlightFootPrint);
    this.getProcessedExtent = dojo.hitch(this,this.getProcessedExtent);
    this.zoomToAOI = dojo.hitch(this,this.zoomToAOI);
    this.zoomAnywhere = dojo.hitch(this,this.zoomAnywhere);
    this.zoomToThese = dojo.hitch(this,this.zoomToThese);
    this.zoomToFootPrint = dojo.hitch(this,this.zoomToFootPrint);
    this.zoomToInitExtent = dojo.hitch(this, this.zoomToInitExtent);
    this.reposition = dojo.hitch(this,this.reposition);
    this.clearGraphics = dojo.hitch(this, this.clearGraphics);
    
  },

  initialize: function() {
    var config = gptMapConfig;

    config.mapElementId = "interactiveMap";
    config.mapToolbarId = "frmSearchCriteria:mapToolbar";
    config.inputEnvelopeXMinId = "frmSearchCriteria:sfsMinX";
    config.inputEnvelopeYMinId = "frmSearchCriteria:sfsMinY";
    config.inputEnvelopeXMaxId = "frmSearchCriteria:sfsMaxX";
    config.inputEnvelopeYMaxId = "frmSearchCriteria:sfsMaxY";
    config.locatorInputId = "frmSearchCriteria:mapInput-locate";
    config.locatorCandidatesId = "locatorCandidates";
    esriConfig.defaults.io.proxyUrl = "<%=request.getContextPath()%>/catalog/download/proxy.jsp";
    esri.config.defaults.io.proxyUrl = "<%=request.getContextPath()%>/catalog/download/proxy.jsp";
 
    this._gptInpEnv = new GptInputEnvelope();
    this._gptInpEnv.initialize(config,this._gptMap,true);
 
  },

  onLocatorKeyPress: function(e) {
    if (!e) e = window.event;
    if (e) {
      var nKey = (e.keyCode) ? e.keyCode : e.which;
      if (nKey == 13) {
        if (this._gptLocator != null) this._gptLocator.locate();
        return false;
      }
    }
    return true;
  },

  onMapButtonClicked: function(sButtonName) {
    if (sButtonName == "zoomToWorld") {
      if (this._gptMap != null) this._gptMap.zoomToWorld();
    } else if (sButtonName == "zoomToInputEnvelope") {
      if (this._gptInpEnv != null) this._gptInpEnv.zoomToInputEnvelope();
    } else if (sButtonName == "locate") {
      if (this._gptLocator != null) this._gptLocator.locate();
    }
  },

  pointToExtent: function(
	      /*esri.Map*/ map, 
	      /*esri.geometry.Point (in map coords)*/ point) {
	    toleranceInPixel = 10;
	    //calculate map coords represented per pixel
	    var pixelWidth = map.extent.getWidth() / map.width;
	    //calculate map coords for tolerance in pixel
	    var toleraceInMapCoords = toleranceInPixel * pixelWidth;
	    //calculate & return computed extent
	    return new esri.geometry.Extent( point.x - toleraceInMapCoords,
	                                     point.y - toleraceInMapCoords,
	                                     point.x + toleraceInMapCoords,
	                                     point.y + toleraceInMapCoords,
	                                     map.spatialReference ); 
	  }, onMouseMove: function(event) {
	    var agsMap = null, bFound = false;
	    var aGfx, i, n, graphic, geometry, symbol, rec, recClass, fillStyle, fillColor;
	    if (this._gptMap != null) agsMap = this._gptMap.getAgsMap();
	    if ((agsMap != null) && (agsMap.graphics != null) && (agsMap.graphics.graphics != null)) {
	      aGfx = agsMap.graphics.graphics;
	      var validEnvs = new Array();
	      var validEnvsIndx = new Array();
	      for (i=aGfx.length-1;i>=0; i--) {
	        graphic = aGfx[i];
	        geometry = graphic.geometry;
	        var tmpGraphic = {};
	        if(graphic.geometry instanceof esri.geometry.Point) {
	          tmpGraphic.geometry = this.pointToExtent(agsMap, graphic.geometry)
	          tmpGraphic.gptSRTag = graphic.gptSRTag;
	        }
	        if(graphic.geometry!=null && graphic.gptSRTag != null && event && 
	          event.mapPoint && geometry!=null) {
	        	 var geomInScope = false;
	        	 if(geometry instanceof esri.geometry.Point) {
	        		 geomInScope = (this.pointToExtent(agsMap, event.mapPoint))
	        		   .contains(geometry);
	        	 } else {
	        		 geomInScope = geometry.contains(event.mapPoint);
	        	 }
	        	 if(geomInScope == true) {
	             validEnvs[graphic.gptSRTag] = "done";
	             validEnvsIndx[validEnvsIndx.length] = graphic.gptSRTag;
	        	 }
	        }
	      }
      for (i=aGfx.length-1;i>=0; i--) {
        graphic = aGfx[i];
        if (graphic.geometry!=null && graphic.gptSRTag != null) {
          recClass= "noneSelectedResultRow";
          geometry = graphic.geometry;
          fillStyle = esri.symbol.SimpleFillSymbol.STYLE_NULL;
          fillColor = new dojo.Color([255,255,0,0.3]);
          var highlight = false;
          if (validEnvs[graphic.gptSRTag] != null) {
            
            if (!bFound || graphic.gptSRTag == validEnvsIndx[0]  ) {
              fillStyle = esri.symbol.SimpleFillSymbol.STYLE_SOLID;
              recClass= "selectedResultRow";
            } else {
              recClass= "selectedResultRowLight";
              highlight = true;
            }
            bFound = true;
          }
          if(graphic.geometry instanceof esri.geometry.Point) {
        	  if(  recClass.indexOf("selected") == 0) {
        		  symbol = new esri.symbol.SimpleMarkerSymbol(
                          esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                          new esri.symbol.SimpleLineSymbol(
                              esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                              new dojo.Color([255,0,0]), 1), 
                              new dojo.Color([255,255,0,0.9]));
        	  } else {
        	    symbol = new esri.symbol.SimpleMarkerSymbol(
                      esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                      new esri.symbol.SimpleLineSymbol(
                          esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                          new dojo.Color([255,0,0]), 1), 
                          new dojo.Color([255,255,0,0.1]));
        	  }
        	  graphic.setSymbol(symbol);
          } else {
            symbol = new esri.symbol.SimpleFillSymbol(fillStyle,
                   new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                   new dojo.Color([255,0,0]), 2), fillColor);
            graphic.setSymbol(symbol);
          }
          rec = document.getElementById(graphic.gptSRTag);
          if (rec != null) rec.className = recClass;
        }
      }
    }
  },

  onMouseOut: function(event) {
    this.onMouseMove({"event":"noMapPoint"});
  },

  reposition: function() {
    if (this._gptMap != null) this._gptMap.reposition();
  },

  zoomToAOI: function(bIsInitializing) {
    var extent, wkid = 4326, mwkid, requiresProjection = false, srefFrom = null, srefTo = null;
    if ((this._gptMap != null) && (typeof(aoiOperator) != 'undefined') && (typeof(aoiMinX) != 'undefined')) {
      if ((aoiOperator == "anywhere") || (aoiMinX == -9999.0)) {
        this.zoomAnywhere();
      } else {
        extent = new esri.geometry.Extent(aoiMinX,aoiMinY,aoiMaxX,aoiMaxY,null);
        if ((aoiWkid != null) && (aoiWkid.length > 0) && !isNaN(aoiWkid)) {
          iwkid = parseInt(aoiWkid);
          if(iwkid != NaN) {
            var spatialReference = 
              new esri.SpatialReference({wkid : iwkid  }); 
            extent = new esri.geometry.Extent(aoiMinX,aoiMinY,aoiMaxX,aoiMaxY,
              spatialReference);
          }
        }
        if (this._gptMap.getAgsMap() != null) {
          srefTo = this._gptMap.getAgsMap().spatialReference;
          if (srefTo != null) {
            mwkid = srefTo.wkid;
            if ((mwkid != null) && (mwkid != wkid)) {
              srefFrom = new esri.SpatialReference({wkid:eval(wkid)});
              requiresProjection = true;
              if (this._gptMap.isGCSWkid(wkid) && this._gptMap.isGCSWkid(wkid)) {
                requiresProjection = false;
              }
            }
          }
        }

        if (!requiresProjection) {
          this._gptMap.zoom(extent);
        } else {
			    this._gptMap.projectExtent(extent, srefFrom, srefTo, dojo.hitch(this,function(gfx) {
			      var poly = this._gptMap.projectedExtentAsPolygon(gfx);
			      if (poly != null) this._gptMap.zoom(poly.getExtent());
			    }));
        }
      }
    }
  },


  zoomAnywhere: function zoomAnywhere() {
    if (this._gptMap != null) {
      if (this._gptMap._initialExtent!=null) {
        this._gptMap.zoomToInitial();
      } else {
        this._gptMap.zoomToDefault();
      }
    }
  },

  zoomToFootPrint: function(rowIndex) {
    var oBBox,extent;
    if ((this._gptMap != null) && (typeof(jsMetadata) != 'undefined') 
    		&& (typeof(jsMetadata.records[rowIndex]) != 'undefined')) {
      oBBox = jsMetadata.records[rowIndex].enclosingEnvelope;
      extent = this.getProcessedExtent(oBBox);
      if(extent.xmin == extent.xmax && extent.ymin == extent.ymax) {
    	  // This is a point
    	  extent.xmin = extent.xmin - 0.005;
    	  extent.xmax = extent.xmax + 0.005;
    	  extent.ymin = extent.ymin - 0.005;
        extent.ymax = extent.ymax + 0.005;
    	  //extent = extent.expand(2);
      }
      this._gptMap.zoomToGCSExtent(extent,true);
    }
  },

  zoomToThese: function zoomToThese() {
    var oBBox = new Array(), extent;
    if ((this._gptMap != null) && (typeof(resultsMapMinX) != 'undefined')) {
      oBBox.minX = resultsMapMinX;
      oBBox.maxX = resultsMapMaxX;
      oBBox.minY = resultsMapMinY;
      oBBox.maxY = resultsMapMaxY;
      extent = this.getProcessedExtent(oBBox);
      
      this._gptMap.zoomToGCSExtent(extent,true);
    }
  },
  
  zoomToInitExtent:function() {
   
    if ((aoiOperator == "anywhere") || (_initialMinX == -9999.0)) {
        this.zoomAnywhere();
        return;
    }
    
    var extent = new esri.geometry.Extent(
      _initialMinX,_initialMinY,_initialMaxX,_initialMaxY,
        new esri.SpatialReference({wkid:4326}));
    //extent = this.getProcessedExtent(extent);
    this._gptMap.zoomToGCSExtent(extent,true);
  }

});

var scMap = new SearchMap();

dojo.addOnLoad(scInit);
dojo.addOnLoad(deserializeFormFromCookie);

function scInit(){
  var elFocus = document.getElementById("frmSearchCriteria:scText");
  if (elFocus != null) elFocus.focus();

  scMap.initialize();
  dojo.connect(window,"onresize",scMap,"reposition");
  scMap.reposition();
} 
  
  
// Checks whether map is loaded and ready for actions
function scIsSearchMapReady() {
  var scSearchMap;
  return (GptUtils.exists(scSearchMap) && GptUtils.exists(scSearchMap.getMap) && GptUtils.exists(scSearchMap.getMap()) && GptUtils.exists(scSearchMap.getMap().loaded) && GptUtils.exists(scSearchMap.getMap().loaded == true));
}
 
/**
Submits from when on enter.
@param event The event variable
@param form The form to be submitted.
**/
function scSubmitForm(event, form) {

  var e = event;
  if (!e) e = window.event;
  var tgt = (e.srcElement) ? e.srcElement : e.target; 
  if ((tgt != null) && tgt.id) {
    if (tgt.id == "frmSearchCriteria:mapInput-locate") return;
  }
  
  if(!GptUtils.exists(event)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitform: could not get event so as to determine if to submit form ");
    return;
  }
  var code;
  
  if(GptUtils.exists(event.which)) {
    code = event.which;
  } else if (GptUtils.exists(event.keyCode)) {
    code = event.keyCode;
  } else {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitForm: Could not determine key pressed");
    return;
  }
  
  if(code == 13) {
    
    // Getting main search button
    var searchButtonId = "frmSearchCriteria:btnDoSearch";
    var searchButton = document.getElementById(searchButtonId);
    if(!GptUtils.exists(searchButton)){
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find button id = " + searchButtonId);
    } else if (!GptUtils.exists(searchButton.click)) {
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find click action on id = " + searchButtonId);
    } else {
      searchButton.click();
    }
  } else {
    return true;
  }
}


/*
* Prevents double submission of the form
* @param form The form to check
*/

function checkFormSubmitted(form) {
  // TODO: how did urban do this?
  if(!GptUtils.exists(form)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Form given not valid while checking  ");
    return false;
  }
  if(!GptUtils.exists(form.submitted) || form.submitted == false){
    form.submitted = true;
    setTimeout ( "uncheckForm()", 5000 );
    return true;
    
  } 
  
  return false;
}

/**
Aids in timed form submission to stop the events overflow
in JSF
**/
function uncheckForm() {
  var eForm = document.getElementById("frmSearchCriteria");
  if(eForm != null && GptUtils.exists(eForm.submitted)) {
    eForm.submitted = false;
  }
}

</script>
</f:verbatim>

<% // layout the page %>


<iframe src="../../../GcService/" width="100%" height="500" name="facetted search" marginheight="0" marginwidth="0" frameborder="0" id="myframe"  onload="startExternalSearch();">
</iframe>

<script>
//extracts search string from url, sends it to solr search field in iframe and clicks seacrhc button 
function startExternalSearch() {
	var parameter = location.search.substring(1); 
	var temp = parameter.split("=");
	if(temp[1] !== undefined){
		searchString = unescape(temp[1]);
	    var x = document.getElementById("myframe");
	    var y = (x.contentWindow || x.contentDocument);
	    if (y.document)y = y.document;
	    y.getElementById("g_solr_Expression_0_text").value = searchString;
	    y.getElementById("searchButton").click();
	}
}
</script>

<f:verbatim> 
</f:verbatim>


