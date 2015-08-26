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
package de.tudresden.gis.manage.geooperators;
  
import java.io.File; 
import java.io.FileNotFoundException;
import java.io.FileOutputStream; 
import java.io.FileReader;
import java.io.IOException; 
import java.io.FileWriter;
import java.io.InputStream;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent; 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; 

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

/**
 * The class implements the action listener for managing (add and remove) geooperators.
 * The class is used to execute actions based on user input in backend section manage geooperators.
 * 
 * @author Christin Henzen, Jochen Lenz 
 *
 */
public class GeooperatorController extends BaseActionListener {
	
	private String geooperator = "";
	private String definition = "";
	private String scopeNote = "";
	private String narrowMatch = "";
	private String broadMatch = "";
	private String closeMatch = "";
	private String formalCategories = "";
	private String geodataCategories = "";
	private String geoinformaticsCategories = "";
	private String legacyGISCategories = "";
	private String pragmaticCategories = "";
	private String technicalCategories = "";
	private String parentgeooperator = "";
	private String removegeooperator = "";
	private SelectableGeooperators selectableGeooperators;
	private String path = Thread.currentThread().getContextClassLoader().getResource("gpt/search/browse/browse-catalog.xml").getPath();
	
	/**
	 * Constructor
	 */
	public GeooperatorController() {
		super();
		selectableGeooperators = new SelectableGeooperators();
		selectableGeooperators.build();
	}
	
	//-- add geooperator ---------------------------------------
	
	/**
	 * Method to get new geooperator name.
	 * 
	 * @return geooperator
	 */
	public String getGeooperator() {
		return geooperator;
	}
	
	/**
	 * Method to get new geooperator definition.
	 * 
	 * @return definition
	 */	
	public String getDefinition() {
		return definition;
	}
	
	/**
	 * Method to get new geooperator scopeNote.
	 * 
	 * @return scopeNote
	 */	
	public String getScopeNote() {
		return scopeNote;
	}
	
	/**
	 * Method to get new geooperator narrowMatch.
	 * 
	 * @return narrowMatch
	 */	
	public String getNarrowMatch() {
		return narrowMatch;
	}
	
	/**
	 * Method to get new geooperator broadMatch.
	 * 
	 * @return broadMatch
	 */	
	public String getBroadMatch() {
		return broadMatch;
	}
	
	/**
	 * Method to get new geooperator closeMatch.
	 * 
	 * @return closeMatch
	 */	
	public String getCloseMatch() {
		return closeMatch;
	}
	
	/**
	 * Method to get new geooperator formalCategories.
	 * 
	 * @return formalCategories
	 */	
	public String getFormalCategories() {
		return formalCategories;
	}
	
	/**
	 * Method to get new geooperator geodataCategories.
	 * 
	 * @return geodataCategories
	 */	
	public String getGeodataCategories() {
		return geodataCategories;
	}
	
	/**
	 * Method to get new geooperator geoinformaticsCategories.
	 * 
	 * @return geoinformaticsCategories
	 */	
	public String getGeoinformaticsCategories() {
		return geoinformaticsCategories;
	}
	
	/**
	 * Method to get new geooperator legacyGISCategories.
	 * 
	 * @return legacyGISCategories
	 */	
	public String getLegacyGISCategories() {
		return legacyGISCategories;
	}
	
	/**
	 * Method to get new geooperator pragmaticCategories.
	 * 
	 * @return pragmaticCategories
	 */	
	public String getPragmaticCategories() {
		return pragmaticCategories;
	}
	
	/**
	 * Method to get new geooperator technicalCategories.
	 * 
	 * @return technicalCategories
	 */	
	public String getTechnicalCategories() {
		return technicalCategories;
	}

	/**
	 * Method to get chosen parent of new geooperator.
	 * 
	 * @return parent geooperator name
	 */
	public String getParentgeooperator() {
		return parentgeooperator;
	}
	
