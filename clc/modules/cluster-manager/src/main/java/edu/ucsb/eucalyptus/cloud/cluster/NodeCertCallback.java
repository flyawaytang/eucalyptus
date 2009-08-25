package edu.ucsb.eucalyptus.cloud.cluster;

import edu.ucsb.eucalyptus.msgs.*;

import com.eucalyptus.config.ClusterConfiguration;
import com.eucalyptus.ws.client.Client;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class NodeCertCallback extends QueuedEventCallback<GetKeysType> implements Runnable {

  private static Logger LOG = Logger.getLogger( NodeCertCallback.class );

  private static int SLEEP_TIMER = 30 * 1000;
  private NavigableSet<GetKeysType> requests = null;
  private NavigableSet<NodeCertInfo> results = null;

  public NodeCertCallback( ClusterConfiguration config ) {
    super( config );
    this.requests = new ConcurrentSkipListSet<GetKeysType>();
    this.results = new ConcurrentSkipListSet<NodeCertInfo>();
  }

  public void process( final Client cluster, final GetKeysType msg ) throws Exception {
    try {
      GetKeysResponseType reply = ( GetKeysResponseType ) cluster.send( msg );
      NodeCertInfo certInfo = reply.getCerts();
      if ( certInfo != null ) {
        certInfo.setServiceTag( certInfo.getServiceTag().replaceAll( "EucalyptusGL", "EucalyptusNC" ) );
        if ( certInfo.getCcCert() != null && certInfo.getCcCert().length() > 0 ) {
          certInfo.setCcCert( new String( Base64.decode( certInfo.getCcCert() ) ) );
        }
        if ( certInfo.getNcCert() != null && certInfo.getNcCert().length() > 0 ) {
          certInfo.setNcCert( new String( Base64.decode( certInfo.getNcCert() ) ) );
        }
        results.add( certInfo );
      }
      requests.remove( msg );
    }
    catch ( Exception e ) {
      LOG.error( e );
    }
  }

  @Override
  public void notifyHandler() {
    if ( requests.isEmpty() ) {
      super.notifyHandler();
    }
  }

  public void run() {
//TODO: finish up here.
//    do {
//      if ( !this.parent.getNodeTags().isEmpty() ) {
//        LOG.debug( "Querying all known service tags:" );
//        for ( String serviceTag : this.parent.getNodeTags() ) {
//          LOG.debug( "- " + serviceTag );
//          GetKeysType msg = new GetKeysType( serviceTag.replaceAll( "EucalyptusNC", "EucalyptusGL" ) );
//          this.requests.add( msg );
//          this.parent.getMessageQueue().enqueue( new QueuedLogEvent( this, msg ) );
//        }
//        this.waitForEvent();
//        this.parent.updateNodeCerts( results );
//        this.results.clear();
//      }
//    } while ( !this.isStopped() && this.sleep( SLEEP_TIMER ) );
  }

}
