package com.ability.emp.mobile.action;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ability.emp.constant.SysConstant;
import com.ability.emp.mobile.entity.MobileHitCardEntity;
import com.ability.emp.mobile.entity.MobileTaskEntity;
import com.ability.emp.mobile.entity.MobileUserTaskEntity;
import com.ability.emp.mobile.entity.MobileWordEntity;
import com.ability.emp.mobile.entity.MobileWordRecordEntity;
import com.ability.emp.mobile.server.MobileBearWordService;
import com.ability.emp.mobile.server.MobileHitCardService;
import com.ability.emp.mobile.server.MobileSystemParamService;
import com.ability.emp.mobile.server.MobileTaskService;
import com.ability.emp.mobile.server.MobileUserService;
import com.ability.emp.mobile.server.MobileUserTaskService;
import com.ability.emp.mobile.server.MobileWordService;
import com.ability.emp.util.CalendarCountUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 开始背单词接口
 * @author Devin
 *
 */
@CrossOrigin//解决跨域请求
@Controller
@RequestMapping("/mobile/bearword")
public class MobileBearWordAction {
	
	
	
	@Resource
	private MobileBearWordService mobileBearWordService;
	
	@Resource
	private MobileUserService mobileUserService;
	
	@Resource
	private MobileTaskService mobileTaskService;
	
	@Resource
	private MobileSystemParamService mobileSystemParamService;
	
	@Resource
	private MobileWordService mobileWordService;
	
	@Resource
	private MobileHitCardService mobileHitCardService;
	
	@Resource
	private MobileUserTaskService mobileUserTaskService;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	ObjectMapper objectMapper = new ObjectMapper();  
	
	
	/**
	 * 返回数据
	 * @param id 用户ID
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/query/{id}")
	@ResponseBody
	public String query(@PathVariable("id") String id) throws Exception{
		//获取用户任务
		MobileUserTaskEntity userTask = new MobileUserTaskEntity();
		userTask.setUserid(id);
		List<MobileUserTaskEntity> userTaskList = mobileUserTaskService.getUserTask(userTask);
		
		int taskcount = 0;
		MobileTaskEntity mte = new MobileTaskEntity();
		for(int i=0;i<userTaskList.size();i++){
			//查询用户未完成的任务
			if(userTaskList.get(i).getCompletepercent().equals(SysConstant.COMPLETE_PERCENT_INIT)){
				mte.setId(userTaskList.get(i).getTaskid());
				MobileTaskEntity task = mobileTaskService.getTask(mte);
				if(task.getCourseid().equals(SysConstant.TASK_TYPE1)){
					taskcount = calculateTaskCount(sdf.format(task.getStart_Date()),sdf.format(task.getEnd_Date()),task.getThesaures_Type());
				    break;
				}
			}
		}
		
		/***
		 * 先获取选中,考试未通过的单词
		 */
		MobileWordRecordEntity mwre = new MobileWordRecordEntity();
		mwre.setIsSel(SysConstant.CHECKED);//选中
		mwre.setIsPass(SysConstant.NO_PASS);//考试未通过
		mwre.setUserId(id);//用户ID
		mwre.setCount(taskcount);//任务量
		
