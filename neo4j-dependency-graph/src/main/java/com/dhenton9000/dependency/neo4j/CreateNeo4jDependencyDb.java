package com.dhenton9000.dependency.neo4j;

import com.dhenton9000.dependency.*;
import com.dhenton9000.neo4j.utils.DatabaseHelper;
import com.dhenton9000.utils.xml.XMLUtils;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.bind.JAXBException;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note that this class will no longer work as the ivy.listing file
 * has been deliberately removed. The embedded database is the only thing
 * that will work now
 *
 */
public class CreateNeo4jDependencyDb {

    public static final String DB_LOCATION = "target/dep-db";
    public static final String FAKE_PROJECTS_INDEX_NAME = "fakeProjects";
    public static final String PROJECT_INDEX_NAME = "projects";
    public static final String PROJECT_LABEL = "project";
    public static final String FAKE_LABEL = "projectID";
    public static final String REVISION = "rev";
    public static final String CONFIGURATION = "conf";
    public static final String ROOT_PROJECT_LABEL_VALUE = "ROOT";
    private static final Logger logger = LoggerFactory.getLogger(CreateNeo4jDependencyDb.class);
    private EmbeddedGraphDatabase graphDb;
    private JaxbService jaxbService = new JaxbService();
    private ProjectCollection items;

    private void createFakeLabels(Node rootNode) {
        Iterable<Relationship> rootRelations = rootNode.getRelationships();
        Index<Node> indexProjects = graphDb.index().forNodes(PROJECT_INDEX_NAME);
        Index<Node> fakeIndexProjects = graphDb.index().forNodes(FAKE_PROJECTS_INDEX_NAME);
        Iterator<Relationship> iter = rootRelations.iterator();
        rootNode.removeProperty(PROJECT_LABEL);
        rootNode.setProperty(FAKE_LABEL, ROOT_PROJECT_LABEL_VALUE);
        int fakeCounter = 0;
        while (iter.hasNext()) {
            Relationship r = iter.next();
            fakeCounter++;
            String idLabel = "P"+String.format("%02d",fakeCounter);
           // String idLabel = "P" + fakeCounter;
            logger.debug("id "+idLabel);
            r.getEndNode().setProperty(FAKE_LABEL, idLabel);
            r.getEndNode().removeProperty(PROJECT_LABEL);
            fakeIndexProjects.add(r.getEndNode(), FAKE_LABEL, idLabel);
        }
        indexProjects.delete();

    }

    private void writeOutSanitized(Node rootNode) throws JAXBException,
            UnsupportedEncodingException, IOException {
        items = new ProjectCollection();
        ArrayList<DependencyCollection> projectDep = items.getProjectDependencies();
        DependencyCollection dc = null;
        Iterable<Relationship> rootRelations = rootNode.getRelationships();
        Iterator<Relationship> iter = rootRelations.iterator();

        while (iter.hasNext()) {
            Relationship rootRel = iter.next();
            Node projectNode = rootRel.getEndNode();
            String projectName =
                    (String) projectNode.getProperty(FAKE_LABEL);
            ArrayList<IvyDependency> depColl = new ArrayList<IvyDependency>();

            // now add the dependencies
            Iterable<Relationship> projectRel = projectNode.getRelationships();
            Iterator<Relationship> projectIter = projectRel.iterator();


            while (projectIter.hasNext()) {
                Relationship projectRelItem = projectIter.next();
                if (projectRelItem.isType(ProjectTypes.DEPENDS_ON)) {
                    IvyDependency ivyDep = new IvyDependency();
                    ivyDep.setRev((String) projectRelItem.getProperty(REVISION));
                    Node depPNode = projectRelItem.getEndNode();
                    ivyDep.setName((String) depPNode.getProperty(FAKE_LABEL));
                    ivyDep.setOrg("com.dhenton9000.neo4j");
                    if (!depColl.contains(ivyDep))
                        depColl.add(ivyDep);
                }
            }
            dc = new DependencyCollection(projectName, depColl);
            items.getProjectDependencies().add(dc);
        }

            String info = jaxbService.ProjectToString(items);
            XMLUtils.stringToFile(info,"src/main/resources/t1.xml");



    }

    public enum ProjectTypes implements RelationshipType {

        IS_PROJECT,
        DEPENDS_ON
    }

    public void writeYed() throws IOException {
        DependencyYedFileWriter yedWriter = null;
        Graph graph = new Neo4jGraph(DB_LOCATION);
        yedWriter = new DependencyYedFileWriter(graph);
        yedWriter.outputGraph("deps.graphml");
    }

    public void doDBCreate() throws Exception {
        DatabaseHelper dbHelper = new DatabaseHelper();
        graphDb = dbHelper.createDatabase(DB_LOCATION, true);

        items =
                jaxbService.jaxBStringToProject(XMLUtils.getStringResource("ivy/listing/items.xml",
                this.getClass().getClassLoader()));

        Node project = null;
        Node depProject = null;
        Node rootNode = null;
        Transaction tx = graphDb.beginTx();

        try {
            rootNode = graphDb.getReferenceNode();
            for (DependencyCollection d : items.getProjectDependencies()) {

                project = createOrReturnProject(d.getProjectName(), rootNode);
                for (IvyDependency dep : d.getDependencies()) {
                    depProject = createOrReturnProject(dep.getName(), rootNode);


                    Relationship rel = project.createRelationshipTo(depProject,
                            ProjectTypes.DEPENDS_ON);

                    rel.setProperty(REVISION, dep.getRev());
                    rel.setProperty(CONFIGURATION,dep.getConf());
                }
            }
            createFakeLabels(rootNode);
            writeOutSanitized(rootNode);
            tx.success();
        } finally {
            tx.finish();
        }
        // dbHelper.dumpGraphToConsole(graphDb);
        graphDb.shutdown();
    }

    public static void main(String[] args) {
        CreateNeo4jDependencyDb dbCreator = new CreateNeo4jDependencyDb();
        try {
           // dbCreator.doDBCreate();
             dbCreator.writeYed();
        } catch (Exception ex) {
            logger.error("Problem ", ex);
        }
    }

    private Node createOrReturnProject(String projectName, Node rootNode) {

        Index<Node> index = graphDb.index().forNodes(PROJECT_INDEX_NAME);
        Node project = index.get(PROJECT_LABEL, projectName).getSingle();
        if (project == null) {
            project = graphDb.createNode();
            project.setProperty(PROJECT_LABEL, projectName);
            index.add(project, PROJECT_LABEL, projectName);
            Relationship rel = rootNode.createRelationshipTo(project,
                    ProjectTypes.IS_PROJECT);
            //logger.debug("creating node "+projectName);
        } else {
            //logger.debug("returning "+projectName);
        }

        return project;


    }
}
