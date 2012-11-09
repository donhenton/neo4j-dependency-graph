package com.dhenton9000.neo4j.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHelper
{
    private   final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);
    
  

    public DatabaseHelper()
    {
    

    }


    public   EmbeddedGraphDatabase createDatabase( String dbDir, boolean cleanOut ) throws IOException
    {
        
       File dbDirFile = new File(dbDir); 
        
       if (cleanOut && dbDirFile.exists() && dbDirFile.isDirectory())
       {
           
           FileUtils.cleanDirectory(dbDirFile);
       }    
           
           
       return new EmbeddedGraphDatabase( dbDir );
    }

    public   File createTempDatabaseDir(String prefix, String suffix)
    {

        File d;
        try
        {
            d = File.createTempFile( prefix, suffix );
            logger.debug( String.format( "Created a new Neo4j database at [%s]", d.getAbsolutePath() ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        if ( !d.delete() )
        {
            throw new RuntimeException( "temp config directory pre-delete failed" );
        }
        if ( !d.mkdirs() )
        {
            throw new RuntimeException( "temp config directory not created" );
        }
        d.deleteOnExit();
        return d;
    }

    public   void ensureRelationshipInDb( Node startNode, RelationshipType relType, Node endNode, Map<String, Object> relationshipProperties )
    {
        for ( Relationship r : startNode.getRelationships( relType, Direction.OUTGOING ) )
        {
            if ( r.getEndNode()
                    .equals( endNode ) )
            {
                return;
            }
        }

        Relationship relationship = startNode.createRelationshipTo( endNode, relType );

        for ( String key : relationshipProperties.keySet() )
        {
            relationship.setProperty( key, relationshipProperties.get( key ) );
        }
    }

    public   void ensureRelationshipInDb( Node startNode, RelationshipType relType, Node endNode )
    {
        ensureRelationshipInDb( startNode, relType, endNode, new HashMap<String, Object>() );
    }

    public void dumpGraphToConsole( GraphDatabaseService db )
    {
        for ( Node n : GlobalGraphOperations.at( db ).getAllNodes() )
        {
            Iterable<String> propertyKeys = n.getPropertyKeys();
            for ( String key : propertyKeys )
            {
             
                logger.debug(  key + " : " +n.getProperty( key ) );
            }
        }
    }

    public int countNodesWithAllGivenProperties( Iterable<Node> allNodes, String... propertyNames )
    {
        Iterator<Node> iterator = allNodes.iterator();
        int count = 0;
        while ( iterator.hasNext() )
        {
            Node next = iterator.next();

            boolean hasAllPropertyNames = true;
            for ( String propertyName : propertyNames )
            {
                hasAllPropertyNames = hasAllPropertyNames && next.hasProperty( propertyName );
                if ( !hasAllPropertyNames )
                {
                    break; // Modest optimisation
                }
            }
            if ( hasAllPropertyNames )
            {
                count++;
            }
        }
        return count;
    }

    public boolean nodeExistsInDatabase( Node node, GraphDatabaseService db  )
    {
        return db.getNodeById( node.getId() ) != null;
    }

    public int destructivelyCountRelationships( Iterable<Relationship> relationships )
    {
        return destructivelyCount( relationships );
    }

    public void dumpNode( Node node )
    {
        if ( node == null )
        {
            logger.debug( "Null Node" );
            return;
        }
        logger.debug( String.format( "Node ID [%d]", node.getId() ) );
        for ( String key : node.getPropertyKeys() )
        {
            
            logger.debug( key + " : "+node.getProperty( key ) );
        }
    }

    public List<Relationship> toListOfRelationships( Iterable<Relationship> iterable )
    {
        ArrayList<Relationship> rels = new ArrayList<Relationship>();
        for ( Relationship r : iterable )
        {
            rels.add( r );
        }
        return rels;
    }

    public List<Node> toListOfNodes( Iterable<Node> nodes )
    {
        ArrayList<Node> rels = new ArrayList<Node>();
        for ( Node n : nodes )
        {
            rels.add( n );
        }
        return rels;
    }

    public int count( IndexHits<Node> indexHits )
    {
        return destructivelyCount( indexHits );
    }

    public int destructivelyCount( Iterable<?> iterable )
    {
        int count = 0;

        for ( @SuppressWarnings("unused") Object o : iterable )
        {
            count++;
        }

        return count;
    }
}