	/**
	 * Method to set new geooperator name.
	 * 
	 * @param geooperator
	 */
	public void setGeooperator(String geooperator) {
		this.geooperator = geooperator;
	}
	
	/**
	 * Method to set new geooperator definition.
	 * 
	 * @param definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * Method to set new geooperator scopeNote.
	 * 
	 * @param scopeNote
	 */
	public void setScopeNote(String scopeNote) {
		this.scopeNote = scopeNote;
	}

	/**
	 * Method to set new geooperator narrowMatch.
	 * 
	 * @param narrowMatch
	 */
	public void setNarrowMatch(String narrowMatch) {
		this.narrowMatch = narrowMatch;
	}

	/**
	 * Method to set new geooperator broadMatch.
	 * 
	 * @param broadMatch
	 */
	public void setBroadMatch(String broadMatch) {
		this.broadMatch = broadMatch;
	}

	/**
	 * Method to set new geooperator closeMatch.
	 * 
	 * @param closeMatch
	 */
	public void setCloseMatch(String closeMatch) {
		this.closeMatch = closeMatch;
	}

	/**
	 * Method to set new geooperator formalCategories.
	 * 
	 * @param formalCategories
	 */
	public void setFormalCategories(String formalCategories) {
		this.formalCategories = formalCategories;
	}

	/**
	 * Method to set new geooperator geodataCategories.
	 * 
	 * @param geodataCategories
	 */
	public void setGeodataCategories(String geodataCategories) {
		this.geodataCategories = geodataCategories;
	}

	/**
	 * Method to set new geooperator geoinformaticsCategories.
	 * 
	 * @param geoinformaticsCategories
	 */
	public void setGeoinformaticsCategories(String geoinformaticsCategories) {
		this.geoinformaticsCategories = geoinformaticsCategories;
	}

	/**
	 * Method to set new geooperator legacyGISCategories.
	 * 
	 * @param legacyGISCategories
	 */
	public void setLegacyGISCategories(String legacyGISCategories) {
		this.legacyGISCategories = legacyGISCategories;
	}

	/**
	 * Method to set new geooperator pragmaticCategories.
	 * 
	 * @param pragmaticCategories
	 */
	public void setPragmaticCategories(String pragmaticCategories) {
		this.pragmaticCategories = pragmaticCategories;
	}

	/**
	 * Method to set new geooperator technicalCategories.
	 * 
	 * @param technicalCategories
	 */
	public void setTechnicalCategories(String technicalCategories) {
		this.technicalCategories = technicalCategories;
	}
	
	/**
	 * Method to set parent of new geooperator.
	 * 
	 * @param parentgeooperator
	 */
	public void setParentgeooperator(String parentgeooperator) {
		this.parentgeooperator = parentgeooperator;
	}
	
	//-- remove geooperator ---------------------------------------
	
	/**
	 * Method to get chosen geooperator, which should be removed.
	 * 
	 * @return geooperator name to be removed
	 */
	public String getRemovegeooperator() {
		return removegeooperator;
	}
	
	/**
	 * Method to set chosen geooperator, which should be removed.
	 * 
	 * @param removegeooperator
	 */
	public void setRemovegeooperator(String removegeooperator) {
		this.removegeooperator = removegeooperator;
	}
	
	//------------------------------------------------------------
	
	/**
	 * Method to get available geooperators.
	 * 
	 * @return geooperators as @see SelectableGeooperators.
	 */
	public SelectableGeooperators getSelectableGeooperators() {
		return selectableGeooperators;
	}
	
	/**
	 * Method get url conform label created from input field prefLabel
	 * @return label
	 * @throws UnsupportedEncodingException
	 */
	public String getLabel() throws UnsupportedEncodingException{
		String label;
		if (!geooperator.equals("0")){
			label = geooperator.replaceAll(" ", "_");
			label = URLEncoder.encode(label, "UTF-8");
		} else {
			label ="notSet";
		}
		return label;
	}
	
