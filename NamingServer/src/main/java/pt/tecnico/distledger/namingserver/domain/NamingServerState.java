package pt.tecnico.distledger.namingserver.domain;

import java.util.HashMap;
import java.util.Map;

public class NamingServerState {

    private Map<String, ServiceEntry> servicesMap;

    public NamingServerState() {
        this.servicesMap = new HashMap<String, ServiceEntry>();
    }

    public Boolean register(String service, String qualifier, String target) {
        if(!servicesMap.containsKey(service)) {
            ServiceEntry serviceEntry = new ServiceEntry(service);
            ServerEntry serverEntry = new ServerEntry(target, qualifier);
            serviceEntry.addServerEntry(serverEntry);
            servicesMap.put(service, serviceEntry);
        } else {
            ServiceEntry serviceEntry = servicesMap.get(service);
            for(ServerEntry se : serviceEntry.getServerEntries()) {
                if(se.getQualifier().equals(qualifier)) {
                    System.out.println("Ja existe");
                    return false;
                }
            }
            ServerEntry serverEntry = new ServerEntry(target, qualifier);
            serviceEntry.addServerEntry(serverEntry);
        }
        return true;
    }

    public void Print() {
        System.out.println(servicesMap.toString());
    }
}