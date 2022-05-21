package hu.hegetomi.webunilogistics.service;

import hu.hegetomi.webunilogistics.config.LogisticsConfigProperties;
import hu.hegetomi.webunilogistics.dto.LoginDto;
import hu.hegetomi.webunilogistics.dto.MilestoneDelayDto;
import hu.hegetomi.webunilogistics.model.Address;
import hu.hegetomi.webunilogistics.model.Milestone;
import hu.hegetomi.webunilogistics.model.Section;
import hu.hegetomi.webunilogistics.model.TransportPlan;
import hu.hegetomi.webunilogistics.repository.AddressRepository;
import hu.hegetomi.webunilogistics.repository.MilestoneRepository;
import hu.hegetomi.webunilogistics.repository.SectionRepository;
import hu.hegetomi.webunilogistics.repository.TransportPlanRepository;
import hu.hegetomi.webunilogistics.security.JwtLoginController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

public class TransportPlanServiceTest {

    private static final String BASE_URI = "/api/transportplans";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    TransportPlanService transportPlanService;

    @Autowired
    TransportPlanRepository transportPlanRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    LogisticsConfigProperties logisticsConfigProperties;

    @Autowired
    JwtLoginController jwtLoginController;

    private final Logger logger = LoggerFactory.getLogger(TransportPlanServiceTest.class);


