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
package org.safehaus.penrose.config;

import java.util.*;

import org.apache.log4j.Logger;
import org.safehaus.penrose.cache.CacheConfig;
import org.safehaus.penrose.cache.EntryCache;
import org.safehaus.penrose.engine.EngineConfig;
import org.safehaus.penrose.interpreter.InterpreterConfig;
import org.safehaus.penrose.connector.ConnectorConfig;
import org.safehaus.penrose.connector.AdapterConfig;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.user.UserConfig;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.handler.SessionHandlerConfig;


/**
 * @author Endi S. Dewata
 */
public class PenroseConfig implements Cloneable {

    Logger log = Logger.getLogger(getClass());

    private String home;

    private Map systemProperties = new LinkedHashMap();
    private Map schemaConfigs    = new LinkedHashMap();
    private Map adapterConfigs   = new LinkedHashMap();
    private Map partitionConfigs = new LinkedHashMap();
    private Map serviceConfigs   = new LinkedHashMap();

    private InterpreterConfig interpreterConfig;

    private CacheConfig entryCacheConfig;
    private CacheConfig sourceCacheConfig;

    private SessionHandlerConfig sessionHandlerConfig;
    private EngineConfig engineConfig;
    private ConnectorConfig connectorConfig;

    private UserConfig rootUserConfig;

