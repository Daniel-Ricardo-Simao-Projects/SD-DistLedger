package pt.tecnico.distledger.namingserver.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import java.util.List;

public class NamingServerState {

    private Map<String, ServiceEntry> servicesMap;

    public NamingServerState() {
        this.servicesMap = new ConcurrentHashMap<>();
    }
    public boolean register(String service, String qualifier, String target) {
        servicesMap.putIfAbsent(service, new ServiceEntry(service));

        ServiceEntry serviceEntry = servicesMap.get(service);
        if (serviceEntry.getServerEntries().stream()
                .anyMatch(se -> se.getQualifier().equals(qualifier))) {
            return false;
        }

        serviceEntry.addServerEntry(new ServerEntry(target, qualifier));
        return true;
    }

    public List<ServerEntry> lookup(String service, String qualifier) {
        return servicesMap.get(service).getServerEntries().stream()
                .filter(serverEntry -> serverEntry.getQualifier().equals(qualifier) || qualifier.isEmpty())
                .toList();
    }

    public boolean delete(String service, String target) {
        ServiceEntry serviceEntry = servicesMap.get(service);
        Optional<ServerEntry> serverEntryOptional = serviceEntry.getServerEntries().stream()
                .filter(se -> se.getTarget().equals(target))
                .findFirst();
        if (serverEntryOptional.isPresent()) {
            serviceEntry.getServerEntries().remove(serverEntryOptional.get());
            return true;
        }
        return false;
    }

}
