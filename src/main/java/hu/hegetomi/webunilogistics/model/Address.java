package hu.hegetomi.webunilogistics.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Address {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "address")
    private List<Milestone> milestones;

    private String iso;
    private String city;
    private String street;
    private String zipCode;
    private String houseNr;
    private float latitude;
    private float longitude;

}
