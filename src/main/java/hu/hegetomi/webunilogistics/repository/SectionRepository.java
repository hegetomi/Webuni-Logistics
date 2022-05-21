package hu.hegetomi.webunilogistics.repository;

import hu.hegetomi.webunilogistics.model.Section;
import hu.hegetomi.webunilogistics.model.TransportPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {


    @Query("Select s from Section s where s.transportPlan=:id")
    List<Section> findAllWhereTransportPlanEquals(TransportPlan id);

}
