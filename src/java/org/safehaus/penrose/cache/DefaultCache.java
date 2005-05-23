/**
 * Copyright (c) 1998-2005, Verge Lab., LLC.
 * All rights reserved.
 */
package org.safehaus.penrose.cache;

import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.event.CacheEvent;
import org.safehaus.penrose.event.CacheListener;
import org.safehaus.penrose.SearchResults;
import org.safehaus.penrose.mapping.Row;
import org.safehaus.penrose.mapping.SourceDefinition;
import org.safehaus.penrose.mapping.Relationship;
import org.safehaus.penrose.mapping.AttributeDefinition;
import org.safehaus.penrose.mapping.EntryDefinition;
import org.safehaus.penrose.mapping.Source;
import org.safehaus.penrose.mapping.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;

import javax.sql.DataSource;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Connection;

/**
 * @author Endi S. Dewata
 */
public class DefaultCache extends Cache {

    public PenroseSourceHome sourceExpirationHome;
    public Map sourceTables = new HashMap();

    public PenroseResultHome resultExpirationHome;
    public Map resultTables = new HashMap();

    private DataSource ds;

    public void init() throws Exception {

        String driver    = getParameter(CacheConfig.DRIVER);
        String url       = getParameter(CacheConfig.URL);
        String username  = getParameter(CacheConfig.USER);
        String password  = getParameter(CacheConfig.PASSWORD);

        log.debug("Driver    : " + driver);
        log.debug("Url       : " + url);
        log.debug("Username  : " + username);
        log.debug("Password  : " + password);

        Properties properties = new Properties();
        properties.put("driver", driver);
        properties.put("url", url);
        properties.put("user", username);
        properties.put("password", password);

        Class.forName(driver);

        GenericObjectPool connectionPool = new GenericObjectPool(null);
        //connectionPool.setMaxActive(0);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                url, properties);

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, connectionPool, null, // statement pool
                // factory
                null, // test query
                false, // read only
                true // auto commit
        );

        ds = new PoolingDataSource(connectionPool);

