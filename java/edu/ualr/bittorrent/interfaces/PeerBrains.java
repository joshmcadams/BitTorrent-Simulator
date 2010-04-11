package edu.ualr.bittorrent.interfaces;

import java.util.List;
import java.util.Map;

import com.sun.tools.javac.util.Pair;

public interface PeerBrains {
  public void setLocalPeer(Peer lcoal);

  public void setActivePeers(Map<Peer, PeerState> activePeers);

  public void setMetainfo(Metainfo metainfo);

  public void setData(Map<Integer, byte[]> data);

  public List<Pair<Peer, Message>> getMessagesToDispatch();
}
