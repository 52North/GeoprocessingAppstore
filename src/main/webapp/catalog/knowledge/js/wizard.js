function toggleWizard() {
    if (document.getElementById("wizard").style.visibility == "hidden") {
        document.getElementById("wizard").style.visibility = "visible";
        document.getElementById("wizard").style.height = "150px";
    } else {
        document.getElementById("wizard").style.visibility = "hidden";
        document.getElementById("wizard").style.height = "0px";
        document.getElementById("legacyGISChoice").style.display = "none";
        document.getElementById("pragmaticChoice").style.display = "none";
    }
}

function setGeodata(facet, state) {
    if (facet == "noPref") {
        document.getElementById("legacyGISChoice").style.display = "block";
        document.getElementById("pragmaticChoice").style.display = "none";
    } else {
        exhibit._componentMap.geodataFacet.setSelection(facet, state);
        exhibit._componentMap.geodataFacet._notifyCollection();
        document.getElementById("legacyGISChoice").style.display = "block";
        document.getElementById("pragmaticChoice").style.display = "none";
    }
}

function setLegacyGIS(facet, state) {
    if (facet == "noPref") {
        document.getElementById("pragmaticChoice").style.display = "block";
    } else {
        exhibit._componentMap.legacyGISFacet.setSelection(facet, state);
        exhibit._componentMap.legacyGISFacet._notifyCollection();
        document.getElementById("pragmaticChoice").style.display = "block";
    }
}

function setPragmatic(facet, state) {
    if (facet == "noPref") {
//        document.getElementById("pragmaticChoice").style.visibility = "visible";
    } else {
        exhibit._componentMap.pragmaticFacet.setSelection(facet, state);
        exhibit._componentMap.pragmaticFacet._notifyCollection();
//        document.getElementById("pragmaticChoice").style.visibility = "visible";
    }
}