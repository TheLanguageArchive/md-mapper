/*
 * Copyright (C) 2013-2014, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.mdmapper;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

/**
 * A mapping rule for an individual field (facet) based on an XPath expression.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class XpathMapping extends Mapping {
    private static final Logger logger = Logger.getLogger(XpathMapping.class);

    private XPath xpath;
    private String expression;

    public XpathMapping(XPath xpath, String expression) {
	this.xpath = xpath;
	this.expression = expression;
    }

    public String apply(Document doc) {
	try {
	    String s = xpath.evaluate(expression, doc);
	    s = s.trim();
	    if (!s.isEmpty()) {
		numUses++;
		return s;
	    }
	} catch (XPathExpressionException e) {
	    logger.error("XPath error (here: " + expression +"), skipping", e);
	}
	return "";
    }

    public String toString() {
	return "XPath '" + expression + "'";
    }
}
