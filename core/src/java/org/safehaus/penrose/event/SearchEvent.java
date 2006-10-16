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
package org.safehaus.penrose.event;

import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;

/**
 * @author Endi S. Dewata
 */
public class SearchEvent extends Event {

    public final static int BEFORE_SEARCH = 0;
    public final static int AFTER_SEARCH  = 1;

    private PenroseSession session;
    private String baseDn;
    private String filter;
    private PenroseSearchControls searchControls;
    private PenroseSearchResults searchResults;

    private int returnCode;

    public SearchEvent(
            Object source,
            int type,
            PenroseSession session,
            String baseDn,
            String filter,
            PenroseSearchControls searchControls,
            PenroseSearchResults searchResults
    ) {
        super(source, type);
        this.session = session;
        this.baseDn = baseDn;
        this.filter = filter;
        this.searchControls = searchControls;
        this.searchResults = searchResults;
    }

    public PenroseSession getSession() {
        return session;
    }

    public void setSession(PenroseSession session) {
        this.session = session;
    }

    /**
     * @deprecated
     */
    public String getBase() {
        return baseDn;
    }

    public String getBaseDn() {
        return baseDn;
    }

    /**
     * @deprecated
     */
    public void setBase(String base) {
        this.baseDn = base;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public PenroseSearchControls getSearchControls() {
        return searchControls;
    }

    public void setSearchControls(PenroseSearchControls searchControls) {
        this.searchControls = searchControls;
    }

    public PenroseSearchResults getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(PenroseSearchResults searchResults) {
        this.searchResults = searchResults;
    }
}
