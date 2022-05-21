package hu.hegetomi.webunilogistics.service;

import hu.hegetomi.webunilogistics.config.LogisticsConfigProperties;
import hu.hegetomi.webunilogistics.dto.MilestoneDelayDto;
import hu.hegetomi.webunilogistics.model.Milestone;
import hu.hegetomi.webunilogistics.model.Section;
import hu.hegetomi.webunilogistics.model.TransportPlan;
import hu.hegetomi.webunilogistics.repository.MilestoneRepository;
import hu.hegetomi.webunilogistics.repository.SectionRepository;
import hu.hegetomi.webunilogistics.repository.TransportPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransportPlanService {

    @Autowired
    LogisticsConfigProperties logisticsConfigProperties;

    @Autowired
    TransportPlanRepository transportPlanRepository;

    @Autowired
    MilestoneRepository milestoneRepository;

    @Autowired
    SectionRepository sectionRepository;

    private final Logger logger = LoggerFactory.getLogger("TransportPlanService.class");

    @Transactional
    public void delayTransportPlan(Long transportPlanId, MilestoneDelayDto milestoneDelayDto) {
        Optional<TransportPlan> transportPlanOpt = transportPlanRepository.findById(transportPlanId);
        Optional<Milestone> milestoneOptional = milestoneRepository.findById(milestoneDelayDto.getMilestoneId());
        if (transportPlanOpt.isEmpty() || milestoneOptional.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            TransportPlan transportPlan = transportPlanOpt.get();
            List<Section> ofThisPlan = sectionRepository.findAllWhereTransportPlanEquals(transportPlan);
            Long delayMilestoneId = milestoneDelayDto.getMilestoneId();
            ofThisPlan.sort(Comparator.comparing(Section::getOrdinal));
            Optional<Section> hasSectionWithMilestone = ofThisPlan.stream().filter(e ->
                    (e.getFromMilestone().getId() == delayMilestoneId
                            || e.getToMilestone().getId() == delayMilestoneId)).findFirst();
            if (hasSectionWithMilestone.isEmpty()) {
                throw new InvalidParameterException();
            } else {
                Section e = hasSectionWithMilestone.get();
                if (e.getFromMilestone().getId() == delayMilestoneId) {
                    incrementIfFirstSelected(e, milestoneDelayDto);
                } else {
                    incrementIfSecondSelected(milestoneDelayDto, ofThisPlan, e);
                }
                calculatePenalty(milestoneDelayDto, transportPlan);
            }
        }

    }

    private void calculatePenalty(MilestoneDelayDto milestoneDelayDto, TransportPlan transportPlan) {
        Integer penaltyPercent = null;
        for (Map.Entry<Integer, Integer> entry : logisticsConfigProperties.getTransportDelay().getPenalty().entrySet()) {
            if (entry.getKey() <= milestoneDelayDto.getDelayMinutes()) {
                penaltyPercent = entry.getValue();
            } else {
                break;
            }
        }
        if (penaltyPercent != null) {
            logger.warn(penaltyPercent.toString());
            logger.warn(String.valueOf(transportPlan.getProjectedIncome()));
            transportPlan.setProjectedIncome(transportPlan.getProjectedIncome() - transportPlan.getProjectedIncome() * penaltyPercent / 100);
            logger.warn(String.valueOf(transportPlan.getProjectedIncome()));
        }
    }

    private void incrementIfSecondSelected(MilestoneDelayDto milestoneDelayDto, List<Section> ofThisPlan, Section e) {
        e.getToMilestone().setPlannedTime(e.getToMilestone().getPlannedTime().plusMinutes(milestoneDelayDto.getDelayMinutes()));
        if (e.getOrdinal() != ofThisPlan.size() - 1) {
            Section f = ofThisPlan.get((int) (e.getOrdinal() + 1));
            f.getFromMilestone().setPlannedTime(f.getFromMilestone().getPlannedTime().plusMinutes(milestoneDelayDto.getDelayMinutes()));
        }
    }

    private void incrementIfFirstSelected(Section e, MilestoneDelayDto milestoneDelayDto) {
        e.getFromMilestone().setPlannedTime(
                e.getFromMilestone()
                        .getPlannedTime()
                        .plusMinutes(milestoneDelayDto.getDelayMinutes()));
        e.getToMilestone().setPlannedTime(
                e.getToMilestone()
                        .getPlannedTime()
                        .plusMinutes(milestoneDelayDto.getDelayMinutes())
        );
    }

}
