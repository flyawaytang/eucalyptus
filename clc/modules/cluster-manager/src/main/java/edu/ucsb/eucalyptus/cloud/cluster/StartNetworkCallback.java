package edu.ucsb.eucalyptus.cloud.cluster;

import edu.ucsb.eucalyptus.cloud.*;
import edu.ucsb.eucalyptus.msgs.*;

import com.eucalyptus.config.ClusterConfiguration;
import com.eucalyptus.ws.client.Client;

import org.apache.log4j.Logger;

public class StartNetworkCallback extends QueuedEventCallback<StartNetworkType> {

  private static Logger LOG = Logger.getLogger( StartNetworkCallback.class );

  private ClusterAllocator parent;
  private NetworkToken networkToken;

  public StartNetworkCallback( final ClusterConfiguration clusterConfig, final ClusterAllocator parent, final NetworkToken networkToken ) {
    super(clusterConfig);
    this.parent = parent;
    this.networkToken = networkToken;
  }

  public void process( final Client clusterClient, final StartNetworkType msg ) throws Exception {
    //:: create the rollback message in case things fail later :://
    parent.msgMap.put( ClusterAllocator.State.ROLLBACK, new QueuedEvent<StopNetworkType>( new StopNetworkCallback( this.getConfig( ), networkToken ), new StopNetworkType( msg ) ) );
    try {
      StartNetworkResponseType reply = ( StartNetworkResponseType ) clusterClient.send( msg );
      if ( !reply.get_return() ) {
        this.parent.getRollback().lazySet( true );
        this.parent.returnAllocationTokens();
      }
    }
    catch ( Exception e ) {
      this.parent.getRollback().lazySet( true );
      this.parent.returnAllocationTokens();
    }
  }

}
