package hu.hegetomi.webunilogistics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TransportPlanDto {

    private long id;

    private long projectedIncome;
    private List<SectionDto> sections;

}
