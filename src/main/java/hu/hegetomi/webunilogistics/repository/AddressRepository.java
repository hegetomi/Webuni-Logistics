package hu.hegetomi.webunilogistics.repository;

import hu.hegetomi.webunilogistics.model.Address;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

    @EntityGraph(attributePaths = "milestones")
    @Query("Select a from Address a")
    List<Address> findAllWithMilestones();

    @EntityGraph(attributePaths = "milestones")
    @Query("Select a from Address a where a.id=:id")
    Optional<Address> findSpecificWithMilestones(Long id);

}
