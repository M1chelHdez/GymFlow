package com.gymflow.member;
import com.gymflow.gym.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException; import java.util.*;
@Service public class MemberService {
 private final MemberRepository members; private final GymRepository gyms;
 public MemberService(MemberRepository members,GymRepository gyms){this.members=members;this.gyms=gyms;}
 @Transactional public MemberResponse create(UUID gymId,MemberRequest r){Gym gym=gyms.findById(gymId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Gimnasio no encontrado.")); if(members.existsByGym_IdAndMemberNumber(gymId,r.memberNumber().trim()))throw new ResponseStatusException(HttpStatus.CONFLICT,"El número de socio ya existe en este gimnasio."); return MemberResponse.from(members.save(new Member(gym,r.memberNumber().trim(),r.fullName().trim(),blankToNull(r.email()),blankToNull(r.phone()))));}
 @Transactional public MemberResponse update(UUID gymId,UUID id,MemberRequest r){
  Member member=members.findById(id).filter(m->m.getGym().getId().equals(gymId)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Socio no encontrado en este gimnasio."));
  String number=r.memberNumber().trim();
  if(members.existsByGym_IdAndMemberNumberAndIdNot(gymId,number,id))throw new ResponseStatusException(HttpStatus.CONFLICT,"El número de socio ya está registrado en este gimnasio.");
  member.update(number,r.fullName().trim(),blankToNull(r.email()),blankToNull(r.phone()));
  return MemberResponse.from(members.save(member));
 }
 @Transactional(readOnly=true) public List<MemberResponse> findAll(UUID gymId){if(!gyms.existsById(gymId))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Gimnasio no encontrado.");return members.findAllByGym_IdOrderByFullName(gymId).stream().map(MemberResponse::from).toList();}
 @Transactional(readOnly=true) public MemberResponse findById(UUID gymId,UUID id){return members.findById(id).filter(m->m.getGym().getId().equals(gymId)).map(MemberResponse::from).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Socio no encontrado en este gimnasio."));}
 private String blankToNull(String s){return s==null||s.isBlank()?null:s.trim();}
}
