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
package org.safehaus.penrose.connector;

import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.ietf.ldap.LDAPException;
import org.safehaus.penrose.session.PenroseSearchResults;
import org.safehaus.penrose.engine.TransformEngine;
import org.safehaus.penrose.util.Formatter;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.partition.SourceConfig;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCAdapter extends Adapter {

    public final static String DRIVER     = "driver";
    public final static String URL        = "url";
    public final static String USER       = "user";
    public final static String PASSWORD   = "password";

    public final static String TABLE_NAME = "tableName";
    public final static String FILTER     = "filter";

    public final static String MAX_ACTIVE                           = "maxActive";
    public final static String MAX_IDLE                             = "maxIdle";
    public final static String MIN_IDLE                             = "minIdle";
    public final static String MAX_WAIT                             = "maxWait";

    public final static String VALIDATION_QUERY                     = "validationQuery";
    public final static String TEST_ON_BORROW                       = "testOnBorrow";
    public final static String TEST_ON_RETURN                       = "testOnReturn";
    public final static String TEST_WHILE_IDLE                      = "testWhileIdle";
    public final static String TIME_BETWEEN_EVICTION_RUNS_MILLIS    = "timeBetweenEvictionRunsMillis";
    public final static String NUM_TESTS_PER_EVICTION_RUN           = "numTestsPerEvictionRun";
    public final static String MIN_EVICTABLE_IDLE_TIME_MILLIS       = "minEvictableIdleTimeMillis";

    public final static String SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS  = "softMinEvictableIdleTimeMillis";
    public final static String WHEN_EXHAUSTED_ACTION                = "whenExhaustedAction";

    GenericObjectPool connectionPool;
    public DataSource ds;

    public JDBCFilterTool filterTool;

    public void init() throws Exception {

        Properties properties = new Properties();
        for (Iterator i=getParameterNames().iterator(); i.hasNext(); ) {
            String param = (String)i.next();
            String value = getParameter(param);
            properties.setProperty(param, value);
        }

        String driver = (String)properties.remove(DRIVER);
        String url = (String)properties.remove(URL);

        Class.forName(driver);

        GenericObjectPool.Config config = new GenericObjectPool.Config();

        String s = (String)properties.remove(MAX_ACTIVE);
        if (s != null) config.maxActive = Integer.parseInt(s);

        s = (String)properties.remove(MAX_IDLE);
        if (s != null) config.maxIdle = Integer.parseInt(s);

        s = (String)properties.remove(MAX_WAIT);
        if (s != null) config.maxWait = Integer.parseInt(s);

        s = (String)properties.remove(MIN_EVICTABLE_IDLE_TIME_MILLIS);
        if (s != null) config.minEvictableIdleTimeMillis = Integer.parseInt(s);

        s = (String)properties.remove(MIN_IDLE);
        if (s != null) config.minIdle = Integer.parseInt(s);

        s = (String)properties.remove(NUM_TESTS_PER_EVICTION_RUN);
        if (s != null) config.numTestsPerEvictionRun = Integer.parseInt(s);

        s = (String)properties.remove(TEST_ON_BORROW);
        if (s != null) config.testOnBorrow = new Boolean(s).booleanValue();

        s = (String)properties.remove(TEST_ON_RETURN);
        if (s != null) config.testOnReturn = new Boolean(s).booleanValue();

        s = (String)properties.remove(TEST_WHILE_IDLE);
        if (s != null) config.testWhileIdle = new Boolean(s).booleanValue();

        s = (String)properties.remove(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        if (s != null) config.timeBetweenEvictionRunsMillis = Integer.parseInt(s);

        //s = (String)properties.remove(SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
        //if (s != null) config.softMinEvictableIdleTimeMillis = Integer.parseInt(s);

        //s = (String)properties.remove(WHEN_EXHAUSTED_ACTION);
        //if (s != null) config.whenExhaustedAction = Byte.parseByte(s);

        connectionPool = new GenericObjectPool(null, config);

        String validationQuery = (String)properties.remove(VALIDATION_QUERY);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, properties);

        //PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(
                        connectionFactory,
                        connectionPool,
                        null, // statement pool factory
                        validationQuery, // test query
                        false, // read only
                        true // auto commit
                );

        ds = new PoolingDataSource(connectionPool);

        filterTool = new JDBCFilterTool();
    }

    public void dispose() throws Exception {
        connectionPool.close();
    }

    public Object openConnection() throws Exception {
        return ds.getConnection();
    }

    public String getFieldNames(SourceConfig sourceConfig) throws Exception {
        StringBuffer sb = new StringBuffer();

        Collection fields = sourceConfig.getFieldConfigs();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            if (sb.length() > 0) sb.append(", ");
            sb.append(fieldConfig.getOriginalName());
        }

        return sb.toString();
    }

    public String getOringialPrimaryKeyFieldNamesAsString(SourceConfig sourceConfig) throws Exception {
        StringBuffer sb = new StringBuffer();

        Collection fields = sourceConfig.getOriginalPrimaryKeyNames();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            String name = (String)i.next();

            if (sb.length() > 0) sb.append(", ");
            sb.append(name);
        }

        return sb.toString();
    }

    public PenroseSearchResults search(SourceConfig sourceConfig, Filter filter, long sizeLimit) throws Exception {

        log.debug("Searching JDBC source "+sourceConfig.getConnectionName()+"/"+sourceConfig.getName());

        PenroseSearchResults results = new PenroseSearchResults();

        String tableName = sourceConfig.getParameter(TABLE_NAME);
        String s = sourceConfig.getParameter(FILTER);

        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        sb.append(getOringialPrimaryKeyFieldNamesAsString(sourceConfig));
        sb.append(" from ");
        sb.append(tableName);

        StringBuffer sqlFilter = new StringBuffer();
        if (s != null) sqlFilter.append(s);

        List parameters = new ArrayList();
        if (filter != null) {
            if (sqlFilter.length() > 0) sqlFilter.append(" and ");
            sqlFilter.append(filterTool.convert(sourceConfig, filter, parameters));
        }

        if (sqlFilter.length() > 0) {
            sb.append(" where ");
            sb.append(sqlFilter);
        }

        sb.append(" order by ");
        sb.append(getOringialPrimaryKeyFieldNamesAsString(sourceConfig));

        String sql = sb.toString();

        java.sql.Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (java.sql.Connection)openConnection();

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters:", 80));

            int counter = 0;
            for (Iterator i=parameters.iterator(); i.hasNext(); ) {
                Object param = i.next();
                ps.setObject(++counter, param);
                log.debug(Formatter.displayLine(" - "+counter+" = "+param, 80));
            }

            log.debug(Formatter.displaySeparator(80));

            rs = ps.executeQuery();

            int width = 0;
            boolean first = true;

            for (int i=0; rs.next() && (sizeLimit == 0 || i<sizeLimit); i++) {
                Row row = getPkValues(sourceConfig, rs);
                results.add(row);

                if (first) {
                    width = printHeader(sourceConfig);
                    first = false;
                }

                printValues(sourceConfig, row);
            }

            if (width > 0) printFooter(width);

            if (rs.next()) {
                log.debug("RC: size limit exceeded.");
                results.setReturnCode(LDAPException.SIZE_LIMIT_EXCEEDED);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        results.close();

        return results;
    }

    public PenroseSearchResults load(SourceConfig sourceConfig, Filter filter, long sizeLimit) throws Exception {

        log.debug("Loading JDBC source "+sourceConfig.getConnectionName()+"/"+sourceConfig.getName());

        PenroseSearchResults results = new PenroseSearchResults();

        String tableName = sourceConfig.getParameter(TABLE_NAME);
        String s = sourceConfig.getParameter(FILTER);

        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        sb.append(getFieldNames(sourceConfig));
        sb.append(" from ");
        sb.append(tableName);

        StringBuffer sqlFilter = new StringBuffer();
        if (s != null) sqlFilter.append(s);

        List parameters = new ArrayList();
        if (filter != null) {
            if (sqlFilter.length() > 0) sqlFilter.append(" and ");
            sqlFilter.append(filterTool.convert(sourceConfig, filter, parameters));
        }

        if (sqlFilter.length() > 0) {
            sb.append(" where ");
            sb.append(sqlFilter);
        }

        sb.append(" order by ");
        sb.append(getOringialPrimaryKeyFieldNamesAsString(sourceConfig));

        String sql = sb.toString();

        java.sql.Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (java.sql.Connection)openConnection();

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            if (parameters.size() > 0) {
                log.debug(Formatter.displayLine("Parameters:", 80));

                int counter = 0;
                for (Iterator i=parameters.iterator(); i.hasNext(); ) {
                    Object param = i.next();
                    ps.setObject(++counter, param);
                    log.debug(Formatter.displayLine(" - "+counter+" = "+param, 80));
                }

                log.debug(Formatter.displaySeparator(80));
            }

            rs = ps.executeQuery();

            int width = 0;
            boolean first = true;

            for (int i=0; rs.next() && (sizeLimit == 0 || i<sizeLimit); i++) {
                AttributeValues av = getValues(sourceConfig, rs);
                results.add(av);

                if (first) {
                    width = printHeader(sourceConfig);
                    first = false;
                }

                printValues(sourceConfig, av);
            }

            if (width > 0) printFooter(width);

            if (rs.next()) {
                log.debug("RC: size limit exceeded.");
                results.setReturnCode(LDAPException.SIZE_LIMIT_EXCEEDED);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        results.close();

        return results;
    }

    public AttributeValues get(SourceConfig sourceConfig, Row pk) throws Exception {

        //log.debug("Getting entry "+pk);

        String tableName = sourceConfig.getParameter(TABLE_NAME);
        String s = sourceConfig.getParameter(FILTER);

        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        sb.append(getFieldNames(sourceConfig));
        sb.append(" from ");
        sb.append(tableName);

        StringBuffer sqlFilter = new StringBuffer();
        if (s != null) sqlFilter.append(s);

        List parameters = new ArrayList();
        for (Iterator i=pk.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Object value = pk.get(name);

            if (sqlFilter.length() > 0) sqlFilter.append(" and ");

            sqlFilter.append(name);
            sqlFilter.append("=?");
            parameters.add(value);
        }

        if (sqlFilter.length() > 0) {
            sb.append(" where ");
            sb.append(sqlFilter);
        }

        sb.append(" order by ");
        sb.append(getOringialPrimaryKeyFieldNamesAsString(sourceConfig));

        String sql = sb.toString();

        java.sql.Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (java.sql.Connection)openConnection();

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            if (parameters.size() > 0) {
                log.debug(Formatter.displayLine("Parameters:", 80));

                int counter = 0;
                for (Iterator i=parameters.iterator(); i.hasNext(); ) {
                    Object param = i.next();
                    ps.setObject(++counter, param);
                    log.debug(Formatter.displayLine(" - "+counter+" = "+param, 80));
                }

                log.debug(Formatter.displaySeparator(80));
            }

            rs = ps.executeQuery();

            if (!rs.next()) return null;

            AttributeValues av = getValues(sourceConfig, rs);

            int width = printHeader(sourceConfig);
            printValues(sourceConfig, av);
            printFooter(width);

            return av;

        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }
    }

    public Row getPkValues(SourceConfig sourceConfig, ResultSet rs) throws Exception {

        Row row = new Row();
        int c = 1;

        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();

        Collection fields = sourceConfig.getPrimaryKeyFieldConfigs();

        for (Iterator i=fields.iterator(); i.hasNext() && c<=count; c++) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            Object value = rs.getObject(c);
            if (value == null) continue;

            row.set(fieldConfig.getName(), value);
        }

        //log.debug("=> values: "+row);

        return row;
    }

    public Row getChanges(SourceConfig sourceConfig, ResultSet rs) throws Exception {

        Row row = new Row();

        row.set("changeNumber", rs.getObject("changeNumber"));
        row.set("changeTime", rs.getObject("changeTime"));
        row.set("changeAction", rs.getObject("changeAction"));
        row.set("changeUser", rs.getObject("changeUser"));

        for (Iterator i=sourceConfig.getPrimaryKeyNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();

            Object value = rs.getObject(name);
            if (value == null) continue;

            row.set(name, value);
        }

        return row;
    }

    public AttributeValues getValues(SourceConfig sourceConfig, ResultSet rs) throws Exception {

        AttributeValues row = new AttributeValues();

        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();

        int c = 1;

        Collection fields = sourceConfig.getFieldConfigs();

        for (Iterator i=fields.iterator(); i.hasNext() && c<=count; c++) {
            FieldConfig fieldConfig = (FieldConfig)i.next();
            
            Object value = rs.getObject(c);
            if (value == null) continue;

            row.add(fieldConfig.getName(), value);
        }

        //log.debug("=> values: "+row);

        return row;
    }

    public int bind(SourceConfig sourceConfig, AttributeValues values, String cred) throws Exception {
        return LDAPException.INVALID_CREDENTIALS;
    }

    public int add(SourceConfig sourceConfig, AttributeValues sourceValues) throws Exception {

        // convert sets into single values
        Collection rows = TransformEngine.convert(sourceValues);
    	Row row = (Row)rows.iterator().next();

        String tableName = sourceConfig.getParameter(TABLE_NAME);

        java.sql.Connection con = null;
        PreparedStatement ps = null;

        try {
            con = (java.sql.Connection)openConnection();

            StringBuffer sb = new StringBuffer();
            StringBuffer sb2 = new StringBuffer();

            Collection fields = sourceConfig.getFieldConfigs();
            Collection parameters = new ArrayList();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();

                if (sb.length() > 0) {
                    sb.append(", ");
                    sb2.append(", ");
                }

                sb.append(fieldConfig.getOriginalName());
                sb2.append("?");

                Object obj = row.get(fieldConfig.getName());
                parameters.add(obj);
            }

            String sql = "insert into "+tableName+" ("+sb+") values ("+sb2+")";

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                log.debug(Formatter.displayLine(sql, 80));
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters:", 80));

            int c = 1;
            for (Iterator i=parameters.iterator(); i.hasNext(); c++) {
                Object obj = i.next();
                ps.setObject(c, obj);
                log.debug(Formatter.displayLine(" - "+c+" = "+(obj == null ? null : obj.toString()), 80));
            }

            log.debug(Formatter.displaySeparator(80));

            ps.executeUpdate();

        } catch (Exception e) {
            log.debug("Add failed: ("+e.getClass().getName()+") "+e.getMessage());
            return LDAPException.OPERATIONS_ERROR;

        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        return LDAPException.SUCCESS;
    }

    public int delete(SourceConfig sourceConfig, AttributeValues sourceValues) throws Exception {

        Row pk = sourceConfig.getPrimaryKeyValues(sourceValues);
        //log.debug("Deleting entry "+pk);

        String tableName = sourceConfig.getParameter(TABLE_NAME);

        java.sql.Connection con = null;
        PreparedStatement ps = null;

        try {
            con = (java.sql.Connection)openConnection();

            StringBuffer sb = new StringBuffer();
            for (Iterator i=pk.getNames().iterator(); i.hasNext(); ) {
                String name = (String)i.next();

                if (sb.length() > 0) sb.append(" and ");

                sb.append(name);
                sb.append("=?");
            }

            String sql = "delete from "+tableName+" where "+sb;

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                log.debug(Formatter.displayLine(sql, 80));
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters:", 80));

            int c = 1;
            for (Iterator i=pk.getNames().iterator(); i.hasNext(); c++) {
                String name = (String)i.next();
                Object value = pk.get(name);
                ps.setObject(c, value);
                log.debug(Formatter.displayLine(" - "+c+" = "+value, 80));
            }

            log.debug(Formatter.displaySeparator(80));

            int count = ps.executeUpdate();
            if (count == 0) return LDAPException.NO_SUCH_OBJECT;

        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        return LDAPException.SUCCESS;
    }

    public int modify(SourceConfig sourceConfig, AttributeValues oldEntry, AttributeValues newEntry) throws Exception {

        // convert sets into single values
        Collection oldRows = TransformEngine.convert(oldEntry);
        Collection newRows = TransformEngine.convert(newEntry);

        Row oldRow = (Row)oldRows.iterator().next();
        Row newRow = (Row)newRows.iterator().next();

        //log.debug("Modifying source "+source.getName()+": "+oldRow+" with "+newRow);

        String tableName = sourceConfig.getParameter(TABLE_NAME);

        java.sql.Connection con = null;
        PreparedStatement ps = null;

        try {
            con = (java.sql.Connection)openConnection();

            StringBuffer columns = new StringBuffer();
            StringBuffer whereClause = new StringBuffer();
            Collection parameters = new ArrayList();

            Collection fields = sourceConfig.getFieldConfigs();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();

                if (columns.length() > 0) columns.append(", ");

                columns.append(fieldConfig.getOriginalName());
                columns.append("=?");

                Object value = newRow.get(fieldConfig.getName());
                parameters.add(value);
            }

            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();
                if (!fieldConfig.isPrimaryKey()) continue;

                if (whereClause.length() > 0) whereClause.append(" and ");

                whereClause.append(fieldConfig.getOriginalName());
                whereClause.append("=?");

                Object value = oldRow.get(fieldConfig.getName());
                parameters.add(value);
            }

            String sql = "update "+tableName+" set "+columns+" where "+whereClause;

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters:", 80));

            int c = 1;
            for (Iterator i=parameters.iterator(); i.hasNext(); c++) {
                Object value = i.next();
                ps.setObject(c, value);
                log.debug(Formatter.displayLine(" - "+c+" = "+value, 80));
            }

            log.debug(Formatter.displaySeparator(80));

            int count = ps.executeUpdate();
            if (count == 0) return LDAPException.NO_SUCH_OBJECT;

        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }
        
        return LDAPException.SUCCESS;
    }

    public int modrdn(
            SourceConfig sourceConfig,
            Row oldRdn,
            Row newRdn)
            throws Exception {

        //log.debug("Renaming source "+source.getName()+": "+oldRdn+" with "+newRdn);

        String tableName = sourceConfig.getParameter(TABLE_NAME);

        java.sql.Connection con = null;
        PreparedStatement ps = null;

        try {
            con = (java.sql.Connection)openConnection();

            StringBuffer columns = new StringBuffer();
            StringBuffer whereClause = new StringBuffer();
            Collection parameters = new ArrayList();

            Collection fields = sourceConfig.getFieldConfigs();
            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();
                if (!fieldConfig.isPrimaryKey()) continue;

                if (columns.length() > 0) columns.append(", ");

                columns.append(fieldConfig.getOriginalName());
                columns.append("=?");

                Object value = newRdn.get(fieldConfig.getName());
                parameters.add(value);
            }

            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig fieldConfig = (FieldConfig)i.next();
                if (!fieldConfig.isPrimaryKey()) continue;

                if (whereClause.length() > 0) whereClause.append(" and ");

                whereClause.append(fieldConfig.getOriginalName());
                whereClause.append("=?");

                Object value = oldRdn.get(fieldConfig.getName());
                parameters.add(value);
            }

            String sql = "update "+tableName+" set "+columns+" where "+whereClause;

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters:", 80));

            int c = 1;
            for (Iterator i=parameters.iterator(); i.hasNext(); c++) {
                Object value = i.next();
                ps.setObject(c, value);
                log.debug(Formatter.displayLine(" - "+c+" = "+value, 80));
            }

            log.debug(Formatter.displaySeparator(80));

            int count = ps.executeUpdate();
            if (count == 0) return LDAPException.NO_SUCH_OBJECT;

        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        return LDAPException.SUCCESS;
    }

    public int getLastChangeNumber(SourceConfig sourceConfig) throws Exception {
        String tableName = sourceConfig.getParameter(TABLE_NAME);
        String sql = "select max(changeNumber) from "+tableName+"_changes";

        java.sql.Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (java.sql.Connection)openConnection();

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                log.debug(Formatter.displayLine(sql, 80));
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            if (!rs.next()) return 0;

            Integer value = (Integer)rs.getObject(1);
            log.debug("Last change number: "+value);
            
            if (value == null) return 0;

            return value.intValue();

        } catch (Exception e) {
            return 0;

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }
    }

    public PenroseSearchResults getChanges(SourceConfig sourceConfig, int lastChangeNumber) throws Exception {

        //log.debug("Searching JDBC source "+sourceConfig.getConnectionName()+"/"+sourceConfig.getName());

        PenroseSearchResults results = new PenroseSearchResults();

        String tableName = sourceConfig.getParameter(TABLE_NAME);
        int sizeLimit = 100;

        StringBuffer columns = new StringBuffer();
        columns.append("select changeNumber, changeTime, changeAction, changeUser");

        StringBuffer table = new StringBuffer();
        table.append("from ");
        table.append(tableName);
        table.append("_changes");

        for (Iterator i=sourceConfig.getPrimaryKeyFieldConfigs().iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            columns.append(", ");
            columns.append(fieldConfig.getOriginalName());
            columns.append(" ");
            columns.append(fieldConfig.getName());
        }

        StringBuffer whereClause = new StringBuffer();
        whereClause.append("where changeNumber > ? order by changeNumber");

        List parameters = new ArrayList();
        parameters.add(new Integer(lastChangeNumber));

        String sql = columns+" "+table+" "+whereClause;

        java.sql.Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = (java.sql.Connection)openConnection();

            if (log.isDebugEnabled()) {
                log.debug(Formatter.displaySeparator(80));
                Collection lines = Formatter.split(sql, 80);
                for (Iterator i=lines.iterator(); i.hasNext(); ) {
                    String line = (String)i.next();
                    log.debug(Formatter.displayLine(line, 80));
                }
                log.debug(Formatter.displaySeparator(80));
            }

            ps = con.prepareStatement(sql);

            log.debug(Formatter.displayLine("Parameters: changeNumber = "+lastChangeNumber, 80));

            int counter = 0;
            for (Iterator i=parameters.iterator(); i.hasNext(); ) {
                Object param = i.next();
                ps.setObject(++counter, param);
                //log.debug(Formatter.displayLine(" - "+counter+" = "+param, 80));
            }

            log.debug(Formatter.displaySeparator(80));

            rs = ps.executeQuery();

            int width = 0;
            boolean first = true;

            for (int i=0; rs.next() && (sizeLimit == 0 || i<sizeLimit); i++) {
                Row row = getChanges(sourceConfig, rs);
                results.add(row);

                if (first) {
                    width = printChangesHeader(sourceConfig);
                    first = false;
                }

                printChanges(sourceConfig, row);
            }

            if (width > 0) printFooter(width);

            if (rs.next()) {
                log.debug("RC: size limit exceeded.");
                results.setReturnCode(LDAPException.SIZE_LIMIT_EXCEEDED);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception e) {}
        }

        results.close();

        return results;
    }

    public int printHeader(SourceConfig sourceConfig) throws Exception {

        StringBuffer resultHeader = new StringBuffer();
        resultHeader.append("|");

        Collection fields = sourceConfig.getFieldConfigs();
        for (Iterator j=fields.iterator(); j.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)j.next();

            String name = fieldConfig.getName();
            int length = fieldConfig.getLength() > 15 ? 15 : fieldConfig.getLength();

            resultHeader.append(" ");
            resultHeader.append(Formatter.rightPad(name, length));
            resultHeader.append(" |");
        }

        int width = resultHeader.length();

        log.debug("Results:");
        log.debug(Formatter.displaySeparator(width));
        log.debug(resultHeader.toString());
        log.debug(Formatter.displaySeparator(width));

        return width;
    }

    public int printChangesHeader(SourceConfig sourceConfig) throws Exception {

        StringBuffer resultHeader = new StringBuffer();
        resultHeader.append("| ");
        resultHeader.append(Formatter.rightPad("#", 5));
        resultHeader.append(" | ");
        resultHeader.append(Formatter.rightPad("time", 19));
        resultHeader.append(" | ");
        resultHeader.append(Formatter.rightPad("action", 10));
        resultHeader.append(" | ");
        resultHeader.append(Formatter.rightPad("user", 10));
        resultHeader.append(" |");

        Collection fields = sourceConfig.getPrimaryKeyFieldConfigs();
        for (Iterator j=fields.iterator(); j.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)j.next();

            String name = fieldConfig.getName();
            int length = fieldConfig.getLength() > 15 ? 15 : fieldConfig.getLength();

            resultHeader.append(" ");
            resultHeader.append(Formatter.rightPad(name, length));
            resultHeader.append(" |");
        }

        int width = resultHeader.length();

        log.debug("Results:");
        log.debug(Formatter.displaySeparator(width));
        log.debug(resultHeader.toString());
        log.debug(Formatter.displaySeparator(width));

        return width;
    }

    public void printValues(SourceConfig sourceConfig, AttributeValues av) throws Exception {

        Row row = new Row();

        for (Iterator i=av.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Collection c = av.get(name);

            Object value;
            if (c == null) {
                value = null;
            } else if (c.size() == 1) {
                value = c.iterator().next().toString();
            } else {
                value = c.toString();
            }

            row.set(name, value);
        }

        printValues(sourceConfig, row);
    }

    public void printValues(SourceConfig sourceConfig, Row row) throws Exception {
        StringBuffer resultFields = new StringBuffer();
        resultFields.append("|");

        Collection fields = sourceConfig.getFieldConfigs();
        for (Iterator j=fields.iterator(); j.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)j.next();

            Object value = row.get(fieldConfig.getName());
            int length = fieldConfig.getLength() > 15 ? 15 : fieldConfig.getLength();

            resultFields.append(" ");
            resultFields.append(Formatter.rightPad(value == null ? "null" : value.toString(), length));
            resultFields.append(" |");
        }

        log.debug(resultFields.toString());
    }

    public void printChanges(SourceConfig sourceConfig, Row row) throws Exception {
        StringBuffer resultFields = new StringBuffer();
        resultFields.append("| ");
        resultFields.append(Formatter.rightPad(row.get("changeNumber").toString(), 5));
        resultFields.append(" | ");
        resultFields.append(Formatter.rightPad(row.get("changeTime").toString(), 19));
        resultFields.append(" | ");
        resultFields.append(Formatter.rightPad(row.get("changeAction").toString(), 10));
        resultFields.append(" | ");
        resultFields.append(Formatter.rightPad(row.get("changeUser").toString(), 10));
        resultFields.append(" |");

        Collection fields = sourceConfig.getPrimaryKeyFieldConfigs();
        for (Iterator j=fields.iterator(); j.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)j.next();

            Object value = row.get(fieldConfig.getName());
            int length = fieldConfig.getLength() > 15 ? 15 : fieldConfig.getLength();

            resultFields.append(" ");
            resultFields.append(Formatter.rightPad(value == null ? "null" : value.toString(), length));
            resultFields.append(" |");
        }

        log.debug(resultFields.toString());
    }

    public void printFooter(int width) throws Exception {
        log.debug(Formatter.displaySeparator(width));
    }

    public Filter convert(EntryMapping entryMapping, SubstringFilter filter) throws Exception {

        String attributeName = filter.getAttribute();
        Collection substrings = filter.getSubstrings();

        AttributeMapping attributeMapping = entryMapping.getAttributeMapping(attributeName);
        String variable = attributeMapping.getVariable();

        if (variable == null) return null;

        int index = variable.indexOf(".");
        String sourceName = variable.substring(0, index);
        String fieldName = variable.substring(index+1);

        StringBuffer sb = new StringBuffer();
        for (Iterator i=substrings.iterator(); i.hasNext(); ) {
            String substring = (String)i.next();
            if ("*".equals(substring)) {
                sb.append("%");
            } else {
                sb.append(substring);
            }
        }

        return new SimpleFilter(fieldName, "like", sb.toString());
    }
}