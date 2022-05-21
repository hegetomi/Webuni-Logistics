package hu.hegetomi.webunilogistics.service;

import hu.hegetomi.webunilogistics.model.Address;

import hu.hegetomi.webunilogistics.model.Address_;
import org.springframework.data.jpa.domain.Specification;

public class AddressSpecifications {

    public static Specification<Address> hasCountryCode(String countryCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Address_.iso), countryCode);
    }

    public static Specification<Address> hasCity(String city) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Address_.city)), (city + "%").toLowerCase());
    }

    public static Specification<Address> hasStreet(String street) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Address_.street)), (street + "%").toLowerCase());
    }

    public static Specification<Address> hasZipCode(String zipCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Address_.zipCode), zipCode);
    }

}
