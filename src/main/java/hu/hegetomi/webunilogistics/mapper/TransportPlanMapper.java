package hu.hegetomi.webunilogistics.mapper;

import hu.hegetomi.webunilogistics.dto.TransportPlanDto;
import hu.hegetomi.webunilogistics.model.TransportPlan;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransportPlanMapper {

    List<TransportPlanDto> addressesToDtos(List<TransportPlan> addresses);

    List<TransportPlan> dtosToAddresses(List<TransportPlanDto> addressDtos);

    TransportPlanDto addressToDto(TransportPlan address);

    TransportPlan dtoToAddress(TransportPlanDto addressDto);

}
