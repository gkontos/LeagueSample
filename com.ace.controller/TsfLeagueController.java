package com.ace.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ace.model.TsfLeague;
import com.ace.model.TsfLeagueView;
import com.ace.model.TsfManager;
import com.ace.model.TsfManagerView;
import com.ace.model.TsfRosterView;
import com.ace.model.TsfScheduleView;
import com.ace.service.TsfLeagueService;
import com.ace.service.TsfManagerService;
import com.ace.service.UserService;
import com.ace.validation.Preconditions;
import com.ace.validation.RestPreconditions;

@RestController
@RequestMapping(value = "/tsfLeague")
public class TsfLeagueController {
	private static Logger logger = LogManager.getLogger(TsfLeagueController.class.getName());

	@Autowired
	TsfLeagueService service;
	@Autowired
	UserService userService;
	@Autowired
	TsfManagerService tsfManagerService;

	@PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
	@PostFilter("hasPermission(filterObject, read) or hasRole('ADMIN') or isMember(filterObject)")
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<TsfLeagueView> findAll() {
		return service.getAllView();
	}

	@PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.ace.model.TsfLeague', read) or isMember(#id, 'com.ace.model.TsfLeague')")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public TsfLeagueView findOne(@PathVariable("id") int id) {
		return RestPreconditions.checkFound(service.getView(id));
	}

	@PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.ace.model.TsfLeague', read) or isMember(#id, 'com.ace.model.TsfLeague')")
	@RequestMapping(value = "/{id}/tsfmanagers", method = RequestMethod.GET)
	@ResponseBody
	public List<TsfManagerView> getManagers(@PathVariable("id") int id) {
		return RestPreconditions.checkFound(service.getManagersView(id));
	}

	@PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.ace.model.TsfLeague', read) or isMember(#id, 'com.ace.model.TsfLeague')")
	@RequestMapping(value = "/{id}/tsfroster", method = RequestMethod.GET)
	@ResponseBody
	public List<TsfRosterView> getRoster(@PathVariable("id") int id) {
		return RestPreconditions.checkFound(service.getRosterView(id));
	}

	@PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.ace.model.TsfLeague', read) or isMember(#id, 'com.ace.model.TsfLeague')")
	@RequestMapping(value = "/{id}/tsfschedule", method = RequestMethod.GET)
	@ResponseBody
	public List<TsfScheduleView> getSchedule(@PathVariable("id") int id) {
		return RestPreconditions.checkFound(service.getScheduleView(id));
	}

	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public int create(@RequestBody TsfLeague resource, Authentication authentication) {
		boolean addUser = false;
		if (resource.getCreatorId() == 0) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
				resource.setCreatorId(userService.getUser(userDetails.getUsername()).getId());

			}
		}

		Preconditions.checkNotNull(resource);
		int tsfLeagueId = service.save(resource);
		if (addUser) {
			TsfManager manager = new TsfManager();
			manager.setUserId(resource.getCreatorId());
			manager.setTsfLeagueId(tsfLeagueId);
			tsfManagerService.save(manager);
		}
		return tsfLeagueId;
	}

	/**
	 * Find tsfLeagues that are open, so that a user can choose a league to join
	 * @param resource
	 * @param authentication
	 * @return
	 */
	@PreAuthorize("hasRole('USER')")
	@RequestMapping(value = "/listopen", method = RequestMethod.GET)
	@ResponseBody
	public List<TsfLeagueView> findOpen(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		int userId = userService.getUser(userDetails.getUsername()).getId();
		return service.getOpenLeagues(userId);
	}
	
	@PreAuthorize("hasRole('USER')")
	@RequestMapping(value = "/{id}/join", method = RequestMethod.GET)
	@ResponseBody
	public Boolean joinLeague(@PathVariable("id") int tsfLeagueId, Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		int userId = userService.getUser(userDetails.getUsername()).getId();
		TsfManager manager = new TsfManager();
		manager.setTsfLeagueId(tsfLeagueId);
		manager.setUserId(userId);

		return (tsfManagerService.save(manager) > 0);
		
	}
	
	@PreAuthorize("hasRole('ADMIN') or hasPermission(#resource, write)")
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)
	public void update(@PathVariable("id") int id, @RequestBody TsfLeague resource) {
		Preconditions.checkNotNull(resource);
		RestPreconditions.checkNotNull(service.get(resource.getId()));
		service.save(resource);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable("id") int id) {
		service.deleteById(id);
	}

}