        initSources();
    }

    /**
     * Load all sources for all entries during initialization.
     *
     * @throws Exception
     */
    public void initSources() throws Exception {

        Date date = new Date();

        Collection entries = config.getEntryDefinitions();
        Set set = new HashSet();

        sourceExpirationHome = new PenroseSourceHome(ds);
        resultExpirationHome = new PenroseResultHome(ds);

        for (Iterator i=entries.iterator(); i.hasNext(); ) {
            EntryDefinition entry = (EntryDefinition)i.next();

            resultExpirationHome.insert(entry);

            String t1 = getTableName(entry, false);
            ResultHome r1 = new ResultHome(ds, entry, t1);
            resultTables.put(t1, r1);

            String t2 = getTableName(entry, true);
            ResultHome r2 = new ResultHome(ds, entry, t2);
            resultTables.put(t2, r2);

            Collection sources = entry.getSources();

            for (Iterator j=sources.iterator(); j.hasNext(); ) {
                Source source = (Source)j.next();

                // if source has been initialized => skip
                if (set.contains(source.getName())) continue;
                set.add(source.getName());

                // create source cache tables
                sourceExpirationHome.insert(source);

                t1 = getTableName(source, false);
                SourceHome s1 = new SourceHome(ds, source, t1);
                sourceTables.put(t1, s1);

                t2 = getTableName(source, true);
                SourceHome s2 = new SourceHome(ds, source, t2);
                sourceTables.put(t2, s2);

                // check global loading parameter
                String s = getParameter(CacheConfig.LOAD_ON_STARTUP);
                boolean globalLoadOnStartup = s == null ? false : new Boolean(s).booleanValue();
                log.debug("Global load on startup: "+globalLoadOnStartup);

                // check source loading parameter
                s = source.getParameter(SourceDefinition.LOAD_ON_STARTUP);
                log.debug(source.getName()+"'s load on startup: "+s);

                // if no need to load => skip
                boolean loadOnStartup = s == null ? globalLoadOnStartup : new Boolean(s).booleanValue();
                if (!loadOnStartup) continue;

                // load the source
                loadSource(entry, source, null, date);

                // compute source cache expiration
                s = source.getParameter(SourceDefinition.CACHE_EXPIRATION);
                int cacheExpiration = s == null ? 0 : Integer.parseInt(s);
                if (cacheExpiration < 0) cacheExpiration = Integer.MAX_VALUE;

                Calendar c = Calendar.getInstance();
                c.add(Calendar.MINUTE, cacheExpiration);

                setExpiration(source, cacheExpiration == 0 ? null : c.getTime());

            }
        }
    }

    /**
     * Reload sources for all entries that has expired.
     *
     * @throws Exception
     */
    public void refresh() throws Exception {

        Date date = new Date();

        Collection entries = config.getEntryDefinitions();
        Set set = new HashSet();

        for (Iterator i=entries.iterator(); i.hasNext(); ) {
            EntryDefinition entry = (EntryDefinition)i.next();

            Collection sources = entry.getSources();

            for (Iterator j=sources.iterator(); j.hasNext(); ) {
                Source source = (Source)j.next();

                if (set.contains(source.getName())) continue;
                set.add(source.getName());

                // Read the source's "cacheExpiration" parameter
                String s = source.getParameter(SourceDefinition.CACHE_EXPIRATION);
                int cacheExpiration = s == null ? 0 : Integer.parseInt(s);
                if (cacheExpiration < 0) cacheExpiration = Integer.MAX_VALUE;

                // Read the source's "loadUponExpiration" parameter
                s = source.getParameter(SourceDefinition.LOAD_UPON_EXPIRATION);
                Boolean loadUponExpiration = s == null ? Boolean.FALSE : new Boolean(s);

                // If the table hasn't been loaded yet and loadUponExpiration == true
                // and expired (current time is past expiration or expiration time == null)
                if (!loadUponExpiration.booleanValue()) continue;

                Date expiration = getExpiration(source);

                // if not yet expired, skip
                if (expiration != null && !expiration.before(date)) continue;

                // reload source
                loadSource(entry, source, null, date);

                Calendar c = Calendar.getInstance();
                c.add(Calendar.MINUTE, cacheExpiration);

                setExpiration(source, cacheExpiration == 0 ? null : c.getTime());
            }
        }
    }

    public Date getExpiration(Source source) throws Exception {
        return sourceExpirationHome.getExpiration(source);
    }

    public void setExpiration(Source source, Date date) throws Exception {
        sourceExpirationHome.setExpiration(source, date);
    }

    public void insert(Source source, Row row, Date date) throws Exception {
        String tableName = getTableName(source, false);
        SourceHome sourceHome = (SourceHome)sourceTables.get(tableName);
        sourceHome.insert(row, date);
    }

    public void update(Source source, Row oldRow, Row newRow, Date date) throws Exception {
        String tableName = getTableName(source, false);
        SourceHome sourceHome = (SourceHome)sourceTables.get(tableName);
        sourceHome.update(oldRow, newRow, date);
    }

    public void delete(Source source, Row row, Date date) throws Exception {
        String tableName = getTableName(source, false);
        SourceHome sourceHome = (SourceHome)sourceTables.get(tableName);
        sourceHome.delete(row, date);
    }

    public Collection search(EntryDefinition entry, Collection primaryKeys) throws Exception {
        String t1 = getTableName(entry, false);
        ResultHome r1 = (ResultHome)resultTables.get(t1);
        return r1.search(primaryKeys);
    }

    public void insert(EntryDefinition entry, Row row, Date date) throws Exception {
        String tableName = getTableName(entry, false);
        ResultHome resultHome = (ResultHome)resultTables.get(tableName);
        resultHome.insert(row, date);
    }

    public void delete(EntryDefinition entry, Row row, Date date) throws Exception {
        String tableName = getTableName(entry, false);
        ResultHome resultHome = (ResultHome)resultTables.get(tableName);
        resultHome.delete(row, date);
    }

    public void delete(EntryDefinition entry, String filter, Date date) throws Exception {
        String tableName = getTableName(entry, false);
        ResultHome resultHome = (ResultHome)resultTables.get(tableName);
        resultHome.delete(filter, date);
    }

    public Date getModifyTime(Source source, String filter) throws Exception {
        String t1 = getTableName(source, true);
        SourceHome s1 = (SourceHome)sourceTables.get(t1);
        return s1.getModifyTime(filter);
    }

    public Date getModifyTime(Source source) throws Exception {
        return sourceExpirationHome.getModifyTime(source);
    }

    public Date getModifyTime(EntryDefinition entry, String filter) throws Exception {
        String t1 = getTableName(entry, false);
        ResultHome r1 = (ResultHome)resultTables.get(t1);
        return r1.getModifyTime(filter);
    }

    public Date getModifyTime(EntryDefinition entry) throws Exception {
        return resultExpirationHome.getModifyTime(entry);
    }

    public void setModifyTime(EntryDefinition entry, Date date) throws Exception {
        resultExpirationHome.setModifyTime(entry, date);
    }

    /**
     * Get the field names
     *
     * @param sources
     *            the collection of sources (from config)
     * @return the string of field/column names separated by comma
     */
    public String getFieldNames(Collection sources) {
        List list = new ArrayList();
        for (Iterator i = sources.iterator(); i.hasNext();) {
            Source source = (Source) i.next();

            Collection fields = source.getFields();
            Set set = new HashSet();
            for (Iterator j = fields.iterator(); j.hasNext();) {
                Field field = (Field)j.next();
                String name = field.getName();

                if (set.contains(name)) continue;
                set.add(name);

                list.add(source.getName() + "." + name);
            }
        }

        StringBuffer sb = new StringBuffer();
        for (Iterator i = list.iterator(); i.hasNext();) {
            String name = (String) i.next();
            sb.append(name);
            if (i.hasNext())
                sb.append(", ");
        }

        return sb.toString();
    }

    /**
     * Get the table names (used in SELECT ... FROM ... clause)
     *
     * @param entry
     *            the entry (from config)
     * @param temporary
     *            whether we are using temporary tables
     * @return the string "left join table1 on ... left join table2 on ... etc."
     */
    public String getTableNames(EntryDefinition entry, boolean temporary) {
        Collection relationships = entry.getRelationships();
        Iterator iterator = relationships.iterator();

        StringBuffer sb = new StringBuffer();
        for (Iterator i = entry.getSources().iterator(); i.hasNext();) {
            Source source = (Source) i.next();
            String tableName = source.getName();

            boolean first = sb.length() == 0;

            if (!first) {
                sb.append(" left join ");
            }

            String sourceName = source.getName();

            sb.append(tableName);
            //sb.append(" ");
            //sb.append(sourceName);

            if (!first) {
                Relationship relationship = (Relationship) iterator.next();
                String expression = relationship.getExpression();

                sb.append(" on ");
                sb.append(expression);
            }
        }

        return sb.toString();
    }

    /**
     * Get the row value
     *
     * @param entry
     *            the entry (from config)
     * @param rs
     *            the result set
     * @return the Map containing the fields/columns and its values
     * @throws Exception
     */
    public Row getRow(EntryDefinition entry, ResultSet rs) throws Exception {
        Collection sources = entry.getSources();
        Iterator iterator = entry.getRelationships().iterator();

        Row values = new Row();

        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();

        int c = 1;
        boolean first = true;
        for (Iterator i = sources.iterator(); i.hasNext() && c <= count;) {
            Source source = (Source) i.next();

            Relationship relationship = null;
            String lhs = null;
            String rhs = null;
            if (!first) {
                relationship = (Relationship) iterator.next();
                String expression = relationship.getExpression();
                int p = expression.indexOf("=");
                lhs = expression.substring(0, p).trim();
                rhs = expression.substring(p + 1).trim();
            }

            Row map = new Row();

            Collection fields = source.getFields();
            Set set = new HashSet();
            boolean valid = true;
            for (Iterator j = fields.iterator(); j.hasNext() && c <= count;) {
                Field field = (Field) j.next();
                String name = field.getName();

                if (set.contains(name)) continue;
                set.add(name);

                Object value = rs.getObject(c);
                String label = source.getName() + "." + name;

                if (!first) {
                    // if the keys used in join relationship is null then the
                    // values are not valid
                    if ((label.equals(lhs) || label.equals(rhs)) && value == null) valid = false;
                }

                if (value != null) map.set(source.getName() + "." + name, value);

                c++;
            }

            if (valid) values.add(map);

            first = false;
        }

        return values;
    }

    /**
     * Join sources
     *
     * @param entry the entry (from config)
     * @return the Collection of rows resulting from the join
     * @throws Exception
     */
    public Collection joinSources(EntryDefinition entry) throws Exception {

        String sqlFieldNames = getFieldNames(entry.getSources());

        // don't use temporary entry tables here
        String sqlTableNames = getTableNames(entry, false);

        String sql = "select " + sqlFieldNames + " from " + sqlTableNames;

        List results = new ArrayList();

        log.debug("Executing " + sql);

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Row row = getRow(entry, rs);
                results.add(row);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception ex) {}
        }

        return results;
    }

    /**
     * Join sources
     *
     * @param con
     *            the JDBC connection
     * @param entry
     *            the entry (from config)
     * @param temporary
     *            whether we are using temporary tables
     * @param sourceConfig
     *            the source
     * @return the Collection of rows resulting from the join
     * @throws Exception
     */
