package com.gymflow.member;
import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/gyms/{gymId}/members") public class MemberController {
 private final MemberService service; public MemberController(MemberService service){this.service=service;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public MemberResponse create(@PathVariable UUID gymId,@Valid @RequestBody MemberRequest r){return service.create(gymId,r);}
 @PutMapping("/{id}") public MemberResponse update(@PathVariable UUID gymId,@PathVariable UUID id,@Valid @RequestBody MemberRequest r){return service.update(gymId,id,r);}
 @GetMapping public List<MemberResponse> findAll(@PathVariable UUID gymId){return service.findAll(gymId);}
 @GetMapping("/{id}") public MemberResponse findById(@PathVariable UUID gymId,@PathVariable UUID id){return service.findById(gymId,id);}
}
