package teo.springjwt.product.controller.admin;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teo.springjwt.product.dto.CreateValueDTO;
import teo.springjwt.product.dto.ResponseValueDTO;
import teo.springjwt.product.service.ValueService;

@RestController
@RequestMapping("/admin/options/value")
@RequiredArgsConstructor
public class ValueController {

  private final ValueService valueService;

  @GetMapping
  public List<ResponseValueDTO> getOptionValues() {
    return valueService.getValues();
  }

  @PostMapping
  public ResponseEntity<String> createOptionType(
      @Valid
      @RequestBody
      CreateValueDTO requestDto) {
    System.out.println("requestDto = " + requestDto);
    valueService.createGroup(requestDto);
    return ResponseEntity.ok("Group created successfully");
  }

  @DeleteMapping("{id}")
  public ResponseEntity<String> deleteOptionType(@PathVariable
  Long id) {
    valueService.deleteGroup(id);
    return ResponseEntity.ok("Value deleted successfully");
  }
}
