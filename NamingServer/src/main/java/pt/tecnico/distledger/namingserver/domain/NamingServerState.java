package pt.tecnico.distledger.namingserver.domain;

import java.util.HashMap;
import java.util.Map;

public class NamingServerState {

    private Map<String, ServiceEntry> servicesMap;

    public NamingServerState() {
        this.servicesMap = new HashMap<String, ServiceEntry>();
    }
    
}