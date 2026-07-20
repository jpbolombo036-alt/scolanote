package com.bulletin.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTeacherRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long teacherId;
}
