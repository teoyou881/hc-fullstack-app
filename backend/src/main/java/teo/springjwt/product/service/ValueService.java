package teo.springjwt.product.service;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teo.springjwt.product.dto.CreateValueDTO;
import teo.springjwt.product.dto.ResponseValueDTO;
import teo.springjwt.product.entity.OptionGroupEntity;
import teo.springjwt.product.entity.OptionValueEntity;
import teo.springjwt.product.repository.group.OptionGroupEntityRepository;
import teo.springjwt.product.repository.value.OptionValueEntityRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ValueService {

  private final OptionValueEntityRepository valueRepository;
  private final OptionGroupEntityRepository groupRepository;

  public List<ResponseValueDTO> getValues() {
    List<OptionValueEntity> all = valueRepository.findAll();
    System.out.println("all = " + all);
    return all.stream().map(ResponseValueDTO::fromEntityFlat).toList();
  }

  public void createGroup(
      @Valid
      CreateValueDTO dto) {
    if (valueRepository.existsByName(dto.getName())) {
      throw new IllegalArgumentException("type with name '" + dto.getName() + "' already exists.");
    }
    OptionGroupEntity optionGroup = groupRepository
        .findById(dto.getOptionGroupId()).orElseThrow(() -> new IllegalArgumentException("no optionGroup with this id"));
    OptionValueEntity group = new OptionValueEntity(dto.getName(), optionGroup, dto.getDisplayOrder());
    valueRepository.save(group);
  }

  public OptionValueEntity deleteGroup(Long id) {
    OptionValueEntity group = valueRepository.findById(id)
                                             .orElseThrow(() -> new IllegalArgumentException("type not found with ID: " + id));
    valueRepository.delete(group);
    return group;
  }
}
