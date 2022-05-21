package hu.hegetomi.webunilogistics.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Entity
public class TransportPlan {

    @Id
    @GeneratedValue
    private long id;

    private long projectedIncome;

    @OneToMany(mappedBy = "transportPlan")
    private List<Section> sections;


}
