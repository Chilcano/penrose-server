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
package org.safehaus.penrose.handler;

import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.matchingRule.EqualityMatchingRule;
import org.safehaus.penrose.mapping.Entry;
import org.safehaus.penrose.mapping.AttributeValues;
import org.safehaus.penrose.util.ExceptionUtil;
import org.safehaus.penrose.partition.Partition;
import org.ietf.ldap.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class CompareHandler {

    Logger log = LoggerFactory.getLogger(getClass());

    private Handler handler;

    public CompareHandler(Handler handler) {
        this.handler = handler;
    }
    
    public int compare(
            PenroseSession session,
            Partition partition,
            Entry entry,
            String attributeName,
            Object attributeValue
    ) throws Exception {

        int rc;
        try {

            List attributeNames = new ArrayList();
            attributeNames.add(attributeName);

            AttributeValues attributeValues = entry.getAttributeValues();
            Collection values = attributeValues.get(attributeName);

            AttributeType attributeType = handler.getSchemaManager().getAttributeType(attributeName);

            String equality = attributeType == null ? null : attributeType.getEquality();
            EqualityMatchingRule equalityMatchingRule = EqualityMatchingRule.getInstance(equality);

            log.debug("Comparing values:");
            for (Iterator i=values.iterator(); i.hasNext(); ) {
                Object value = i.next();

                boolean b = equalityMatchingRule.compare(value, attributeValue);
                log.debug(" - ["+value+"] => "+b);

                if (b) return LDAPException.COMPARE_TRUE;

            }

            return LDAPException.COMPARE_FALSE;

        } catch (LDAPException e) {
            rc = e.getResultCode();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            rc = ExceptionUtil.getReturnCode(e);
        }

        if (rc == LDAPException.SUCCESS) {
            log.warn("Compare operation succeded.");
        } else {
            log.warn("Compare operation failed. RC="+rc);
        }

        return rc;
    }

    public Handler getEngine() {
        return handler;
    }

    public void setEngine(Handler handler) {
        this.handler = handler;
    }
}
