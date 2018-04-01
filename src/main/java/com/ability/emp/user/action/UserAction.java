package com.ability.emp.user.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ability.emp.user.entity.UserEntity;
import com.ability.emp.user.server.UserService;
import com.ability.emp.util.speechsynthesis.SpeechUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin//解决跨域请求
@Controller
@RequestMapping("/user")
public class UserAction {
	
	
	@Resource
	private UserService userService;
	
	ObjectMapper objectMapper = new ObjectMapper();  
	
	
	@RequestMapping("/add")
	public boolean add(UserEntity userEntity) {
		if(userService.add(userEntity)>0){
			return true;
		}
	    return false;
	}
	
	@RequestMapping(value="/del/{id}",method = RequestMethod.GET)
	@ResponseBody
	public String del(@PathVariable("id") String id) {
		//System.out.println("删除"+id);
		if(userService.del(id)>0){
			return "1";
		}
	    return "0";
	}
	
	@RequestMapping("/update")
	public String update() {
	    return "";
	}
	
	@RequestMapping(value="/query",method = RequestMethod.GET)  
	@ResponseBody
	public String query(HttpServletResponse response) throws Exception {
		List<UserEntity> ulist = userService.queryAll();
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("data", ulist);
		String temp = ulist.get(0).getUsername();
		//语音合成
		SpeechUtil.convert(temp);
		
	    return objectMapper.writeValueAsString(map);
	}

}