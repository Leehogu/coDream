package com.code.dream.classes;

import java.util.List;
import java.util.Map;

import com.code.dream.dto.ClassDto;
import com.code.dream.dto.RegisterDto;
import com.code.dream.dto.StudentDto;

public interface IClassService {

	// 강의 개설
	public boolean insertClass(ClassDto dto);
	
	// 해시태그별 강의 조회
	public List<ClassDto> hashedClassList(String value);
	
	// 강의 해시 조회
	public List<String> linkHashList(int cl_seq);
	
	// 신규 해시 등록
	public boolean insertHash(String value);
	
	// 해시태그 존재여부 검색
	public List<Map<Integer, String>> checkHash(String value);
	
	// 강의 해시 등록
	public boolean updateLinkHash(int cl_seq, String value);
	
	// 강의 전체 조회
	public List<ClassDto> classList();
	
	// 강의 상세 조회
	public ClassDto classDetail(int cl_seq);
	
	// 수강 신청
	public boolean insertStudent(StudentDto dto);
	
	// 알림서비스 대상 조회
	public List<RegisterDto> emailList();
}