		List<MobileWordRecordEntity> list = mobileBearWordService.query(mwre);
		//有数据则返回
		if(list!=null && list.size()>0){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("total",list.size());
			map.put("rows", list);
			return objectMapper.writeValueAsString(map);
		}else{
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			MobileHitCardEntity mhce = new MobileHitCardEntity();
			mhce.setUserId(id);
			mhce.setStringDate(sf.format(new Date()));
			List<MobileHitCardEntity> ifhit = mobileHitCardService.queryByUserID(mhce);
			if(ifhit!=null && ifhit.size()>=1){
			   //当天已打卡
			   mwre.setIsSel(null);
			   mwre.setStringPassDate(sf.format(new Date()));;//当天考试通过日期
			   mwre.setIsPass(SysConstant.PASS);//考试通过
			   mwre.setUserId(id);//用户ID
			   //原返回当天考试通过的单词
			   List<MobileWordRecordEntity> passlist = mobileBearWordService.query(mwre);
			   Map<String,Object> map = new HashMap<String,Object>();
			   map.put("total",passlist.size());
			   map.put("rows", passlist);
			   return objectMapper.writeValueAsString(map);
			}else{
			    //当天未打卡  
				//没有数据则重新随机获取指定任务量单词
				mwre.setIsSel(SysConstant.NO_CHECKED);//未选中
				mwre.setIsPass(SysConstant.NO_PASS);//考试未通过
				mwre.setUserId(id);//用户ID
				mwre.setCount(taskcount);//任务量
				//随机返回指定任务量的单词
				List<MobileWordRecordEntity> list2 = mobileBearWordService.query(mwre);
				MobileWordRecordEntity mwre2 = new MobileWordRecordEntity();
				//将获取的单词选中
				for(int i=0;i<list2.size();i++){
					mwre2.setId(list2.get(i).getId());
					mwre2.setIsSel(SysConstant.CHECKED);
					mobileBearWordService.update(mwre2);
				}
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("total",list2.size());
				map.put("rows", list2);
				return objectMapper.writeValueAsString(map);
			}
		}
	}
	
	
	/**
	 * 单词释义
	 * @param id 单词ID
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping("/mean/{id}")
	@ResponseBody
	public String mean(@PathVariable("id") String id) throws JsonProcessingException{
		   MobileWordEntity mwe = mobileWordService.mean(id);
		   return objectMapper.writeValueAsString(mwe);
	}
	
	
	/**
	 * 已完成单词数量
	 * @param id 用户ID
	 * @return
	 * @throws JsonProcessingException 
	 */
	@RequestMapping("/doneWordCount/{id}")
	@ResponseBody
	public String doneWordCount(@PathVariable("id") String id) throws JsonProcessingException{
		   MobileWordRecordEntity mwre = new MobileWordRecordEntity();
		   mwre.setUserId(id);//用户ID
		   mwre.setIsPass(SysConstant.PASS);//考试通过
		   int count = mobileBearWordService.queryDoneCount(mwre);
		   Map<String,Object> map = new HashMap<String,Object>();
		   map.put("totaldone", count);
		   
		   return objectMapper.writeValueAsString(map);
	}
	
	/**
	 * 当天是否完成打卡
	 * @param id 用户ID
	 * @return
	 * @throws JsonProcessingException 
	 */
	@RequestMapping("/whetherDoneHitCard/{id}")
	@ResponseBody
	public String whetherDoneHitCard(@PathVariable("id") String id) throws JsonProcessingException{
		   SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		   MobileHitCardEntity mhce = new MobileHitCardEntity();
		   mhce.setUserId(id);
		   mhce.setStringDate(sf.format(new Date()));
		   List<MobileHitCardEntity> list = mobileHitCardService.queryByUserID(mhce);
		   
		   Map<String,Object> map = new HashMap<String,Object>();
		   if(list!=null && list.size()>=1){
			   map.put("taskstatus", 1);
		   }else{
			   map.put("taskstatus", 0);
		   }
		   
		   return objectMapper.writeValueAsString(map);
	}
	
	private int calculateTaskCount(String startDate,String endDate,String thesaure) throws Exception {
		try{
			//根据词库ID获取词库数量
			MobileWordEntity m = new MobileWordEntity();
			m.setThesaurusType(thesaure);
			List<MobileWordEntity> list = mobileWordService.queryWordAll(m);
			if(list==null || list.size()==0){
				return 0;
			}
			//计算2个日期相差的天数
			int x = CalendarCountUtil.getDays(startDate, endDate);
			if(x==0){
				return 0;
			}
			
			//计算每天的任务量
			int days = x+1;
			double c = list.size()/days;
			if(c<0.5){
				return 0;
			}
			
			BigDecimal b = new BigDecimal(c);
			int taskcount = b.setScale(2,RoundingMode.HALF_UP).intValue();
			return taskcount;
		}catch(Exception e){
			return 0;
		}
	}

}