	@Override
	/**
	 * Method to trigger add or remove scripts, based on user's action.
	 */
	protected void processSubAction(ActionEvent event, RequestContext context)
	    throws AbortProcessingException, Exception {
		
		 UIComponent component = event.getComponent();
		 String sCommand = Val.chkStr((String) component.getAttributes().get("submit"));
		 if (sCommand.equals("add")) { 
//			 checkMandatoryFields();
			 addGeooperator();
			 
			 //TODO: finish implementation, bug fix file location, ... and uncomment this.
//			 addRDF();
//			 addJSON();
		 } else { 
			 removeGeooperator(); 
		 } 
	}
	
	/**
	 * Method checks if mandatory fields in form are filled.
	 */
	private void checkMandatoryFields(){
		if (geooperator.equals("0")){
			throw new IllegalArgumentException( "Name of new geooperator is requierd." );
		}
		if (definition.equals("0")){
			throw new IllegalArgumentException( "Definition of new geooperator is requierd." );			
		}
	}

	/**
	 * Method creates RDF model with values from form.
	 * 
	 * @return Model 
	 */
	private Model getRDFModel() throws UnsupportedEncodingException{
		String nameSpace = "http://purl.org/net/jbrauner/geooperators#";

		Model model = ModelFactory.createDefaultModel();
		 
		 Resource description;
		 description = model.createResource( nameSpace + getLabel() );

		 if (!definition.equals("0")){
			 Property property_definition = model.createProperty( nameSpace, "definition" );
			 model.add (description, property_definition, ResourceFactory.createTypedLiteral(definition, XSDDatatype.XSDstring));
		 }

		 if (!geooperator.equals("0")){
			 Property property_prefLabel = model.createProperty( nameSpace, "prefLabel" );
			 model.add (description, property_prefLabel, ResourceFactory.createTypedLiteral(geooperator, XSDDatatype.XSDstring));
		 }

		 if (!scopeNote.equals("0")){
			 Property property_scopeNote = model.createProperty( nameSpace, "scopeNote" );
			 model.add (description, property_scopeNote, ResourceFactory.createTypedLiteral(scopeNote, XSDDatatype.XSDstring));
		 }

		 if (!narrowMatch.equals("0")){
			 Property property_narrowMatch = model.createProperty( nameSpace, "narrowMatch" );
			 Resource resource_narrowMatch = model.createResource( nameSpace + narrowMatch );
			 model.add( description, property_narrowMatch, resource_narrowMatch );
		 }		 

		 if (!broadMatch.equals("0")){
			 Property property_broadMatch = model.createProperty( nameSpace, "broadMatch" );
			 Resource resource_broadMatch = model.createResource( nameSpace + broadMatch );
			 model.add( description, property_broadMatch, resource_broadMatch );
		 }		 

		 if (!closeMatch.equals("0")){
			 Property property_closeMatch = model.createProperty( nameSpace, "closeMatch" );
			 Resource resource_closeMatch = model.createResource( nameSpace + closeMatch );
			 model.add( description, property_closeMatch, resource_closeMatch );
		 }

		 if (!formalCategories.equals("0")){
			 Property property_formalCategories = model.createProperty( nameSpace, "formalCategories" );
			 Resource resource_formalCategories = model.createResource( nameSpace + formalCategories );
			 model.add( description, property_formalCategories, resource_formalCategories );
		 }

		 if (!geodataCategories.equals("0")){
			 Property property_geodataCategories = model.createProperty( nameSpace, "geodataCategories" );
			 Resource resource_geodataCategories = model.createResource( nameSpace + geodataCategories );
			 model.add( description, property_geodataCategories, resource_geodataCategories );
		 }

		 if (!geoinformaticsCategories.equals("0")){
			 Property property_geoinformaticsCategories = model.createProperty( nameSpace, "geoinformaticsCategories" );
			 Resource resource_geoinformaticsCategories = model.createResource( nameSpace + geoinformaticsCategories );
			 model.add( description, property_geoinformaticsCategories, resource_geoinformaticsCategories );
		 }

		 if (!legacyGISCategories.equals("0")){
			 Property property_legacyGISCategories = model.createProperty( nameSpace, "legacyGISCategories" );
			 Resource resource_legacyGISCategories = model.createResource( nameSpace + legacyGISCategories );
			 model.add( description, property_legacyGISCategories, resource_legacyGISCategories );
		 }

		 if (!pragmaticCategories.equals("0")){		 
			 Property property_pragmaticCategories = model.createProperty( nameSpace, "pragmaticCategories" );
			 Resource resource_pragmaticCategories = model.createResource( nameSpace + pragmaticCategories );
			 model.add( description, property_pragmaticCategories, resource_pragmaticCategories );
		 }

		 if (!technicalCategories.equals("0")){			 
			 Property property_technicalCategories = model.createProperty( nameSpace, "technicalCategories" );
			 Resource resource_technicalCategories = model.createResource( nameSpace + technicalCategories );
			 model.add( description, property_technicalCategories, resource_technicalCategories );
		 }
		 
		 model.setNsPrefix( "skos", nameSpace );
		 
		 return model;
	}
	
