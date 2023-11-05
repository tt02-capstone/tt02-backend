package com.nus.tt02backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryFriendResponse {
    private long user_id;
    private String email;
    private String name;
    private String profile_pic;
}
