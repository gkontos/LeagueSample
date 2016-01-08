package com.ace.tools;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ace.model.TsfLeague;
import com.ace.service.TsfLeagueService;
import com.ace.service.UserService;

public class IsMemberSecurityExpressionRoot  extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
		  
		private static Logger logger = LogManager.getLogger(IsMemberSecurityExpressionRoot.class.getName());
	  
	    private Object filterObject;
	    private Object returnObject;
	    private Object target;
	    
	    TsfLeagueService tsfLeagueService;
		UserService userService;
	      
	    public  boolean isMember(Object tsfLeague) {
	    	UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    	int userId = userService.getUser(userDetails.getUsername()).getId();
	    	return  tsfLeagueService.isMember(userId, (TsfLeague)tsfLeague);
	    }
	     
	    public boolean isMember(Serializable targetId, String targetType) {
	    	if (targetType.equals("com.ace.model.TsfLeague")){
		    	UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		    	int userId = userService.getUser(userDetails.getUsername()).getId();
		    	return  tsfLeagueService.isMember(userId, targetId);
	    	} 
	    	return false;
		}
	    
	    public IsMemberSecurityExpressionRoot(Authentication a, TsfLeagueService tsfLeagueService, UserService userService) {
	    	super(a);
	    	this.tsfLeagueService = tsfLeagueService;
	    	this.userService = userService;
	    }
	 
	    public void setFilterObject(Object filterObject) {
	        this.filterObject = filterObject;
	    }
	 
	    public Object getFilterObject() {
	        return filterObject;
	    }
	 
	    public void setReturnObject(Object returnObject) {
	        this.returnObject = returnObject;
	    }
	 
	    public Object getReturnObject() {
	        return returnObject;
	    }
	 
	    void setThis(Object target) {
	        this.target = target;
	    }
	 
	    public Object getThis() {
	        return target;
	    }
	 
	}
