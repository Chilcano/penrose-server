package org.safehaus.penrose.management.source;

import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionConfigManager;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.BaseService;
import org.safehaus.penrose.management.PenroseJMXService;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.session.Session;
import org.safehaus.penrose.session.SessionManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.SourceConfig;

import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class SourceService extends BaseService implements SourceServiceMBean {

    private PartitionManager partitionManager;
    private String partitionName;
    private String sourceName;

    public SourceService(
            PenroseJMXService jmxService,
            PartitionManager partitionManager,
            String partitionName,
            String sourceName
    ) throws Exception {
        super(SourceServiceMBean.class);
        
        this.jmxService = jmxService;
        this.partitionManager = partitionManager;
        this.partitionName = partitionName;
        this.sourceName = sourceName;
    }

    public String getObjectName() {
        return SourceClient.getStringObjectName(partitionName, sourceName);
    }

    public Object getObject() {
        return getSource();
    }

    public SourceConfig getSourceConfig() throws Exception {
        return getPartitionConfig().getSourceConfigManager().getSourceConfig(sourceName);
    }

    public Source getSource() {
        return getPartition().getSourceManager().getSource(sourceName);
    }

    public PartitionConfig getPartitionConfig() {
        return partitionManager.getPartitionConfig(partitionName);
    }

    public Partition getPartition() {
        return partitionManager.getPartition(partitionName);
    }

    public Session createAdminSession() throws Exception {
        Partition partition = getPartition();
        SessionManager sessionManager = partition.getPartitionContext().getSessionManager();
        return sessionManager.createAdminSession();
    }

    public Long getCount() throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            return source.getCount(session);

        } finally {
            session.close();
        }
    }

    public void create() throws Exception {

        log.debug("Creating source "+partitionName+"/"+sourceName+"...");

        Source source = getSource();
        source.create();

        log.debug("Source created.");
    }

    public void clear() throws Exception {

        log.debug("Clearing source "+partitionName+"/"+sourceName+"...");

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.clear(session);
            
        } finally {
            session.close();
        }

        log.debug("Source cleared.");
    }

    public void drop() throws Exception {

        log.debug("Dropping source "+partitionName+"/"+sourceName+"...");

        Source source = getSource();
        source.drop();

        log.debug("Source dropped.");
    }

    public String getAdapterName() throws Exception {

        SourceConfig sourceConfig = getSourceConfig();

        String partitionName = sourceConfig.getPartitionName();
        String connectionName = sourceConfig.getConnectionName();

        Partition partition = getPartition();
        if (partitionName != null) partition = partition.getPartitionContext().getPartition(partitionName);

        ConnectionConfigManager connectionConfigManager = partition.getPartitionConfig().getConnectionConfigManager();

        ConnectionConfig connectionConfig = connectionConfigManager.getConnectionConfig(connectionName);
        return connectionConfig.getAdapterName();
    }

    public String getConnectionName() throws Exception {
        SourceConfig sourceConfig = getSourceConfig();
        return sourceConfig.getConnectionName();
    }

    public String getParameter(String name) throws Exception {
        SourceConfig sourceConfig = getSourceConfig();
        return sourceConfig.getParameter(name);
    }

    public Collection<String> getParameterNames() throws Exception {
        SourceConfig sourceConfig = getSourceConfig();
        return sourceConfig.getParameterNames();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public AddResponse add(
            String dn,
            Attributes attributes
    ) throws Exception {

        AddRequest request = new AddRequest();
        request.setDn(dn);
        request.setAttributes(attributes);

        AddResponse response = new AddResponse();

        return add(request, response);
    }

    public AddResponse add(
            RDN rdn,
            Attributes attributes
    ) throws Exception {

        AddRequest request = new AddRequest();
        request.setDn(rdn);
        request.setAttributes(attributes);

        AddResponse response = new AddResponse();

        return add(request, response);
    }

    public AddResponse add(
            DN dn,
            Attributes attributes
    ) throws Exception {

        AddRequest request = new AddRequest();
        request.setDn(dn);
        request.setAttributes(attributes);

        AddResponse response = new AddResponse();

        return add(request, response);
    }

    public AddResponse add(
            AddRequest request,
            AddResponse response
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.add(session, request, response);

            int rc = response.getReturnCode();
            log.debug("RC: "+rc);

            return response;

        } finally {
            session.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Delete
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DeleteResponse delete(
            String dn
    ) throws Exception {

        DeleteRequest request = new DeleteRequest();
        request.setDn(dn);

        DeleteResponse response = new DeleteResponse();

        return delete(request, response);
    }

    public DeleteResponse delete(
            RDN rdn
    ) throws Exception {

        DeleteRequest request = new DeleteRequest();
        request.setDn(rdn);

        DeleteResponse response = new DeleteResponse();

        return delete(request, response);
    }

    public DeleteResponse delete(
            DN dn
    ) throws Exception {

        DeleteRequest request = new DeleteRequest();
        request.setDn(dn);

        DeleteResponse response = new DeleteResponse();

        return delete(request, response);
    }

    public DeleteResponse delete(
            DeleteRequest request,
            DeleteResponse response
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.delete(session, request, response);

            int rc = response.getReturnCode();
            log.debug("RC: "+rc);

            return response;

        } finally {
            session.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Find
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public SearchResult find(String dn) throws Exception {
        return find(new DN(dn));
    }

    public SearchResult find(RDN rdn) throws Exception {
        return find(new DN(rdn));
    }

    public SearchResult find(
            DN dn
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            return source.find(session, dn);

        } finally {
            session.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Modify
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ModifyResponse modify(
            String dn,
            Collection<Modification> modifications
    ) throws Exception {

        ModifyRequest request = new ModifyRequest();
        request.setDn(dn);
        request.setModifications(modifications);

        ModifyResponse response = new ModifyResponse();

        return modify(request, response);
    }

    public ModifyResponse modify(
            RDN rdn,
            Collection<Modification> modifications
    ) throws Exception {

        ModifyRequest request = new ModifyRequest();
        request.setDn(rdn);
        request.setModifications(modifications);

        ModifyResponse response = new ModifyResponse();

        return modify(request, response);
    }

    public ModifyResponse modify(
            DN dn,
            Collection<Modification> modifications
    ) throws Exception {

        ModifyRequest request = new ModifyRequest();
        request.setDn(dn);
        request.setModifications(modifications);

        ModifyResponse response = new ModifyResponse();

        return modify(request, response);
    }

    public ModifyResponse modify(
            ModifyRequest request,
            ModifyResponse response
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.modify(session, request, response);

            int rc = response.getReturnCode();
            log.debug("RC: "+rc);

            return response;

        } finally {
            session.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ModRdn
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ModRdnResponse modrdn(
            String dn,
            String newRdn,
            boolean deleteOldRdn
    ) throws Exception {

        ModRdnRequest request = new ModRdnRequest();
        request.setDn(dn);
        request.setNewRdn(newRdn);
        request.setDeleteOldRdn(deleteOldRdn);

        ModRdnResponse response = new ModRdnResponse();

        return modrdn(request, response);
    }

    public ModRdnResponse modrdn(
            RDN rdn,
            RDN newRdn,
            boolean deleteOldRdn
    ) throws Exception {

        ModRdnRequest request = new ModRdnRequest();
        request.setDn(rdn);
        request.setNewRdn(newRdn);
        request.setDeleteOldRdn(deleteOldRdn);

        ModRdnResponse response = new ModRdnResponse();

        return modrdn(request, response);
    }

    public ModRdnResponse modrdn(
            DN dn,
            RDN newRdn,
            boolean deleteOldRdn
    ) throws Exception {

        ModRdnRequest request = new ModRdnRequest();
        request.setDn(dn);
        request.setNewRdn(newRdn);
        request.setDeleteOldRdn(deleteOldRdn);

        ModRdnResponse response = new ModRdnResponse();

        return modrdn(request, response);
    }

    public ModRdnResponse modrdn(
            ModRdnRequest request,
            ModRdnResponse response
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.modrdn(session, request, response);

            int rc = response.getReturnCode();
            log.debug("RC: "+rc);

            return response;

        } finally {
            session.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Search
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public SearchResponse search(
            String dn,
            String filter,
            int scope
    ) throws Exception {

        SearchRequest request = new SearchRequest();
        request.setDn(dn);
        request.setFilter(filter);
        request.setScope(scope);

        SearchResponse response = new SearchResponse();

        return search(request, response);
    }

    public SearchResponse search(
            RDN rdn,
            Filter filter,
            int scope
    ) throws Exception {

        SearchRequest request = new SearchRequest();
        request.setDn(rdn);
        request.setFilter(filter);
        request.setScope(scope);

        SearchResponse response = new SearchResponse();

        return search(request, response);
    }

    public SearchResponse search(
            DN dn,
            Filter filter,
            int scope
    ) throws Exception {

        SearchRequest request = new SearchRequest();
        request.setDn(dn);
        request.setFilter(filter);
        request.setScope(scope);

        SearchResponse response = new SearchResponse();

        return search(request, response);
    }

    public SearchResponse search(
            SearchRequest request,
            SearchResponse response
    ) throws Exception {

        Session session = createAdminSession();

        try {
            Source source = getSource();
            source.search(session, request, response);

            int rc = response.waitFor();
            log.debug("RC: "+rc);

            return response;

        } finally {
            session.close();
        }
    }
}
