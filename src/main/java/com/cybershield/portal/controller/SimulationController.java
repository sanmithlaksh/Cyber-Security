package com.cybershield.portal.controller;

import com.cybershield.portal.model.User;
import com.cybershield.portal.service.AuthService;
import com.cybershield.portal.service.DijkstraService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
public class SimulationController {

    private final DijkstraService dijkstraService;
    private final AuthService authService;

    public SimulationController(DijkstraService dijkstraService, AuthService authService) {
        this.dijkstraService = dijkstraService;
        this.authService = authService;
    }

    /**
     * Renders the Cyber Attack Simulation Lab for administrators.
     */
    @GetMapping("/admin/simulation")
    public String showSimulationLab(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("user", user);
        return "admin/simulation-lab";
    }

    /**
     * Dijkstra pathfinder API.
     */
    @PostMapping("/api/simulation/dijkstra")
    @ResponseBody
    public ResponseEntity<?> calculateAttackPath(@RequestBody Map<String, String> payload) {
        String start = payload.get("startNode");
        String end = payload.get("endNode");

        if (start == null || end == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "startNode and endNode parameters are required."));
        }

        DijkstraService.PathResult result = dijkstraService.findShortestPath(start, end);
        return ResponseEntity.ok(Map.of(
                "path", result.getPath(),
                "totalWeight", result.getTotalWeight()
        ));
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        return authService.findUserByEmail(principal.getName()).orElse(null);
    }
}
