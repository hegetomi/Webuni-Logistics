package hu.hegetomi.webunilogistics.model;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Section {

    @Id
    @GeneratedValue
    private long id;

    @OneToOne
    //@JoinColumn(name = "milestone_id")
    private Milestone fromMilestone;

    @OneToOne
    //@JoinColumn(name = "milestone_id")
    private Milestone toMilestone;

    @ManyToOne
    private TransportPlan transportPlan;

    private long ordinal;

}