    public PenroseConfig() {

        interpreterConfig = new InterpreterConfig();

        sourceCacheConfig = new CacheConfig();
        sourceCacheConfig.setName(ConnectorConfig.DEFAULT_CACHE_NAME);
        sourceCacheConfig.setCacheClass(ConnectorConfig.DEFAULT_CACHE_CLASS);

        entryCacheConfig = new CacheConfig();
        entryCacheConfig.setName(EntryCache.DEFAULT_CACHE_NAME);
        entryCacheConfig.setCacheClass(EntryCache.DEFAULT_CACHE_CLASS);

        connectorConfig = new ConnectorConfig();
        engineConfig = new EngineConfig();
        sessionHandlerConfig = new SessionHandlerConfig();

        rootUserConfig = new UserConfig("uid=admin,ou=system", "secret");
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    public EngineConfig getEngineConfig() {
        return engineConfig;
    }

    public void setInterpreterConfig(InterpreterConfig interpreterConfig) {
        this.interpreterConfig = interpreterConfig;
    }

    public InterpreterConfig getInterpreterConfig() {
        return interpreterConfig;
    }

    public Collection getAdapterConfigs() {
        return adapterConfigs.values();
    }

    public AdapterConfig getAdapterConfig(String name) {
        return (AdapterConfig)adapterConfigs.get(name);
    }

    public void setAdapterConfigs(Map adapterConfigs) {
        this.adapterConfigs = adapterConfigs;
    }

    public void addAdapterConfig(AdapterConfig adapter) {
        adapterConfigs.put(adapter.getName(), adapter);
    }

    public String getSystemProperty(String name) {
        return (String)systemProperties.get(name);
    }

    public Collection getSystemPropertyNames() {
        return systemProperties.keySet();
    }
    
    public void setSystemProperty(String name, String value) {
        systemProperties.put(name, value);
    }

    public String removeSystemProperty(String name) {
        return (String)systemProperties.remove(name);
    }

    public void setConnectorConfig(ConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public ConnectorConfig getConnectorConfig() {
        return connectorConfig;
    }
    
    public CacheConfig getEntryCacheConfig() {
        return entryCacheConfig;
    }

    public void setEntryCacheConfig(CacheConfig entryCacheConfig) {
        this.entryCacheConfig = entryCacheConfig;
    }

    public CacheConfig getSourceCacheConfig() {
        return sourceCacheConfig;
    }

    public void setSourceCacheConfig(CacheConfig sourceCacheConfig) {
        this.sourceCacheConfig = sourceCacheConfig;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void addSchemaConfig(SchemaConfig schemaConfig) {
        schemaConfigs.put(schemaConfig.getName(), schemaConfig);
    }

    public SchemaConfig getSchemaConfig(String name) {
        return (SchemaConfig)schemaConfigs.get(name);
    }

    public Collection getSchemaConfigs() {
        return schemaConfigs.values();
    }

    public SchemaConfig removeSchemaConfig(String name) {
        return (SchemaConfig)schemaConfigs.remove(name);
    }

    public void addPartitionConfig(PartitionConfig partitionConfig) {
        partitionConfigs.put(partitionConfig.getName(), partitionConfig);
    }

    public PartitionConfig getPartitionConfig(String name) {
        return (PartitionConfig)partitionConfigs.get(name);
    }

    public Collection getPartitionConfigs() {
        return partitionConfigs.values();
    }

    public PartitionConfig removePartitionConfig(String name) {
        return (PartitionConfig)partitionConfigs.remove(name);
    }

    public void addServiceConfig(ServiceConfig serviceConfig) {
        serviceConfigs.put(serviceConfig.getName(), serviceConfig);
    }

    public ServiceConfig getServiceConfig(String name) {
        return (ServiceConfig)serviceConfigs.get(name);
    }

    public Collection getServiceConfigs() {
        return serviceConfigs.values();
    }

    public ServiceConfig removeServiceConfig(String name) {
        return (ServiceConfig)serviceConfigs.remove(name);
    }

    public UserConfig getRootUserConfig() {
        return rootUserConfig;
    }

    public void setRootUserConfig(UserConfig rootUserConfig) {
        this.rootUserConfig = rootUserConfig;
    }

    public int hashCode() {
        return (home == null ? 0 : home.hashCode()) +
                (systemProperties == null ? 0 : systemProperties.hashCode()) +
                (schemaConfigs == null ? 0 : schemaConfigs.hashCode()) +
                (adapterConfigs == null ? 0 : adapterConfigs.hashCode()) +
                (partitionConfigs == null ? 0 : partitionConfigs.hashCode()) +
                (serviceConfigs == null ? 0 : serviceConfigs.hashCode()) +
                (interpreterConfig == null ? 0 : interpreterConfig.hashCode()) +
                (entryCacheConfig == null ? 0 : entryCacheConfig.hashCode()) +
                (sourceCacheConfig == null ? 0 : sourceCacheConfig.hashCode()) +
                (sessionHandlerConfig == null ? 0 : sessionHandlerConfig.hashCode()) +
                (engineConfig == null ? 0 : engineConfig.hashCode()) +
                (connectorConfig == null ? 0 : connectorConfig.hashCode()) +
                (rootUserConfig == null ? 0 : rootUserConfig.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if((object == null) || (object.getClass() != this.getClass())) return false;

        PenroseConfig penroseConfig = (PenroseConfig)object;

        if (!equals(home, penroseConfig.home)) return false;

        if (!equals(systemProperties, penroseConfig.systemProperties)) return false;
        if (!equals(schemaConfigs, penroseConfig.schemaConfigs)) return false;
        if (!equals(adapterConfigs, penroseConfig.adapterConfigs)) return false;
        if (!equals(partitionConfigs, penroseConfig.partitionConfigs)) return false;
        if (!equals(serviceConfigs, penroseConfig.serviceConfigs)) return false;

        if (!equals(interpreterConfig, penroseConfig.interpreterConfig)) return false;

        if (!equals(entryCacheConfig, penroseConfig.entryCacheConfig)) return false;
        if (!equals(sourceCacheConfig, penroseConfig.sourceCacheConfig)) return false;

        if (!equals(sessionHandlerConfig, penroseConfig.sessionHandlerConfig)) return false;
        if (!equals(engineConfig, penroseConfig.engineConfig)) return false;
        if (!equals(connectorConfig, penroseConfig.connectorConfig)) return false;

        if (!equals(rootUserConfig, penroseConfig.rootUserConfig)) return false;

        return true;
    }

    public void copy(PenroseConfig penroseConfig) {
        home = penroseConfig.home;

        systemProperties.clear();
        systemProperties.putAll(penroseConfig.systemProperties);

        schemaConfigs.clear();
        for (Iterator i=penroseConfig.schemaConfigs.values().iterator(); i.hasNext(); ) {
            SchemaConfig schemaConfig = (SchemaConfig)i.next();
            addSchemaConfig((SchemaConfig)schemaConfig.clone());
        }

        adapterConfigs.clear();
        for (Iterator i=penroseConfig.adapterConfigs.values().iterator(); i.hasNext(); ) {
            AdapterConfig adapterConfig = (AdapterConfig)i.next();
            addAdapterConfig((AdapterConfig)adapterConfig.clone());
        }

        partitionConfigs.clear();
        for (Iterator i=penroseConfig.partitionConfigs.values().iterator(); i.hasNext(); ) {
            PartitionConfig partitionConfig = (PartitionConfig)i.next();
            addPartitionConfig((PartitionConfig)partitionConfig.clone());
        }

        serviceConfigs.clear();
        for (Iterator i=penroseConfig.serviceConfigs.values().iterator(); i.hasNext(); ) {
            ServiceConfig serviceConfig = (ServiceConfig)i.next();
            addServiceConfig((ServiceConfig)serviceConfig.clone());
        }

        interpreterConfig.copy(interpreterConfig);

        entryCacheConfig.copy(entryCacheConfig);
        sourceCacheConfig.copy(sourceCacheConfig);

        sessionHandlerConfig.copy(sessionHandlerConfig);
        engineConfig.copy(engineConfig);
        connectorConfig.copy(connectorConfig);

        rootUserConfig.copy(rootUserConfig);
    }

    public Object clone() {
        PenroseConfig penroseConfig = new PenroseConfig();
        penroseConfig.copy(this);

        return penroseConfig;
    }

    public SessionHandlerConfig getSessionHandlerConfig() {
        return sessionHandlerConfig;
    }

    public void setSessionHandlerConfig(SessionHandlerConfig sessionHandlerConfig) {
        this.sessionHandlerConfig = sessionHandlerConfig;
    }
}