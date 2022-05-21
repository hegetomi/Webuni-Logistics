package hu.hegetomi.webunilogistics.controller;

import hu.hegetomi.webunilogistics.dto.AddressDto;
import hu.hegetomi.webunilogistics.dto.LoginDto;
import hu.hegetomi.webunilogistics.mapper.AddressMapper;
import hu.hegetomi.webunilogistics.model.Address;
import hu.hegetomi.webunilogistics.repository.AddressRepository;
import hu.hegetomi.webunilogistics.security.JwtLoginController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class AddressControllerTest {
    private static final String BASE_URI = "/api/addresses";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    AddressMapper addressMapper;

    @Autowired
    JwtLoginController jwtLoginController;

    String token;


    @BeforeEach
    void init() {
        addressRepository.deleteAll();
        token = jwtLoginController.login(LoginDto.builder().username("addressManager").password("pass").build());
    }

    @Test
    void testPostAddressWithId() {
        AddressDto dto = AddressDto.builder()
                .id(1L)
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .latitude(40.0f)
                .longitude(50.0f)
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testPostAddressWithEmpty() {
        AddressDto dto = new AddressDto();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testPostAddressWithEmptyCity() {
        AddressDto dto = AddressDto.builder()
                .city("")
                .iso("HU")
                .houseNr("15/A")
                .latitude(40.0f)
                .longitude(50.0f)
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testPostAddressWithNullStreet() {
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .latitude(40.0f)
                .longitude(50.0f)
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testPostAddressWithGoodStats() {
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .latitude(40.0f)
                .longitude(50.0f)
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isOk();
    }

    @Test
    void testPostAddressWithoutLatLong() {
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isOk();
    }

    @Test
    void testGetAllAddresses(){
        webTestClient.get().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).exchange().expectStatus().isOk();
    }

    @Test
    void testPutAddressAcceptable(){
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        Address address = addressRepository.save(addressMapper.MAPPER.dtoToAddress(dto));
        dto = AddressDto.builder()
                .id(address.getId())
                .city("Nyíregyháza")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.put().uri(BASE_URI+ "/" + dto.getId()).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isOk();
        Address changedAddress = addressRepository.findById(address.getId()).get();
        assertThat(changedAddress.getCity()).isEqualTo(dto.getCity());
    }
    @Test
    void testPutAddressInvalidId(){
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();

        Address address = addressRepository.save(addressMapper.MAPPER.dtoToAddress(dto));
        dto = AddressDto.builder()
                .id(address.getId())
                .city("Nyíregyháza")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.put().uri(BASE_URI+ "/" + 2).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }

    @Test
    void testPutAddressBadBody(){
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        Address address = addressRepository.save(addressMapper.MAPPER.dtoToAddress(dto));
        dto = AddressDto.builder()
                .id(address.getId())
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.put().uri(BASE_URI+ "/" + dto.getId()).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isBadRequest();
    }
    @Test
    void testPutAddressNotFound(){
        AddressDto dto = AddressDto.builder()
                .city("Budapest")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.post().uri(BASE_URI).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange();
        dto = AddressDto.builder()
                .id(5L)
                .city("Nyíregyháza")
                .iso("HU")
                .houseNr("15/A")
                .street("Szerémi")
                .zipCode("1111")
                .build();
        webTestClient.put().uri(BASE_URI+ "/" + 5).headers(e -> e.setBearerAuth(token)).bodyValue(dto).exchange().expectStatus().isNotFound();
    }
}
