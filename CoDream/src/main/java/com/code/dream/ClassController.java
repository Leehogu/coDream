package com.code.dream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.code.dream.classes.IClassService;
import com.code.dream.dto.ClassDto;

@Controller
public class ClassController {
	
	private static final Logger logger = LoggerFactory.getLogger(ClassController.class);
	
	@Autowired
	IClassService iClassService;
	
	@RequestMapping(value = "/board/classList", method = RequestMethod.GET)
	public String classList(Model model) {
		logger.info("[ClassController] classList 강의 전체 조회 페이지");
		
		// ClassDto에 조회 결과값을 받아 Model에 넘겨줌 
		List<ClassDto> cList = iClassService.classList();
		
		model.addAttribute("cList",cList);
		
		return "/board/classList";
	}
	
	@RequestMapping(value = "/board/classInput", method = RequestMethod.GET)
	public String classInputPage(Model model) {
		logger.info("[ClassController] classInput 강의 개설 페이지");
		
		// 권한이 맞지 않는다면 "잘못된 접근입니다." 표시 후 되돌려보냄
		// 권한이 맞다면 화면으로 넘겨줌
		
		return "/board/classInput";
	}
	
	@RequestMapping(value = "/board/jusoPopup", method = RequestMethod.GET)
	public String jusoPopup() {
		logger.info("[ClassController] jusoPopup 도로명주소 검색 팝업");
		return "/board/jusoPopup";
	}
	
	@RequestMapping(value = "/board/jusoPopup", method = RequestMethod.POST)
	public String jusoPopupSuccess(String inputYn, String roadFullAddr, String addrDetail, Model model) {
		logger.info("[ClassController] jusoPopup 도로명주소 검색 완료");
		System.out.println(roadFullAddr);
		model.addAttribute("inputYn", "Y");
		model.addAttribute("roadFullAddr", roadFullAddr);
		return "/board/jusoPopup";
	}
	
	@RequestMapping(value = "/board/classInput", method = RequestMethod.POST)
	public String classInput(/* Authentication authentication, */ClassDto cdto, @RequestParam String hash, @RequestParam String sday, @RequestParam String eday) {
		logger.info("[ClassController] classInput 강의 개설 실행");
//		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
//		RegisterDto rdto = usDto.getDto();
//		String teacher = rdto.getId();
//		cdto.setTeacher(teacher);
		
		cdto.setTeacher("DUMMY002"); // 임시 데이터
		
		// 날짜 값을 연결해줌
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			cdto.setStartday(sdf.parse(sday));
			cdto.setEndday(sdf.parse(eday));
			logger.info("[ClassController] classInput 개설 강의 정보 {}", cdto);
			
			// 문제가 없을 경우, 데이터를 삽입합니다
			boolean isc = iClassService.insertClass(cdto, hash);
			if(isc) { // 성공 시
				return "redirect:/board/classDetail?cl_seq="+cdto.getCl_seq();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return "redirect:/board/classList";
	}
	
	@RequestMapping(value = "/board/classDetail", method = RequestMethod.GET)
	public String classDetail(Model model, int cl_seq) {
		logger.info("[ClassController] classDetail 강의 상세조회");
		ClassDto cdto = iClassService.classDetail(cl_seq);
		
		if(cdto != null) {
			model.addAttribute("cDto", cdto);
			return "/board/classDetail";
		} else {
			return "redirect:/board/classList";
		}
		
	}
	
	

/*
 * 강의 개설
신규 해시 등록
해시태그 검색
해시태그별 강의 조회
강의 해시 조회
강의 해시 등록
강의 전체 조회
강의 상세 조회
수강 신청
알림 서비스
 * */
	
}
