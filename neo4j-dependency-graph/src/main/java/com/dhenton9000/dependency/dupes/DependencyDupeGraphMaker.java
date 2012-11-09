/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.dependency.dupes;

import java.util.Iterator;
import java.util.Map;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.dhenton9000.dependency.neo4j.CreateNeo4jDependencyDb.*;
import com.dhenton9000.utils.xml.XMLUtils;
import java.io.IOException;
import java.util.Collection;
import org.neo4j.graphdb.Relationship;
import java.util.HashSet;
import java.util.logging.Level;
import org.neo4j.graphdb.Node;
/**
 *
 * @author dhenton
 */
public class DependencyDupeGraphMaker {

    protected static GraphDatabaseService staticgraphDb;
    private static final String mainQuery = "start m=node:fakeProjects(projectID='%s') "
            + "MATCH m-[relations:DEPENDS_ON*]->n "
            + "where n.projectID = '%s' with relations, m.projectID as project ,"
            + "n.projectID as dependency return project, dependency, relations;";
    private static final Logger logger =
            LoggerFactory.getLogger(DependencyDupeGraphMaker.class);

    private String getGraphMLHeader() {
        String header = "<?xml version=\"1.0\" ?>";
        header += "\r\n<graphml\r\n  xmlns=\"http://graphml.graphdrawing.org/xmlns\"\r\n  "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n  "
                + "xmlns:y=\"http://www.yworks.com/xml/graphml\"\r\n  "
                + "xmlns:yed=\"http://www.yworks.com/xml/yed/3\"\r\n  "
                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\r\n  "
                + "http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"\r\n>";
        header += "\r\n  <key for=\"node\" id=\"d5\" "
                + "attr.name=\"description\" attr.type=\"string\" />";
        header += "\r\n  <key for=\"node\" id=\"d6\" "
                + "yfiles.type=\"nodegraphics\"/>";
        header += "\r\n  <key for=\"edge\" id=\"d9\" "
                + "yfiles.type=\"edgegraphics\"/>";
        header += "\r\n  <graph id=\"G\" edgedefault=\"directed\">";
        return header;
    }

    private String getGraphMLFooter() {
        String footer = "\r\n  </graph>\r\n</graphml>";
        return footer;
    }

    private String getNode(String id, String label) {
        String node = "\r\n    <node id=\"" + id + "\">";
        node += "\r\n      <data key=\"d5\"/>";
        node += "\r\n      <data key=\"d6\">";
        node += "\r\n        <y:ShapeNode>";
        node += "\r\n          <y:NodeLabel>" + label + "</y:NodeLabel>";
        node += "\r\n          <y:Shape type=\"rectangle\"/>";
        node += "\r\n        </y:ShapeNode>";
        node += "\r\n      </data>";
        node += "\r\n    </node>";
        return node;
    }

    private String getEdge(Relationship edge) {
        Node source = edge.getStartNode();
        Node target = edge.getEndNode();
        String edgeId = edge.getId()+"";
        String sourceId = source.getId()+"";
        String targetId = target.getId()+"";
        //String label = edge.getLabel();
         String label = (String) edge.getProperty(REVISION);
        // label += " "+(String) edge.getProperty(CONFIGURATION);
        // logger.debug("label is "+label);

        String edgeXml = "\r\n<edge id=\""
                + edgeId + "\" source=\""
                + sourceId + "\" target=\""
                + targetId + "\">";

        edgeXml += "\r\n  <data key=\"d9\">";
        edgeXml += "\r\n      <y:PolyLineEdge>";
        edgeXml += "\r\n           <y:Arrows source=\"none\" target=\"standard\"/>";
        edgeXml += "\r\n           <y:EdgeLabel>"+label+"</y:EdgeLabel>";
        edgeXml += "\r\n           <y:BendStyle smoothed=\"false\"/> ";
        edgeXml += "\r\n      </y:PolyLineEdge>";
        edgeXml += "\r\n  </data>";
        edgeXml += "\r\n</edge>";

        return edgeXml;
    }

    protected  void prepareEmbeddedDatabase(String dbLocation) {
        staticgraphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbLocation);
        registerShutdownHook();
    }

    private String displayRelationship(Relationship r)
    {
       String t = r.getStartNode().getProperty(FAKE_LABEL)+
                            " - "+r.getProperty(REVISION)+" -> "
                                 +r.getEndNode().getProperty(FAKE_LABEL);
       
       return t;
    }
    
    
    public void  createDuplicationGraph(String project, String dependency) throws IOException {
        ExecutionEngine exEngine = new ExecutionEngine(staticgraphDb);

        //from dependency
        String q = String.format(mainQuery, project, dependency);
        logger.debug("\n"+q);
        ExecutionResult eResult = exEngine.execute(q);
        Iterator<Map<String, Object>> res = eResult.javaIterator();
        int cc = 0;
        HashSet<Node> pathSet = new HashSet<Node>();
        HashSet<Relationship> relationSet = new HashSet<Relationship>();
        while (res.hasNext()) {
            Map<String, Object> t = res.next();
            //for (String k : t.keySet()) {
                //  logger.debug(cc + " key " + k + " " + t.get(k));
                Collection col = (Collection) t.get("relations");
                Iterator iter = col.iterator();
               
               // logger.debug("Path "+cc);
                while (iter.hasNext())
                {
                    Relationship r = (Relationship) iter.next();
                   // logger.debug("\t"+ displayRelationship(r));
                    pathSet.add(r.getStartNode());
                    pathSet.add(r.getEndNode());
                    relationSet.add(r);
                    
                }
                
           // }
            
            cc++;
        }
       // logger.debug("Pathset "+pathSet);
       // String temp = "\n";
       
       // logger.debug("Relationset "+temp);
        //logger.debug(q);
        String xml = this.getGraphMLHeader();
        
        Iterator<Node> verticesIterator = pathSet.iterator();
        while (verticesIterator.hasNext()) {
            Node vertex = verticesIterator.next();
            String labelValue = (String) vertex.getProperty(FAKE_LABEL);
            String id = vertex.getId()+"";
            String node = getNode(id, labelValue);
            xml += node;
            cc++;
        }
        
        Iterator<Relationship> edgesIter = relationSet.iterator();
        while (edgesIter.hasNext())
        {
             Relationship r = edgesIter.next();
             xml += getEdge(r);
        }
        
        xml += this.getGraphMLFooter();
        // logger.debug(xml);
        XMLUtils.stringToFile(xml,"/home/dhenton/neo4j/sample_graphs/"+project+"_"+dependency+".graphml");
        
        
        
         
    }

    public static void main(String[] args) {
        DependencyDupeGraphMaker d = new DependencyDupeGraphMaker();
        d.prepareEmbeddedDatabase(DB_LOCATION);
        try {
            // d.createDuplicationGraph("P29","P16");
            // d.createDuplicationGraph("P72","P17");
            // d.createDuplicationGraph("P13","P16");
            d.createDuplicationGraph("P53","P20");
        } catch ( Exception ex) {
           logger.error("problem with graph creation: "+ex.getMessage(),ex);
            
        }
    }
    
     private static void registerShutdownHook() {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                staticgraphDb.shutdown();
            }
        });
    }
}
