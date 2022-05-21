package hu.hegetomi.webunilogistics.mapper;

import hu.hegetomi.webunilogistics.dto.MilestoneDto;
import hu.hegetomi.webunilogistics.model.Milestone;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {

    List<MilestoneDto> addressesToDtos(List<Milestone> addresses);

    List<Milestone> dtosToAddresses(List<MilestoneDto> addressDtos);

    MilestoneDto addressToDto(Milestone address);

    Milestone dtoToAddress(MilestoneDto addressDto);

}
