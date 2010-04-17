package edu.ualr.bittorrent.interfaces;

import java.util.List;
import java.util.Map;

import com.sun.tools.javac.util.Pair;

/**
 * While {@link Peer} objects encapsulate much of the mechanics behind the
 * BitTorrent protocol, the decision making in regards to which {@link Peer}s to
 * communicate with and what messages to send is better served in a separate
 * object. The PeerBrains are the decision centers for the {@link Peers}.
 */
public interface PeerBrains {
  /**
   * Let the brain know about the {@link Peer} that it is making decisions for.
   *
   * @param lcoal
   */
  public void setLocalPeer(Peer lcoal);

  /**
   * Share a map of known remote {@link Peer}s and their states with the brain.
   *
   * @param activePeers
   */
  public void setActivePeers(Map<Peer, PeerState> activePeers);

  /**
   * Let the brain know about the {@link Metainfo} that the torrent is based on.
   *
   * @param metainfo
   */
  public void setMetainfo(Metainfo metainfo);

  /**
   * The brain needs to know what data has been downloaded already.
   *
   * @param data
   */
  public void setData(Map<Integer, byte[]> data);

  /**
   * The brain gives the {@link Peer} a list of messages to dispatch to other
   * {@link Peer}s.
   *
   * @return
   */
  public List<Pair<Peer, Message>> getMessagesToDispatch();
}