	/**
	 * Method to create RDF file for geooperator.
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */	
	private void addRDF() throws FileNotFoundException, UnsupportedEncodingException {
		
		String pathRDF = path.replaceAll("browse-catalog.xml", "geooperators.rdf");
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open( pathRDF );
		if (in == null) {
			Model model2 = getRDFModel();
			FileOutputStream fout=new FileOutputStream(pathRDF);
			model2.write(fout);
		} else {
			model.read(in, null);
			Model model3 = getRDFModel();
			Model model4 = model.union(model3);
			FileOutputStream fout=new FileOutputStream(pathRDF);
			model4.write(fout);
		}
	}
	
	/**
	 * Method creates JSON Object with values from form.
	 * 
	 * @return JSONObject 
	 */
    private JSONObject getJSONObject() throws UnsupportedEncodingException{
    	JSONObject obj = new JSONObject();
    	
		obj.put("uri", "http://purl.org/net/jbrauner/geooperators#" + getLabel());
		
		obj.put("definition", definition);
		
	 	obj.put("label", getLabel());
		
		obj.put("prefLabel", geooperator);
		
		obj.put("scopeNote", scopeNote); 
		
		if (!narrowMatch.equals("0")){
			JSONArray narrowMatchArray = new JSONArray();
			narrowMatchArray.add(narrowMatch);
		 	obj.put("narrowMatch", narrowMatchArray);
		}
		if (!broadMatch.equals("0")){
			JSONArray broadMatchArray = new JSONArray();
			broadMatchArray.add(broadMatch);
		 	obj.put("broadMatch", broadMatchArray);
		}
		if (!closeMatch.equals("0")){
			JSONArray closeMatchArray = new JSONArray();
			closeMatchArray.add(closeMatch);
		 	obj.put("closeMatch", closeMatchArray);	 		 	
		}
		if (!narrowMatch.equals("0") && !broadMatch.equals("0")&& !closeMatch.equals("0")){
			JSONArray relatedGeooperators = new JSONArray();
			if (!narrowMatch.equals("0")){
				relatedGeooperators.add(narrowMatch);
			}
			if (!broadMatch.equals("0")){
				relatedGeooperators.add(broadMatch);
			}
			if (!closeMatch.equals("0")){
				relatedGeooperators.add(closeMatch);
			}
		 	obj.put("relatedGeooperators", relatedGeooperators);
		}
	 	
		obj.put("type", "Concept");
		
		if (!formalCategories.equals("0")){
			JSONArray formalCategoriesArray = new JSONArray();
			formalCategoriesArray.add(formalCategories);
		 	obj.put("formalCategories", formalCategoriesArray);
		}

		if (!geodataCategories.equals("0")){
			JSONArray geodataCategoriesArray = new JSONArray();
			geodataCategoriesArray.add(geodataCategories);
		 	obj.put("geodataCategories", geodataCategoriesArray);
		}

		if (!geoinformaticsCategories.equals("0")){		 	
			JSONArray geoinformaticsCategoriesArray = new JSONArray();
			geoinformaticsCategoriesArray.add(geoinformaticsCategories);
		 	obj.put("geoinformaticsCategories", geoinformaticsCategoriesArray);
		}

		if (!legacyGISCategories.equals("0")){
			JSONArray legacyGISCategoriesArray = new JSONArray();
			legacyGISCategoriesArray.add(legacyGISCategories);
		 	obj.put("legacyGISCategories", legacyGISCategoriesArray);
		}

	 	if (!pragmaticCategories.equals("0")){
			JSONArray pragmaticCategoriesArray = new JSONArray();
			pragmaticCategoriesArray.add(pragmaticCategories);
		 	obj.put("pragmaticCategories", pragmaticCategoriesArray);
	 	}

	 	if (!technicalCategories.equals("0")){
			JSONArray technicalCategoriesArray = new JSONArray();
			technicalCategoriesArray.add(technicalCategories);
		 	obj.put("technicalCategories", technicalCategoriesArray);
	 	}
	 	
	 	return obj;
    }
    
