package com.ability.emp.mobile.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ability.emp.base.BaseDao;
import com.ability.emp.mobile.entity.MobileSceCategoryEntity;


@Mapper
public interface MobileSceCategoryDao<T> extends BaseDao<T> {
	
	
	   public List<MobileSceCategoryEntity> getScenListDropLetData(MobileSceCategoryEntity me);
	   
	   public List<MobileSceCategoryEntity> selectCourseData(MobileSceCategoryEntity me);

}
