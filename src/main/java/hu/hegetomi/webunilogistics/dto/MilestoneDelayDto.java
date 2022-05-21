package hu.hegetomi.webunilogistics.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
public class MilestoneDelayDto {

    @NotNull
    private Long milestoneId;
    @NotNull
    @Positive
    private Integer delayMinutes;

}
