package com.kbs.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopWeekdayAvgExpenseDTO {
    /** MONDAY~SUNDAY (Korean label recommended for UI) */
    private String weekday;

    /** average expense amount for that weekday within the requested period */
    private double avgAmount;
}
