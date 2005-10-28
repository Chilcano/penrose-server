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

import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.config.Config;
import org.safehaus.penrose.graph.GraphVisitor;
import org.safehaus.penrose.graph.Graph;
import org.safehaus.penrose.graph.GraphIterator;
import org.safehaus.penrose.util.Formatter;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class SearchLocalRunner extends GraphVisitor {

    Logger log = Logger.getLogger(getClass());

    private Config config;
    private Graph graph;
    private Engine engine;
    private EngineContext engineContext;
    private EntryDefinition entryDefinition;
    private AttributeValues sourceValues;
    private Map filters;
    private Source startingSource;

    private Stack filterStack = new Stack();

    private Collection results = new TreeSet();

    public SearchLocalRunner(
            Engine engine,
            EntryDefinition entryDefinition,
            AttributeValues sourceValues,
            SearchPlanner planner,
            Source startingSource,
            Collection relationships) throws Exception {

        this.engine = engine;
        this.engineContext = engine.getEngineContext();
        this.entryDefinition = entryDefinition;
        this.filters = planner.getFilters();
        this.startingSource = startingSource;
        this.sourceValues = sourceValues;

        config = engineContext.getConfig(entryDefinition.getDn());
        graph = engine.getGraph(entryDefinition);

        Filter filter = (Filter)filters.get(startingSource);

        Map map = new HashMap();
        map.put("filter", filter);
        map.put("relationships", relationships);

        filterStack.push(map);
    }

    public void run() throws Exception {
        graph.traverse(this, startingSource);
    }

    public void visitNode(GraphIterator graphIterator, Object node) throws Exception {

        Source source = (Source)node;

        log.debug(Formatter.displaySeparator(40));
        log.debug(Formatter.displayLine("Visiting "+source.getName(), 40));
        log.debug(Formatter.displaySeparator(40));

        Map map = (Map)filterStack.peek();
        Filter filter = (Filter)map.get("filter");
        Collection relationships = (Collection)map.get("relationships");

        log.debug("Filter: "+filter);
        log.debug("Relationships: "+relationships);

        String s = source.getParameter(Source.FILTER);
        if (s != null) {
            Filter sourceFilter = engineContext.getFilterTool().parseFilter(s);
            filter = engineContext.getFilterTool().appendAndFilter(filter, sourceFilter);
        }

        log.debug("Searching source "+source.getName()+" with filter "+filter);

        ConnectionConfig connectionConfig = config.getConnectionConfig(source.getConnectionName());
        SourceDefinition sourceDefinition = connectionConfig.getSourceDefinition(source.getSourceName());

        Collection tmp = engineContext.getSyncService().search(sourceDefinition, filter);

        Collection list = new ArrayList();
        for (Iterator i=tmp.iterator(); i.hasNext(); ) {
            AttributeValues av = (AttributeValues)i.next();

            AttributeValues sv = new AttributeValues();
            sv.add(sourceValues);
            sv.add(source.getName(), av);
            list.add(sv);
        }
/*
        log.debug("Search results:");

        int counter = 1;
        for (Iterator j=list.iterator(); j.hasNext(); counter++) {
            AttributeValues sv = (AttributeValues)j.next();
            log.debug(" - Result #"+counter);
            for (Iterator k=sv.getNames().iterator(); k.hasNext(); ) {
                String name = (String)k.next();
                Collection values = sv.get(name);
                log.debug("   - "+name+": "+values);
            }
        }
*/
        if (results.isEmpty()) {
            results.addAll(list);

        } else {
            Collection temp;
            if (source.isRequired()) {
                temp = engine.getJoinEngine().join(results, list, relationships);
            } else {
                temp = engine.getJoinEngine().leftJoin(results, list, relationships);
            }

            results.clear();
            results.addAll(temp);
        }

        graphIterator.traverseEdges(node);
    }

    public void visitEdge(GraphIterator graphIterator, Object node1, Object node2, Object object) throws Exception {

        Source fromSource = (Source)node1;
        Source toSource = (Source)node2;
        Relationship relationship = (Relationship)object;

        log.debug(Formatter.displaySeparator(40));
        log.debug(Formatter.displayLine(relationship.toString(), 40));
        log.debug(Formatter.displaySeparator(40));

        if (entryDefinition.getSource(toSource.getName()) == null) {
            log.debug("Source "+toSource.getName()+" is not defined in entry.");
            return;
        }

        Collection relationships = new ArrayList();
        relationships.add(relationship);

        Filter filter = null;

        log.debug("Generating filters:");
        for (Iterator i=results.iterator(); i.hasNext(); ) {
            AttributeValues av = (AttributeValues)i.next();

            Filter f = engine.generateFilter(toSource, relationships, av);
            log.debug(" - "+f);

            filter = engineContext.getFilterTool().appendOrFilter(filter, f);
        }

        Filter sourceFilter = (Filter)filters.get(toSource);
        filter = engineContext.getFilterTool().appendAndFilter(filter, sourceFilter);

        if (filter == null) return;

        Map map = new HashMap();
        map.put("filter", filter);
        map.put("relationships", relationships);

        filterStack.push(map);

        graphIterator.traverse(node2);

        filterStack.pop();
    }

    public Collection getResults() {
        return results;
    }
}