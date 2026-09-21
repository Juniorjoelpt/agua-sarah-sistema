package com.aguasarah.terceiros.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/terceiros/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardTerceiroController {

    private final DashboardTerceiroService dashboardTerceiroService;

    @GetMapping("/resumo")
    public DashboardTerceiroResumoDTO resumo() {
        return dashboardTerceiroService.gerarResumo();
    }
}
