package com.ace.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ace.data.ScheduleDao;
import com.ace.data.TsfLeagueDao;
import com.ace.data.TsfManagerDao;
import com.ace.data.TsfRosterDao;
import com.ace.data.TsfScheduleDao;
import com.ace.model.GameResult;
import com.ace.model.Season;
import com.ace.model.TsfLeague;
import com.ace.model.TsfLeagueView;
import com.ace.model.TsfManager;
import com.ace.model.TsfManagerView;
import com.ace.model.TsfRoster;
import com.ace.model.TsfRosterView;
import com.ace.model.TsfSchedule;
import com.ace.model.TsfScheduleView;
import com.ace.tools.SecurityService;

@Service("tsfLeagueService")
public class TsfLeagueServiceImpl implements TsfLeagueService {
	private static Logger logger = LogManager.getLogger(TsfLeagueServiceImpl.class.getName());
	@Autowired
	TsfLeagueDao tsfLeagueDao;
	@Autowired
	TsfManagerDao tsfManagerDao;
	@Autowired
	TsfScheduleDao tsfScheduleDao;
	@Autowired
	TsfRosterDao tsfRosterDao;
	@Autowired
	SecurityService securityService;
	@Autowired
	ScheduleDao scheduleDao;
	@Autowired
	SeasonService seasonService;
	
	@Override
	public List<TsfLeague> getAll() {
		return tsfLeagueDao.getAll();
	}

	@Override
	public TsfLeague get(int tsfLeagueId) {
		return tsfLeagueDao.get(tsfLeagueId);
	}

	@Override
	public int save(TsfLeague tsfLeague) {
		int leagueId = tsfLeagueDao.save(tsfLeague);
		tsfLeague.setId(leagueId);
		securityService.setTsfLeaguePermissions(tsfLeague);
		return leagueId;
	}

	@Override
	public int update(TsfLeague tsfLeague) {
		int creatorId = tsfLeagueDao.get(tsfLeague.getId()).getCreatorId();
		
		int leagueId = tsfLeagueDao.save(tsfLeague);
		tsfLeague.setId(leagueId);
		
		if (creatorId != tsfLeague.getCreatorId()) {
			securityService.deleteUserTsfLeaguePermissions(tsfLeague.getId(), creatorId);
			securityService.setTsfLeaguePermissions(tsfLeague);	
		}
		return leagueId;
	}
	
	@Override
	public boolean delete(TsfLeague tsfLeague) {
		securityService.deleteAllTsfLeaguePermissions(tsfLeague);
		return tsfLeagueDao.delete(tsfLeague);
	}
	@Override
	public boolean deleteById(int id) {
		securityService.deleteAllTsfLeaguePermissions(id);
		return tsfLeagueDao.deleteById(id);
	}

	@Override
	public List<TsfManager> getManagers(int id) {
		return tsfManagerDao.getByLeague(id);
	}

	@Override
	public List<TsfRoster> getRoster(int id) {
		
		return tsfRosterDao.getByLeague(id);
	}

	@Override
	public List<TsfSchedule> getSchedule(int id) {
		return tsfScheduleDao.getByLeague(id);
	}

	@Override
	public List<TsfLeagueView> getAllView() {
		
		return tsfLeagueDao.getViewAll();
	}

	@Override
	public TsfLeagueView getView(int tsfLeagueId) {
		return tsfLeagueDao.getView(tsfLeagueId);
	}

	@Override
	public List<TsfManagerView> getManagersView(int id) {
		return tsfManagerDao.getViewByLeague(id);
	}

	@Override
	public List<TsfRosterView> getRosterView(int id) {
		return tsfRosterDao.getViewByLeague(id);
	}

	@Override
	public List<TsfScheduleView> getScheduleView(int id) {
		return tsfScheduleDao.getViewByLeague(id);
	}

