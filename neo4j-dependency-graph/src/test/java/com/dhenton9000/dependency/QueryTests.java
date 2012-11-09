/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.dependency;

import com.dhenton9000.dependency.neo4j.CreateNeo4jDependencyDb;
import com.dhenton9000.neo4j.utils.DatabaseHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import static com.dhenton9000.dependency.neo4j.CreateNeo4jDependencyDb.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import static org.junit.Assert.*;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhenton
 */
public class QueryTests extends BaseNeo4jTest {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryTests.class);
    private static DatabaseHelper dbHelper = new DatabaseHelper();
    
    @BeforeClass
    public static void createDatabase() throws Exception {
        prepareEmbeddedDatabase(DB_LOCATION);
    }
    
    @AfterClass
    public static void closeTheDatabase() {
        staticgraphDb.shutdown();
    }
    
    @Test
    public void testCountOfProjects() {
        Node rootNode = staticgraphDb.getReferenceNode();
        //dbHelper.dumpGraphToConsole(graphDb);
        Iterable<Relationship> rootRelations = rootNode.getRelationships();
        int counter = 0;
        Iterator<Relationship> iter = rootRelations.iterator();
        while (iter.hasNext()) {
            counter++;
            iter.next();
        }
        assertEquals(84, counter);
        
    }
    
    @Test
    public void testLabels()
    {
        Node n1 =  staticgraphDb.index().forNodes(FAKE_PROJECTS_INDEX_NAME).
                 get( "projectID", "P01" ).getSingle();
        assertNotNull(n1);
        assertEquals("P01",n1.getProperty(FAKE_LABEL));
        
        n1 =  staticgraphDb.index().forNodes(FAKE_PROJECTS_INDEX_NAME).
                 get( "projectID", "P1" ).getSingle();
        assertNull(n1);
        
    }
    
    
    @Test
    public void testImmediateDependencies() {
        ExecutionEngine exEngine = new ExecutionEngine(staticgraphDb);
        String q = "START m=node:" + FAKE_PROJECTS_INDEX_NAME + "(projectID='P25') "
                + "match m <-- o "
                + "return o ";
        logger.debug("\n"+q+"\n");
        ExecutionResult eResult = exEngine.execute(q);
        Iterator<Map<String, Object>> res = eResult.javaIterator();
        int cc = 0;
        while (res.hasNext()) {
            Map<String, Object> t = res.next();
            for (String k : t.keySet()) {
                //  logger.debug(cc + " key " + k + " " + t.get(k));
            }
            cc++;
        }
        assertEquals(4, cc);
        //logger.debug("\n"+eResult.dumpToString());

    }
    
    @Test
    public void testFullDependencies() {
        ExecutionEngine exEngine = new ExecutionEngine(staticgraphDb);
        String q = "START m=node:" + FAKE_PROJECTS_INDEX_NAME + "(projectID='P16') "
                + "match m <-[:DEPENDS_ON*]- o "
                + "where o.projectID <> 'ROOT' "
                + "return distinct o ";
        
        logger.debug("\n"+q+"\n");
        ExecutionResult eResult = exEngine.execute(q);
        Iterator<Map<String, Object>> res = eResult.javaIterator();
        int cc = 0;
        while (res.hasNext()) {
            Map<String, Object> t = res.next();
            for (String k : t.keySet()) {
                //  logger.debug(cc + " key " + k + " " + t.get(k));
            }
            cc++;
        }
        assertEquals(23, cc);
        //logger.debug("\n"+eResult.dumpToString());

    }
    
    @Test
    public void testListingDependencies() {
        ExecutionEngine exEngine = new ExecutionEngine(staticgraphDb);
        String q = "START m=node:" + FAKE_PROJECTS_INDEX_NAME + "(projectID='P1') "
                + "match z=m -[r:DEPENDS_ON*]-> o "
                + "where o.projectID = 'P71'"
                   + "WITH   r  as rels "
                + "return   rels   ";
         logger.debug("\n"+q+"\n");
        ExecutionResult eResult = exEngine.execute(q);
        Iterator<Map<String, Object>> res = eResult.javaIterator();
        int cc = 0;
        String tinfo = "\n";
        while (res.hasNext()) {
            
            tinfo +="P1";
            Map<String, Object> t = res.next();
            for (String k : t.keySet()) {
                
                Collection z = (Collection) t.get(k);
                
                Iterator ziter = z.iterator();
                while (ziter.hasNext()) {
                    Relationship r = (Relationship) ziter.next();
                    tinfo += computeRelationDisplay(r);
                }
                
                
            }
            
            tinfo +="\n";
            cc++;
        }
        logger.debug(tinfo);
        logger.debug("\n" + eResult.dumpToString());
    }
    
    private String computeRelationDisplay(Relationship r) {
        String t = "";
        
        t = "--(" + r.getProperty(REVISION) + ")--> " + r.getEndNode().getProperty(FAKE_LABEL);
                
        return t;
    }
    
   // This query will find all paths with greater than five nodes from
   // any node to any node
   // start m=node(*) MATCH m-[r:DEPENDS_ON*5..100]->n return r,n,m
   // start m=node(*) MATCH m-[r:DEPENDS_ON*4..100]->n return n,m,length(r) as len order by n.projectID, m.projectID, len;
}
