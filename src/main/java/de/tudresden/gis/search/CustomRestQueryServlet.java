/**
 * ﻿Copyright (C) 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package de.tudresden.gis.search;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.discovery.rest.RestQueryParser;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

/**
 * Implement REST query for geooperators, container types and platforms.
 * Based on RestQueryServlet.
 * 
 * @author Christin Henzen
 * 
 */
public class CustomRestQueryServlet extends RestQueryServlet {
	/**
	 * generated id
	 */
	private static final long serialVersionUID = 228635289332241528L;
	private static String REST_PARAM_KEY1 = "geooperator";
	private static String REST_PARAM_KEY2 = "containertype";
	private static String REST_PARAM_KEY3 = "platform";
 
	/**
	 * Method to parse the request - relate the rest queryable to the CSW queryables
	 */
	protected RestQuery parseRequest(HttpServletRequest request, RequestContext context) {
		 
		Logger LOG = Logger.getLogger(RestQuery.class.getCanonicalName());
		RestQuery query = super.parseRequest(request, context);
		RestQueryParser parser = new RestQueryParser(request, context, query); 
		
		//switch: true = OR, false = AND
	    parser.parsePropertyList(REST_PARAM_KEY1, "geooperator", ",", true);
		parser.parsePropertyList(REST_PARAM_KEY2, "containertype", ",", true);
		parser.parsePropertyList(REST_PARAM_KEY3, "platform", ",", true); 
 
		/**
		 * The below is shown as an example parser.parseRepositoryId("rid");
		 * parser.parseResponseFormat("f");
		 * parser.parseResponseGeometry("geometryType");
		 * parser.parseResponseStyle("style");
		 * parser.parseResponseTarget("target");
		 * parser.parseStartRecord("start",1); parser.parseMaxRecords("max",10);
		 * parser.parsePropertyIsEqualTo("uuid","uuid");
		 * parser.parsePropertyIsLike("searchText","anytext");
		 * parser.parsePropertyList("contentType","dc:type",",",true);
		 * parser.parsePropertyList("dataCategory","dc:subject",",",true);
		 * parser.parsePropertyRange("after","before","dct:modified");
		 * parser.parseSpatialClause("bbox","spatialRel","geometry");
		 * parser.parseSortables("orderBy");
		 **/
		LOG.log(Level.FINER, "In Custom Rest Query Servlet");

		return query;
	}
 
	/**
	 * Populate the searchCriteria with the rest queryable geooperator, container type and platform.
	 */
	protected SearchCriteria toSearchCriteria(HttpServletRequest request, RequestContext context, RestQuery query) {
		SearchCriteria criteria = super.toSearchCriteria(request, context, query);
		RestQueryParser parser = new RestQueryParser(request, context, query);
		
		String sGeooperator = Val.chkStr(parser.getRequestParameter(REST_PARAM_KEY1)); 
		if (sGeooperator.length() > 0) { 
			SearchFilterGeooperators filterGeooperator = new SearchFilterGeooperators();
			filterGeooperator.setGeooperator(sGeooperator);
			criteria.getMiscelleniousFilters().add(filterGeooperator);
		}
		
		String sContainertype = Val.chkStr(parser.getRequestParameter(REST_PARAM_KEY2));
		if (sContainertype.length() > 0) {  
			SearchFilterContainertype filterContainertype = new SearchFilterContainertype();
			filterContainertype.setContainertype(sContainertype);
			criteria.getMiscelleniousFilters().add(filterContainertype);
		}
		
		String sPlatform = Val.chkStr(parser.getRequestParameter(REST_PARAM_KEY3));
		if (sPlatform.length() > 0) { 
			SearchFilterPlatform filterPlatform = new SearchFilterPlatform();
			filterPlatform.setPlatform(sPlatform);
			criteria.getMiscelleniousFilters().add(filterPlatform);
		}
		
		return criteria;
	}
}