	@Override
	public List<TsfLeagueView> getOpenLeagues(int userId) {
		logger.debug("leagues open for user = " + userId);
		List<TsfLeagueView> leagues = tsfLeagueDao.getOpenTsfLeagueView(userId);
		logger.debug(leagues.size());
		return leagues;
	}

	@Override
	public boolean isMember(int userId, TsfLeague tsfLeague) {
		if (tsfLeague.getCreatorId() == userId) {
			return true;
		}
		List<TsfManager> managers = getManagers(tsfLeague.getId());
		for (TsfManager mgr : managers) {
			if (mgr.getUserId() == userId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMember(int userId, Serializable targetId) {
		return isMember(userId, tsfLeagueDao.get((int)targetId));
	}

	@Override
	public void getHeadToHeadPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap) {
		
		
		List<GameResult> results = scheduleDao.getResults(season.getYear(), season.getSeasonType());
		for (GameResult r : results) { 
			
			TsfManager winner = null, loser = null;
			if (r.getWinnerId() != 0) {
				winner = tsfManagerDao.getByTeam(tsfLeagueId, r.getWinnerId());
			}
			if (r.getLoserId() != 0) {
				loser = tsfManagerDao.getByTeam(tsfLeagueId, r.getLoserId());
			}
			if (winner != null && loser != null) {
				
				pointsMap.put(winner.getId(), pointsMap.get(winner.getId()) + 10);
				pointsMap.put(loser.getId(), pointsMap.get(loser.getId()) - 10);
			}
		}
		
	}

	@Override
	public void updatePoints(int tsfLeagueId) {
		List<TsfManager> managers = getManagers(tsfLeagueId);
		Map<Integer, TsfManager> managerMap = new HashMap<Integer, TsfManager>();
		Map<Integer, Integer> pointsMap = new HashMap<Integer, Integer>();
		managers.forEach( m -> {
			managerMap.put(m.getId(), m);
			pointsMap.put(m.getId(), 0);
		});
		
		TsfLeague tsfLeague = tsfLeagueDao.get(tsfLeagueId);
		Season season = seasonService.get(tsfLeague.getSeasonId());
		
		getHeadToHeadPoints(tsfLeagueId, season, pointsMap);
		logger.debug("Head To Head");
		logPoints(pointsMap);
		getBoobiePrizePoints(pointsMap);
		logger.debug("Boobie Prize");
		logPoints(pointsMap);
		getPlayoffPoints(tsfLeagueId, "WC", season, pointsMap);
		logger.debug("PLayoffs - WildCard");
		logPoints(pointsMap);
		getPlayoffPoints(tsfLeagueId, "CON", season, pointsMap);
		logger.debug("Playoffs -- Conference");
		logPoints(pointsMap);
		getPlayoffPoints(tsfLeagueId, "DIV", season, pointsMap);
		logger.debug("Playoffs - division");
		logPoints(pointsMap);
		getSuperBowlPoints( tsfLeagueId,  season, pointsMap);
		logger.debug("Superbowl");
		logPoints(pointsMap);
		getSuperBowlWinnerPoints( tsfLeagueId,  season, pointsMap);
		logger.debug("superbowl winner");
		logPoints(pointsMap);
		
		pointsMap.forEach( (k,v) -> {
			logger.info(" id = " + k + " points = " + v);
			managerMap.get(k).setPoints(v);
			tsfManagerDao.save(managerMap.get(k));
		});
	}

	private void logPoints(Map<Integer, Integer> pointsMap) {
		StringBuffer str = new StringBuffer("");
		for (Map.Entry<Integer, Integer> entry : pointsMap.entrySet()) {
			str.append("<="+entry.getKey() +":" +entry.getValue()+"=>");
		}
		logger.debug(str);
	}
	@Override
	public void getBoobiePrizePoints(Map<Integer, Integer> pointsMap) {
		 
		try {
			Map.Entry<Integer, Integer> minPoints = pointsMap.entrySet().stream().min(Map.Entry.comparingByValue(Integer::compareTo)).get();
			Map<Integer, Integer> hasMinPoints = pointsMap.entrySet().stream().filter( v -> v.getValue() == minPoints.getValue()).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
			int boobies = hasMinPoints.size();
			int allPlayers = pointsMap.size();
			
			int pointsTaken = (allPlayers - boobies) * 30;
			int pointsAwarded = new Long(Math.round((double)pointsTaken / (double)boobies)).intValue();
			for (Map.Entry<Integer, Integer> entry : pointsMap.entrySet()) {
				if (entry.getValue() != minPoints.getValue()) {
					entry.setValue(entry.getValue()- 30);
				}
				if (entry.getValue() == minPoints.getValue()) {
					
					entry.setValue(entry.getValue() + pointsAwarded);
				}
			}
		} catch (NoSuchElementException ne) {
			// do nothing.
		}
	}

	@Override
	public void getPlayoffPoints(int tsfLeagueId, String playoffRound, Season season, Map<Integer, Integer> pointsMap) {
		List<GameResult> results = scheduleDao.getResults(season.getYear(), "POST", playoffRound);
		for (GameResult r : results) {
			
			int points = 15;
			if (playoffRound.equals("WC")) {
				
			} else if (playoffRound.equals("DIV")) {
				points = 30;
			} else if (playoffRound.equals("CON")) {
				points = 40;
			} 
			TsfManager winner = null, loser = null;
			if (r.getWinnerId() != 0) {
				winner = tsfManagerDao.getByTeam(tsfLeagueId, r.getWinnerId());
			}
			if (r.getLoserId() != 0) {
				loser = tsfManagerDao.getByTeam(tsfLeagueId, r.getLoserId());
			}
			if (winner != null && loser != null) {
				
				pointsMap.put(winner.getId(), pointsMap.get(winner.getId()) + points);
				pointsMap.put(loser.getId(), pointsMap.get(loser.getId()) - points);
			}
		}
		
	}

	/**
	 * if a player has a superbowl team, they get 50 points from each player who does not have a superbowl team
	 */
	@Override
	public void getSuperBowlPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap) {
		List<GameResult> results = scheduleDao.getResults(season.getYear(), "POST", "SB");
		if (results.size() == 1) {
			GameResult r = results.get(0);
			TsfManager winner = tsfManagerDao.getByTeam(tsfLeagueId, r.getWinnerId());
			TsfManager loser = tsfManagerDao.getByTeam(tsfLeagueId, r.getLoserId());
			
			int awardPoints = 0;
			if (winner.getId() != 0) {
				awardPoints ++;
			}
			if (loser.getId() != 0) {
				awardPoints ++;
			}
			int playerCount = pointsMap.size();
			int losePoints = playerCount - awardPoints;
			int multiplier = awardPoints; // number of correctly chosen teams
			for (Map.Entry<Integer, Integer> entry : pointsMap.entrySet()) {
				if (entry.getKey() == winner.getId() || entry.getKey() == loser.getId()) {
					entry.setValue( entry.getValue() + (losePoints * 50)); // the winners get 50 points from each player who lost
				} else {
					entry.setValue( entry.getValue() - (multiplier * 50));
				}
			}
		}
		

	}

	/**
	 * if a player has the superbowl winner, they get 60 points from each player
	 */
	@Override
	public void getSuperBowlWinnerPoints(int tsfLeagueId, Season season, Map<Integer, Integer> pointsMap) {
		List<GameResult> results = scheduleDao.getResults(season.getYear(), "POST", "SB");
		if (results.size() == 1) {
			GameResult r = results.get(0);
			TsfManager winner = tsfManagerDao.getByTeam(tsfLeagueId, r.getWinnerId());
			if (winner.getId() != 0) {
				pointsMap.put(winner.getId(), pointsMap.get(winner.getId()) + 60);
				for (Map.Entry<Integer, Integer> entry : pointsMap.entrySet()) {
					if (entry.getKey() != winner.getId()) {
						entry.setValue(entry.getValue() - 60);
					}
				}
			} 
		}
	}
}
