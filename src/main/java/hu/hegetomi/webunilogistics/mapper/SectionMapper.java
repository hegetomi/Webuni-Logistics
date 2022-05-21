package hu.hegetomi.webunilogistics.mapper;

import hu.hegetomi.webunilogistics.dto.AddressDto;
import hu.hegetomi.webunilogistics.dto.SectionDto;
import hu.hegetomi.webunilogistics.model.Address;
import hu.hegetomi.webunilogistics.model.Section;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    List<SectionDto> addressesToDtos(List<Section> addresses);

    List<Section> dtosToAddresses(List<SectionDto> addressDtos);

    SectionDto addressToDto(Section address);

    Section dtoToAddress(SectionDto addressDto);


}
