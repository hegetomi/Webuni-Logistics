package hu.hegetomi.webunilogistics.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Milestone {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToOne
    private Section section;

    private LocalDateTime plannedTime;

}
