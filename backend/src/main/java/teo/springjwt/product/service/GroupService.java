package teo.springjwt.product.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teo.springjwt.product.dto.CreateGroupDTO;
import teo.springjwt.product.dto.ResponseGroupDTO;
import teo.springjwt.product.dto.ResponseValueDTO;
import teo.springjwt.product.entity.OptionGroupEntity;
import teo.springjwt.product.repository.group.OptionGroupEntityRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

  private final OptionGroupEntityRepository groupRepository;


  public List<ResponseGroupDTO> getGroups() {
    List<OptionGroupEntity> all = groupRepository.findAll();
    return all.stream().map(ResponseGroupDTO::fromEntity).toList();
  }

  public List<ResponseValueDTO> findOptionValuesByOptionGroupId(Long id) {
    OptionGroupEntity group = groupRepository.findById(id)
                                             .orElseThrow(() -> new IllegalArgumentException("type not found with ID: " + id));
    return group.getOptionValues().stream().map(ResponseValueDTO::fromEntityFlat).toList();
  }

  public OptionGroupEntity createGroup(CreateGroupDTO dto) {
    if (groupRepository.existsByName(dto.getName())) {
      throw new IllegalArgumentException("type with name '" + dto.getName() + "' already exists.");
    }
    OptionGroupEntity group = new OptionGroupEntity(dto.getName(), dto.getDisplayOrder());
    groupRepository.save(group);

    return group;
  }

  public OptionGroupEntity deleteGroup(Long id) {
    OptionGroupEntity group = groupRepository.findById(id)
                                             .orElseThrow(() -> new IllegalArgumentException("type not found with ID: " + id));
    groupRepository.delete(group);
    return group;
  }


}
