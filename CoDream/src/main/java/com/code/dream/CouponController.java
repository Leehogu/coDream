package com.code.dream;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.code.dream.classes.IClassService;
import com.code.dream.coupon.ICouponService;
import com.code.dream.dto.ClassDto;
import com.code.dream.dto.CouponDto;
import com.code.dream.dto.ReceiptDto;
import com.code.dream.dto.RegisterDto;
import com.code.dream.security.UserSecurityDto;
import com.code.dream.student.IStudentService;

@Controller
public class CouponController {

	@Autowired
	private ICouponService service;
	
	@Autowired
	IStudentService iStudentService;
	
	@Autowired
	private IClassService cservice;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	

	@RequestMapping(value = "/coupon/insertpage", method = { RequestMethod.GET, RequestMethod.POST })
	public String CouponPage() {
		return "coupon/insertCoupon";
	}

	@RequestMapping(value = "/coupon/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String CouponList(Model model, Authentication authentication) {
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto dto = usDto.getDto();
		List<CouponDto> lists = service.CouponAll();
		model.addAttribute("dto", dto);
		System.out.println(lists);
		model.addAttribute("lists", lists);
		return "coupon/CouponList";
	}
	
	@RequestMapping(value = "/coupon/memlist", method = { RequestMethod.GET, RequestMethod.POST })
	public String MemCouponList(Model model, Authentication authentication, int cl_seq) {
		logger.info("[CouponController] 멤버 쿠폰 리스트 {}", cl_seq);
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto dto = usDto.getDto();
		System.out.println(dto.getId());
		List<CouponDto> lists = service.MemCoupon(dto.getId());
		ClassDto cdto = cservice.classDetail(cl_seq);
		model.addAttribute("dto", dto);
		System.out.println(lists);
		model.addAttribute("lists", lists);
		model.addAttribute("cdto", cdto);
		System.out.println("@@@@cdto는 : "+cdto);
		return "coupon/member_CouponList";
	}

	@RequestMapping(value = "/coupon/input", method = { RequestMethod.GET, RequestMethod.POST })
	public String Couponinput(HttpServletRequest req) {
		System.out.println(req.getParameter("couponname"));
		System.out.println(req.getParameter("discount"));
		System.out.println(req.getParameter("maxprice"));
		System.out.println(req.getParameter("status"));
		
		 String COUPONNAME = req.getParameter("couponname");
		 int DISCOUNT = Integer.parseInt(req.getParameter("discount"));
		 int MAXPRICE = Integer.parseInt(req.getParameter("maxprice"));
		 String STATUS = req.getParameter("status");
		 CouponDto dto = new CouponDto();
		 dto.setCouponname(COUPONNAME);
		 dto.setDiscount(DISCOUNT);
		 dto.setMaxprice(MAXPRICE);
		 dto.setStatus(STATUS);
		 System.out.println(dto);
		
		service.insertCoupon(dto);

		return "coupon/insertCoupon";
	}
	
	@RequestMapping(value = "/coupon/insertcoupon", method = { RequestMethod.GET})
	public String Couponinsertcoupon(HttpServletRequest req) {

		 CouponDto dto = new CouponDto();
		 String id = req.getParameter("id");
		 String coupon_seq = req.getParameter("seq");
		 System.out.println(id);
		 System.out.println(coupon_seq);
		 dto.setId(id);
		 dto.setCoupon_seq(coupon_seq);
		 System.out.println(dto);	
		 service.insertMemCoupon(dto);

		return "coupon/member_CouponList";
	}
	
