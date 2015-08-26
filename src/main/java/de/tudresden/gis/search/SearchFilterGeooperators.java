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
 
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchParameterMap;
import com.esri.gpt.catalog.search.SearchParameterMap.Value;
import com.esri.gpt.framework.util.Val;

/**
 * Implements a filter for geooperators (stored in the metadata tag)
 * 
 * @author Christin Henzen
 * 
 */
public class SearchFilterGeooperators implements ISearchFilterGeooperators { 
	
	/**
	 * generated id
	 */
	private static final long serialVersionUID = -7885005957167785700L;
	private static String KEY_GEOOPERATOR = "geooperator"; 
	private String geooperator;

	/**
	 * Method to return selected geooperator.
	 * @return geooperator as string
	 */
	public String getGeooperator() { 
		return Val.chkStr(geooperator);
	}

	/**
	 * Method to set geooperator value from JSP pages.
	 * @param geooperator
	 */
	public void setGeooperator(String geooperator) { 
		this.geooperator = geooperator;
	}

	/**
	 * Method to serialize selected geooperator as map
	 * @return geooperator as map
	 */
	public SearchParameterMap getParams() {
		SearchParameterMap map = new SearchParameterMap();
		map.put(KEY_GEOOPERATOR, map.new Value(this.getGeooperator(), "")); 
		return map;
	}
 
	/**
	 * Method to filter geooperator from all given search parameters and to set the intern geooperator value.
	 */
	public void setParams(SearchParameterMap parameterMap) throws SearchException {
		Value val = parameterMap.get(KEY_GEOOPERATOR);
		this.setGeooperator(val.getParamValue()); 		 
	}

	/**
	 * Method to check whether search filter type is search filter geooperator
	 */
	public boolean isEquals(Object obj) {
		if (obj instanceof SearchFilterGeooperators)
			return ((SearchFilterGeooperators) obj).getGeooperator().equals(this.getGeooperator()); 
		return false;
	}

	/**
	 * Method to reset selected values.
	 */
	public void reset() { 
		this.setGeooperator("");
	}

	/**
	 * Method to validate.
	 * (non-Javadoc)
	 * @see com.esri.gpt.catalog.search.ISearchFilter#validate()
	 */
	public void validate() throws SearchException {
		if (this.getGeooperator().equals("this should throw an exception")) { 
			throw new SearchException("this should throw an exception");
		}
	}

	@Override
	public String getSelectedGeooperator() { 
		return geooperator;
	}

	@Override
	public void setSelectedGeooperator(String geooperator) { 
		this.geooperator = geooperator;
	}
}
