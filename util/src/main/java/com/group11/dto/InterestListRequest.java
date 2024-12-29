package com.group11.dto;

import lombok.Data;
import java.util.List;

@Data
public class InterestListRequest {
    private String userId;
    private List<String> timeSlotIds;
}