	@RequestMapping(value = "/coupon/kakaopay", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String kakaopay(HttpSession session, String result, String title, String seq) {
		Map<String,Object> map = new HashMap<String, Object>();
		System.out.println("-----------결제 시작-----------");
		try {
			URL kakaourl = new URL("https://kapi.kakao.com/v1/payment/ready"); //카카오페이 단건 결제 준비에 필요한 REST API
			HttpURLConnection kakaoserver = (HttpURLConnection) kakaourl.openConnection();
			kakaoserver.setRequestMethod("POST"); // POST로 요청
			kakaoserver.setRequestProperty("Authorization", "KakaoAK da2e0e25242d6645fdabd978c6a02c92");
			//카카오페이에서 발급받은 admin key로 권한 부여
			kakaoserver.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
			// content-type 설정
			kakaoserver.setDoOutput(true);
			String ctitle = title;
			String amount = result;
			String cseq = seq;
			System.out.println(ctitle);
			System.out.println(amount);
			String param = "cid=TC0ONETIME&partner_order_id=partner_order_id&partner_user_id=partner_user_id&"
					+ "item_name="+ctitle+"&quantity=1&total_amount="+amount+"&vat_amount=200&tax_free_amount=0&"
							+ "approval_url=http://localhost:8099/coupon/PayResult?seq="+seq+"&"
					+ "fail_url=http://localhost:8099/coupon/list&cancel_url=http://localhost:8099";
			// 결제에 필요한 parameter값들을 param에 넣음
			OutputStream outst = kakaoserver.getOutputStream();
			DataOutputStream dataout = new DataOutputStream(outst); 
			dataout.writeBytes(param);
			dataout.close();	
			
			int resultcode = kakaoserver.getResponseCode(); //http 응답 상태 코드
			
			InputStream inst;
			if(resultcode == 200) {
				inst = kakaoserver.getInputStream();
			}else {
				inst = kakaoserver.getErrorStream();
			}
			InputStreamReader readst = new InputStreamReader(inst);
			BufferedReader br = new BufferedReader(readst);
			String str = br.readLine();
			System.out.println("BufferedReader 값"+str);
			map.put("json", str);
			session.setAttribute("payinfo", str);
			String str2 = (String)session.getAttribute("payinfo");
			System.out.println("session 값"+str2);
			System.out.println("-----------결제 시도-----------");
//			return map;
//			return "{\"result\":\"NO\"}";
			return str2;
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		map.put("json", "{\"result\":\"NO\"}");
		String str2 = (String)session.getAttribute("payinfo");
		return str2;
//		return "{\"result\":\"NO\"}";
	}
	
	@RequestMapping(value = "/coupon/kakaoapprove", method = RequestMethod.GET)
	@ResponseBody
	public String kakaopayment(Authentication authentication, String pg_token, HttpSession session, String seq) {
		Map<String,String> map = new HashMap<String, String>();
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto udto = usDto.getDto();
		System.out.println("-----------승인 시작-----------");
		try {
			URL kakaourl = new URL("https://kapi.kakao.com/v1/payment/approve");
			HttpURLConnection kakaoserver = (HttpURLConnection) kakaourl.openConnection();
			String str2 = (String)session.getAttribute("payinfo");
			JSONParser parser = new JSONParser();
			try {
				JSONObject jsonObj = (JSONObject) parser.parse(str2);
				String tid = (String) jsonObj.get("tid");
				String productseq = seq;
				System.out.println("session 값"+str2);
				
				// id, product_seq, price, pay_Seq
				//iStudentService.
			kakaoserver.setRequestMethod("POST");
			kakaoserver.setRequestProperty("Authorization", "KakaoAK da2e0e25242d6645fdabd978c6a02c92");
			kakaoserver.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
			kakaoserver.setDoOutput(true);
			String param = "cid=TC0ONETIME&tid="+tid+"&partner_order_id=partner_order_id&partner_user_id=partner_user_id&pg_token="+ pg_token;
			OutputStream outst = kakaoserver.getOutputStream();
			DataOutputStream dataout = new DataOutputStream(outst);
			dataout.writeBytes(param);			
			dataout.close();
			
			int resultcode = kakaoserver.getResponseCode(); 
			
			InputStream inst;
			if(resultcode == 200) {
				inst = kakaoserver.getInputStream();
			}else {
				inst = kakaoserver.getErrorStream();
			}
			InputStreamReader readst = new InputStreamReader(inst);
			BufferedReader br = new BufferedReader(readst);
			String str = br.readLine();
			System.out.println(str);
			session.removeAttribute("payinfo");
			session.setAttribute("payinfo", str);
			String str3 = (String)session.getAttribute("payinfo");
			System.out.println("결제 성공 정보"+str3);
			System.out.println("-----------결제 승인 완료-----------");
			// str parsing
			JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj1 = (JSONObject) jsonParser.parse(str3);
            System.out.println(jsonObj1);
            JSONObject amount = (JSONObject) jsonObj1.get("amount");
            String amount1 = String.valueOf(amount.get("total"));
            String buyer = String.valueOf(jsonObj1.get("item_name"));
            System.out.println(amount1);
			
			
			// dto 생성 및 값 세팅
			ReceiptDto dto = new ReceiptDto();
			dto.setBuyer(udto.getId());
			dto.setProduct_seq(seq);
			dto.setTid(tid);
			dto.setPrice(amount1);
			System.out.println(dto);
			service.insertPay(dto);
			map.put("json", str);
			return str3;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		map.put("json", "{\"result\":\"NO\"}");
		String str3 = (String)session.getAttribute("payinfo");
		return str3;
	}
	
	@RequestMapping(value = "/coupon/PayResult", method = { RequestMethod.GET, RequestMethod.POST })
	public String PayResult(Model model, Authentication authentication) {
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto dto = usDto.getDto();
		List<CouponDto> lists = service.CouponAll();
		//ClassDto cdto = cservice.classDetail(cl_seq);
		model.addAttribute("dto", dto);
		System.out.println(lists);
		model.addAttribute("lists", lists);
		//model.addAttribute("cdto", cdto);
		return "coupon/PayResult";
	}
	
	@RequestMapping(value = "/coupon/paylist", method = { RequestMethod.GET, RequestMethod.POST })
	public String PayList(Model model, Authentication authentication) {
		UserSecurityDto usDto = (UserSecurityDto) authentication.getPrincipal();
		RegisterDto dto = usDto.getDto();
		List<ReceiptDto> lists = service.PaymentAll();
		model.addAttribute("dto", dto);
		System.out.println(lists);
		model.addAttribute("lists", lists);
		return "mypage/myClass";
	}
	
	
	
}
