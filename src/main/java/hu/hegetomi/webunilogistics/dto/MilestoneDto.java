package hu.hegetomi.webunilogistics.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class MilestoneDto {

    private long id;
    private AddressDto address;
    private SectionDto section;
    private LocalDateTime plannedTime;

}
