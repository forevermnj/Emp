package com.ability.emp.mobile.entity;

import java.util.Date;

public class MobileSceCategoryEntity {
	
	
	
	
	private String id;

    private String fircatid;

    private String scecatname;

    private Integer index;

    private String icon;

    private String dropletid;

    private String dropletconftypeid;
    
    private String coursestate;
    
    private String coursetype;

    private Date courseenddate;
    
    
    
    /**
     * 表外字段
     * @return
     */
    private String userid;
    
    
    
    
    
    
   

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getCoursetype() {
		return coursetype;
	}

	public void setCoursetype(String coursetype) {
		this.coursetype = coursetype;
	}

	public Date getCourseenddate() {
		return courseenddate;
	}

	public void setCourseenddate(Date courseenddate) {
		this.courseenddate = courseenddate;
	}

	public String getCoursestate() {
		return coursestate;
	}

	public void setCoursestate(String coursestate) {
		this.coursestate = coursestate;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFircatid() {
		return fircatid;
	}

	public void setFircatid(String fircatid) {
		this.fircatid = fircatid;
	}

	public String getScecatname() {
		return scecatname;
	}

	public void setScecatname(String scecatname) {
		this.scecatname = scecatname;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDropletid() {
		return dropletid;
	}

	public void setDropletid(String dropletid) {
		this.dropletid = dropletid;
	}

	public String getDropletconftypeid() {
		return dropletconftypeid;
	}

	public void setDropletconftypeid(String dropletconftypeid) {
		this.dropletconftypeid = dropletconftypeid;
	}
    
    
    
    

}
