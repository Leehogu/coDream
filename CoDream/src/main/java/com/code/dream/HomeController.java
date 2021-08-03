package com.code.dream;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.code.dream.classes.IClassService;
import com.code.dream.dto.AttachFileDto;
import com.code.dream.dto.ClassDto;
import com.code.dream.dto.RegisterDto;
import com.code.dream.dto.RegteacherDto;
import com.code.dream.regteacher.IRegteacherService;
import com.code.dream.security.UserSecurityDto;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Autowired
	private IRegteacherService service;
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
	IClassService iClassService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		logger.info("[HomeController] 첫페이지");
		
		List<ClassDto> ncList = iClassService.newestClass();
		List<ClassDto> dcList = iClassService.deadlineClass();

		model.addAttribute("ncList", ncList);
		model.addAttribute("dcList", dcList);
		
		
		return "home";
	}
	
	@RequestMapping(value = "/regteacherForm", method = RequestMethod.GET)
	public String regteacherForm(Model model, Authentication authentication) {
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto dto = usDto.getDto();
		dto.setPassword(null);
		model.addAttribute("dto", dto);
		return "admin/regteacherForm";
	}
	
	@RequestMapping(value = "/regteacher", method = RequestMethod.POST)
	public String regteacher(RegteacherDto dto, Authentication authentication) {
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto rdto = usDto.getDto();
		dto.setUserid(rdto.getId());
		service.insertRegteacher(dto);
		return "redirect:/admin/regteacherList";
	}
	
	@RequestMapping(value = "/admin/regteacherList", method = RequestMethod.GET)
	public String regteacherList(Model model) {
		List<RegteacherDto> list = service.selectRegteacher();
		model.addAttribute("list", list);
		return "/admin/regteacherList";
	}
	
	@RequestMapping(value = "/regteacherDetail", method = RequestMethod.GET)
	public String regteacherDetail(Model model, String te_seq) {
		RegteacherDto dto = service.detailRegteacher(te_seq);
		List<AttachFileDto> list = service.selectFiles(te_seq);
		model.addAttribute("dto", dto);
		model.addAttribute("list", list);
		return "admin/regteacherDetail";
	}
	
	@RequestMapping(value = "/regteacherModify", method = RequestMethod.GET)
	public String regteacherModify(String te_seq, String te_admit) {
		service.modifyRegteacher(te_admit, te_seq);
		return "redirect:/admin/regteacherList";
	}
}
