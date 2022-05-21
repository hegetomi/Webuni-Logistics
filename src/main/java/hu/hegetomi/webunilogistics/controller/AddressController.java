package hu.hegetomi.webunilogistics.controller;

import hu.hegetomi.webunilogistics.dto.AddressDto;
import hu.hegetomi.webunilogistics.dto.AddressSearchDto;
import hu.hegetomi.webunilogistics.mapper.AddressMapper;
import hu.hegetomi.webunilogistics.model.Address;
import hu.hegetomi.webunilogistics.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    AddressMapper addressMapper;

    private Logger logger = LoggerFactory.getLogger("AddressController.class");

    @PostMapping()
    public AddressDto save(@RequestBody @Valid AddressDto addressDto) {
        if (addressDto == null || addressDto.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        logger.warn(addressDto.getCity());
        logger.warn(addressMapper.MAPPER.dtoToAddress(addressDto).getCity());

        return addressMapper.MAPPER.addressToDto(addressService.save(addressMapper.MAPPER.dtoToAddress(addressDto)));
    }

    @GetMapping
    public List<AddressDto> findAll() {
        return addressMapper.MAPPER.addressesToDtos(addressService.findAll());
    }

    @GetMapping("/{id}")
    public AddressDto findSpecific(@PathVariable long id) {
        return addressMapper.MAPPER.addressToDto(addressService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        addressService.deleteById(id);
    }

    @PutMapping("/{id}")
    public AddressDto modifyAddress(@PathVariable long id, @RequestBody @Valid AddressDto dto) {
        if (id != dto.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return addressMapper.MAPPER.addressToDto(addressService.modifyById(addressMapper.MAPPER.dtoToAddress(dto))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping(value = "/search")
    public ResponseEntity<List<AddressDto>> searchByExample(@RequestBody AddressSearchDto searchDto,
                                                            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                            @RequestParam(value = "size", required = false) Integer size,
                                                            @RequestParam(value = "sort", required = false, defaultValue = "id,asc") String sort) {

        if (searchDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (size == null) {
            size = Integer.MAX_VALUE;
        }


        String sortBy;
        String[] sortParam = sort.split(",");
        Sort order;

        if (!"".equals(sortParam[0])) {
            sortBy = sortParam[0];
            if ("desc".equals(sortParam[1])) {
                order = Sort.by(sortBy).descending();
            } else {
                order = Sort.by(sortBy).ascending();
            }
        } else {
            if ("desc".equals(sortParam[1])) {
                order = Sort.by("id").descending();
            } else {
                order = Sort.by("id").ascending();
            }
        }

        Pageable pageable = PageRequest.of(page, size).withSort(order);
        Page<Address> pages = addressService.findByExample(searchDto, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pages.getTotalElements()))
                .body(addressMapper.MAPPER.addressesToDtosNoMilestone(pages.getContent()));
    }

}
