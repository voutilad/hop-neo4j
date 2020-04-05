package org.neo4j.hop.entries.check;

import org.apache.hop.job.entry.IJobEntry;
import org.apache.hop.metastore.persist.MetaStoreFactory;
import org.neo4j.hop.shared.MetaStoreUtil;
import org.neo4j.driver.Session;
import org.neo4j.hop.core.Neo4jDefaults;
import org.neo4j.hop.shared.DriverSingleton;
import org.neo4j.hop.shared.NeoConnection;
import org.apache.hop.cluster.SlaveServer;
import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.JobEntry;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.job.entry.JobEntryBase;
import org.apache.hop.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@JobEntry(
  id = "NEO4J_CHECK_CONNECTIONS",
  name = "Check Neo4j Connections",
  description = "Check to see if we can connecto to the listed Neo4j databases",
  image = "neo4j_check.svg",
  categoryDescription = "i18n:org.apache.hop.job:JobCategory.Category.Conditions",
  documentationUrl = "https://github.com/knowbi/knowbi-pentaho-pdi-neo4j-output/wiki/"
)
public class CheckConnections extends JobEntryBase implements IJobEntry {

  private List<String> connectionNames;

  public CheckConnections() {
    this.connectionNames = new ArrayList<>();
  }

  public CheckConnections( String name ) {
    this( name, "" );
  }

  public CheckConnections( String name, String description ) {
    super( name, description );
    connectionNames = new ArrayList<>();
  }

  @Override public String getXML() {
    StringBuilder xml = new StringBuilder();
    // Add entry name, type, ...
    //
    xml.append( super.getXML() );

    xml.append( XMLHandler.openTag( "connections" ) );

    for ( String connectionName : connectionNames ) {
      xml.append( XMLHandler.addTagValue( "connection", connectionName ) );
    }

    xml.append( XMLHandler.closeTag( "connections" ) );
    return xml.toString();
  }

  @Override public void loadXML( Node node, IMetaStore metaStore ) throws HopXMLException {

    super.loadXML( node );

    connectionNames = new ArrayList<>();
    Node connectionsNode = XMLHandler.getSubNode( node, "connections" );
    List<Node> connectionNodes = XMLHandler.getNodes( connectionsNode, "connection" );
    for ( Node connectionNode : connectionNodes ) {
      String connectionName = XMLHandler.getNodeValue( connectionNode );
      connectionNames.add( connectionName );
    }
  }


  @Override public Result execute( Result result, int nr ) throws HopException {

    try {
      metaStore = MetaStoreUtil.findMetaStore( this );
    } catch ( Exception e ) {
      throw new HopException( "Error finding metastore", e );
    }
    MetaStoreFactory<NeoConnection> connectionFactory = new MetaStoreFactory<>( NeoConnection.class, metaStore, Neo4jDefaults.NAMESPACE );

    // Replace variables & parameters
    //
    List<String> realConnectionNames = new ArrayList<>();
    for ( String connectionName : connectionNames ) {
      realConnectionNames.add( environmentSubstitute( connectionName ) );
    }

    // Check all the connections.  If any one fails, fail the step
    // Check 'm all though, report on all, nr of errors is nr of failed connections
    //
    int testCount = 0;
    for ( String connectionName : realConnectionNames ) {
      testCount++;
      try {
        NeoConnection connection = connectionFactory.loadElement( connectionName );
        if ( connection == null ) {
          throw new HopException( "Unable to find connection with name '" + connectionName + "'" );
        }
        connection.initializeVariablesFrom( this );

        Session session = connection.getSession( log );
        session.close();

      } catch ( Exception e ) {
        // Something bad happened, log the error, flag error
        //
        result.increaseErrors( 1 );
        result.setResult( false );
        logError( "Error on connection: " + connectionName, e );
      }
    }

    if ( result.getNrErrors() == 0 ) {
      logBasic( testCount + " Neo4j connections tested without error" );
    } else {
      logBasic( testCount + " Neo4j connections tested with " + result.getNrErrors() + " error(s)" );
    }

    return result;
  }

  @Override public String getDialogClassName() {
    return super.getDialogClassName();
  }

  /**
   * Gets connectionNames
   *
   * @return value of connectionNames
   */
  public List<String> getConnectionNames() {
    return connectionNames;
  }

  /**
   * @param connectionNames The connectionNames to set
   */
  public void setConnectionNames( List<String> connectionNames ) {
    this.connectionNames = connectionNames;
  }

  @Override public boolean evaluates() {
    return true;
  }

  @Override public boolean isUnconditional() {
    return false;
  }
}