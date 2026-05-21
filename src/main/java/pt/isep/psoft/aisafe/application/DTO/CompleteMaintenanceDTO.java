package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.NotBlank;

public record CompleteMaintenanceDTO(

        @NotBlank(message = "Completion notes cannot be blank.")
        String completionNotes
) {}