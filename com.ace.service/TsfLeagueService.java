package com.ace.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ace.model.Season;
import com.ace.model.TsfLeague;
import com.ace.model.TsfLeagueView;
import com.ace.model.TsfManager;
import com.ace.model.TsfManagerView;
import com.ace.model.TsfRoster;
import com.ace.model.TsfRosterView;
import com.ace.model.TsfSchedule;
import com.ace.model.TsfScheduleView;

public interface TsfLeagueService {
	public List<TsfLeague> getAll();
	public TsfLeague get(int tsfLeagueId);
	public int save(TsfLeague tsfLeague);
	public boolean delete(TsfLeague tsfLeague);
	public boolean deleteById(int id);
	public List<TsfManager> getManagers(int id);
	public List<TsfRoster> getRoster(int id);
	public List<TsfSchedule> getSchedule(int id);
	
	public List<TsfLeagueView> getAllView();
	public TsfLeagueView getView(int tsfLeagueId);
	public List<TsfManagerView> getManagersView(int id);
	public List<TsfRosterView> getRosterView(int id);
	public List<TsfScheduleView> getScheduleView(int id);
	int update(TsfLeague tsfLeague);
	public List<TsfLeagueView> getOpenLeagues(int userId);
	boolean isMember(int userId, TsfLeague tsfLeague);
	public boolean isMember(int userId, Serializable targetId);
	
	void updatePoints(int tsfLeagueId);
	void getBoobiePrizePoints(Map<Integer, Integer> pointsMap);
	void getHeadToHeadPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap);
	/**
	 * 
	 * @param tsfLeagueId
	 * @param playoffRound one of WC, DIV, or CONF
	 * @param season
	 * @param pointsMap
	 */
	void getPlayoffPoints(int tsfLeagueId, String playoffRound, Season season, Map<Integer, Integer> pointsMap);
	void getSuperBowlPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap);
	void getSuperBowlWinnerPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap);
}
