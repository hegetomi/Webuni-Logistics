package hu.hegetomi.webunilogistics.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class AddressSearchDto {

    private String iso;
    private String city;
    private String street;
    private String zipCode;

}
