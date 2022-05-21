package hu.hegetomi.webunilogistics.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AddressDto {

    private Long id;

    @Size(max = 2)
    @NotEmpty
    private String iso;
    @NotEmpty
    private String city;
    @NotEmpty
    private String street;
    @NotEmpty
    private String zipCode;
    @NotEmpty
    private String houseNr;

    private float latitude;
    private float longitude;
    private List<MilestoneDto> milestones;
}
