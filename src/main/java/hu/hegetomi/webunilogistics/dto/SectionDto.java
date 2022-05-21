package hu.hegetomi.webunilogistics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SectionDto {

    private long id;
    private MilestoneDto fromMilestone;
    private MilestoneDto toMilestone;
    private TransportPlanDto transportPlan;
    private long ordinal;

}
