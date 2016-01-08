package com.ace.model;

import java.lang.reflect.Method;

public class TsfLeagueView extends TsfLeague {

	public ObjectLink creator;
	public ObjectLink league;
	public ObjectLink season;
	
	public TsfLeagueView(TsfLeague s) {
		for (Method getMethod : s.getClass().getMethods()) {
	        if (getMethod.getName().startsWith("get")) {
	            try {
	                Method setMethod = this.getClass().getMethod(getMethod.getName().replace("get", "set"), getMethod.getReturnType());
	                setMethod.invoke(this, getMethod.invoke(s, (Object[]) null));

	            } catch (Exception e) {
	                //not found set
	            }
	        }
	    }
	}

	public ObjectLink getCreator() {
		return creator;
	}

	public void setCreator(ObjectLink creator) {
		this.creator = creator;
	}

	public ObjectLink getLeague() {
		return league;
	}

	public void setLeague(ObjectLink league) {
		this.league = league;
	}

	public ObjectLink getSeason() {
		return season;
	}

	public void setSeason(ObjectLink season) {
		this.season = season;
	}

	
}
