package com.mikeshermandev.clientactivity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoredResponseDTO {
    public String activity;
    public String type;
    public int participants;
    public float price;
    public String link;
    public String key;
    public float accessibilty;
}
