package com.mountainpier.social.web;

import com.mountainpier.social.domain.User;
import com.mountainpier.social.service.UserServiceImpl;
import com.mountainpier.social.web.model.UserRequest;
import com.mountainpier.social.web.model.UserResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(UserController.userBaseURI)
public class UserController {

	static final String userBaseURI = "/api/social";

	private final UserServiceImpl userService;
	
	@Autowired
	UserController(UserServiceImpl userService) {
		this.userService = userService;
	}
	
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public Page<UserResponse> getUsers(@RequestParam(value = "page", required = false) Integer page,
									   @RequestParam(value = "size", required = false) Integer size,
									   @RequestParam(value = "username", required = false) String username) {
		page = page != null ? page : 0;
		size = size != null ? size : 25;
		Page<User> userPage;
		if (username != null) userPage = userService.getUsersWithUsername(username, page, size);
		else userPage = userService.getUsers(page, size);
		return userPage
			.map(UserResponse::new);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public UserResponse createUser(@Valid @RequestBody UserRequest userRequest,
								   HttpServletResponse response) {
		User user = userService.createUser(userRequest);
		response.addHeader(HttpHeaders.LOCATION, userBaseURI + "/users/" + user.getId().toString());
		return new UserResponse(user);
	}

	@RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
	public UserResponse getUserById(@PathVariable("userId") final String userId) {
		return new UserResponse(userService.getUserById(UUID.fromString(userId)));
	}
	
	@RequestMapping(value = "/users/by", method = RequestMethod.GET)
	public UserResponse getUserBy(@RequestParam(name = "username", required = false) final String username,
								  @RequestParam(name = "email", required = false) final String email,
								  HttpServletResponse response) {
		User user;
		if (username != null && username.length() > 0) {
			user = userService.getUserByUsername(username);
		} else if (email != null && email.length() > 0) {
			user = userService.getUserByRegEmail(email);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		if (user != null) {
			return new UserResponse(user);
		} else {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		}
	}
	
	@RequestMapping(value = "/users/{userId}", method = RequestMethod.PATCH)
	public UserResponse updateUser(@PathVariable("userId") final String userId,
								   @RequestBody @Valid UserRequest userRequest) {
		return new UserResponse(userService.updateUserById(UUID.fromString(userId), userRequest));
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/users/{userId}", method = RequestMethod.DELETE)
	public void deleteUser(@PathVariable("userId") final String userId) {
		userService.deleteUserById(UUID.fromString(userId));
	}
	
	@RequestMapping(value = "/users/{userId}/friends", method = RequestMethod.GET)
	public Page<UserResponse> GetFriendsOfUserById(@PathVariable("userId") final String userId,
	                                               @RequestParam(value = "page", required = false) Integer page,
	                                               @RequestParam(value = "size", required = false) Integer size) {
		page = page != null ? page : 0;
		size = size != null ? size : 25;
		return userService.getFriendsOfUserById(UUID.fromString(userId), page, size)
			.map(UserResponse::new);
	}
	
}
