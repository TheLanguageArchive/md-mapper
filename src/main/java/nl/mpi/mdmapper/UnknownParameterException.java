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

/**
 * An exception caused by reference to a parameter name that does not
 * exist.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class UnknownParameterException extends Exception {
    public UnknownParameterException(String message) {
        super(message);
    }
}
