package pt.tecnico.distledger.namingserver.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    System.out.println("Already exists");
                    return false;
                }
            }
            ServerEntry serverEntry = new ServerEntry(target, qualifier);
            serviceEntry.addServerEntry(serverEntry);
        }
        return true;
    }

    public Boolean delete(String service, String target) {
        ServiceEntry serviceEntry = servicesMap.get(service);
        for(ServerEntry se : serviceEntry.getServerEntries()) {
            if(se.getTarget().equals(target)) {
                serviceEntry.getServerEntries().remove(se);
                return true;
            }
        }

        return false;
    }

    public List<ServerEntry> lookup(String service, String qualifier) {
        return servicesMap.get(service).getServerEntries().stream()
                .filter(serverEntry -> serverEntry.getQualifier().equals(qualifier) || qualifier.isEmpty())
                .toList();
    }

    public void Print() {
        System.out.println(servicesMap.toString());
    }
}