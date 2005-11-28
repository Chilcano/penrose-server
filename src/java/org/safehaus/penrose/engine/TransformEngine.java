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
package org.safehaus.penrose.engine;


import java.util.*;

import org.apache.log4j.Logger;
import org.safehaus.penrose.interpreter.Interpreter;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.connector.ConnectionConfig;

/**
 * @author Endi S. Dewata
 */
public class TransformEngine {

    static Logger log = Logger.getLogger(TransformEngine.class);

    public Engine engine;

    public int joinDebug = 0;
    public static int crossProductDebug = 0;

    public TransformEngine(Engine engine) {
        this.engine = engine;
    }

    /**
     * Convert attribute values into rows.
     *
     * Input: AttributeValues(value1=Collection(a, b, c), value2=Collection(1, 2, 3))
     * Output: List(Row(value1=a, value2=1), Row(value1=a, value2=2), ... )
     *
     * @param attributes
     * @return collection of Rows
     */
    public static Collection convert(AttributeValues attributes) {
        return convert(attributes.getValues());
    }

    /**
     * Convert map of values into rows.
     *
     * Input: Map(value1=Collection(a, b, c), value2=Collection(1, 2, 3))
     * Output: List(Row(value1=a, value2=1), Row(value1=a, value2=2), ... )
     *
     * @param values Map of collections.
     * @return collection of Rows
     */
    public static Collection convert(Map values) {
        List names = new ArrayList(values.keySet());
        List results = new ArrayList();
        Map temp = new HashMap();

        if (crossProductDebug >= 65535) {
            log.debug("Generating cross product:");
            log.debug("Names: "+names);
        }

        convert(values, names, 0, temp, results);

        return results;
    }

    public static void convert(Map values, List names, int pos, Map temp, Collection results) {

        if (pos < names.size()) {

            // get each attribute's values
            String name = (String)names.get(pos);
            Collection c = (Collection)values.get(name);

            if (c.isEmpty()) {
                c = new HashSet();
                c.add(null);
            }

            if (crossProductDebug >= 65535) {
            	//log.debug(name+": "+c);
            }

            for (Iterator iterator = c.iterator(); iterator.hasNext(); ) {
                Object value = iterator.next();

                temp.put(name, value);

                convert(values, names, pos+1, temp, results);
            }

        } else if (!temp.isEmpty()) {

            Row map = new Row(temp);
            results.add(map);

            //if (crossProductDebug >= 65535) {
            	//log.debug("Generated: "+map);
            //}

        } else {
            if (crossProductDebug >= 65535) {
            	//log.debug("Temp is empty: "+temp);
            }
        }
    }

    public Row translate(SourceMapping sourceMapping, AttributeValues input, AttributeValues output) throws Exception {

        PartitionConfig partitionConfig = engine.getConfigManager().getConfig(sourceMapping);
        ConnectionConfig connectionConfig = partitionConfig.getConnectionConfig(sourceMapping.getConnectionName());
        SourceDefinition sourceDefinition = connectionConfig.getSourceDefinition(sourceMapping.getSourceName());

        Interpreter interpreter = engine.getInterpreterFactory().newInstance();
        interpreter.set(input);

        Row pk = new Row();
        Collection fields = sourceMapping.getFieldMappings();

        //log.debug("Translating for sourceMapping "+sourceMapping.getName()+":");
        for (Iterator j=fields.iterator(); j.hasNext(); ) {
            FieldMapping fieldMapping = (FieldMapping)j.next();
            FieldDefinition fieldDefinition = sourceDefinition.getFieldDefinition(fieldMapping.getName());

            String name = fieldMapping.getName();
            //log.debug(" - "+name);

            Object newValues = interpreter.eval(fieldMapping);

            if (newValues == null) {
                if (fieldDefinition.isPrimaryKey()) pk = null;
                continue;
            }
/*
            if (field.getEncryption() != null) {
                // if field encryption is enabled

                String encryptionMethod = PasswordUtil.getEncryptionMethod(value);
                String encodingMethod = PasswordUtil.getEncodingMethod(value);
                String encryptedPassword = PasswordUtil.getEncryptedPassword(value);

                if (encryptionMethod == null) {
                    // if value is not encryption then encrypt value

                    value = PasswordUtil.encrypt(field.getEncryption(), field.getEncoding(), value);
                    log.debug("TRANSLATE - encrypt with "+field.getEncryption()+": "+value);

                } else if (field.getEncryption().equals(encryptionMethod)) {
                    // if field encryption is equal to value encryption

                    value = encryptedPassword;
                    log.debug("TRANSLATE - already encrypted: "+value);

                } else {
                	log.debug("TRANSLATE - unchanged: "+value);
                }
            }
*/

            //log.debug("   => "+newValues);

            if (fieldDefinition.isPrimaryKey()) {
                if (pk != null) pk.set(name, newValues);
            }

            output.add(name, newValues);
        }

        interpreter.clear();

        return pk;
    }

    public static Collection getPrimaryKeys(SourceDefinition sourceDefinition, AttributeValues sourceValues) throws Exception {

        AttributeValues pkValues = new AttributeValues();

        Collection pkFields = sourceDefinition.getPrimaryKeyFieldDefinitions();
        for (Iterator j=pkFields.iterator(); j.hasNext(); ) {
            FieldDefinition fieldDefinition = (FieldDefinition)j.next();

            Collection values = sourceValues.get(fieldDefinition.getName());
            if (values == null) {
                return new ArrayList();
            }

            pkValues.set(fieldDefinition.getName(), values);
        }

        return convert(pkValues);
    }

    public Map split(SourceMapping sourceMapping, AttributeValues entry) throws Exception {

        PartitionConfig partitionConfig = engine.getConfigManager().getConfig(sourceMapping);
        ConnectionConfig connectionConfig = partitionConfig.getConnectionConfig(sourceMapping.getConnectionName());
        SourceDefinition sourceDefinition = connectionConfig.getSourceDefinition(sourceMapping.getSourceName());

        Collection fields = sourceDefinition.getPrimaryKeyFieldDefinitions();

        AttributeValues output = new AttributeValues();
        Row m = translate(sourceMapping, entry, output);
        log.debug("PKs: "+m);
        log.debug("Output: "+output);

        Collection rows = convert(output);
        Map map = new TreeMap();

        for (Iterator i=rows.iterator(); i.hasNext(); ) {
            Row row = (Row)i.next();
            log.debug(" - "+row);

            AttributeValues av = new AttributeValues();
            av.add(row);

            Row pk = new Row();
            for (Iterator j=fields.iterator(); j.hasNext(); ) {
                FieldDefinition fieldDefinition = (FieldDefinition)j.next();
                pk.set(fieldDefinition.getName(), row.get(fieldDefinition.getName()));
            }

            map.put(pk, av);
        }

        return map;
    }

}