    @Test
    void testControllerStatus() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());
        Milestone mi7 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(59).build();
        MilestoneDelayDto norFoundDelayDto = MilestoneDelayDto.builder().milestoneId(1234568L).delayMinutes(59).build();
        MilestoneDelayDto badReqDelayDto = MilestoneDelayDto.builder().milestoneId(mi7.getId()).delayMinutes(59).build();
        MilestoneDelayDto negativeDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(-59).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());

        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(negativeDelayDto).exchange().expectStatus().isBadRequest();
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(norFoundDelayDto).exchange().expectStatus().isNotFound();
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(badReqDelayDto).exchange().expectStatus().isBadRequest();
        webTestClient.post().uri(BASE_URI + "/" + 9999 + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isNotFound();

        String invalidToken = jwtLoginController.login(LoginDto.builder().username("addressManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(invalidToken)).bodyValue(milestoneDelayDto).exchange().expectStatus().isForbidden();

    }

    @Test
    void testNoPenaltyFirstDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(29).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 1, 1, 30));
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 1, 30));
        assertThat(modMi3.getPlannedTime()).isEqualTo(mi3.getPlannedTime());
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000);
    }

    @Test
    void testNoPenaltySecondDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi2.getId()).delayMinutes(29).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(mi1.getPlannedTime());
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 1, 30));
        assertThat(modMi3.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 3, 1, 30));
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000);
    }

    @Test
    void testThirtyPenaltyFirstDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(30).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 1, 1, 31));
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 1, 31));
        assertThat(modMi3.getPlannedTime()).isEqualTo(mi3.getPlannedTime());
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(30);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testThirtyPenaltySecondDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi2.getId()).delayMinutes(30).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(mi1.getPlannedTime());
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 1, 31));
        assertThat(modMi3.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 3, 1, 31));
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(30);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testSixtyPenaltyFirstDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(70).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 1, 2, 11));
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 2, 11));
        assertThat(modMi3.getPlannedTime()).isEqualTo(mi3.getPlannedTime());
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(60);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testSixtyPenaltySecondDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi2.getId()).delayMinutes(75).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(mi1.getPlannedTime());
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 2, 16));
        assertThat(modMi3.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 3, 2, 16));
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(60);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testHundredTwentyPenaltyFirstDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi1.getId()).delayMinutes(120).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 1, 3, 1));
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 3, 1));
        assertThat(modMi3.getPlannedTime()).isEqualTo(mi3.getPlannedTime());
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(120);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testHundredTwentyPenaltySecondDelayed() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi2.getId()).delayMinutes(150).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(mi1.getPlannedTime());
        assertThat(modMi2.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 2, 3, 31));
        assertThat(modMi3.getPlannedTime()).isEqualTo(LocalDateTime.of(2000, 1, 3, 3, 31));
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime());
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(120);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

    @Test
    void testHundredTwentyPenaltySecondDelayedLastMilestone() {
        Address ad1 = addressRepository.save(
                Address.builder().street("Kaiser").city("Berlin").houseNr("40/a").iso("DE").zipCode("6045BE").build());
        Address ad2 = addressRepository.save(
                Address.builder().street("König").city("Karlsruhe").houseNr("16/2").iso("DE").zipCode("1601KA").build());
        Address ad3 = addressRepository.save(
                Address.builder().street("Ady Endre").city("Nyíregyháza").houseNr("26").iso("HU").zipCode("4400").build());
        Address ad4 = addressRepository.save(
                Address.builder().street("Váci").city("Budapest").houseNr("112").iso("HU").zipCode("1113").build());

        Milestone mi1 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 1, 1, 1))
                .address(ad1).build());
        Milestone mi2 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 2, 1, 1))
                .address(ad2).build());
        Milestone mi3 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 3, 1, 1))
                .address(ad3).build());
        Milestone mi4 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 4, 1, 1))
                .address(ad4).build());
        Milestone mi5 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 5, 1, 1))
                .address(ad1).build());
        Milestone mi6 = milestoneRepository.save(Milestone.builder()
                .plannedTime(LocalDateTime.of(2000, 1, 6, 1, 1))
                .address(ad2).build());

        TransportPlan tp1 = transportPlanRepository.save(TransportPlan.builder().projectedIncome(1000).build());

        Section se1 = sectionRepository.save(Section.builder().ordinal(0).fromMilestone(mi1).toMilestone(mi2).transportPlan(tp1).build());
        Section se2 = sectionRepository.save(Section.builder().ordinal(1).fromMilestone(mi3).toMilestone(mi4).transportPlan(tp1).build());
        Section se3 = sectionRepository.save(Section.builder().ordinal(2).fromMilestone(mi5).toMilestone(mi6).transportPlan(tp1).build());

        MilestoneDelayDto milestoneDelayDto = MilestoneDelayDto.builder().milestoneId(mi6.getId()).delayMinutes(150).build();

        String token = jwtLoginController.login(LoginDto.builder().username("transportManager").password("pass").build());
        webTestClient.post().uri(BASE_URI + "/" + tp1.getId() + "/delay").headers(e -> e.setBearerAuth(token)).bodyValue(milestoneDelayDto).exchange().expectStatus().isOk();

        Milestone modMi1 = milestoneRepository.findById(mi1.getId()).get();
        Milestone modMi2 = milestoneRepository.findById(mi2.getId()).get();
        Milestone modMi3 = milestoneRepository.findById(mi3.getId()).get();
        Milestone modMi4 = milestoneRepository.findById(mi4.getId()).get();
        Milestone modMi5 = milestoneRepository.findById(mi5.getId()).get();
        Milestone modMi6 = milestoneRepository.findById(mi6.getId()).get();
        TransportPlan modTp1 = transportPlanRepository.findById(tp1.getId()).get();

        assertThat(modMi1.getPlannedTime()).isEqualTo(mi1.getPlannedTime());
        assertThat(modMi2.getPlannedTime()).isEqualTo(mi2.getPlannedTime());
        assertThat(modMi3.getPlannedTime()).isEqualTo(mi3.getPlannedTime());
        assertThat(modMi4.getPlannedTime()).isEqualTo(mi4.getPlannedTime());
        assertThat(modMi5.getPlannedTime()).isEqualTo(mi5.getPlannedTime());
        assertThat(modMi6.getPlannedTime()).isEqualTo(mi6.getPlannedTime().plusMinutes(milestoneDelayDto.getDelayMinutes()));
        Integer penalty = logisticsConfigProperties.getTransportDelay().getPenalty().get(120);
        assertThat(modTp1.getProjectedIncome()).isEqualTo(1000 - 1000L * penalty / 100);
    }

}
