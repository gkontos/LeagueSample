package com.ace.data;

import java.util.List;

import com.ace.model.TsfLeague;
import com.ace.model.TsfLeagueView;

public interface TsfLeagueDao {
	public List<TsfLeague> getAll();
	public TsfLeague get(int tsfLeagueId);
	public int save(TsfLeague tsfLeague);
	public boolean delete(TsfLeague tsfLeague);
	public boolean deleteById(int id);
	public TsfLeagueView getView(int tsfLeagueId);
	public List<TsfLeagueView> getViewAll();
	public List<TsfLeagueView> getOpenTsfLeagueView(int userId);
}
