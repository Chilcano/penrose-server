/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.engine;

import org.safehaus.penrose.entry.DN;
import org.safehaus.penrose.entry.AttributeValues;
import org.safehaus.penrose.entry.RDN;
import org.safehaus.penrose.mapping.EntryMapping;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class EntryData {

    private DN dn;
    private EntryMapping entryMapping;
    private AttributeValues mergedValues;
    private Collection rows;
    private RDN filter;
    private AttributeValues loadedSourceValues;
    private boolean complete;

    public EntryData() {
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public AttributeValues getMergedValues() {
        return mergedValues;
    }

    public void setMergedValues(AttributeValues mergedValues) {
        this.mergedValues = mergedValues;
    }

    public Collection getRows() {
        return rows;
    }

    public void setRows(Collection rows) {
        this.rows = rows;
    }

    public RDN getFilter() {
        return filter;
    }

    public void setFilter(RDN filter) {
        this.filter = filter;
    }

    public AttributeValues getLoadedSourceValues() {
        return loadedSourceValues;
    }

    public void setLoadedSourceValues(AttributeValues loadedSourceValues) {
        this.loadedSourceValues = loadedSourceValues;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }
}
