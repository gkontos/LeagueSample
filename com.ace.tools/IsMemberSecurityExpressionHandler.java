package com.ace.tools;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import com.ace.service.TsfLeagueService;
import com.ace.service.UserService;

public class IsMemberSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler implements MethodSecurityExpressionHandler  {
	 
	   TsfLeagueService tsfLeagueService;
	   UserService userService;
	   
	   @Override
	   protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
		   	IsMemberSecurityExpressionRoot root = new IsMemberSecurityExpressionRoot(authentication, tsfLeagueService, userService);
	         root.setThis(invocation.getThis());
	         root.setPermissionEvaluator(getPermissionEvaluator());
	         return root;
	   }

	public TsfLeagueService getTsfLeagueService() {
		return tsfLeagueService;
	}

	public void setTsfLeagueService(TsfLeagueService tsfLeagueService) {
		this.tsfLeagueService = tsfLeagueService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	   
	   
	}
