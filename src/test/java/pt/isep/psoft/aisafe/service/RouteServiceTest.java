package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService; // Aqui usamos a classe real!

    // US228
    @Test
    void shouldExportRoutesToGeoJsonSuccessfully() {

        String geoJsonContent = routeService.exportRoutesAsGeoJson();

        assertNotNull(geoJsonContent, "O ficheiro gerado não pode ser nulo");
        assertFalse(geoJsonContent.isEmpty(), "O ficheiro não pode estar vazio");
        assertTrue(geoJsonContent.contains("FeatureCollection"), "Falta a tag FeatureCollection");
    }
}