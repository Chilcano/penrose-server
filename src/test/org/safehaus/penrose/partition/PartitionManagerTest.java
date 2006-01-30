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
package org.safehaus.penrose.partition;

import junit.framework.TestCase;
import org.apache.log4j.*;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.config.DefaultPenroseConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.Penrose;
import org.safehaus.penrose.session.PenroseSession;
import org.safehaus.penrose.session.PenroseSearchControls;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.ietf.ldap.LDAPEntry;
import org.ietf.ldap.LDAPException;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class PartitionManagerTest extends TestCase {

    PenroseConfig penroseConfig;
    Penrose penrose;

    public void setUp() throws Exception {

        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("[%d{MM/dd/yyyy HH:mm:ss}] %m%n"));
        BasicConfigurator.configure(appender);

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.OFF);

        Logger logger = Logger.getLogger("org.safehaus.penrose");
        logger.setLevel(Level.INFO);

        penroseConfig = new DefaultPenroseConfig();

        SchemaConfig schemaConfig = new SchemaConfig("samples/schema/example.schema");
        penroseConfig.addSchemaConfig(schemaConfig);

        penrose = new Penrose(penroseConfig);
        penrose.start();

    }

    public void tearDown() throws Exception {
        penrose.stop();
    }

    public void testAddingPartition() throws Exception {

        System.out.println("Searching before adding the partition");
        int rc = search();
        assertFalse("Search should fail", LDAPException.SUCCESS == rc);

        penrose.stop();

        PartitionConfig partitionConfig = new PartitionConfig("example", "samples/conf");
        penroseConfig.addPartitionConfig(partitionConfig);

        PartitionReader partitionReader = new PartitionReader();
        Partition partition = partitionReader.read(partitionConfig);

        PartitionManager partitionManager = penrose.getPartitionManager();
        partitionManager.addPartition(partitionConfig.getName(), partition);

        penrose.start();

        System.out.println("Searching after adding the partition");
        rc = search();
        assertTrue("Search should succeed", LDAPException.SUCCESS == rc);
    }

    public int search() throws Exception {

        PenroseSession session = penrose.newSession();
        session.bind(penroseConfig.getRootUserConfig().getDn(), penroseConfig.getRootUserConfig().getPassword());

        PenroseSearchControls sc = new PenroseSearchControls();
        sc.setScope(PenroseSearchControls.SCOPE_ONE);

        String baseDn = "ou=Categories,dc=Example,dc=com";

        System.out.println("Searching "+baseDn+":");
        PenroseSearchResults results = session.search(baseDn, "(objectClass=*)", sc);

        for (Iterator i = results.iterator(); i.hasNext();) {
            LDAPEntry entry = (LDAPEntry) i.next();
            System.out.println("dn: "+entry.getDN());
        }

        session.unbind();

        session.close();

        return results.getReturnCode();
    }
}