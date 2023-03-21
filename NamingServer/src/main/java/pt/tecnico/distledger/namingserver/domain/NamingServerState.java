package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.namingServerExceptions.NotPossibleToRemoveServerException;
import pt.tecnico.distledger.namingserver.namingServerExceptions.QualifierAlreadyRegisteredException;
import pt.tecnico.distledger.namingserver.namingServerExceptions.TargetAlreadyRegisteredException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import java.util.List;

public class NamingServerState {

    private Map<String, ServiceEntry> servicesMap;

    public NamingServerState() {
        this.servicesMap = new ConcurrentHashMap<>();
    }

    public void register(String service, String qualifier, String target) throws QualifierAlreadyRegisteredException, TargetAlreadyRegisteredException {
        servicesMap.putIfAbsent(service, new ServiceEntry(service));

        ServiceEntry serviceEntry = servicesMap.get(service);
        if (serviceEntry.getServerEntries().stream()
                .anyMatch(se -> se.getQualifier().equals(qualifier))) {
            throw new QualifierAlreadyRegisteredException();
        }
        else if(serviceEntry.getServerEntries().stream()
                .anyMatch(se -> se.getTarget().equals(target))) {
            throw new TargetAlreadyRegisteredException();
        }

        serviceEntry.addServerEntry(new ServerEntry(target, qualifier));
    }

    public List<ServerEntry> lookup(String service, String qualifier) {
        return servicesMap.get(service).getServerEntries().stream()
                .filter(serverEntry -> serverEntry.getQualifier().equals(qualifier) || qualifier.isEmpty())
                .toList();
    }

    public void delete(String service, String target) throws NotPossibleToRemoveServerException {
        ServiceEntry serviceEntry = servicesMap.get(service);
        Optional<ServerEntry> serverEntryOptional = serviceEntry.getServerEntries().stream()
                .filter(se -> se.getTarget().equals(target))
                .findFirst();
        if (serverEntryOptional.isPresent()) {
            serviceEntry.getServerEntries().remove(serverEntryOptional.get());
        }
        else {
            throw new NotPossibleToRemoveServerException();
        }
    }

}