/*
    public Collection joinSourcesIncrementally(java.sql.Connection con,
            EntryDefinition entry, boolean temporary, SourceDefinition sourceConfig, Map incRow)
            throws Exception {

        String sqlFieldNames = getFieldNames(entry.getSources());
        // don't use temporary entry tables here
        String sqlTableNames = getTableNames(entry, false);

        String whereClause = "1=1";

        String sql = "select " + sqlFieldNames + " from " + sqlTableNames + " where " + whereClause;

        // Add pk clause
        Collection pkFields = sourceConfig.getPrimaryKeyFields();
        boolean started = false;
        for (Iterator pkIter = pkFields.iterator(); pkIter.hasNext();) {
            FieldDefinition pkField = (FieldDefinition) pkIter.next();
            String pk = pkField.getName();
            sql += (started ? " and " : " where ") + sourceConfig.getName() + "."
                    + pk + " = ?";
            started = true;
        }
        List results = new ArrayList();

        log.debug("Executing " + sql);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            Iterator pkIter = pkFields.iterator();
            for (int i = 1; pkIter.hasNext(); i++) {
                FieldDefinition pkField = (FieldDefinition) pkIter.next();
                String pk = pkField.getName();
                Object obj = incRow.get(pk);
                if (obj instanceof Collection && ((Collection) obj).size() == 1) {
                    Collection coll = (Collection) obj;
                    Iterator iter = coll.iterator();
                    Object obj1 = iter.next();
                    ps.setObject(i, obj1);
                    log.debug(" - " + i + " = " + obj1.toString() + " (class="
                            + obj1.getClass().getName() + ")");
                    log.debug("   " + i + " = " + obj.toString() + " (class="
                            + obj.getClass().getName() + ")");
                } else {
                    ps.setObject(i, obj);
                    log.debug(" - " + i + " = " + obj.toString() + " (class="
                            + obj.getClass().getName() + ")");
                }
            }
            rs = ps.executeQuery();

            while (rs.next()) {
                Map row = getRow(entry, rs);

                results.add(row);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
        }

        return results;
    }
*/
    /**
     * Get the primary key attribute names
     *
     * @param entry
     *            the entry (from config)
     * @return the string "pk1, pk2, ..."
     */
    public String getPkAttributeNames(EntryDefinition entry) {
        StringBuffer sb = new StringBuffer();

        Map attributes = entry.getAttributes();
        Set set = new HashSet();
        for (Iterator j = attributes.values().iterator(); j.hasNext();) {
            AttributeDefinition attribute = (AttributeDefinition) j.next();
            if (!attribute.isRdn()) continue;

            String name = attribute.getName();

            if (set.contains(name)) continue;
            set.add(name);

            if (sb.length() > 0) sb.append(", ");
            sb.append(name);
        }

        return sb.toString();
    }

    /**
     * @param entry
     * @param filter
     * @return
     * @throws Exception
     */
    public Collection searchPrimaryKeys(
            EntryDefinition entry,
            String filter)
            throws Exception {

        String tableName = getTableName(entry, false);

        String attributeNames = getPkAttributeNames(entry);

        String sql = "select distinct " + attributeNames + " from " + tableName;

        if (filter != null) {
            sql += " where " + filter;
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List pks = new ArrayList();

        log.debug("Executing " + sql);
        try {
            con = ds.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            //log.debug("Result:");

            while (rs.next()) {

                Map pk = getPk(entry, rs);
                //log.debug(" - "+row);

                pks.add(pk);
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
            if (con != null) try { con.close(); } catch (Exception ex) {}
        }

        return pks;
    }

    /**
     * Get the primary key from a given entry and result set
     *
     * @param entry
     *            the entry (from config)
     * @param rs
     *            the result set
     * @return a Map containing primary keys and its values
     * @throws Exception
     */
    public Map getPk(EntryDefinition entry, ResultSet rs) throws Exception {
        Map values = new HashMap();

        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();

        int c = 1;
        Collection attributes = entry.getRdnAttributes();

        Set set = new HashSet();
        for (Iterator j = attributes.iterator(); j.hasNext() && c <= count;) {
            AttributeDefinition attribute = (AttributeDefinition) j.next();
            String name = attribute.getName();

            if (set.contains(name)) continue;
            set.add(name);

            Object value = rs.getObject(c);

            values.put(name, value);

            c++;
        }

        return values;
    }

    /**
     * Get table name for a given dn (distinguished name) by replacing "=, ."
     * with _
     *
     * @param entry
     *            entry
     * @param temporary
     *            whether we are operating on temporary table
     * @return table name
     */
    public String getTableName(EntryDefinition entry, boolean temporary) {
        String dn = entry.getDn();
		dn = dn.replace('=', '_');
		dn = dn.replace(',', '_');
		dn = dn.replace(' ', '_');
		dn = dn.replace('.', '_');
		return temporary ? dn + "__tmp" : dn;
	}

    /**
     * Get the table name for a given source
     *
     * @param source the source
     * @param temporary whether we are operating on temporary table
     * @return table name
     */
    public String getTableName(Source source, boolean temporary) {
        return source.getName() + (temporary ? "__tmp" : "");
    }

    public void postCacheEvent(SourceDefinition sourceConfig, CacheEvent event)
            throws Exception {
        List listeners = sourceConfig.getListeners();

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            Object listener = i.next();
            if (!(listener instanceof CacheListener))
                continue;

            CacheListener cacheListener = (CacheListener) listener;
            switch (event.getType()) {
            case CacheEvent.BEFORE_LOAD_ENTRIES:
                cacheListener.beforeLoadEntries(event);
                break;

            case CacheEvent.AFTER_LOAD_ENTRIES:
                cacheListener.afterLoadEntries(event);
                break;
            }
        }
    }

    public SearchResults loadSource(
            EntryDefinition entry,
            Source source,
            Filter sqlFilter,
            Date date)
            throws Exception {

        log.info("-------------------------------------------------");
        log.info("LOAD SOURCE");
        log.info(" - source: " + source.getName());
        log.info(" - filter: " + sqlFilter);
        log.info("");

        SourceDefinition sourceConfig = source.getSourceDefinition();

        CacheEvent beforeEvent = new CacheEvent(cacheContext, sourceConfig, CacheEvent.BEFORE_LOAD_ENTRIES);
        postCacheEvent(sourceConfig, beforeEvent);

        SearchResults results = source.search(sqlFilter);

        String stringFilter = cacheFilterTool.toSQLFilter(entry, sqlFilter);

        String t1 = getTableName(source, true);
        SourceHome s1 = (SourceHome)sourceTables.get(t1);
        s1.delete(stringFilter, date);

        for (Iterator j = results.iterator(); j.hasNext();) {
            Row row = (Row) j.next();
            s1.insert(row, date);
        }

        String t2 = getTableName(source, false);
        SourceHome s2 = (SourceHome)sourceTables.get(t2);
        s2.delete(stringFilter, date);

        s2.copy(s1, stringFilter);

        CacheEvent afterEvent = new CacheEvent(cacheContext, sourceConfig, CacheEvent.AFTER_LOAD_ENTRIES);
        postCacheEvent(sourceConfig, afterEvent);

        return results;
    }

    public CacheFilterTool getCacheFilterTool() {
        return cacheFilterTool;
    }

    public void setCacheFilterTool(CacheFilterTool cacheFilterTool) {
        this.cacheFilterTool = cacheFilterTool;
    }


}
