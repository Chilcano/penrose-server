/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.handler;

import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.Entry;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.cache.EntryCache;
import org.apache.log4j.Logger;
import org.ietf.ldap.LDAPException;
import org.ietf.ldap.LDAPDN;
import org.ietf.ldap.LDAPConnection;
import org.ietf.ldap.LDAPSearchConstraints;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ModRdnHandler {

    Logger log = Logger.getLogger(getClass());

    public SessionHandler sessionHandler;

	public ModRdnHandler(SessionHandler sessionHandler) throws Exception {
        this.sessionHandler = sessionHandler;
	}

	public int modrdn(PenroseSession session, String dn, String newRdn)
			throws Exception {

        log.debug("-------------------------------------------------------------------------------");
        log.debug("MODRDN:");
        if (session != null && session.getBindDn() != null) log.info(" - Bind DN: " + session.getBindDn());
        log.debug(" - DN: " + dn);
        log.debug(" - New RDN: " + newRdn);

        String ndn = LDAPDN.normalize(dn);

        Entry entry = sessionHandler.getSearchHandler().find(session, ndn);
        if (entry == null) return LDAPException.NO_SUCH_OBJECT;

        int rc = performModRdn(session, entry, newRdn);
        if (rc != LDAPException.SUCCESS) return rc;
        
        String parentDn = Entry.getParentDn(dn);
        String newDn = newRdn+","+parentDn;

        PenroseSearchResults results = new PenroseSearchResults();

        sessionHandler.getSearchHandler().search(
                null,
                newDn,
                LDAPConnection.SCOPE_SUB,
                LDAPSearchConstraints.DEREF_NEVER,
                "(objectClass=*)",
                new ArrayList(),
                results
        );

        EntryCache entryCache = sessionHandler.getEngine().getEntryCache();
        for (Iterator i=results.iterator(); i.hasNext(); ) {
            Entry e = (Entry)i.next();
            entryCache.put(e);
        }

        sessionHandler.getEngine().getEntryCache().remove(entry);

        return rc;
	}

    public int performModRdn(
            PenroseSession session,
            Entry entry,
            String newRdn)
			throws Exception {

        int rc = sessionHandler.getACLEngine().checkModify(session, entry);
        if (rc != LDAPException.SUCCESS) return rc;

        EntryMapping entryMapping = entry.getEntryMapping();
        Partition partition = sessionHandler.getPartitionManager().getPartition(entryMapping);
        if (partition.isDynamic(entryMapping)) {
            return modRdnVirtualEntry(session, entry, newRdn);

        } else {
            return modRdnStaticEntry(entryMapping, newRdn);
        }
	}

    public int modRdnStaticEntry(
            EntryMapping entry,
            String newRdn)
			throws Exception {

        Partition partition = sessionHandler.getPartitionManager().getPartitionByDn(entry.getDn());
        partition.renameEntryMapping(entry, newRdn);

        return LDAPException.SUCCESS;
    }

    public int modRdnVirtualEntry(
            PenroseSession session,
            Entry entry,
			String newRdn)
            throws Exception {

        return sessionHandler.getEngine().modrdn(entry, newRdn);
    }
}
