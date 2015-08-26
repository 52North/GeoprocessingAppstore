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
package de.tudresden.gis.manage.xml;
import java.util.List;

/**
 * used for threaded reading with XmlHelpMethods
 * @author Bernd Grafe
 *
 */
public class threadReader implements Runnable{
	List<String> list; int start; int stop;int no;
	String text;
	
	public threadReader(int threadNumber,List<String> list, int start, int stop){
		this.no=threadNumber;
		this.list = list;
		this.start = start;
		this.stop = stop;
		text="";
	}
	
	@Override
	public void run() {
		//System.out.println("thread started from "+start+" to "+stop );
		for (int i = start;i<stop;i++){
			text+=list.get(i);
		}
		XmlHelpMethods.str[no]=text;
		//System.out.println("thread "+no+" done");	
	}
}