package hu.hegetomi.webunilogistics.service;

import hu.hegetomi.webunilogistics.dto.AddressSearchDto;
import hu.hegetomi.webunilogistics.model.Address;
import hu.hegetomi.webunilogistics.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    @Autowired
    AddressRepository addressRepository;

    private Logger logger = LoggerFactory.getLogger(AddressService.class);

    @Transactional
    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public List<Address> findAll() {
        return addressRepository.findAllWithMilestones();
    }

    public Optional<Address> findById(long id) {
        return addressRepository.findSpecificWithMilestones(id);
    }

    @Transactional
    public void deleteById(long id) {
        if (addressRepository.findById(id).isPresent())
            addressRepository.deleteById(id);
    }

    @Transactional
    public Optional<Address> modifyById(Address address) {
        if (addressRepository.findById(address.getId()).isPresent())
            return Optional.of(addressRepository.save(address));
        return Optional.empty();
    }

    public Page<Address> findByExample(AddressSearchDto searchDto,
                                       @PageableDefault(sort = "id", direction = Sort.Direction.ASC, value = Integer.MAX_VALUE) Pageable pageable) {

        String iso = searchDto.getIso();
        String city = searchDto.getCity();
        String street = searchDto.getStreet();
        String zip = searchDto.getZipCode();

        logger.warn(iso);
        logger.warn(city);
        logger.warn(street);
        logger.warn(zip);

        Specification<Address> spec = Specification.where(null);

        if (StringUtils.hasText(iso)) {
            spec = spec.and(AddressSpecifications.hasCountryCode(iso));
        }
        if (StringUtils.hasText(city)) {
            spec = spec.and(AddressSpecifications.hasCity(city));
        }
        if (StringUtils.hasText(street)) {
            spec = spec.and(AddressSpecifications.hasStreet(street));
        }
        if (StringUtils.hasText(zip)) {
            spec = spec.and(AddressSpecifications.hasZipCode(zip));
        }

        return addressRepository.findAll(spec, pageable);

    }

}

