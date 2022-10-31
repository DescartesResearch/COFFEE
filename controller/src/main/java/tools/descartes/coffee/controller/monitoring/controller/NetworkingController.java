package tools.descartes.coffee.controller.monitoring.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.database.networking.NetworkingService;
import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;
import tools.descartes.coffee.controller.procedure.collection.networking.RequestNetwork;
import tools.descartes.coffee.shared.NetworkingData;

@RestController
@RequestMapping("/network")
public class NetworkingController {

    private final NetworkingService networkingService;

    public NetworkingController(NetworkingService networkingService) {
        this.networkingService = networkingService;
    }

    public void store(NetworkTime networkTime) {
        this.networkingService.add(networkTime);
    }

    @PostMapping("/store")
    public void storeNetworkingTimes(@RequestBody NetworkingData networkingData) {
        NetworkTime networkTime = new NetworkTime(RequestNetwork.getNetworkingId(), networkingData);
        this.networkingService.add(networkTime);
    }
}
