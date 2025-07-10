package tn.gti.E_Transfert.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.gti.E_Transfert.dto.request.UserRequestDTO;
import tn.gti.E_Transfert.dto.response.UserResponseDTO;
import tn.gti.E_Transfert.entity.User;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setAmbiguityIgnored(true)
                .setSkipNullEnabled(true);

        // Custom mapping for User role
        mapper.typeMap(UserRequestDTO.class, User.class)
                .addMapping(UserRequestDTO::getRole, User::setRole);

        // Ensure profilePhotoPath is mapped
        mapper.typeMap(User.class, UserResponseDTO.class)
                .addMapping(User::getProfilePhotoPath, UserResponseDTO::setProfilePhotoPath);

        return mapper;
    }
}