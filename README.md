# ARCHIVED

This project is no longer maintained and will not receive any further updates. If you plan to continue using it, please be aware that future security issues will not be addressed.

# GeoprocessingAppstore 

Based on ESRI Geoportal (https://github.com/Esri/geoportal-server).<br/><br/>
Information about modified ESRI files can be found here: https://github.com/GeoinformationSystems/GeoprocessingAppstore/blob/master/src/de/tudresden/gis/docu/modifications<br/>
Information about used tutorials/howtos can be found here: https://github.com/GeoinformationSystems/GeoprocessingAppstore/blob/master/src/de/tudresden/gis/docu/readme<br/>

The Geoprocessing Appstore is a Web-platform respectively a portal to share geospatial algorithms in a community, and provides a software solution for current algorithm discovery and access problems. Based on the core concept of the publish-find-bind paradigm for service-oriented architectures, it connects developers and users of geospatial software (Figure 1): an algorithm provider publishes algorithm source code including a description of the provided functionality with technical requirements to execute the source code. Eventually, users can discover algorithms according to their needs and finally bind, respectively apply them. <br/><br/>
A live demo is available at: http://apps1.glues.geo.tu-dresden.de:8080/appstore.

## Structure

The application is based on Java, JSP, Javascript and HTML. The modules and their functionality are briefly described here.

* /WebContent - Browser part of the application (Javscript, HTML, JSP)
  * New manage websites for geooperators: https://github.com/GeoinformationSystems/GeoprocessingAppstore/blob/master/WebContent/catalog/publication
* /src - Server components of the application (Java)
  * New search, upload, download, edit implemenations: https://github.com/GeoinformationSystems/GeoprocessingAppstore/tree/master/src/de/tudresden/gis
  * New schema files for editor: https://github.com/GeoinformationSystems/GeoprocessingAppstore/tree/master/src/gpt/gxe/mcp
  * New schema files for detail view and template: https://github.com/GeoinformationSystems/GeoprocessingAppstore/tree/master/src/gpt/metadata/mcp

## Building

Prerequisites: 
* Download http://esri.github.io/geoportal-server/distribution/1.2.5/geoportal-1.2.5.zip
* Unzip archive, then unzip ../Web Applications/Geoportal/geoportal.war
* Copy the paths to ../geoportal/WEB-INF/lib/[arcgis_agsws_stubs.jar, arcgis_ws_runtime.jar, gpt-1.2.5.jar]
* Execute the following maven commands:
  * mvn install:install-file -Dfile=path-to-arcgis_agsws_stubs.jar -DgroupId=com.esri.arcgisws -DartifactId=arcgis_agsws_stubs -Dpackaging=jar -Dversion=10.0.0
  * mvn install:install-file -Dfile=path-to-arcgis_ws_runtime.jar -DgroupId=com.esri.arcgisws -DartifactId=arcgis_ws_runtime -Dpackaging=jar -Dversion=10.0.0
  * mvn install:install-file -Dfile=path-to-gpt-1.2.5.jar -DgroupId=com.esri.gpt -DartifactId=gpt -Dpackaging=jar -Dversion=1.2.5

You should now be able to build the Geoprocessing Appstore using the command mvn install 
  
## Installation

Information about the installation can be found here:
https://github.com/Esri/geoportal-server/wiki/Install-Esri-Geoportal-Server
(The appstore uses Tomcat and PostGIS)

1) Prepare appstore project <br/>
1a) Run ESRI Geoportal installation (configure database, ...) <br/>
1b) Create WAR file based on Geoprocessing Appstore project and deploy it in your favourite container (e.g. Tomcat) as "appstore" <br/>
1c) Create folders for source code storage and comment storage C:/MCPackage and C:/MCComment <br/>
1d) Choose folder for indices. Define path in appstore\WEB-INF\classes\gpt\config\gpt.xml -> line 76 (choose an existing folder) <br/>

2) Install and deploy solr <br/>
2a) Use solr install instructions <br/>
2b) A solr WAR file must be deploy in your container <br/>
2c) Adapt scheme: Copy file schema.xml (Additional_Configuration_Files) in ..\solr\collection1\conf and overwrite existing file <br/>
2d) Add folder solr\collection1\conf\lib and copy mcp-spliFilter.jar (Additional_Configuration_Files) into this folder <br/>

3) Prepare facet project (https://github.com/Esri/geoportal-server/wiki/Geoportal-Facet) <br/>
3a) Create WAR file based on Geoprocessing Appstore Facet project and deploy it in your favourite container (e.g. Tomcat) as "GcService" <br/>
3b) Configure database in gptdb2solr.xml <br/> 

4) Deploy 52°North WPS <br/>
41) Download and deploy WPS release (http://52north.org/communities/geoprocessing/wps/download.html) in your container as "52n-wps-webapp" <br/>
 
The Geoprocessing Appstore project uses and integrates a modified <b>ESRI Geoportal Facet</b> project (https://github.com/GeoinformationSystems/GeoprocessingAppstoreFacets). The facet website replaces the default advanced search in the ESRI geoportal. To remove this dependencies and use the default advanced search please modify WebContent/catalog/search/searchBody.jsp -> iframe.

## Configuration

Configurations (e.g. admin, database, ldap, ...) can be made in geoportal\WEB-INF\classes\gpt\config\gpt.xml

## ESRI Geoportal Server

Features, License, Support, Issues for the ESRI Geoportal Server can be found here: https://github.com/Esri/geoportal-server

## License

The Geoprocessing Appstore project is licensed under The Apache Software License, Version 2.0

## Contact (for TUD modifications)

Christin Henzen (christin.henzen@tu-dresden.de)
