package com.ace.data;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import com.ace.model.TsfLeague;
import com.ace.model.TsfLeagueView;

@Repository
public class TsfLeagueDaoImpl implements TsfLeagueDao {
	private static Logger logger = LogManager.getLogger(TsfLeagueDaoImpl.class.getName());
	@Autowired
	private Sql2o tsfSql;
	
	@Override
	public List<TsfLeague> getAll() {
		String sql = "SELECT * FROM tsf_league";
		try(Connection con = tsfSql.open()) {
            return con.createQuery(sql)
            	.setAutoDeriveColumnNames(true)
                .executeAndFetch(TsfLeague.class);
        }
		
	}

	@Override
	public TsfLeague get(int tsf_leagueId) {
		String sql = "SELECT * FROM tsf_league WHERE id = :id";
		try (Connection con = tsfSql.open()) {
			return con.createQuery(sql)
				.addParameter("id", tsf_leagueId)
				.setAutoDeriveColumnNames(true)
				.executeAndFetchFirst(TsfLeague.class);
		}
	}

	@Override
	public int save(TsfLeague obj) {
		int objId = 0;
    	
		if (obj.getId() == 0) {
			String insertSql = 
					"insert into tsf_league(name, creator_id, season_id, league_id) " +
					"values (:name, :creatorId, :seasonId, :leagueId)";

				try (Connection con = tsfSql.open()) {
				    objId = con.createQuery(insertSql)
				    	.bind(obj)
					    .executeUpdate()
					    .getKey(Integer.class);
				}
		} else {
			String updateSql = "update tsf_league set name = :name, creator_id = :creatorId, season_id =:seasonId, league_id =:leagueId where id = :id";

			try (Connection con = tsfSql.open()) {
			    con.createQuery(updateSql)
			    	.bind(obj)
					.addParameter("id", obj.getId())
				    .executeUpdate();
			    objId = obj.getId();
			}
		}
		return objId;
	}

	@Override
	public boolean delete(TsfLeague obj) {
		return deleteById(obj.getId());
	} 
	public boolean deleteById(int id) {
		String sql = "DELETE FROM tsf_league WHERE id = :id";
		try (Connection con = tsfSql.open()) {
			return con.createQuery(sql)
				.addParameter("id", id)
				.executeUpdate() != null;
		}
	}

	@Override
	public TsfLeagueView getView(int tsfLeagueId) {
		String sql = "SELECT r.*, u.username as \'creator.name\', u.id as \'creator.id\', "
				+ "		CONCAT_WS(' ', s.year, s.season_type) as \'season.name\', s.id as \'season.id\', "
				+ "		l.name as \'league.name\', l.id as \'league.id\' "
				+ "		FROM tsf_league r"
				+ "			LEFT JOIN users u ON r.creator_id = u.id"
				+ "			LEFT JOIN season s ON r.season_id = s.id"
				+ "			LEFT JOIN league l ON r.league_id = l.id"
				+ "		WHERE r.id = :id";
		try (Connection con = tsfSql.open()) {
			return con.createQuery(sql)
				.addParameter("id", tsfLeagueId)
				.setAutoDeriveColumnNames(true)
				.executeAndFetchFirst(TsfLeagueView.class);
		}
	}

	@Override
	public List<TsfLeagueView> getViewAll() {
		String sql = "SELECT r.*, u.username as \'creator.name\', u.id as \'creator.id\', "
				+ "		CONCAT_WS(' ', s.year, s.season_type) as \'season.name\', s.id as \'season.id\', "
				+ "		l.name as \'league.name\', l.id as \'league.id\' "
				+ "		FROM tsf_league r"
				+ "			LEFT JOIN users u ON r.creator_id = u.id"
				+ "			LEFT JOIN season s ON r.season_id = s.id"
				+ "			LEFT JOIN league l ON r.league_id = l.id";
		try (Connection con = tsfSql.open()) {
			return con.createQuery(sql)
				.setAutoDeriveColumnNames(true)
				.executeAndFetch(TsfLeagueView.class);
		}
	}

	@Override
	public List<TsfLeagueView> getOpenTsfLeagueView(int userId) {
		String sql = "SELECT r.*, u.username as \'creator.name\', u.id as \'creator.id\', "
				+ "		CONCAT_WS(' ', s.year, s.season_type) as \'season.name\', s.id as \'season.id\', "
				+ "		l.name as \'league.name\', l.id as \'league.id\' "
				+ "		FROM tsf_league r"
				+ "			LEFT JOIN users u ON r.creator_id = u.id"
				+ "			LEFT JOIN season s ON r.season_id = s.id"
				+ "			LEFT JOIN league l ON r.league_id = l.id"
				+ " 		LEFT JOIN tsf_manager userleagues ON (userleagues.tsf_league_id = r.id AND userleagues.user_id = 3) "
				+ " 		LEFT JOIN ("
				+ "				SELECT count(*) as managerCount, tsf_league_id FROM tsf_manager"
				+ "				) as managers ON managers.tsf_league_id = r.id "
				+ "		WHERE (managerCount IS NULL OR managerCount < 4)"
				+ "     	AND userleagues.id IS NULL"
				+ "			AND r.creator_id != :userId";
		logger.debug(sql);
		try (Connection con = tsfSql.open()) {
			return con.createQuery(sql)
				.addParameter("userId", userId)
				.setAutoDeriveColumnNames(true)
				.executeAndFetch(TsfLeagueView.class);
		}
	}

}
