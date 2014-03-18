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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * A mapping rule for an individual field (facet) that always returns
 * a string, irrespective of the content of the source. Variables can be
 * embedded in the string, with syntax "${variable}", in which case they
 * will be expanded (at the time the mapping is applied).
 *
 * @author Lari Lampen (MPI-PL)
 */
public class StringMapping extends Mapping {
    private static final Logger logger = Logger.getLogger(StringMapping.class);

    private final String string;

    /**
     * Parameters set in this configuration will be used for variable expansion.
     */
    private Configuration vars;

    /**
     * Create a new string mapping with the given literal string.
     */
    public StringMapping(String string) {
	this.string = string;
	this.vars = null;
    }

    /**
     * Create a new string mapping with the specified string, with variable
     * expansion performed from the given set of variable definitions. Note
     * that expansion occurs at the time of call (not time of creating the
     * StringMapping object) and reflects the values of the variables at that
     * time.
     *
     * @param string the base string, possibly containing ${var} references
     * @param variables configuration that defines values of variables
     */
    public StringMapping(String string, Configuration variables) {
	this.string = string;
	this.vars = variables;
    }

    @Override
    public String apply(Document doc) {
	numUses++;
	return expand(string, vars);
    }

    /**
     * Perform variable expansion on the string (syntax is ${var}).
     */
    public static String expand(String str, Configuration variables) {
	if (variables == null || str == null)
	    return str;

	String res = str;

	Pattern pattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
	for (;;) {
	    Matcher m = pattern.matcher(res);
	    if (!m.find())
		break;

	    String varName = m.group(1);
	    String val = variables.getParam(varName);
	    res = res.replace(m.group(0), (val == null) ? "" : val);
	}
	return res;
    }

    @Override
    public String toString() {
	return "'" + string + "'";
    }
}