	/**
	 * Method writes JSON File to harddisk.
	 * 
	 * @param obj - JSON Object wich will be written inside the file.
	 * @param path - path on harddisk where file is written
	 */
    private void writeJSON(JSONObject obj, String path){
		try {
			FileWriter file = new FileWriter(path);
			file.write(obj.toJSONString());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * Method to create JSON file for geooperator.
	 * @throws org.json.simple.parser.ParseException 
	 * @throws UnsupportedEncodingException 
	 */
	private void addJSON() throws org.json.simple.parser.ParseException, UnsupportedEncodingException {
		
		JSONParser parser = new JSONParser();
		String pathJSON = path.replaceAll("browse-catalog.xml", "geooperators.json");
		
		try {
			Object obj = parser.parse(new FileReader(pathJSON));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject jsonObject2 = getJSONObject();
		 	JSONArray list = new JSONArray();
			JSONArray items = (JSONArray) jsonObject.get("items");		
			Iterator<JSONObject> iterator = items.iterator();
			while (iterator.hasNext()) {
				list.add(iterator.next());
			}			 
			list.add(jsonObject2);
			jsonObject.put("items", list);

			writeJSON(jsonObject, pathJSON);
		} catch (FileNotFoundException e) {
			JSONObject jsonObject = new JSONObject();
			JSONObject obj = getJSONObject();
		 	JSONArray list = new JSONArray();
			list.add(obj);
			jsonObject.put("items", list);			 	
		
			writeJSON(jsonObject, pathJSON);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		 
	/**
	 * Method to add a geooperator to the geooperator registry, stored in a xml file.
	 * The registry is used to fill browse view in the appstore front end.
	 */
	private void addGeooperator() { 
		MessageBroker msgBroker = extractMessageBroker();
    
		try { 
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();   
			NodeList nList = doc.getElementsByTagName("item"); 
			 
			String[] addedGeoopArray = new String[1];
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp); 

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
  
					if (eElement.getElementsByTagName("id").item(0).getTextContent().equals(parentgeooperator)) {
						Node geooperatorNode = createGeooperatorNode(doc, createGeooperatorId(geooperator), geooperator, "searchText=keywords:\"" + geooperator + "\"");
						Node geooperatorImportNode = doc.importNode(geooperatorNode, true);
						nNode.appendChild(geooperatorImportNode); 
						writeUpdatedXMLFile(doc, path);
						
						addedGeoopArray[0] = geooperator;
					}
				} 
			}
			
			if (addedGeoopArray[0] == null) {
				Node geooperatorNode = createGeooperatorNode(doc, createGeooperatorId(geooperator), geooperator, "searchText=keywords:\"" + geooperator + "\"");
				Node geooperatorImportNode = doc.importNode(geooperatorNode, true);
				NodeList treeList = doc.getElementsByTagName("tree"); 
				for (int temp = 0; temp < treeList.getLength(); temp++) {
					Node nNode = treeList.item(temp); 
					nNode.appendChild(geooperatorImportNode); 
					writeUpdatedXMLFile(doc, path);
					addedGeoopArray[0] = geooperator;
				} 
			}
			
			msgBroker.addSuccessMessage("catalog.publication.addGeooperator.success", addedGeoopArray); 
		} catch (Exception e) {
			e.printStackTrace();
			msgBroker.addErrorMessage("catalog.publication.addGeooperator.error");
		}
	}
	
	/**
	 * Method to generate a random id for new geooperators.
	 * Geooperator registry requires unique id for each stored geooperator.
	 * 
	 * @param geooperator
	 * @return id
	 */
	private String createGeooperatorId(String geooperator) {
		String id = geooperator.replace(" ", "");
		if (selectableGeooperators.getItems().contains(geooperator)) 
			id = id + Math.random(); 
		return id;
	}
	
	/**
	 * Method to generate a new geooperator node to insert in the geooperator registry file.
	 * 
	 * @param doc - geooperator registry store file.
	 * @param id - id of new geooperator
	 * @param name - name of new geooperator
	 * @param query - query string: how to query process descriptions based on new geooperator
	 * @return xml node with information about new geooperator
	 */
	private Node createGeooperatorNode(Document doc, String id, String name, String query) {
		Node item = doc.createElement("item");
		
		Node idNode = doc.createElement("id");
		Node idTextNode = doc.createTextNode(id);
		Node idTextImportNode = doc.importNode(idTextNode, true);
		idNode.appendChild(idTextImportNode);
		Node idImportNode = doc.importNode(idNode, true);
		item.appendChild(idImportNode);
		
		Node nameNode = doc.createElement("name");
		Node nameTextNode = doc.createTextNode(name);
		Node nameTextImportNode = doc.importNode(nameTextNode, true);
		nameNode.appendChild(nameTextImportNode);
		Node nameImportNode = doc.importNode(nameNode, true);
		item.appendChild(nameImportNode);
		
		Node queryNode = doc.createElement("query");
		Node queryTextNode = doc.createTextNode(query);
		Node queryTextImportNode = doc.importNode(queryTextNode, true);
		queryNode.appendChild(queryTextImportNode);
		Node queryImportNode = doc.importNode(queryNode, true);
		item.appendChild(queryImportNode);
		
		return item;
	}
	
	/**
	 * Method to store updated geooperator registry.
	 * 
	 * @param doc - updated list of geooperators as xml document
	 * @param path - path of geooperator registry xml document
	 */
	private void writeUpdatedXMLFile(Document doc, String path) { 
	    try { 
	      Source xmlSource = new DOMSource(doc); 
	      Result result = new StreamResult(new FileOutputStream(path)); 
	      TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
	      Transformer transformer = transformerFactory.newTransformer(); 
	      transformer.setOutputProperty("indent", "yes"); 
	      transformer.transform(xmlSource, result);
	    } 
	    catch (TransformerFactoryConfigurationError | TransformerException | IOException e) {
	      e.printStackTrace();
	    }
	}
	
	/**
	 * Method to remove a geooperator from the registry.
	 */
	private void removeGeooperator() {
		MessageBroker msgBroker = extractMessageBroker();
		
		try { 
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();   
			NodeList nList = doc.getElementsByTagName("item"); 
			 
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp); 

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
  
					if (eElement.getElementsByTagName("id").item(0).getTextContent().equals(removegeooperator)) {
						nNode.getParentNode().removeChild(nNode);
						writeUpdatedXMLFile(doc, path);
					}
				}
			}
			
			msgBroker.addSuccessMessage("catalog.publication.removeGeooperator.success", new String[1]); 
		} catch (Exception e) {
			e.printStackTrace();
			msgBroker.addErrorMessage("catalog.publication.removeGeooperator.error");
		}
	}
}
