package pt.isep.psoft.aisafe.api;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.AirportViewDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private final AirportService service;

    public AirportController(AirportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntityModel<AirportViewDTO>> createAirport(@RequestBody RegisterAirportDTO dto) {
        // 1. Chama o serviço para criar o aeroporto e as pistas
        AirportViewDTO created = service.registerAirport(dto);

        // 2. Cria o modelo HATEOAS para a resposta
        EntityModel<AirportViewDTO> resource = EntityModel.of(created);

        // 3. Adiciona o link "self" (obrigatório para nota máxima na Fase 1)
        resource.add(linkTo(methodOn(AirportController.class).createAirport(dto)).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }
}