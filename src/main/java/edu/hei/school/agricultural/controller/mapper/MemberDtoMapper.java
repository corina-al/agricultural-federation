package edu.hei.school.agricultural.controller.mapper;

import edu.hei.school.agricultural.controller.dto.CreateMemberDto;
import edu.hei.school.agricultural.entity.Collectivity;
import edu.hei.school.agricultural.entity.Member;
import edu.hei.school.agricultural.repository.CollectivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberDtoMapper {
    private final CollectivityRepository collectivityRepository;

    public Member mapToEntity(CreateMemberDto createMemberDto) {
        Optional<Collectivity> optionalCollectivity = collectivityRepository.findById(createMemberDto.collectivityIdentifier);
        if (optionalCollectivity.isEmpty()) {
            throw new RuntimeException("Collectivity not found");
        }
        var member = Member.builder()
                .firstName(createMemberDto.firstName)
                .lastName(createMemberDto.lastName)
                .birthDate(createMemberDto.birthDate)
                // etc
                .build();
        member.addCollectivity(optionalCollectivity.get());
        return member;
    }
}
