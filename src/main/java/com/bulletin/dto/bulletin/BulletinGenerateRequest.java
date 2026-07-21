package com.bulletin.dto.bulletin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinGenerateRequest {

    @NotNull
    private Long classroomId;

    @NotNull
    private Long periodId;
}
