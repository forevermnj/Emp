package com.ability.emp.admin.action;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ability.emp.admin.entity.AdminTaskEntity;
import com.ability.emp.admin.entity.AdminThesauresPramEntity;
import com.ability.emp.admin.entity.AdminWordEntity;
import com.ability.emp.admin.server.AdminSystemParamService;
import com.ability.emp.admin.server.AdminTaskService;
import com.ability.emp.admin.server.AdminThesauresPramService;
import com.ability.emp.admin.server.AdminWordService;
import com.ability.emp.constant.SysConstant;
import com.ability.emp.util.CalendarCountUtil;
import com.ability.emp.util.UUIDUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;



/**
 * 任务
 * @author Devin
 * @since 2018-4-3
 *
 */
@CrossOrigin//解决跨域请求
@Controller
@RequestMapping("/admin/task")
public class AdminTaskListAction {
	
	
	@Resource
	private AdminTaskService adminTaskService;
	@Resource
	private AdminSystemParamService adminSystemParamService;
	@Resource
	AdminThesauresPramService adminThesauresPramService;
	
	@Resource
	private AdminWordService adminWordService;
	
	ObjectMapper objectMapper = new ObjectMapper();  
	
	
	/**
	 * 返回首页
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("")
	public String listPage(HttpServletRequest request,HttpServletResponse response){
		return "tasklist";
	}
	
	/**
	 * 返回数据
	 * @param request
	 * @param response
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping("/queryAll")
	@ResponseBody
	public String queryAll(int pageSize,int pageNumber,AdminTaskEntity ate) throws JsonProcessingException{
		//第一个参数当前页码，第二个参数每页条数
		PageHelper.startPage(pageNumber,pageSize);  
		List<AdminTaskEntity> data = adminTaskService.queryAll(ate);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		AdminThesauresPramEntity adminThesauresPramEntity=null;
		String tParamid="";
		/**
		 * 日期转换为String类型
		 */
		if(data!=null && data.size()>0){
			for(int i=0;i<data.size();i++){
				data.get(i).setStartStringDate(sf.format(data.get(i).getStartDate()!=null?data.get(i).getStartDate():""));
				data.get(i).setEndStringDate(sf.format(data.get(i).getEndDate()!=null?data.get(i).getEndDate():""));
				tParamid=data.get(i).getThesauresType();
				if(!StringUtils.pathEquals("", tParamid)){
					adminThesauresPramEntity=adminThesauresPramService.getByID(data.get(i).getThesauresType());
					if(adminThesauresPramEntity!=null){
						data.get(i).setThesauresTypeName(adminThesauresPramEntity.getName());
					}
				}
				if(data.get(i).getTasktype().equals(SysConstant.TASK_TYPE0)){
					data.get(i).setTasktypename(SysConstant.getTaskTypeMap().get(SysConstant.TASK_TYPE0).toString());
				}
				if(data.get(i).getTasktype().equals(SysConstant.TASK_TYPE1)){
					data.get(i).setTasktypename(SysConstant.getTaskTypeMap().get(SysConstant.TASK_TYPE1).toString());
				}
			}
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		PageInfo<AdminTaskEntity> page = new PageInfo<>(data);
		map.put("total",page.getTotal());
		map.put("rows", data);
		return objectMapper.writeValueAsString(map);
	}
	
	@RequestMapping("/add")
	@ResponseBody
	public String addTask(AdminTaskEntity taskEntity) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	    
		taskEntity.setStartDate(sf.parse(taskEntity.getStartStringDate()));
		taskEntity.setEndDate(sf.parse(taskEntity.getEndStringDate()));
		taskEntity.setId(UUIDUtil.generateUUID());
		int i = adminTaskService.insert(taskEntity);  
		if(i>0){
			return "0";
		}else{
			return "1";
		}
	}
	
	@RequestMapping(value = "/taskedit/{id}")
	@ResponseBody
	public String queryTaskById(@PathVariable("id") String id) throws Exception {
		AdminTaskEntity task = adminTaskService.queryTaskById(id);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		if(task!=null){
			task.setStartStringDate(sf.format(task.getStartDate()!=null?task.getStartDate():""));
			task.setEndStringDate(sf.format(task.getEndDate()!=null?task.getEndDate():""));
		}
		return objectMapper.writeValueAsString(task);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@ResponseBody
	public String updateTask(AdminTaskEntity taskEntity) throws Exception {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");	
		
		taskEntity.setStartDate(sf.parse(taskEntity.getStartStringDate()));
		taskEntity.setEndDate(sf.parse(taskEntity.getEndStringDate()));
		int i = adminTaskService.update(taskEntity); 
		if(i>0){
			return "0";
		}else{
			return "1";
		}
	}
	
	
	/**
	 * 计算任务量
	 * @param taskEntity
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/calculateTaskCount")
	@ResponseBody
	public String calculateTaskCount(String startDate,String endDate,String thesaure) throws Exception {
		try{
			//查询对应词库的单词总数
			AdminWordEntity awe = new AdminWordEntity();
			awe.setThesaurusType(thesaure);
			List<AdminWordEntity> list = adminWordService.queryWordAll(awe);
			if(list==null || list.size()==0){
				return "0";
			}
			//计算2个日期相差的天数
			int x = CalendarCountUtil.getDays(startDate, endDate);
			if(x==0){
				return "0";
			}
			
			//计算每天的任务量
			int days = x+1;
			double c = list.size()/days;
			if(c<0.5){
				return "-1";
			}
			
			BigDecimal b = new BigDecimal(c);
			int taskcount = b.setScale(2,RoundingMode.HALF_UP).intValue();
			return String.valueOf(taskcount);
		}catch(Exception e){
			return "-1";
		}
	}

}
