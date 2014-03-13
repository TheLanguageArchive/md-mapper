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

import org.w3c.dom.Document;

/**
 * A mapping rule for an individual field (facet).
 *
 * @author Lari Lampen (MPI-PL)
 */
public abstract class Mapping {
    /**
     * Number of times this mapping has been applied with non-empty
     * result (for debug / statistics purporses).
     */
    protected int numUses;

    protected Mapping() {
	numUses = 0;
    }

    /**
     * Apply this mapping to the specificed document tree.
     *
     * @return result of mapping as a string, or the empty string if
     * there is no other result.
     */
    public abstract String apply(Document doc);

    /**
     * Get number of times this mapping has been applied with
     * non-empty result.
     */
    public int getNumUses() {
	return numUses;
    }
}
