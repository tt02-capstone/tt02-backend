package com.nus.tt02backend.dto;

import com.nus.tt02backend.models.enums.RoomTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.time.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRoomCountResponse {

    private String accommodation_name;
    private LocalDate date;
    private RoomTypeEnum type;
    private Integer count;
}
