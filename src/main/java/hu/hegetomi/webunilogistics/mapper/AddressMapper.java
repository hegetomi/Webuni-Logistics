package hu.hegetomi.webunilogistics.mapper;

import hu.hegetomi.webunilogistics.dto.AddressDto;
import hu.hegetomi.webunilogistics.model.Address;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressMapper MAPPER = Mappers.getMapper(AddressMapper.class);

    List<AddressDto> addressesToDtos(List<Address> addresses);

    @IterableMapping(qualifiedByName = "noMilestone")
    List<AddressDto> addressesToDtosNoMilestone(List<Address> addresses);

    @Named("noMilestone")
    @Mapping(target = "milestones", ignore = true)
    AddressDto addressToDtoNoMilestone(Address address);

    List<Address> dtosToAddresses(List<AddressDto> addressDtos);

    AddressDto addressToDto(Address address);

    Address dtoToAddress(AddressDto addressDto);

}
