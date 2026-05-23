package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {


    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository modelRepository;


    @InjectMocks
    private AircraftService aircraftService;

    @Test
    void shouldSearchAircraftsByManufacturingYear() {

        Aircraft mockAircraft = mock(Aircraft.class);
        AircraftModel mockModel = mock(AircraftModel.class);


        when(mockAircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(mockAircraft.getManufacturingDate()).thenReturn(LocalDate.of(2020, 1, 1));
        when(mockAircraft.getActualSeatingCapacity()).thenReturn(180);
        when(mockAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);
        when(mockAircraft.getAircraftModel()).thenReturn(mockModel);

        when(mockModel.getModelName()).thenReturn(new ModelName("A320"));
        when(mockModel.getModelPhotoUrl()).thenReturn("url");


        when(aircraftRepository.findByManufacturingYear(2020)).thenReturn(List.of(mockAircraft));


        List<AircraftViewDTO> results = aircraftService.searchAircrafts(null, null, 2020);


        assertEquals(1, results.size());
        assertEquals("CS-TPA", results.get(0).registrationNumber());
        assertEquals("A320", results.get(0).modelName());


        verify(aircraftRepository, times(1)).findByManufacturingYear(2020);
    }
}